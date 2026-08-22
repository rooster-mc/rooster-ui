package dev.rooster.ui.tracking

import java.util.ArrayDeque

class TrackingScope {
    private val observations = linkedSetOf<Dependency>()

    fun record(dependency: Dependency) {
        observations += dependency
    }

    fun dependencies(): List<Dependency> = observations.toList()
}

object Tracker {
    private val stack = ThreadLocal<ArrayDeque<TrackingScope>?>()

    fun install(scope: TrackingScope) {
        val scopes = stack.get() ?: ArrayDeque<TrackingScope>().also { stack.set(it) }
        scopes.addLast(scope)
    }

    fun uninstall() {
        val scopes = stack.get() ?: return
        scopes.removeLast()
        if (scopes.isEmpty()) stack.remove()
    }

    fun record(dependency: Dependency) {
        stack.get()?.lastOrNull()?.record(dependency)
    }
}
