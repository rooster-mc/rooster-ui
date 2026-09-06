package dev.rooster.ui.items

import dev.rooster.core.message.Message
import dev.rooster.core.util.createItem
import dev.rooster.ui.UIConstants
import dev.rooster.ui.interfaces.ClickInfo
import dev.rooster.ui.interfaces.Context
import dev.rooster.ui.interfaces.InterfaceInfo
import dev.rooster.ui.interfaces.RoosterInterface
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import kotlin.reflect.KClass

class InterfaceItem<T : Context> {
    constructor(clazz: KClass<T>) {
        this.contextClass = clazz
        this.condition = ConditionMap(clazz)
    }

    private val contextClass: KClass<T>

    internal var slots: Slots? = null
    internal var condition: ConditionMap<T>

    internal var priority: (InterfaceInfo<T>.() -> Int) = { 0 }
    internal var staticPriority: Int? = null

    internal var displayItem: (InterfaceInfo<T>.() -> ItemStack)? = { createItem(Material.AIR) }

    internal var onClick: (ClickInfo<T>.() -> Unit)? = null
    private var contextModifier: (ClickInfo<T>.() -> Unit)? = null
    private var routeToInterface: (ClickInfo<T>.() -> Unit)? = null
    internal var locksClick: (ClickInfo<T>.() -> Boolean) = { true }
    internal val onClickMerged: (ClickInfo<T>.() -> Unit)
        get() = {
            if (locksClick.invoke(this)) event.isCancelled = true
            onClick?.invoke(this)
            contextModifier?.invoke(this)
            routeToInterface?.invoke(this)
        }

    val state = State()

    inner class State {
        val slots get() = this@InterfaceItem.slots
        val condition get() = this@InterfaceItem.condition
        val priority get() = this@InterfaceItem.priority
        val staticPriority get() = this@InterfaceItem.staticPriority
        val displayItem get() = this@InterfaceItem.displayItem
        val onClick get() = this@InterfaceItem.onClick
        val contextModifier get() = this@InterfaceItem.contextModifier
        val routeToInterface get() = this@InterfaceItem.routeToInterface
        val locksClick get() = this@InterfaceItem.locksClick
        val onClickMerged get() = this@InterfaceItem.onClickMerged
    }

    internal fun check(info: InterfaceInfo<T>): Boolean = slots.targetsSlot(info.slot) && condition.flattend(info)

    fun usedWhen(conditionKey: String = ConditionMap.ANONYMOUS_KEY, condition: InterfaceInfo<T>.() -> Boolean) =
        copy {
            this.condition.set(condition, conditionKey)
        }

    fun atSlot(slot: Int) =
        copy {
            this.slots = Slots(slot)
        }

    fun atSlot(row: Int, slot: Int) =
        copy {
            this.slots = Slots((row - 1) * UIConstants.ROW_SIZE + slot)
        }

    fun atSlots(vararg slots: Int) = atSlots(slots.toList())

    fun atSlots(slots: Slots) = copy { this.slots = slots }

    fun atSlots(slots: List<Int>) =
        copy {
            this.slots = Slots(slots)
        }

    fun forAllSlots() = copy { this.slots = Slots.all() }

    fun forPlayerInventory() = copy { this.slots = Slots.playerInventory() }

    fun forEverything() = copy { this.slots = Slots.everything() }

    fun resetConditions(excludingConditionKeys: List<String>) =
        copy {
            this.condition.resetConditions(excludingConditionKeys)
        }

    fun priority(priority: InterfaceInfo<T>.() -> Int,): InterfaceItem<T> =
        copy {
            this.priority = { it: InterfaceInfo<T> -> priority(it) }
            this.staticPriority = null
        }

    fun priority(priority: Int): InterfaceItem<T> =
        copy {
            this.priority = { priority }
            this.staticPriority = priority
        }

    /** Lowest possible priority; this item only applies when nothing else matches. */
    fun asFallback(): InterfaceItem<T> = priority(Int.MIN_VALUE)

    /** Highest possible priority; this item always wins over every other item. */
    fun aboveAll(): InterfaceItem<T> = priority(Int.MAX_VALUE)

    /** Does something when the item is clicked */
    fun onClick(action: ClickInfo<T>.() -> Unit): InterfaceItem<T> = copy { this.onClick = action }

    /** Controls whether clicking this item locks the slot (cancels the underlying inventory event). Defaults to locking. */
    fun locksClick(predicate: ClickInfo<T>.() -> Boolean): InterfaceItem<T> = copy { this.locksClick = predicate }

    /** Unlocks the slot so clicks pass through to vanilla behaviour (the event is not cancelled). */
    fun unlocked(): InterfaceItem<T> = copy { this.locksClick = { false } }

    /** Unlocks the slot so clicks pass through to vanilla behaviour whenever [predicate] holds. */
    fun unlockedWhen(predicate: ClickInfo<T>.() -> Boolean): InterfaceItem<T> = copy { this.locksClick = { !predicate() } }

    /** Modifies the context of the click info when the item is clicked, and opens the inventory with the modified context by default */
    fun modifyContext(openInventory: Boolean = true, action: ClickInfo<T>.() -> Unit): InterfaceItem<T> =
        copy {
            if (!openInventory) {
                this.contextModifier = action
            } else {
                this.contextModifier = {
                    action()
                    clickedInterface.openInventory(click.player, context)
                }
            }
        }

    fun <E : Context> routeTo(targetInterface: RoosterInterface<E>, context: E? = null) =
        copy {
            this.routeToInterface = {
                if (context == null) {
                    targetInterface.openInventory(click.player)
                } else {
                    targetInterface.openInventory(click.player, context)
                }
            }
        }

    fun <E : Context> routeTo(targetInterfaceItem: RoosterInterface<E>, getContext: (ClickInfo<T>.() -> E)) =
        copy {
            this.routeToInterface = {
                targetInterfaceItem.openInventory(click.player, getContext())
            }
        }

    fun displayAs(itemStackCreator: InterfaceInfo<T>.() -> ItemStack) = copy { this.displayItem = itemStackCreator }

    fun displayAs(itemStack: ItemStack): InterfaceItem<T> = copy { this.displayItem = { itemStack } }

    /** Renders nothing at the slot, leaving whatever item was already there untouched. */
    fun leavesSlotUntouched(): InterfaceItem<T> = copy { this.displayItem = null }

    fun displayAs(
        material: Material,
        name: Message,
        description: List<Message>? = null,
        amount: Int = 1,
        additional: (ItemMeta) -> Unit = {}
    ): InterfaceItem<T> = displayAs { createItem(material, name, player, description, amount, additional) }

    fun copy(modifyingBlock: InterfaceItem<T>.() -> Unit): InterfaceItem<T> {
        val copy = InterfaceItem(contextClass).also {
            it.condition = condition.copy()
            it.displayItem = displayItem
            it.onClick = onClick
            it.locksClick = locksClick
            it.priority = priority
            it.staticPriority = staticPriority
            it.slots = slots
        }
        copy.modifyingBlock()
        return copy
    }
}
