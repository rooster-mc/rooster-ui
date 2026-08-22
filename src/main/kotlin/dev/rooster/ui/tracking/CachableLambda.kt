package dev.rooster.ui.tracking

import dev.rooster.ui.interfaces.Context
import dev.rooster.ui.interfaces.InterfaceInfo

class CachableLambda<T : Context, E>(
    private val lambda: InterfaceInfo<T>.() -> E
) {
    private var evaluated = false
    private var result: E? = null
    private var dependencies: List<Dependency> = emptyList()

    constructor(value: E) : this({ value })

    operator fun invoke(info: InterfaceInfo<T>): E {
        if (!evaluated) return evaluate(info)
        if (dependencies.any { it.isStale(info) }) return evaluate(info)

        @Suppress("UNCHECKED_CAST")
        return result as E
    }

    private fun evaluate(info: InterfaceInfo<T>): E {
        val scope = TrackingScope()
        Tracker.install(scope)
        try {
            val value = lambda(info)
            dependencies = scope.dependencies()
            result = value
            evaluated = true
        } finally {
            Tracker.uninstall()
        }

        @Suppress("UNCHECKED_CAST")
        return result as E
    }
}
