package dev.rooster.ui.interfaces

import dev.rooster.ui.items.InterfaceItem
import dev.rooster.ui.tracking.ContextDependency
import dev.rooster.ui.tracking.PlayerDependency
import dev.rooster.ui.tracking.SlotDependency
import dev.rooster.ui.tracking.TrackedProperty
import dev.rooster.ui.tracking.Tracker
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import java.lang.reflect.Field
import kotlin.reflect.KClass

typealias Slot = Int

class InterfaceInfo<T : Context>(
    slot: Slot,
    context: T,
    player: Player
) {
    private val _slot = slot
    private val _context = context
    private val _player = player

    val slot: Slot
        get() {
            Tracker.record(SlotDependency(_slot))
            return _slot
        }

    val context: T
        get() {
            Tracker.record(ContextDependency(_context))
            return _context
        }

    val player: Player
        get() {
            Tracker.record(PlayerDependency(_player.uniqueId))
            return _player
        }
}

data class ClickInfo<T : Context>(
    val click: Click,
    val context: T,
    val event: InventoryClickEvent,
    val clickedInterface: RoosterInterface<T>
)

data class Click(
    var event: InventoryClickEvent,
    var player: Player,
    var item: ItemStack?,
    var material: Material?,
    var slot: Int
) {
    @Suppress("unused")
    val isEmpty = lazy { item != null }
}

/**
 * A class meant to be overwritten to implement the specific needs
 * four your interface. Basically some sort of value that is being
 * saved in between clicks, to save the current state of the interface.
 */
open class Context {
    fun trackedValues(): Map<String, Any?> =
        trackedPropertyFields().mapValues { (_, field) -> (field.get(this) as TrackedProperty<*>).current }

    fun restoreTrackedValues(values: Map<String, Any?>) {
        val fields = trackedPropertyFields()
        values.forEach { (name, value) ->
            if (value != null) (fields[name]?.get(this) as? TrackedProperty<*>)?.restore(value)
        }
    }

    private fun trackedPropertyFields(): Map<String, Field> {
        val result = LinkedHashMap<String, Field>()
        var clazz: Class<*>? = javaClass
        while (clazz != null && clazz != Any::class.java) {
            clazz.declaredFields.forEach { field ->
                if (TrackedProperty::class.java.isAssignableFrom(field.type)) {
                    field.isAccessible = true
                    result.putIfAbsent(field.name.removeSuffix("\$delegate"), field)
                }
            }
            clazz = clazz.superclass
        }
        return result
    }
}

interface ContextHandler<T : Context> {
    val contextClass: KClass<T>

    fun item() = InterfaceItem(contextClass)

    fun defaultContext(player: Player): T
}

object DefaultContextHandler : ContextHandler<Context> {
    override val contextClass: KClass<Context> = Context::class

    override fun defaultContext(player: Player) = Context()
}

fun <T : Context> KClass<T>.toHandler(defaultContext: (Player) -> T) =
    object : ContextHandler<T> {
        override val contextClass: KClass<T> = this@toHandler

        override fun defaultContext(player: Player): T = defaultContext(player)
    }

fun <T : Context> KClass<T>.toHandler(defaultContext: T) = this.toHandler { defaultContext }

inline fun <reified T : Context> handler(noinline default: (Player) -> T) = T::class.toHandler(default)

inline fun <reified T : Context> handler(default: T) = T::class.toHandler(default)
