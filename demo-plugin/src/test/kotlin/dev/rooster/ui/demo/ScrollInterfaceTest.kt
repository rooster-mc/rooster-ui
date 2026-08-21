package dev.rooster.ui.demo

import dev.rooster.ui.demo.ui.TestScrollInterface
import org.bukkit.Material
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ScrollInterfaceTest : InterfaceTestBase() {
    @Test
    fun `renders content from position 0`() {
        TestScrollInterface.openInventory(player)
        tick()

        val top = player.openInventory.topInventory
        assertEquals(Material.DIAMOND, top.getItem(0)?.type)
        assertEquals(Material.BAKED_POTATO, top.getItem(1)?.type)
    }

    @Test
    fun `scrolling shifts content`() {
        TestScrollInterface.openInventory(player)
        tick()

        click(53)
        tick()

        val top = player.openInventory.topInventory
        assertEquals(Material.BAMBOO_BLOCK, top.getItem(0)?.type)
    }
}
