package dev.rooster.ui.interfaces.constructors.indexed_content

import dev.rooster.ui.UIConstants
import dev.rooster.ui.interfaces.Context
import dev.rooster.ui.interfaces.ContextHandler
import dev.rooster.ui.interfaces.Slot
import dev.rooster.ui.interfaces.handler
import dev.rooster.ui.items.InterfaceItem
import dev.rooster.ui.messages.ScrollMessages
import dev.rooster.ui.tracking.track
import org.bukkit.Material
import org.bukkit.entity.Player

open class ScrollContext(
    initialPosition: Int = 0
) : Context() {
    var position by track(initialPosition)

    companion object {
        val defaultHandler = handler { ScrollContext() }
    }
}

data class ScrollStep(
    val normal: Int = 1,
    val shift: Int = 5
) {
    fun resolve(isShift: Boolean) = if (isShift) shift else normal
}

sealed class ScrollerObject<ContextType : ScrollContext> {
    abstract fun buildItems(iface: ScrollInterface<ContextType, *>): List<InterfaceItem<ContextType>>

    class None<ContextType : ScrollContext> : ScrollerObject<ContextType>() {
        override fun buildItems(iface: ScrollInterface<ContextType, *>) = emptyList<InterfaceItem<ContextType>>()
    }

    class Solo<ContextType : ScrollContext>(
        val scrollStep: ScrollStep = ScrollStep(),
        val modify: InterfaceItem<ContextType>.() -> InterfaceItem<ContextType> = { this }
    ) : ScrollerObject<ContextType>() {
        override fun buildItems(iface: ScrollInterface<ContextType, *>): List<InterfaceItem<ContextType>> {
            val label = ScrollMessages.default.scroller.withReplacements(
                "normal" to scrollStep.normal.toString(),
                "shift" to scrollStep.shift.toString()
            )
            return listOf(
                iface
                    .item()
                    .atSlot(iface.contentArea.bottomRow + UIConstants.MAX_COLUMN_INDEX)
                    .displayAs(Material.COMPASS, label.name, label.description)
                    .modifyContext {
                        var amount = scrollStep.resolve(event.click.isShiftClick)
                        if (event.click.isRightClick) amount *= -1
                        context.position += amount
                        if (context.position < 0) context.position = 0
                    }.run(modify)
            )
        }
    }

    class Dual<ContextType : ScrollContext>(
        val scrollStep: ScrollStep = ScrollStep(),
        val modifyUp: InterfaceItem<ContextType>.() -> InterfaceItem<ContextType> = { this },
        val modifyDown: InterfaceItem<ContextType>.() -> InterfaceItem<ContextType> = { this }
    ) : ScrollerObject<ContextType>() {
        override fun buildItems(iface: ScrollInterface<ContextType, *>): List<InterfaceItem<ContextType>> {
            val replacements = arrayOf("normal" to scrollStep.normal.toString(), "shift" to scrollStep.shift.toString())
            val labelUp = ScrollMessages.default.scrollerUp.withReplacements(*replacements)
            val labelDown = ScrollMessages.default.scrollerDown.withReplacements(*replacements)
            return listOf(
                iface
                    .item()
                    .atSlot(iface.contentArea.bottomRow + UIConstants.MAX_COLUMN_INDEX - 1)
                    .displayAs(Material.COMPASS, labelUp.name, labelUp.description)
                    .modifyContext {
                        context.position = (context.position - scrollStep.resolve(event.click.isShiftClick)).coerceAtLeast(0)
                    }.run(modifyUp),
                iface
                    .item()
                    .atSlot(iface.contentArea.bottomRow + UIConstants.MAX_COLUMN_INDEX)
                    .displayAs(Material.COMPASS, labelDown.name, labelDown.description)
                    .modifyContext {
                        context.position += scrollStep.resolve(event.click.isShiftClick)
                    }.run(modifyDown)
            )
        }
    }
}

open class ScrollInterfaceOptions<T : ScrollContext> : IndexedContentInterface.IndexedContentOptions<T>() {
    var scrollDirection: ScrollInterface.ScrollDirection = ScrollInterface.ScrollDirection.TOP_BOTTOM
    var scrollerObject: ScrollerObject<T> = ScrollerObject.Solo()
}

abstract class ScrollInterface<ContextType : ScrollContext, DataType : Any>(
    contextHandler: ContextHandler<ContextType>,
    val scrollOptions: ScrollInterfaceOptions<ContextType> = ScrollInterfaceOptions()
) : IndexedContentInterface<ContextType, Int, DataType>(contextHandler, scrollOptions) {
    enum class ScrollDirection {
        TOP_BOTTOM,
        LEFT_RIGHT
    }

    init {
        addItems { addAll(scrollOptions.scrollerObject.buildItems(this@ScrollInterface)) }
    }

    final override fun slotToId(slot: Slot, context: ContextType, player: Player): Int? {
        val (x, y) = contentArea.offset(slot) ?: return null
        return if (scrollOptions.scrollDirection == ScrollDirection.TOP_BOTTOM) {
            x + (y + context.position) * contentArea.xWidth
        } else {
            y + (x + context.position) * contentArea.yWidth
        }
    }
}
