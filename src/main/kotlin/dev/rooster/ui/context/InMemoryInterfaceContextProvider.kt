package dev.rooster.ui.context

import dev.rooster.ui.interfaces.Context
import dev.rooster.ui.interfaces.RoosterInterface
import org.bukkit.entity.Player
import java.util.UUID

class InMemoryInterfaceContextProvider : InterfaceContextProvider() {
    private val store = HashMap<Pair<UUID, String>, Context>()

    override fun <T : Context> updateContext(player: Player, interfaceInstance: RoosterInterface<T>, context: T) {
        store[player.uniqueId to interfaceInstance.interfaceName] = context
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Context> getContext(player: Player, interfaceInstance: RoosterInterface<T>): T? {
        return store[player.uniqueId to interfaceInstance.interfaceName] as T?
    }
}
