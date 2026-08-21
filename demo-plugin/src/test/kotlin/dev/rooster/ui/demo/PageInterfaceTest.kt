package dev.rooster.ui.demo

import dev.rooster.ui.demo.ui.TestPageInterface
import org.bukkit.Material
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PageInterfaceTest : InterfaceTestBase() {
    @Test
    fun `renders page 0`() {
        TestPageInterface.openInventory(player)
        tick()

        val top = player.openInventory.topInventory
        assertEquals(Material.DIAMOND, top.getItem(4)?.type)
    }

    @Test
    fun `page turner advances page`() {
        TestPageInterface.openInventory(player)
        tick()

        click(53)
        tick()

        val top = player.openInventory.topInventory
        assertNull(top.getItem(4))
    }
}
