package dev.rooster.ui.shared

import com.destroystokyo.paper.profile.ProfileProperty
import dev.rooster.core.message.Message
import net.kyori.adventure.text.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta
import java.util.UUID

@JvmInline
value class CustomHead(
    val textureValue: String
) {
    private fun applyTexture(meta: SkullMeta) {
        val profile = Bukkit.createProfile(UUID.randomUUID())
        profile.setProperty(ProfileProperty("textures", textureValue))
        meta.playerProfile = profile
    }

    fun toItem(): ItemStack {
        val item = ItemStack(Material.PLAYER_HEAD)
        val meta = item.itemMeta as SkullMeta
        applyTexture(meta)
        item.itemMeta = meta
        return item
    }

    fun toItem(
        name: Message,
        player: Player,
        description: List<Message>? = null,
        amount: Int = 1,
        additional: (ItemMeta) -> Unit = {}
    ): ItemStack {
        val item = ItemStack(Material.PLAYER_HEAD, amount)
        val meta = item.itemMeta as SkullMeta
        applyTexture(meta)
        meta.displayName(name.resolve(player))
        if (description != null) meta.lore(description.map { it.resolve(player) })
        additional(meta)
        item.itemMeta = meta
        return item
    }

    fun toItem(
        name: TextComponent? = null,
        description: List<TextComponent>? = null,
        amount: Int = 1,
        additional: (ItemMeta) -> Unit = {}
    ): ItemStack {
        val item = ItemStack(Material.PLAYER_HEAD, amount)
        val meta = item.itemMeta as SkullMeta
        applyTexture(meta)
        if (name != null) meta.displayName(name)
        if (description != null) meta.lore(description)
        additional(meta)
        item.itemMeta = meta
        return item
    }
}
