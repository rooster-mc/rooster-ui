package dev.rooster.ui.interfaces

data class InventorySize(val slots: Int) {
    companion object {
        fun fromRows(rows: Int): InventorySize {
            require(rows in 1..6) { "Inventory rows must be between 1 and 6" }
            return InventorySize(rows * 9)
        }

        val ONE_ROW by lazy { fromRows(1) }
        val TWO_ROWS by lazy { fromRows(2) }
        val THREE_ROWS by lazy { fromRows(3) }
        val FOUR_ROWS by lazy { fromRows(4) }
        val FIVE_ROWS by lazy { fromRows(5) }
        val SIX_ROWS by lazy { fromRows(6) }
    }
}
