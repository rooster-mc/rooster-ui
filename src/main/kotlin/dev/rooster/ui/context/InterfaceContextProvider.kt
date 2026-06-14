package dev.rooster.ui.context

import dev.rooster.core.RoosterService
import dev.rooster.ui.interfaces.Context
import dev.rooster.ui.interfaces.RoosterInterface
import org.bukkit.entity.Player
import kotlin.reflect.KClass

abstract class InterfaceContextProvider : RoosterService {
    /**
     * (Upsert) Saves a Context-Instance, with the keys player and interface
     * (recommended: PlayerUUID, InterfaceName)
     */
    abstract fun <T : Context> updateContext(player: Player, interfaceInstance: RoosterInterface<T>, context: T)

    /**
     * Reads the Context-Instance if it exists, with the keys player and
     * interface (recommended: PlayerUUID, InterfaceName)
     */
    abstract fun <T : Context> getContext(player: Player, interfaceInstance: RoosterInterface<T>): T?

    override fun targetClass(): KClass<out RoosterService> = InterfaceContextProvider::class
}
