package dev.rooster.ui.interfaces.constructors.confirmation

import dev.rooster.ui.interfaces.Click
import dev.rooster.ui.interfaces.ClickInfo
import dev.rooster.ui.interfaces.Context
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent

class CancelEvent {
    var clickInfo: Pair<InventoryClickEvent, Click>? = null
    var closeInfo: InventoryCloseEvent? = null

    constructor(itemClickEvent: InventoryClickEvent, click: Click) {
        this.clickInfo = itemClickEvent to click
    }

    constructor(inventoryCloseEvent: InventoryCloseEvent) {
        this.closeInfo = inventoryCloseEvent
    }
}

class CancelInfo<T : Context>(
    var cancelEvent: CancelEvent,
    var clickedInterface: BaseConfirmationInterface<*>,
    var context: T
) {
    companion object {
        fun <T : Context> fromClick(info: ClickInfo<T>): CancelInfo<T> =
            CancelInfo(
                CancelEvent(info.event, info.click),
                info.clickedInterface as BaseConfirmationInterface<T>,
                info.context
            )
    }
}
