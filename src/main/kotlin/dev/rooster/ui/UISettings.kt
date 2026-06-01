package dev.cypdashuhn.rooster.ui

import dev.cypdashuhn.rooster.common.RoosterSettings
import dev.cypdashuhn.rooster.common.WarningScaffold

enum class UIWarnings(
    override var warningMethod: (Any) -> String = { "" },
    override var parents: List<WarningScaffold> = emptyList(),
    override var defaultValue: Boolean = true
) : WarningScaffold {
    ALL(),
    INTERFACE_PAGES(ALL),
    INTERFACE_PAGES_EMPTY({
        "Interface pages are empty!"
    }, INTERFACE_PAGES),
    INTERFACE_PAGES_OVERLAP({
        @Suppress("UNCHECKED_CAST")
        val map = it as Map<Int, Int>
        val string = map.map { "${it.key} (${it.value}x)" }.joinToString { ", " }
        "Following Interface Pages defined multiple Times: $string"
    }, INTERFACE_PAGES),
    INTERFACE_PAGES_SKIPPED_FIRST({
        "First Page was skipped. If you didn't knew, Page-Index starts at 0."
    }, INTERFACE_PAGES);

    constructor(vararg parents: UIWarnings) : this(warningMethod = { "" }, parents = parents.toList())
    constructor(defaultValue: Boolean, vararg parents: UIWarnings) : this(warningMethod = { "" }, defaultValue = defaultValue, parents = parents.toList())
    constructor(method: (Any) -> String, vararg parents: UIWarnings) : this(warningMethod = method, parents = parents.toList())
    constructor(defaultValue: Boolean, method: (Any) -> String, vararg parents: UIWarnings) : this(warningMethod = method, defaultValue = defaultValue, parents = parents.toList())

    override fun disable() = UISettings.setWarningOption(this, false)
    override fun enable() = UISettings.setWarningOption(this, true)
    internal fun warn(obj: Any = -1) = warnScaffold(this.name, RoosterUI.logger, UISettings, obj)
}

object UISettings : RoosterSettings<UIWarnings>(UIWarnings.entries)