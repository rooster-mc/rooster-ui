package dev.rooster.ui.tracking

class DataSource<T>(val id: String, initialValue: T) {
    private var value: T = initialValue

    var version: Long = 0
        private set

    val current: T
        get() {
            Tracker.record(DataSourceDependency(this, version))
            return value
        }

    fun set(newValue: T) {
        if (value != newValue) {
            value = newValue
            version++
        }
    }

    fun bump() {
        version++
    }
}
