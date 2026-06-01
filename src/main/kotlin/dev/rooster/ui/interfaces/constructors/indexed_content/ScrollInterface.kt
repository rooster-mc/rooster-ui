package dev.rooster.ui.interfaces.constructors.indexed_content

import dev.rooster.core.util.createItem
import dev.rooster.ui.interfaces.Context
import dev.rooster.ui.interfaces.ContextHandler
import dev.rooster.ui.interfaces.Slot
import dev.rooster.ui.interfaces.handler
import dev.rooster.ui.items.InterfaceItem
import org.bukkit.Material
import org.bukkit.entity.Player

open class ScrollContext(
    open var position: Int = 0
) : Context() {
    companion object {
        val defaultHandler = handler { ScrollContext() }
    }
}

open class ScrollInterfaceOptions<T : Context> : IndexedContentInterface.IndexedContentOptions<T>() {
    var scrollDirection: ScrollInterface.ScrollDirection = ScrollInterface.ScrollDirection.LEFT_RIGHT

    var modifyScroller: InterfaceItem<T>.() -> InterfaceItem<T> = { this }
}

abstract class ScrollInterface<ContextType : ScrollContext, DataType : Any>(
    override var interfaceName: String,
    contextHandler: ContextHandler<ContextType>,
    val scrollOptions: ScrollInterfaceOptions<ContextType> = ScrollInterfaceOptions()
) : IndexedContentInterface<ContextType, Int, DataType>(interfaceName, contextHandler, scrollOptions) {
    enum class ScrollDirection {
        TOP_BOTTOM,
        LEFT_RIGHT
    }

    private val rowSize: Int
        get() = if (scrollOptions.scrollDirection == ScrollDirection.LEFT_RIGHT) contentXWidth else contentYWidth

    private val scroller
        get() = item()
            .atSlot(bottomRow + 8)
            .displayAs(createItem(Material.COMPASS))
            .modifyContext {
                var scrollAmount = if (event.click.isShiftClick) 5 else 1
                if (event.click.isLeftClick) scrollAmount *= -1

                context.position += scrollAmount
                if (context.position < 0) context.position = 0
            }.run(scrollOptions.modifyScroller)

    final override fun getFrameItems(): List<InterfaceItem<ContextType>> = listOf(
        scroller
    )

    final override fun slotToId(slot: Slot, context: ContextType, player: Player): Int? {
        val (x, y) = offset(slot) ?: return null
        val result = if (scrollOptions.scrollDirection == ScrollDirection.LEFT_RIGHT)
            x + (y + context.position) * contentXWidth
        else y + (x + context.position) * contentYWidth

        return result
    }
}
