package dev.rooster.ui.tracking

import dev.rooster.ui.interfaces.InterfaceInfo
import dev.rooster.ui.interfaces.Slot
import java.util.UUID

sealed interface Dependency {
    fun isStale(info: InterfaceInfo<*>): Boolean
}

class SlotDependency(private val slot: Slot) : Dependency {
    override fun isStale(info: InterfaceInfo<*>) = info.slot != slot
}

class PlayerDependency(private val uuid: UUID) : Dependency {
    override fun isStale(info: InterfaceInfo<*>) = info.player.uniqueId != uuid
}

class ContextDependency(private val reference: Any) : Dependency {
    override fun isStale(info: InterfaceInfo<*>) = info.context !== reference
}

class PropertyDependency(
    private val property: TrackedProperty<*>,
    private val version: Long
) : Dependency {
    override fun isStale(info: InterfaceInfo<*>) = property.version != version
}

class DataSourceDependency(
    private val dataSource: DataSource<*>,
    private val version: Long
) : Dependency {
    override fun isStale(info: InterfaceInfo<*>) = dataSource.version != version
}
