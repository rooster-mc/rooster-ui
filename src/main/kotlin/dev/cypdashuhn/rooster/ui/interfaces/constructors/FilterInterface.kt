package dev.cypdashuhn.rooster.ui.interfaces.constructors

import dev.cypdashuhn.rooster.ui.interfaces.Context
import dev.cypdashuhn.rooster.ui.interfaces.ContextHandler
import dev.cypdashuhn.rooster.ui.interfaces.RoosterInterface
import dev.cypdashuhn.rooster.ui.items.InterfaceItem

// TODO: Finish this
/** Interface not finished, don't use! */
abstract class FilterInterface<T : FilterInterface.FilterContext>(
    override var interfaceName: String,
    contextHandler: ContextHandler<T>,
) : RoosterInterface<T>(interfaceName, contextHandler) {
    abstract class FilterContext(
        val filter: MutableMap<String, Any?>
    ) : Context()

    override fun getInterfaceItems(): List<InterfaceItem<T>> {
        TODO("Not yet implemented")
    }
}