package dev.rooster.ui.localization

import dev.rooster.localization.core.RoosterLocalization
import dev.rooster.localization.message.RoosterTranslatableMessage
import dev.rooster.ui.messages.ConfirmationMessages

fun installUILocalization() {
    RoosterLocalization.addSource(RoosterUILocalization::class.java, "/rooster-ui-locales/")
    ConfirmationMessages.default = ConfirmationMessages(
        confirm = RoosterTranslatableMessage("rooster-ui.confirmation.confirm"),
        cancel = RoosterTranslatableMessage("rooster-ui.confirmation.cancel"),
        title = RoosterTranslatableMessage("rooster-ui.confirmation.title")
    )
}

private object RoosterUILocalization
