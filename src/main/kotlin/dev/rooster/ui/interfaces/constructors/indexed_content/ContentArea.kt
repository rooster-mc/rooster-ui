package dev.rooster.ui.interfaces.constructors.indexed_content

class ContentArea(
    val minX: Int,
    val minY: Int,
    val maxX: Int,
    val maxY: Int
) {
    init {
        require(minX in 0..8 && minY in 0..5) { "Content area start out of bounds: x in 0..8, y in 0..5" }
        require(maxX in minX..8 && maxY in minY..5) { "Content area end must be >= start and within bounds" }
    }

    companion object {
        /** Full-width area spanning the given number of rows from the top. */
        fun fromRows(rows: Int) = ContentArea(0, 0, 8, rows - 1)

        /** Compatibility constructor from the legacy pair-of-pairs format ((minX, minY) to (maxX, maxY)). */
        fun fromBoundingBox(area: Pair<Pair<Int, Int>, Pair<Int, Int>>) = ContentArea(area.first.first, area.first.second, area.second.first, area.second.second)
    }

    val xWidth: Int get() = maxX - minX + 1
    val yWidth: Int get() = maxY - minY + 1
    val xRange: IntRange get() = minX..maxX
    val yRange: IntRange get() = minY..maxY
    val bottomRow: Int get() = maxY * 9

    /** Returns the (x, y) offset relative to the top-left corner, or null if the slot is outside this area. */
    fun offset(slot: Int): Pair<Int, Int>? {
        val x = slot % 9
        val y = slot / 9
        return if (x in xRange && y in yRange) x - minX to y - minY else null
    }

    fun allValidSlots(): List<Int> = xRange.flatMap { x ->
        yRange.map { y -> y * 9 + x }
    }
}
