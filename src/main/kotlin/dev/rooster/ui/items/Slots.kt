package dev.rooster.ui.items

class Slots {
    var slots: Array<Int>
    var all: Boolean = false
    var playerInventory: Boolean = false

    constructor(vararg slots: Int) {
        this.slots = slots.toTypedArray()
    }

    constructor(slots: Iterable<Int>) {
        this.slots = slots.toList().toTypedArray()
    }

    constructor(slots: IntRange) {
        this.slots = slots.toList().toTypedArray()
    }

    private constructor(mode: Mode) {
        this.slots = arrayOf(-1)
        when (mode) {
            Mode.ALL -> {
                this.all = true
            }

            Mode.PLAYER_INVENTORY -> {
                this.playerInventory = true
            }

            Mode.EVERYTHING -> {
                this.all = true
                this.playerInventory = true
            }
        }
    }

    private enum class Mode {
        ALL,
        PLAYER_INVENTORY,
        EVERYTHING
    }

    companion object {
        fun all() = Slots(Mode.ALL)

        fun playerInventory() = Slots(Mode.PLAYER_INVENTORY)

        fun everything() = Slots(Mode.EVERYTHING)
    }

    fun targetsSlot(slot: Int) = all || slots.contains(slot)

    fun targetsPlayerInventory() = playerInventory
}

fun Slots?.targetsSlot(slot: Int) = this?.targetsSlot(slot) ?: true

fun Slots?.targetsNullableSlot(slot: Int) = this?.targetsSlot(slot) ?: true

fun Slots?.targetsPlayerInventory() = this?.targetsPlayerInventory() ?: false
