package dev.rooster.ui.interfaces.constructors.indexed_content

import dev.rooster.ui.interfaces.*
import dev.rooster.ui.items.InterfaceItem
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

interface ContentProvidable<ContextType : Context, IdType : Any, DataType : Any> {
    fun contentProvider(id: IdType, context: ContextType): DataType?
}

abstract class IndexedContentInterface<ContextType : Context, IdType : Any, DataType : Any>(
    interfaceName: InterfaceName,
    contextHandler: ContextHandler<ContextType>,
    indexedContentOptions: IndexedContentOptions<ContextType> = IndexedContentOptions()
) : RoosterInterface<ContextType>(interfaceName, contextHandler, indexedContentOptions),
    ContentProvidable<ContextType, IdType, DataType>
{
    open class IndexedContentOptions<T : Context> : RoosterInterfaceOptions<T>() {
        var contentArea: Pair<Pair<Int, Int>, Pair<Int, Int>> = (0 to 0) to (8 to 5)

        var modifyContentItem: InterfaceItem<T>.() -> InterfaceItem<T> = { this }
        var modifyClickInArea: InterfaceItem<T>.() -> InterfaceItem<T> = { this }
    }

    val indexedContentOptions = (super.options as IndexedContentOptions<ContextType>).also {
        require(
            it.contentArea.first.first in 0..8 && it.contentArea.first.second in 0..5 &&
                    it.contentArea.second.first >= it.contentArea.first.first && it.contentArea.second.second >= it.contentArea.first.second
        ) {
            "require valid content area. x 0-8, y 0-5, second always larger then first"
        }
    }

    val contentAreaStartX by lazy { indexedContentOptions.contentArea.first.second }
    val contentAreaStartY by lazy { indexedContentOptions.contentArea.first.second }
    val contentAreaEndX by lazy { indexedContentOptions.contentArea.second.first }
    val contentAreaEndY by lazy { indexedContentOptions.contentArea.second.second }

    val contentXWidth by lazy { contentAreaEndX - contentAreaStartX + 1 }
    val contentYWidth by lazy { contentAreaEndY - contentAreaStartY + 1 }
    val contentXRange by lazy { contentAreaStartX..contentAreaEndX }
    val contentYRange by lazy { contentAreaStartY..contentAreaEndY }

    val bottomRow by lazy { contentAreaEndY * 9 }

    /** Returns the Offset, the Relative being the upper left corner. */
    fun offset(slot: Slot): Pair<Int, Int>? {
        val x = (slot % 9) // 9 being inventory width
        val y = (slot / 9)

        return if (x in contentXRange && y in contentYRange) {
            x - contentAreaStartX to y - contentAreaStartY
        } else {
            null
        }
    }

    fun allValidSlots(): List<Int> {
        return contentXRange.flatMap { x ->
            contentYRange.map { y ->
                (y + contentAreaStartY) * 9 + (x + contentAreaStartX)
            }
        }
    }

    private val contentItem
        get() = item()
            .atSlots(allValidSlots())
            .usedWhen {
                val data = dataFromPosition(slot, context, player)
                data != null
            }
            .displayAs {
                val data = dataFromPosition(slot, context, player)!!
                contentDisplay(data, context).invoke(this)
            }
            .onClick {
                val data = dataFromPosition(click.slot, context, click.player)!!
                contentClick(data, context).invoke(this)
            }

    private val clickInArea
        get() = item()
            .atSlots((0..(6 * 9)) - allValidSlots().toSet())
            .usedWhen {
                val dataExists = dataFromPosition(slot, context, player) != null
                !dataExists
            }
            .displayAs(ItemStack(Material.AIR))
            .priority(-1)
            .onClick { }

    abstract fun contentDisplay(data: DataType, context: ContextType): InterfaceInfo<ContextType>.() -> ItemStack
    abstract fun contentClick(data: DataType, context: ContextType): ClickInfo<ContextType>.() -> Unit

    final override fun getInterfaceItems(): List<InterfaceItem<ContextType>> {
        val list = mutableListOf(
            indexedContentOptions.modifyContentItem(contentItem),
            indexedContentOptions.modifyClickInArea(clickInArea)
        )

        list.addAll(getFrameItems())
        list.addAll(getOtherItems())

        return list
    }

    abstract fun getFrameItems(): List<InterfaceItem<ContextType>>
    open fun getOtherItems(): List<InterfaceItem<ContextType>> = emptyList()

    abstract fun slotToId(slot: Slot, context: ContextType, player: Player): IdType?
    protected fun dataFromPosition(slot: Int, context: ContextType, player: Player): DataType? {
        val absoluteSlot = slotToId(slot, context, player) ?: return null
        return contentProvider(absoluteSlot, context)
    }
}
