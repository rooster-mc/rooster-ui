package dev.rooster.ui

import com.google.common.cache.CacheBuilder
import dev.rooster.core.RoosterCache
import dev.rooster.core.RoosterModuleBuilder
import dev.rooster.core.RoosterServices
import dev.rooster.core.initRooster
import dev.rooster.ui.context.InterfaceContextProvider
import dev.rooster.ui.context.YmlInterfaceContextProvider
import dev.rooster.ui.interfaces.InterfaceListener
import dev.rooster.ui.interfaces.RoosterInterface
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

object RoosterUI {
    internal lateinit var plugin: JavaPlugin
    internal val pluginFolder by lazy { plugin.dataFolder }
    internal var logger = Logger.getLogger("RoosterUI")
    internal val interfaces: MutableList<RoosterInterface<*>> = mutableListOf()
    internal var services: RoosterServices = RoosterServices()

    internal lateinit var cache: RoosterCache<String, Any>

    internal val interfaceContextProvider by services.delegate<InterfaceContextProvider>()

    fun init(
        plugin: JavaPlugin,
        interfaces: List<RoosterInterface<*>> = emptyList(),
        services: RoosterServices? = null,
        cache: RoosterCache<String, Any>? = null
    ) {
        this.plugin = plugin
        this.interfaces.addAll(interfaces)
        if (services != null) this.services.byOther(services)

        if (!this.services.hasService(InterfaceContextProvider::class)) {
            this.services.set<InterfaceContextProvider>(YmlInterfaceContextProvider())
        }
        this.cache = cache ?: RoosterCache(CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES))

        val pluginManager = Bukkit.getPluginManager()
        pluginManager.registerEvents(InterfaceListener, plugin)

        initRooster(plugin) {
        }
    }
}

fun RoosterModuleBuilder.ui(interfaces: List<RoosterInterface<*>>) {
    RoosterUI.init(plugin, interfaces, services, cache)
}
