package dev.rooster.ui.demo.ui

import dev.rooster.core.util.createItem
import dev.rooster.ui.interfaces.Context
import dev.rooster.ui.interfaces.constructors.NoContextInterface
import dev.rooster.ui.items.InterfaceItem
import org.bukkit.Material

object TestInterface : NoContextInterface() {
    val test = item().atSlot(4).displayAs(createItem(Material.DIAMOND)).onClick {
        click.player.sendMessage("test")
    }

    override fun getInterfaceItems(): List<InterfaceItem<Context>> = listOf(test)
}
