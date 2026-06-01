package dev.rooster.ui.interfaces.constructors

import dev.rooster.ui.interfaces.*

abstract class NoContextInterface(
    override var interfaceName: String,
    options: RoosterInterfaceOptions<Context> = options { }
) : RoosterInterface<Context>(interfaceName, DefaultContextHandler, options)
