package dev.cypdashuhn.rooster.ui.interfaces

import dev.cypdashuhn.rooster.ui.RoosterUI
import dev.cypdashuhn.rooster.ui.RoosterUI.cache
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.PlayerInventory

typealias InterfaceName = String

internal object InterfaceManager {
    fun closeInterface(player: Player, event: InventoryCloseEvent) {
        cache.invalidate(CURRENT_INTERFACE_KEY, player)

        // Invoke onClose for the interface
        @Suppress("UNCHECKED_CAST")
        val correspondingInterface = currentInterface(player) as RoosterInterface<Context>? ?: return
        val context = correspondingInterface.getContext(player)
        correspondingInterface.onClose(player, context, event)
    }

    const val CHANGES_INTERFACE_KEY = "rooster_ui_changes_interface"
    const val CURRENT_INTERFACE_KEY = "rooster_ui_current_interface"

    /**
     * This function opens the interface it could find depending on the
     * [targetInterface] for the given [player] applied with the current state
     * of the interface ([context]).
     */
    fun <T : Context> openTargetInterface(player: Player, targetInterface: RoosterInterface<T>, context: T): Inventory {
        cache.put(CHANGES_INTERFACE_KEY, player, true)
        cache.put(CURRENT_INTERFACE_KEY, player, targetInterface.interfaceName)

        RoosterUI.interfaceContextProvider.updateContext(player, targetInterface, context)

        val inventory = getInventory(targetInterface, context, player)
        player.openInventory(inventory)
        return inventory
    }

    fun <T : Context> getInventory(
        targetInventory: RoosterInterface<T>,
        context: T,
        player: Player
    ): Inventory {
        val inventory = targetInventory.getInventory(player, context)
        for (slot in 0 until inventory.size) {
            val info = InterfaceInfo(slot, context, player)
            targetInventory.forVisibleItem(info) {
                inventory.setItem(slot, it.displayItem(info))
            }
        }
        return inventory
    }

    fun currentInterface(player: Player): RoosterInterface<*>? {
        val interfaceName = cache.getIfPresent(CURRENT_INTERFACE_KEY, player) as String? ?: return null
        if (interfaceName.isEmpty()) return null

        return RoosterUI.interfaces
            .firstOrNull { currentInterface -> currentInterface.interfaceName == interfaceName }
    }

    fun handleInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as Player

        val targetInterface = currentInterface(player) ?: return

        if (event.currentItem == null && targetInterface.options.ignoreEmptySlots) return
        if (event.inventory is PlayerInventory && targetInterface.options.ignorePlayerInventory) return

        val click = Click(event, player, event.currentItem, event.currentItem?.type, event.slot)

        @Suppress("UNCHECKED_CAST")
        val typedInterface = targetInterface as RoosterInterface<Context>

        val context = typedInterface.getContext(player)
        val info = InterfaceInfo(click.slot, context, click.player)

        val clickInfo = ClickInfo(click, context, event, targetInterface)
        if (typedInterface.options.cancelEvent(clickInfo)) event.isCancelled = true

        typedInterface.forVisibleItem(info) { it.onClickMerged(clickInfo) }
    }
}