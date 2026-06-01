package dev.rooster.ui.demo.ui

import dev.rooster.core.util.createItem
import dev.rooster.core.util.toComponent
import dev.rooster.ui.interfaces.ClickInfo
import dev.rooster.ui.interfaces.InterfaceInfo
import dev.rooster.ui.interfaces.constructors.indexed_content.ScrollContext
import dev.rooster.ui.interfaces.constructors.indexed_content.ScrollInterface
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class Entry(
    val name: String,
    val material: Material
)

object TestScrollInterface :
    ScrollInterface<ScrollContext, Entry>("test-scroll", ScrollContext.defaultHandler) {
    val list = mutableListOf(
        Entry("Robert", Material.DIAMOND),
        Entry("Peter", Material.BAKED_POTATO),
        Entry("John", Material.BREAD),
        Entry("Jane", Material.BEETROOT),
        Entry("Stephan", Material.BAMBOO_BLOCK)
    )
    val list2 = list + list + list + list + list + list + list

    override fun contentDisplay(
        data: Entry,
        context: ScrollContext
    ): InterfaceInfo<ScrollContext>.() -> ItemStack {
        return { createItem(data.material, data.name.toComponent()) }
    }

    override fun contentClick(
        data: Entry,
        context: ScrollContext
    ): ClickInfo<ScrollContext>.() -> Unit {
        return { click.player.sendMessage(data.name) }
    }

    override fun contentProvider(
        id: Int,
        context: ScrollContext
    ): Entry? {
        return list2.getOrNull(id)
    }

}
