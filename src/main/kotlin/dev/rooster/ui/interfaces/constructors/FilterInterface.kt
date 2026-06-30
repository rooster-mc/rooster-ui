package dev.rooster.ui.interfaces.constructors

import dev.rooster.ui.interfaces.Context
import dev.rooster.ui.interfaces.ContextHandler
import dev.rooster.ui.interfaces.RoosterInterface
import dev.rooster.ui.items.InterfaceItem

// TODO: Finish this

/** Interface not finished, don't use! */
abstract class FilterInterface<T : FilterInterface.FilterContext>(
    contextHandler: ContextHandler<T>,
) : RoosterInterface<T>(contextHandler) {
    abstract class FilterContext(
        val filter: MutableMap<String, Any?>
    ) : Context()

    override fun getInterfaceItems(): List<InterfaceItem<T>> {
        TODO("Not yet implemented")
    }
}
