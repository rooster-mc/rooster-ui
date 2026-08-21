package dev.rooster.ui.demo

import dev.rooster.ui.demo.ui.TestInterface
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TestInterfaceTest : InterfaceTestBase() {
    @Test
    fun `renders diamond at slot 4`() {
        TestInterface.openInventory(player)
        tick()

        val top = player.openInventory.topInventory
        assertEquals(Material.DIAMOND, top.getItem(4)?.type)
        assertNull(top.getItem(0))
    }

    @Test
    fun `click on item sends message`() {
        TestInterface.openInventory(player)
        tick()

        click(4)

        assertEquals(Component.text("test"), player.nextComponentMessage())
    }
}
