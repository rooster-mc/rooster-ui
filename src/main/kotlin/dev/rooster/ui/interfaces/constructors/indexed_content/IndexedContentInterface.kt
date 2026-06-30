package dev.rooster.ui.interfaces.constructors.indexed_content

import dev.rooster.ui.UIConstants
import dev.rooster.ui.interfaces.*
import dev.rooster.ui.items.InterfaceItem
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

interface ContentProvidable<ContextType : Context, IdType : Any, DataType : Any> {
    fun contentProvider(id: IdType, context: ContextType): DataType?
}

fun <T : Context> IndexedContentInterface.IndexedContentOptions<T>.sizeFromRows(rows: Int) {
    inventorySize = InventorySize.fromRows(rows)
    contentArea = ContentArea.fromRows(rows - 1)
}

abstract class IndexedContentInterface<ContextType : Context, IdType : Any, DataType : Any>(
    contextHandler: ContextHandler<ContextType>,
    indexedContentOptions: IndexedContentOptions<ContextType> = IndexedContentOptions()
) : RoosterInterface<ContextType>(contextHandler, indexedContentOptions),
    ContentProvidable<ContextType, IdType, DataType> {
    open class IndexedContentOptions<T : Context> : RoosterInterfaceOptions<T>() {
        var contentArea: ContentArea = ContentArea.fromRows(UIConstants.INVENTORY_MAX_ROWS - 1)

        var modifyContentItem: InterfaceItem<T>.() -> InterfaceItem<T> = { this }
        var modifyClickInArea: InterfaceItem<T>.() -> InterfaceItem<T> = { this }
    }

    val indexedContentOptions = super.options as IndexedContentOptions<ContextType>
    val contentArea get() = indexedContentOptions.contentArea

    private val contentItem
        get() = item()
            .atSlots(contentArea.allValidSlots())
            .usedWhen {
                val data = dataFromPosition(slot, context, player)
                data != null
            }.displayAs {
                val data = dataFromPosition(slot, context, player)!!
                contentDisplay(data, context).invoke(this)
            }.onClick {
                val data = dataFromPosition(click.slot, context, click.player)!!
                contentClick(data, context).invoke(this)
            }

    private val clickInArea
        get() = item()
            .atSlots(contentArea.allValidSlots())
            .usedWhen {
                val dataExists = dataFromPosition(slot, context, player) != null
                !dataExists
            }.displayAs(ItemStack(Material.AIR))
            .priority(-1)
            .onClick { }

    init {
        addItems {
            add(indexedContentOptions.modifyContentItem(contentItem))
            add(indexedContentOptions.modifyClickInArea(clickInArea))
        }
    }

    abstract fun contentDisplay(data: DataType, context: ContextType): InterfaceInfo<ContextType>.() -> ItemStack

    abstract fun contentClick(data: DataType, context: ContextType): ClickInfo<ContextType>.() -> Unit

    abstract fun slotToId(slot: Slot, context: ContextType, player: Player): IdType?

    protected fun dataFromPosition(slot: Int, context: ContextType, player: Player): DataType? {
        val absoluteSlot = slotToId(slot, context, player) ?: return null
        return contentProvider(absoluteSlot, context)
    }
}
