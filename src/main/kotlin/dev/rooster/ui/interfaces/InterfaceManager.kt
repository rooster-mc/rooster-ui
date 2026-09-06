package dev.rooster.ui.interfaces

import dev.rooster.ui.RoosterUI
import dev.rooster.ui.RoosterUI.cache
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory

internal object InterfaceManager {
    fun closeInterface(player: Player, event: InventoryCloseEvent) {
        @Suppress("UNCHECKED_CAST")
        val correspondingInterface = currentInterface(player) as RoosterInterface<Context>? ?: return
        cache.invalidate(CURRENT_INTERFACE_KEY, player)
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
        RoosterUI.interfaceContextProvider.updateContext(player, targetInterface, context)

        val inventory = getInventory(targetInterface, context, player)
        Bukkit.getScheduler().runTask(RoosterUI.plugin, Runnable {
            cache.put(CHANGES_INTERFACE_KEY, player, true)
            cache.put(CURRENT_INTERFACE_KEY, player, targetInterface)
            player.openInventory(inventory)
        })
        return inventory
    }

    fun <T : Context> getInventory(targetInventory: RoosterInterface<T>, context: T, player: Player): Inventory {
        val inventory = targetInventory.getInventory(player, context)
        for (slot in 0 until inventory.size) {
            val info = InterfaceInfo(slot, context, player)
            targetInventory.forVisibleItem(info) {
                it.displayItem?.invoke(info)?.let { display -> inventory.setItem(slot, display) }
            }
        }
        return inventory
    }

    fun currentInterface(player: Player): RoosterInterface<*>? = cache.getIfPresent(CURRENT_INTERFACE_KEY, player) as RoosterInterface<*>?

    fun handleInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as Player

        val targetInterface = currentInterface(player) ?: return

        val click = Click(event, player, event.currentItem, event.currentItem?.type, event.slot)

        @Suppress("UNCHECKED_CAST")
        val typedInterface = targetInterface as RoosterInterface<Context>

        val context = typedInterface.getContext(player)
        val info = InterfaceInfo(click.slot, context, click.player, click.region)

        val clickInfo = ClickInfo(click, context, event, targetInterface)

        typedInterface.forVisibleItem(info) { it.onClickMerged(clickInfo) }
    }
}
