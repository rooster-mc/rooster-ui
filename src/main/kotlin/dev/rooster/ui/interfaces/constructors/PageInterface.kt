package dev.rooster.ui.interfaces.constructors

import dev.rooster.core.util.createItem
import dev.rooster.ui.UIConstants
import dev.rooster.ui.UIWarnings
import dev.rooster.ui.interfaces.Context
import dev.rooster.ui.interfaces.ContextHandler
import dev.rooster.ui.interfaces.RoosterInterface
import dev.rooster.ui.interfaces.constructors.PageInterface.Page
import dev.rooster.ui.interfaces.handler
import dev.rooster.ui.items.InterfaceItem
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

abstract class DefaultPageInterface(
    pageOptions: PageInterfaceOptions<PageContext> = PageInterfaceOptions<PageContext>()
) : PageInterface<PageInterface.PageContext>(PageContext.defaultHandler, pageOptions)

abstract class PageInterface<T : PageInterface.PageContext>(
    contextHandler: ContextHandler<T>,
    pageOptions: PageInterfaceOptions<T> = PageInterfaceOptions<T>()
) : RoosterInterface<T>(contextHandler, pageOptions) {
    // TODO: Handle different Page Turners
    open class PageInterfaceOptions<T : Context> : RoosterInterfaceOptions<T>() {
        var pageTurnerModifier: (InterfaceItem<T>) -> InterfaceItem<T> = { it }

        /** Value between 1-5 (last row is bottom bar) */
        var contentRowAmount: Int = 5
    }

    val pageOptions = super.options as PageInterfaceOptions<T>

    companion object {
        const val PAGE_CONDITION_KEY = "rooster_page"
    }

    open class PageContext(
        open var page: Int = 0
    ) : Context() {
        companion object {
            val defaultHandler = handler { PageContext() }
        }
    }

    data class Page<T : Context>(
        val page: Int,
        val items: List<InterfaceItem<T>>
    )

    val bottomBar
        get() = pageOptions.contentRowAmount * UIConstants.ROW_SIZE

    abstract fun getPages(): List<Page<T>>

    val pageTurner
        get() = item()
            .atSlot(bottomBar + UIConstants.MAX_COLUMN_INDEX)
            .displayAs(createItem(Material.COMPASS, name = Component.empty()))
            .modifyContext {
                if (event.click.isLeftClick) {
                    context.page += 1
                } else {
                    context.page -= 1
                }
                if (context.page < 0) context.page = 0
            }

    val forwardPageTurner
        get() = item()
            .atSlot(bottomBar + UIConstants.MAX_COLUMN_INDEX - 1)
            .displayAs(createItem(Material.COMPASS, name = Component.empty()))
            .modifyContext {
                context.page += 1
                if (context.page < 0) context.page = 0
            }
    val backwardsPageTurner
        get() = item()
            .atSlot(bottomBar + UIConstants.MAX_COLUMN_INDEX - 2)
            .displayAs(createItem(Material.COMPASS, name = Component.empty()))
            .modifyContext {
                context.page -= 1
                if (context.page < 0) context.page = 0
            }

    final override fun getInterfaceItems(): List<InterfaceItem<T>> {
        val baseItems = mutableListOf<InterfaceItem<T>>()

        val pages = getPages()
        if (pages.isEmpty()) {
            UIWarnings.INTERFACE_PAGES_EMPTY.warn()
        } else if (pages.none { it.page == 0 } && pages.any { it.page > 0 }) {
            UIWarnings.INTERFACE_PAGES_SKIPPED_FIRST.warn()
        } else {
            val overlappingPages = pages
                .groupBy { it.page }
                .filter { it.value.size > 1 }

            if (overlappingPages.isNotEmpty()) UIWarnings.INTERFACE_PAGES_OVERLAP.warn(overlappingPages.mapValues { it.value.size })
        }

        baseItems.addAll(pages.flatMap { page ->
            page.items.onEach { item ->
                item.condition.add({ context.page == page.page }, PAGE_CONDITION_KEY)
            }
        })

        baseItems.add(pageOptions.pageTurnerModifier(pageTurner))

        return baseItems
    }

    override fun getInventory(player: Player, context: T): Inventory {
        if (pageOptions.contentRowAmount !in 1..(UIConstants.INVENTORY_MAX_ROWS - 1)) {
            throw IllegalArgumentException("Content row amount must be between 1 and ${UIConstants.INVENTORY_MAX_ROWS - 1}")
        }
        return Bukkit.createInventory(player, (pageOptions.contentRowAmount + 1) * UIConstants.ROW_SIZE, getInventoryName(player, context))
    }

    open fun getInventoryName(player: Player, context: T): TextComponent = Component.text("$interfaceName #${context.page + 1}")

    fun pages(block: PageListBuilder<T>.() -> Unit): List<Page<T>> {
        val builder = PageListBuilder<T>()
        builder.block()
        return builder.build()
    }
}

class PageListBuilder<T : Context> {
    private val pages = mutableListOf<Page<T>>()

    fun page(pageNumber: Int, block: MutableList<InterfaceItem<T>>.() -> Unit) {
        val items = mutableListOf<InterfaceItem<T>>().apply(block)
        pages += Page(pageNumber, items)
    }

    fun build() = pages.toList()
}
