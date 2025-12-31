package dev.cypdashuhn.rooster.localization.provider

import dev.cypdashuhn.rooster.common.YmlOperations
import dev.cypdashuhn.rooster.common.YmlShell
import dev.cypdashuhn.rooster.common.util.uuid
import org.bukkit.entity.Player
import java.util.*

class YmlLocaleProvider(
    override var locales: Map<Language, Locale>,
    override var defaultLocale: Language
) : LocaleProvider(locales, defaultLocale), YmlOperations by YmlShell("languages.yml") {
    private val playerKey = "PlayerLanguages"
    private val generalKey = "GlobalLanguage"

    override fun playerLanguage(player: Player): Language? {
        val section = config.getConfigurationSection(playerKey)
        return section?.getString(player.uuid())
    }

    override fun changeLanguage(player: Player, language: Language) {
        val section = config.getConfigurationSection(playerKey) ?: config.createSection(playerKey)
        section.set(player.uuid(), language)
        saveConfig()
    }

    override fun getGlobalLanguage(): Language {
        return config.getString(generalKey) ?: defaultLocale
    }

    override fun changeGlobalLanguage(language: Language) {
        config.set(generalKey, language)
        saveConfig()
    }
}