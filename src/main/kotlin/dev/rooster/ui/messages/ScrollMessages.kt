package dev.rooster.ui.messages

import dev.rooster.core.message.MessageLabel
import dev.rooster.core.message.PlainMessage

data class ScrollMessages(
    val scroller: MessageLabel = MessageLabel(
        PlainMessage("Scroll"),
        listOf(
            PlainMessage("Click left for down, right for up"),
            PlainMessage("Click: \${normal} | Shift-click: \${shift}")
        )
    ),
    val scrollerUp: MessageLabel = MessageLabel(
        PlainMessage("Scroll Up"),
        listOf(PlainMessage("Click: \${normal} | Shift-click: \${shift}"))
    ),
    val scrollerDown: MessageLabel = MessageLabel(
        PlainMessage("Scroll Down"),
        listOf(PlainMessage("Click: \${normal} | Shift-click: \${shift}"))
    )
) {
    companion object {
        var default = ScrollMessages()
    }
}
