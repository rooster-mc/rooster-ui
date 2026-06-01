package dev.rooster.ui.interfaces

import dev.rooster.ui.items.InterfaceItem
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import kotlin.reflect.KClass

typealias Slot = Int

data class InterfaceInfo<T : Context>(
    val slot: Slot,
    val context: T,
    val player: Player
)

data class ClickInfo<T : Context>(
    val click: Click,
    val context: T,
    val event: InventoryClickEvent,
    val clickedInterface: RoosterInterface<T>
)

data class Click(
    var event: InventoryClickEvent,
    var player: Player,
    var item: ItemStack?,
    var material: Material?,
    var slot: Int
) {
    @Suppress("unused")
    val isEmpty = lazy { item != null }
}

/**
 * A class meant to be overwritten to implement the specific needs
 * four your interface. Basically some sort of value that is being
 * saved in between clicks, to save the current state of the interface.
 */
open class Context

interface ContextHandler<T : Context> {
    val contextClass: KClass<T>
    fun item() = InterfaceItem(contextClass)
    fun defaultContext(player: Player): T
}

object DefaultContextHandler : ContextHandler<Context> {
    override val contextClass: KClass<Context> = Context::class
    override fun defaultContext(player: Player) = Context()
}

fun <T : Context> KClass<T>.toHandler(defaultContext: (Player) -> T) = object : ContextHandler<T> {
    override val contextClass: KClass<T> = this@toHandler
    override fun defaultContext(player: Player): T = defaultContext(player)
}

fun <T : Context> KClass<T>.toHandler(defaultContext: T) = this.toHandler { defaultContext }

inline fun <reified T : Context> handler(noinline default: (Player) -> T) =
    T::class.toHandler(default)

inline fun <reified T : Context> handler(default: T) =

T::class.toHandler(default)
