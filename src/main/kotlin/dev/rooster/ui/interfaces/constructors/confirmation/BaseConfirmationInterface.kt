package dev.cypdashuhn.rooster.ui.interfaces.constructors.confirmation

import dev.cypdashuhn.rooster.common.util.createItem
import dev.cypdashuhn.rooster.localization.t
import dev.cypdashuhn.rooster.ui.interfaces.ClickInfo
import dev.cypdashuhn.rooster.ui.interfaces.Context
import dev.cypdashuhn.rooster.ui.interfaces.ContextHandler
import dev.cypdashuhn.rooster.ui.interfaces.RoosterInterface
import dev.cypdashuhn.rooster.ui.interfaces.options
import dev.cypdashuhn.rooster.ui.items.InterfaceItem
import net.kyori.adventure.text.Component
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
            .displayAs(createItem(Material.GREEN_STAINED_GLASS_PANE, name = Component.text(t("confirm"))))
            .onClick(onConfirm)

    private val cancelItem
        get() = item()
            .atSlot(0)
            .displayAs(createItem(Material.RED_STAINED_GLASS_PANE, name = Component.text("")))
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