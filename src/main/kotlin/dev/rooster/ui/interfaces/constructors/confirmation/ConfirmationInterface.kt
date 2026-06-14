package dev.rooster.ui.interfaces.constructors.confirmation

import dev.rooster.core.util.createItem
import dev.rooster.ui.UIConstants
import dev.rooster.ui.interfaces.ClickInfo
import dev.rooster.ui.interfaces.Context
import dev.rooster.ui.interfaces.DefaultContextHandler
import dev.rooster.ui.items.InterfaceItem
import dev.rooster.ui.messages.ConfirmationMessages
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

abstract class ConfirmationInterface(
    override val interfaceName: String,
    override val onConfirm: (ClickInfo<Context>) -> Unit,
    override val onCancel: (CancelInfo<Context>) -> Unit,
) : BaseConfirmationInterface<Context>(interfaceName, DefaultContextHandler, onConfirm, onCancel) {
    open fun getInventoryName(player: Player, context: Context): Component = ConfirmationMessages.default.title.resolve(player)

    override fun getInventory(player: Player, context: Context): Inventory =
        Bukkit
            .createInventory(null, UIConstants.ROW_SIZE, getInventoryName(player, context))

    override fun getOtherItems(): List<InterfaceItem<Context>> =
        listOf(
            item().displayAs(createItem(Material.GREEN_STAINED_GLASS_PANE, name = Component.text("")))
        )
}
