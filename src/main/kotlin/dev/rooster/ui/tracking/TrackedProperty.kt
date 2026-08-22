package dev.rooster.ui.tracking

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class TrackedProperty<T>(initialValue: T) : ReadWriteProperty<Any?, T> {
    private var value: T = initialValue

    var version: Long = 0
        private set

    val current: T
        get() = value

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        Tracker.record(PropertyDependency(this, version))
        return value
    }

    override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        if (this.value != value) {
            this.value = value
            version++
        }
    }

    fun restore(raw: Any) {
        @Suppress("UNCHECKED_CAST")
        val converted = coerce(raw) as T
        if (value != converted) {
            value = converted
            version++
        }
    }

    private fun coerce(raw: Any): Any = when (value) {
        is Int -> (raw as Number).toInt()
        is Long -> (raw as Number).toLong()
        is Double -> (raw as Number).toDouble()
        is Float -> (raw as Number).toFloat()
        is Boolean -> raw as Boolean
        is String -> raw as String
        else -> raw
    }
}

fun <T> track(initialValue: T): TrackedProperty<T> = TrackedProperty(initialValue)
