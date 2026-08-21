# Mock Bukkit Testing (Plan)

> Goal: test the demo plugin (and, later, dependency tracking) end-to-end against a mock
> Bukkit server, without running a real Paper server.

## Approach

Use [MockBukkit](https://github.com/MockBukkit/MockBukkit) (Maven Central coordinate
`org.mockbukkit.mockbukkit:mockbukkit-v1.21`, latest `4.116.3`) as the Bukkit mock. Tests
instantiate a plugin, call `RoosterUI.init(...)` with the demo interfaces, open inventories
on a mock player, and assert rendered contents + click behaviour.

The old `rooster-monolith` had a `simulator` module doing exactly this (via the JitPack
`com.github.seeseemelk:MockBukkit-v1.21` coordinate and a terminal REPL). We keep the idea but
use the modern Central coordinate and plain JUnit tests instead of a terminal.

## Where tests live

Add a `src/test` source set to `demo-plugin/` (it is a composite build with its own
`settings.gradle.kts`, so its tests run independently). The demo plugin is the consumer we
want to exercise.

Two layers, kept separate:

1. **Interface tests** (first): use `MockBukkit.createMockPlugin()` + `RoosterUI.init(...)`
   with the demo interfaces. This sidesteps CommandAPI (see below) and tests the UI engine.
2. **Full plugin load** (optional, later): `MockBukkit.load(DemoPlugin::class.java)` — only if
   CommandAPI (`onLoad`/`onEnable`) behaves under MockBukkit; otherwise skip.

## Gradle changes (`demo-plugin/build.gradle.kts`)

```kotlin
dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v1.21:4.116.3")
    testImplementation("io.papermc.paper:paper-api:1.21.5-R0.1-SNAPSHOT")
    // ...existing compileOnly/implementation deps...
}
```

Notes:

- MockBukkit is now on Maven Central — no JitPack needed.
- MockBukkit bundles a specific Paper API version (its JAR manifest exposes `Paper-Version`).
  Align `paper-api` on the test classpath with that version; if it differs from the main
  source set's `1.21.5`, the test dependency can override for the test classpath only.
- JVM 21 toolchain and `useJUnitPlatform()` are already set.

## Test harness

```kotlin
class InterfaceTest {
    private lateinit var server: ServerMock
    private lateinit var plugin: PluginMock
    private lateinit var player: PlayerMock

    @BeforeEach fun setUp() {
        server = MockBukkit.mock()
        plugin = MockBukkit.createMockPlugin()
        RoosterUI.init(plugin, listOf(TestInterface, TestPageInterface, TestScrollInterface))
        player = server.addPlayer()
    }

    @AfterEach fun tearDown() {
        MockBukkit.unmock()
    }
}
```

Singleton-state gotcha: `RoosterUI` is an `object` whose `interfaces` list only grows via
`init(...)`. Clear `RoosterUI.interfaces` (and reset `interfaceContextProvider`) in `setUp`
or make `init` idempotent, otherwise interfaces accumulate across tests in the same JVM.

## What to test first

- **`TestInterface`**: after `openInventory(player)`, tick the scheduler and assert the top
  inventory has `DIAMOND` at slot 4 and `AIR` elsewhere.
- **Click**: simulate a click on slot 4 via `PlayerSimulation(player).simulateInventoryClick(...)`
  (the `PlayerMock.simulateInventoryClick` wrappers are deprecated; `PlayerSimulation` is the
  non-deprecated path) and assert the sent component via `player.nextComponentMessage()`.
- **`TestScrollInterface`**: open, assert the content area reflects `list2` from position 0;
  simulate a scroll click, assert `context.position` changed and the re-rendered content
  shifted.
- **`TestPageInterface`**: page navigation items change the page / render the right page.
- **Context persistence**: `RoosterUI.interfaceContextProvider` (default `Yml...`) reads/writes
  context between opens (MockBukkit provides a temp data folder, deleted on `unmock()`).

## Gotchas

1. **Deferred open**: `InterfaceManager.openTargetInterface` wraps `player.openInventory` in
   `Bukkit.getScheduler().runTask(...)`. In tests, tick the scheduler
   (`server.scheduler.performOneTick()` / `performTicks(n)`) before asserting
   `player.openInventory`.
2. **Skipped tests**: MockBukkit throws `UnimplementedOperationException` (an
   `AssumptionException`) for unsupported API — tests get *skipped*, not failed. Watch for
   silently green suites. `player.sendMessage`, `Bukkit.createInventory`,
   `player.openInventory`, the scheduler, and the plugin manager are supported; `CustomHead`
   uses `Bukkit.createProfile` and is only exercised if a head item is rendered (demo does not).
3. **`RoosterUI.init`** reads `plugin.dataFolder` and registers listeners — both fine under
   MockBukkit.
4. **Per-test isolation**: `MockBukkit.unmock()` tears down the server and temp dir; ensure no
   test leaks a reference across tests.

## Verification

```bash
cd demo-plugin
./gradlew test
```

The rooster-ui library's own unit tests (future `tracking` package) stay Bukkit-free and run
under the root build's `./gradlew test`.
