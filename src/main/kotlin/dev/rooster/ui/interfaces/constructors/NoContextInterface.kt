package dev.rooster.ui.interfaces.constructors

import dev.rooster.ui.interfaces.*

abstract class NoContextInterface(
    options: RoosterInterfaceOptions<Context> = options { }
) : RoosterInterface<Context>(DefaultContextHandler, options)
