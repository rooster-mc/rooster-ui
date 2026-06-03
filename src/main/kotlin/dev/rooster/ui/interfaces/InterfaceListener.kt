package dev.rooster.ui.interfaces

import dev.rooster.ui.RoosterUI.cache
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent

@Suppress("unused")
object InterfaceListener : Listener{
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        InterfaceManager.handleInventoryClick(event)
    }

    @EventHandler
    fun onInventoryOpen(event: InventoryOpenEvent) {
        cache.invalidate(InterfaceManager.CHANGES_INTERFACE_KEY, event.player)
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        if (cache.getIfPresent(InterfaceManager.CHANGES_INTERFACE_KEY, event.player) as Boolean? == true) {
            cache.invalidate(InterfaceManager.CHANGES_INTERFACE_KEY, event.player)
            return
        }
        InterfaceManager.closeInterface(event.player as Player, event)
    }
}
