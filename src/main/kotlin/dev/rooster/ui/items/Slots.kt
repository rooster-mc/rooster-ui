package dev.cypdashuhn.rooster.ui.items

class Slots {
    var slots: Array<Int>
    var all: Boolean = false

    constructor(vararg slots: Int) {
        this.slots = slots.toTypedArray()
    }

    constructor(slots: Iterable<Int>) {
        this.slots = slots.toList().toTypedArray()
    }

    constructor(slots: IntRange) {
        this.slots = slots.toList().toTypedArray()
    }

    private constructor() {
        this.slots = arrayOf(-1)
        this.all = true
    }

    companion object {
        fun all() = Slots()
    }

    fun targetsSlot(slot: Int) = all || slots.contains(slot)
}

fun Slots?.targetsSlot(slot: Int) = this?.targetsSlot(slot) ?: true
fun Slots?.targetsNullableSlot(slot: Int) = this?.targetsSlot(slot) ?: true