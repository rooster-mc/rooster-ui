package dev.rooster.ui.interfaces

import dev.rooster.ui.RoosterUI.interfaceContextProvider
import dev.rooster.ui.items.InterfaceItem
import dev.rooster.ui.items.InterfaceItemList
import dev.rooster.ui.items.targetsNullableSlot
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory


inline fun <T : Context, reified E : RoosterInterface.RoosterInterfaceOptions<T>> options(
    block: E.() -> Unit
): E {
    val instance = E::class.java.getDeclaredConstructor().newInstance()
    instance.block()
    return instance
}

/**
 * An Instance of Interface is a model of a UI component. It's main
 * ingredient is [getInterfaceItems], which get resolved dynamically. The
 * field [interfaceName] is the key connected to the particular Interface.
 */
abstract class RoosterInterface<T : Context>(
    open val interfaceName: String,
    contextHandler: ContextHandler<T>,
    val options: RoosterInterfaceOptions<T> = options { }
) : ContextHandler<T> by contextHandler {
    open class RoosterInterfaceOptions<T : Context>() {
        // Click behaviour
        var cancelEvent: (ClickInfo<T>) -> Boolean = { true }
        var ignorePlayerInventory: Boolean = true
        var ignoreEmptySlots: Boolean = true

        // Inventory-Creator
        var inventorySize: InventorySize = InventorySize.SIX_ROWS
        var inventoryTitle: ((Player, T) -> Component)? = null
    }

    private val _itemBlocks = mutableListOf<MutableList<InterfaceItem<T>>.() -> Unit>()
    protected fun addItems(block: MutableList<InterfaceItem<T>>.() -> Unit) { _itemBlocks += block }
    internal fun getInterfaceListItems(): List<InterfaceItem<T>> =
        mutableListOf<InterfaceItem<T>>().also { list -> _itemBlocks.forEach { list.it() } }

    val items by lazy { getInterfaceItems() + getInterfaceListItems() }
    val bottomRow by lazy { options.inventorySize.slots - 9 }


    open fun getInventory(player: Player, context: T): Inventory {
        return Bukkit.createInventory(
            player,
            options.inventorySize.slots,
            options.inventoryTitle?.invoke(player, context) ?: Component.text(interfaceName)
        )
    }

    abstract fun getInterfaceItems(): List<InterfaceItem<T>>

    open fun onClose(player: Player, context: T, event: InventoryCloseEvent) {}

    internal fun forVisibleItem(info: InterfaceInfo<T>, action: (InterfaceItem<T>) -> Unit) {
        val items = groupedItems(info.player)[info.slot]
        requireNotNull(items) { "Slot somehow not included" }
        val target = items.get(info)
        if (target != null) action(target)
    }

    fun openInventory(player: Player): Inventory {
        return openInventory(player, getCurrentContext(player) ?: defaultContext(player))
    }

    fun openInventory(player: Player, context: T): Inventory {
        return InterfaceManager.openTargetInterface(player, this, context)
    }

    fun getContext(player: Player): T {
        return interfaceContextProvider.getContext(player, this) ?: defaultContext(player)
    }

    fun getCurrentContext(player: Player): T? {
        return interfaceContextProvider.getContext(player, this)
    }

    val groupedItems = emptyMap<Player, Map<Slot, InterfaceItemList<T>>>().toMutableMap()
    internal fun groupedItems(player: Player): Map<Slot, InterfaceItemList<T>> {
        val cachedItems = groupedItems[player]
        if (cachedItems != null) return cachedItems

        var maxSlot = items
            .mapNotNull { it.slots }
            .flatMap { it.slots.toList() }
            .maxOrNull() ?: 0
        if (maxSlot < 6 * 9) maxSlot = 6 * 9

        val map = mutableMapOf<Slot, InterfaceItemList<T>>()
        for (i in 0..maxSlot) {
            val items = items.filter { it.slots.targetsNullableSlot(i) }
            val list = InterfaceItemList(items)
            map += i to list
        }

        groupedItems += player to map
        return map
    }
}
