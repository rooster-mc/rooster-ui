package dev.rooster.ui.interfaces.constructors.confirmation

import dev.rooster.ui.interfaces.ClickInfo
import dev.rooster.ui.interfaces.Context
import dev.rooster.ui.interfaces.ContextHandler
import dev.rooster.ui.interfaces.RoosterInterface
import dev.rooster.ui.interfaces.options
import dev.rooster.ui.items.InterfaceItem
import dev.rooster.ui.messages.ConfirmationMessages
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent

abstract class BaseConfirmationInterface<T : Context>(
    override val interfaceName: String,
    contextHandler: ContextHandler<T>,
    open val onConfirm: (ClickInfo<T>) -> Unit,
    open val onCancel: (CancelInfo<T>) -> Unit,
    val baseConfirmationOptions: BaseConfirmationOptions<T> = options { }
) : RoosterInterface<T>(interfaceName, contextHandler) {
    class BaseConfirmationOptions<T : Context> : RoosterInterfaceOptions<T>() {
        var modifyConfirmationItem: InterfaceItem<T>.() -> InterfaceItem<T> = { this }
        var modifyCancelItem: InterfaceItem<T>.() -> InterfaceItem<T> = { this }
    }

    private val confirmationItem
        get() = item()
            .atSlot(8)
            .displayAs(Material.GREEN_STAINED_GLASS_PANE, ConfirmationMessages.default.confirm)
            .onClick(onConfirm)

    private val cancelItem
        get() = item()
            .atSlot(0)
            .displayAs(Material.RED_STAINED_GLASS_PANE, ConfirmationMessages.default.cancel)
            .onClick { onCancel(CancelInfo.fromClick(this)) }

    abstract fun getOtherItems(): List<InterfaceItem<T>>
    override fun getInterfaceItems(): List<InterfaceItem<T>> {
        val list = mutableListOf(
            baseConfirmationOptions.modifyConfirmationItem(confirmationItem),
            baseConfirmationOptions.modifyCancelItem(cancelItem),
        )

        list.addAll(getOtherItems())

        return list
    }

    override fun onClose(player: Player, context: T, event: InventoryCloseEvent) {
        onCancel(CancelInfo(CancelEvent(event), this, context))
    }
}
