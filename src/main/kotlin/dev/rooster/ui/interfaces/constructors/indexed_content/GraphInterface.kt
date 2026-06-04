package dev.rooster.ui.interfaces.constructors.indexed_content

import dev.rooster.core.util.createItem
import dev.rooster.ui.interfaces.Context
import dev.rooster.ui.interfaces.ContextHandler
import dev.rooster.ui.interfaces.Slot
import dev.rooster.ui.interfaces.options
import dev.rooster.ui.items.InterfaceItem
import org.bukkit.Material
import org.bukkit.entity.Player

abstract class GraphInterface<ContextType : GraphInterface.GraphContext, DataType : Any>(
    override val interfaceName: String,
    contextHandler: ContextHandler<ContextType>,
    val graphOptions: GraphOptions<ContextType> = options { }
) : IndexedContentInterface<ContextType, Pair<Int, Int>, DataType>(interfaceName, contextHandler, graphOptions) {
    class GraphOptions<T : Context> : IndexedContentOptions<T>() {
        var modifyVerticalPager: InterfaceItem<T>.() -> InterfaceItem<T> = { this }
        var modifyHorizontalPager: InterfaceItem<T>.() -> InterfaceItem<T> = { this }
    }

    open class GraphContext(
        open var position: Pair<Int, Int> = 0 to 0
    ) : Context()

    private val verticalPager
        get() = item()
            .atSlot(contentArea.bottomRow + 8)
            .displayAs(createItem(Material.COMPASS))
            .modifyContext {
                val y = context.position.second
                var scrollAmount = if (event.isShiftClick) 5 else 1
                if (event.click.isLeftClick) scrollAmount *= -1
                context.position = context.position.first to y + scrollAmount
            }

    private val horizontalPager
        get() = item()
            .atSlot(contentArea.bottomRow + 8)
            .displayAs(createItem(Material.COMPASS))
            .modifyContext {
                val x = context.position.first
                var scrollAmount = if (event.isShiftClick) 5 else 1
                if (event.click.isLeftClick) scrollAmount *= -1
                context.position = x + scrollAmount to context.position.second
            }

    final override fun getFrameItems(): List<InterfaceItem<ContextType>> = listOf(
        graphOptions.modifyVerticalPager(verticalPager),
        graphOptions.modifyHorizontalPager(horizontalPager)
    )

    override fun slotToId(slot: Slot, context: ContextType, player: Player): Pair<Int, Int>? {
        val (x, y) = contentArea.offset(slot) ?: return null
        val (posX, posY) = context.position

        return x + posX to y + posY
    }
}
