package dev.rooster.ui.interfaces

import dev.rooster.ui.UIConstants

data class InventorySize(
    val slots: Int
) {
    companion object {
        fun fromRows(rows: Int): InventorySize {
            require(rows in 1..UIConstants.INVENTORY_MAX_ROWS) { "Inventory rows must be between 1 and ${UIConstants.INVENTORY_MAX_ROWS}" }
            return InventorySize(rows * UIConstants.ROW_SIZE)
        }

        val ONE_ROW by lazy { fromRows(1) }
        val TWO_ROWS by lazy { fromRows(2) }
        val THREE_ROWS by lazy { fromRows(3) }
        val FOUR_ROWS by lazy { fromRows(4) }
        val FIVE_ROWS by lazy { fromRows(5) }
        val SIX_ROWS by lazy { fromRows(6) }
    }
}
