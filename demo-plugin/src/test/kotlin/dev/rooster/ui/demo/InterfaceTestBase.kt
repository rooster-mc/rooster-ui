package dev.rooster.ui.demo

import dev.rooster.core.RoosterServices
import dev.rooster.ui.RoosterUI
import dev.rooster.ui.context.InMemoryInterfaceContextProvider
import dev.rooster.ui.demo.ui.TestInterface
import dev.rooster.ui.demo.ui.TestPageInterface
import dev.rooster.ui.demo.ui.TestScrollInterface
import org.bukkit.event.inventory.ClickType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.entity.PlayerMock
import org.mockbukkit.mockbukkit.plugin.PluginMock
import org.mockbukkit.mockbukkit.simulate.entity.PlayerSimulation

abstract class InterfaceTestBase {
    lateinit var server: ServerMock
    lateinit var plugin: PluginMock
    lateinit var player: PlayerMock

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
        plugin = MockBukkit.createMockPlugin()
        val services = RoosterServices()
        services.set(InMemoryInterfaceContextProvider())
        RoosterUI.init(plugin, listOf(TestInterface, TestPageInterface, TestScrollInterface), services)
        player = server.addPlayer()
    }

    @AfterEach
    fun tearDown() {
        MockBukkit.unmock()
    }

    fun tick() = server.scheduler.performTicks(1L)

    fun click(slot: Int, clickType: ClickType = ClickType.LEFT) =
        PlayerSimulation(player).simulateInventoryClick(player.openInventory, clickType, slot)
}
