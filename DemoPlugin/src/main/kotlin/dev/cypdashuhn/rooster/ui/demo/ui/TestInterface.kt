package dev.cypdashuhn.rooster.ui.demo.ui

import dev.cypdashuhn.rooster.common.util.createItem
import dev.cypdashuhn.rooster.ui.interfaces.constructors.NoContextInterface
import dev.cypdashuhn.rooster.ui.items.InterfaceItem
import dev.cypdashuhn.rooster.ui.interfaces.Context
import org.bukkit.Material

object TestInterface : NoContextInterface("test") {
    val test = item().atSlot(4).displayAs(createItem(Material.DIAMOND)).onClick {
        click.player.sendMessage("test")
    }

    override fun getInterfaceItems(): List<InterfaceItem<Context>> {
        return listOf(test)
    }
}
