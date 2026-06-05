package dev.rooster.ui.messages

import dev.rooster.core.message.Message
import dev.rooster.core.message.PlainMessage

data class ConfirmationMessages(
    val confirm: Message = PlainMessage("Confirm"),
    val cancel: Message = PlainMessage("Cancel"),
    val title: Message = PlainMessage("<color:#c80000># Confirm #</color>")
) {
    companion object {
        var default = ConfirmationMessages()
    }
}
