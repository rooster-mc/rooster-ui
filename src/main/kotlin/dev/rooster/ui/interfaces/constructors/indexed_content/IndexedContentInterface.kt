package dev.rooster.ui.interfaces.constructors.indexed_content

import dev.rooster.ui.interfaces.*
import dev.rooster.ui.interfaces.constructors.indexed_content.IndexedContentInterface.IndexedContentOptions
import dev.rooster.ui.items.InterfaceItem
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

interface ContentProvidable<ContextType : Context, IdType : Any, DataType : Any> {
    fun contentProvider(id: IdType, context: ContextType): DataType?
}

fun <T : Context> IndexedContentOptions<T>.sizeFromRows(rows: Int) {
    inventorySize = InventorySize.fromRows(rows)
    contentArea = ContentArea.fromRows(rows - 1)
}


abstract class IndexedContentInterface<ContextType : Context, IdType : Any, DataType : Any>(
    interfaceName: InterfaceName,
    contextHandler: ContextHandler<ContextType>,
    indexedContentOptions: IndexedContentOptions<ContextType> = IndexedContentOptions()
) : RoosterInterface<ContextType>(interfaceName, contextHandler, indexedContentOptions),
    ContentProvidable<ContextType, IdType, DataType>
{
    open class IndexedContentOptions<T : Context> : RoosterInterfaceOptions<T>() {
        var contentArea: ContentArea = ContentArea.fromRows(5)

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
            .atSlots((0..(6 * 9)) - contentArea.allValidSlots().toSet())
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
