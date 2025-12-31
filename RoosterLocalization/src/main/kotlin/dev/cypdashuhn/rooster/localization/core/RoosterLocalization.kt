package dev.cypdashuhn.rooster.localization.core

import com.google.common.cache.CacheBuilder
import dev.cypdashuhn.rooster.common.RoosterCache
import dev.cypdashuhn.rooster.common.RoosterModuleBuilder
import dev.cypdashuhn.rooster.common.RoosterServices
import dev.cypdashuhn.rooster.common.initRooster
import dev.cypdashuhn.rooster.localization.provider.LocaleProvider
import dev.cypdashuhn.rooster.localization.provider.YmlLocaleProvider
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

object RoosterLocalization {
    internal lateinit var plugin: JavaPlugin
    internal val logger: Logger = Logger.getLogger("RoosterLocalization")
    lateinit var cache: RoosterCache<String, Any>
    internal var services: RoosterServices = RoosterServices()
    internal val localeProvider by services.delegate<LocaleProvider>()

    fun init(
        plugin: JavaPlugin,
        services: RoosterServices? = null,
        cache: RoosterCache<String, Any>? = null
    ) {
        this.plugin = plugin
        if (services != null) this.services.byOther(services)
        if (!this.services.hasService(LocaleProvider::class)) {
            this.services.set<LocaleProvider>(YmlLocaleProvider(mapOf("en_US" to Locale.ENGLISH), "en_US"))
        }
        this.cache = cache ?: RoosterCache(
            CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES)
        )

        initRooster(plugin, this.services, this.cache)
    }
}

fun RoosterModuleBuilder.localization() {
    RoosterLocalization.init(plugin, services, cache)
}