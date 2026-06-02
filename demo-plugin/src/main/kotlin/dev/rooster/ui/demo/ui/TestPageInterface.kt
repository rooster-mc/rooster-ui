package dev.rooster.ui.demo.ui

import dev.rooster.core.util.createItem
import dev.rooster.ui.interfaces.constructors.DefaultPageInterface
import org.bukkit.Material
import org.bukkit.entity.Player

object TestPageInterface : DefaultPageInterface("test-page") {
    val test = item().atSlot(4).displayAs(createItem(Material.DIAMOND)).onClick {
        click.player.sendMessage("test")
    }

    override fun getPages(): List<Page<PageContext>> {
        return pages {
            page(0) {
                add(test)
            }
        }
    }

    override fun defaultContext(player: Player): PageContext {
        return PageContext(0)
    }
}
