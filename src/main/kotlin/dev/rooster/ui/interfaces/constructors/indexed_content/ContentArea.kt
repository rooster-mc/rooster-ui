package dev.rooster.ui.interfaces.constructors.indexed_content

import dev.rooster.ui.UIConstants

class ContentArea(
    val minX: Int,
    val minY: Int,
    val maxX: Int,
    val maxY: Int
) {
    init {
        require(minX in 0..UIConstants.MAX_COLUMN_INDEX && minY in 0..(UIConstants.INVENTORY_MAX_ROWS - 1)) {
            "Content area start out of bounds: x in 0..${UIConstants.MAX_COLUMN_INDEX}, y in 0..${UIConstants.INVENTORY_MAX_ROWS - 1}"
        }
        require(maxX in minX..UIConstants.MAX_COLUMN_INDEX && maxY in minY..(UIConstants.INVENTORY_MAX_ROWS - 1)) {
            "Content area end must be >= start and within bounds"
        }
    }

    companion object {
        /** Full-width area spanning the given number of rows from the top. */
        fun fromRows(rows: Int) = ContentArea(0, 0, UIConstants.MAX_COLUMN_INDEX, rows - 1)

        /** Compatibility constructor from the legacy pair-of-pairs format ((minX, minY) to (maxX, maxY)). */
        fun fromBoundingBox(area: Pair<Pair<Int, Int>, Pair<Int, Int>>) =
            ContentArea(area.first.first, area.first.second, area.second.first, area.second.second)
    }

    val xWidth: Int get() = maxX - minX + 1
    val yWidth: Int get() = maxY - minY + 1
    val xRange: IntRange get() = minX..maxX
    val yRange: IntRange get() = minY..maxY
    val bottomRow: Int get() = (maxY + 1) * UIConstants.ROW_SIZE

    /** Returns the (x, y) offset relative to the top-left corner, or null if the slot is outside this area. */
    fun offset(slot: Int): Pair<Int, Int>? {
        val x = slot % UIConstants.ROW_SIZE
        val y = slot / UIConstants.ROW_SIZE
        return if (x in xRange && y in yRange) x - minX to y - minY else null
    }

    fun allValidSlots(): List<Int> =
        xRange.flatMap { x ->
            yRange.map { y -> y * UIConstants.ROW_SIZE + x }
        }
}
