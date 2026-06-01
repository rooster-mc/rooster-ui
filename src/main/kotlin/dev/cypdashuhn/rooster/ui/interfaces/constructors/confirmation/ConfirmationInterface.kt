package dev.cypdashuhn.rooster.ui.interfaces.constructors.confirmation

import dev.cypdashuhn.rooster.common.util.createItem
import dev.cypdashuhn.rooster.localization.t
import dev.cypdashuhn.rooster.ui.interfaces.ClickInfo
import dev.cypdashuhn.rooster.ui.interfaces.Context
import dev.cypdashuhn.rooster.ui.interfaces.DefaultContextHandler
import dev.cypdashuhn.rooster.ui.items.InterfaceItem
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

abstract class ConfirmationInterface(
    override val interfaceName: String,
    override val onConfirm: (ClickInfo<Context>) -> Unit,
    override val onCancel: (CancelInfo<Context>) -> Unit,
) : BaseConfirmationInterface<Context>(interfaceName, DefaultContextHandler, onConfirm, onCancel) {
    open fun getInventoryName(player: Player, context: Context): Component {
        return Component
            .text("# ${t("confirm", player)} #")
            .color(TextColor.color(200, 0, 0))
    }

    override fun getInventory(player: Player, context: Context): Inventory {
        return Bukkit.createInventory(null, 9, getInventoryName(player, context))
    }

    override fun getOtherItems(): List<InterfaceItem<Context>> {
        return listOf(
            item().displayAs(createItem(Material.GREEN_STAINED_GLASS_PANE, name = Component.text("")))
        )
    }
}