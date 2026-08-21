# Dependency Tracking (Design)

> This is the design for the feature described in `dependency-tracking.md` (repo root).
> The old `rooster-monolith` caching layer is used as a loose reference only — not copied 1:1.

## Goal

Cache interface contents (condition, display item, priority, etc.) by automatically
tracking which parts of the render state a lambda actually reads, instead of requiring the
author to declare dependencies by hand.

The old system required writing, for example:

```kotlin
InterfaceDependency.default<T>().dependsOnContext { arrayOf(it.position) }.and(PLAYER)
```

The new system observes the lambda once and records the dependencies on its own.

## Core model

A lambda is evaluated under a *tracker*. Every read of a tracked value registers a
dependency. After the first run the lambda knows exactly what it read, and its result is
memoized against a snapshot of those dependencies. On later calls, if every recorded
dependency is unchanged, the cached result is returned; otherwise the lambda is re-run and
the dependency set is re-recorded.

Three kinds of "dependable things":

| Kind | What is tracked | Change detection |
|---|---|---|
| Contract (built-in) | `slot`, `player` (UUID identity), whole `context` identity | value / identity equality |
| Context field | `track`-delegated fields on `T : Context` | write-version counter |
| Datasource | manually-defined enumerable with an `id` | `id` + version counter |

> "For a list of contracts like context, player we assume that the datasource id is stable,
> so as long as the datasource didn't change, and these dependable fields didn't, we assume
> that the result won't change either."

## New package `dev.rooster.ui.tracking`

### `Tracker`

A thread-local observation scope, active only during a lambda's first evaluation.

```kotlin
object Tracker {
    private val current = ThreadLocal<Tracker?>()
    fun install(t: Tracker) = current.set(t)
    fun uninstall() = current.remove()
    fun record(dep: Dependency) = current.get()?.observations?.add(dep)
}
```

### `Dependency`

Sealed, self-describing; `isStale(info)` checks against the current receiver.

```kotlin
sealed interface Dependency {
    fun isStale(info: InterfaceInfo<*>): Boolean
}

class SlotDependency(val slot: Int) : Dependency          // info.slot != slot
class PlayerDependency(val uuid: UUID) : Dependency       // info.player.uniqueId != uuid
class ContextDependency(val ref: Any) : Dependency        // info.context !== ref
class PropertyDependency(val prop: TrackedProperty<*>, val v: Long) : Dependency // prop.version != v
class DataSourceDependency(val ds: DataSource<*>) : Dependency // id changed or version != recorded
```

### `TrackedProperty<T>`

The delegate that makes a context field observable. `track()` returns a
`ReadWriteProperty`; `trackRead` / `track { }` cover read-only values. On read it calls
`Tracker.record(PropertyDependency(this, version))`; on write it increments `version`.

```kotlin
fun <T> track(initial: T): ReadWriteProperty<Any?, T>   // for context vars
fun <T> trackRead(value: T): ReadOnlyProperty<Any?, T>  // for info.slot / player / context
```

### `DataSource<T>`

An "enumerable with an identifier": a stable `id`, a mutable `value`, and a `version`
counter bumped on `set`/`bump()`.

```kotlin
class DataSource<T>(val id: String, initial: T) {
    var version = 0L; private set
    var value: T
        get() { Tracker.record(DataSourceDependency(this)); return field }
        // ...
    fun set(v: T) { /* ... */ version++ }
    fun bump() { version++ }
}
```

### `CachableLambda<T, E>`

The memoizer (replaces the monolith's `InterfaceChachableLambda`).

```kotlin
class CachableLambda<T : Context, E>(val lambda: InterfaceInfo<T>.() -> E) {
    private var evaluated = false
    private var result: E? = null
    private var deps: List<Dependency> = emptyList()

    operator fun invoke(info: InterfaceInfo<T>): E {
        if (!evaluated) return firstRun(info)
        if (deps.any { it.isStale(info) }) return reRun(info)
        return result as E
    }
}
```

Special cases preserved from the old system:

- zero recorded deps after first run -> constant-fold (`dependsOnNothing`).
- a `CachableLambda(value)` constructor for constants (like old `InterfaceChachableLambda(itemStack)`).

## Changes to existing code

- **`InterfaceInfo`** (`interfaces/InterfaceDTOs.kt`): convert `data class` -> regular class,
  expose `slot` / `player` / `context` as `trackRead` delegates so reads route through the
  `Tracker`. The constructor stays `InterfaceInfo(slot, context, player)`; there are only two
  construction sites (`InterfaceManager.kt:44,69`). Add manual `equals`/`hashCode`/`toString`
  if needed (grep shows nothing relies on `copy`/`componentN`).
- **`InterfaceItem`** (`items/InterfaceItem.kt`): `displayItem`, `condition`, `priority`
  become `CachableLambda<T, E>` instead of raw lambdas. `displayAs` / `usedWhen` / `priority`
  wrap inputs via `lambda.toCachableLambda()`.
- **`ConditionMap` / `InterfaceItemList`** (`items/ItemDTOs.kt`): map values become
  `CachableLambda<T, Boolean>`; `flattend` invokes them.
- **Context migration** (user-facing, mechanical): `ScrollContext.position` ->
  `var position by track(0)`; `PageContext` fields likewise. `Context` itself stays an open
  class — tracking is opt-in per field.
- **`IndexedContentInterface`**: no lambda changes needed — its lambdas already read
  `slot`/`context`/`player` through the receiver, which is the whole point of auto-tracking.
  Its `contentProvider` can optionally be backed by a `DataSource` for full caching.

## Edge cases / pitfalls

1. **Non-deterministic reads** (time, `Random`, live Bukkit getters like `player.health`):
   only `info.player.uniqueId` is tracked, so a lambda reading `player.health` will not
   invalidate when health changes. Document this; users route volatile data through a
   `DataSource`.
2. **Branching**: the dependency set is re-recorded on every re-run, so a lambda that
   conditionally reads different fields self-corrects.
3. **Threading**: rendering is main-thread; `Tracker` uses `ThreadLocal` + try/finally.
   `CachableLambda` memo state is not thread-safe (fine for the Bukkit main thread — note it).
4. **Backstop expiry**: `RoosterUI.cache` (Guava, 5 min) stays, but tracked lambdas are
   invalidated by version counters, not time.
5. **`InterfaceInfo` data-class removal**: verify no `copy`/destructuring usage remains.

## Testing

The core tracking classes are Bukkit-free, so plain JVM unit tests cover: `TrackedProperty`
version bumps, `CachableLambda` constant-fold / stale-detect / branch re-record, `DataSource`
invalidation, and `Tracker` install/uninstall.

End-to-end interface tests run against the demo plugin under MockBukkit — see
`docs/mock-bukkit-testing.md`.

## Open decisions

1. **Datasource access path** — context-held field (`context.stock.value`) vs interface
   registry (`info.dataSource(id)`). Lean context-held for v1.
2. **Granularity of `context`** — track whole-object coarse *and* fine fields simultaneously
   (proposed), or fine-only.
3. **Naming** — `CachableLambda` (monolith spelling) vs `MemoizedLambda` / `TrackedLambda`.
