package dev.cypdashuhn.rooster.ui.items

import dev.cypdashuhn.rooster.common.util.nextName
import dev.cypdashuhn.rooster.ui.interfaces.Context
import dev.cypdashuhn.rooster.ui.interfaces.InterfaceInfo
import kotlin.reflect.KClass

class ConditionMap<T : Context> {
    private constructor(map: Map<String, ((InterfaceInfo<T>) -> Boolean)>, clazz: KClass<T>) {
        this.conditionMap = map.toMutableMap()
        this.clazz = clazz
    }

    constructor(clazz: KClass<T>) {
        this.clazz = clazz
    }

    companion object {
        const val ANONYMOUS_KEY = "ANONYMOUS"
    }

    private var conditionMap: MutableMap<String, ((InterfaceInfo<T>) -> Boolean)> = mutableMapOf()
    private val clazz: KClass<T>

    fun add(
        condition: InterfaceInfo<T>.() -> Boolean,
        key: String = ANONYMOUS_KEY,
    ) {
        conditionMap[nextName(key, conditionMap.keys.toList())] = { it: InterfaceInfo<T> -> condition(it) }
    }

    fun set(
        condition: InterfaceInfo<T>.() -> Boolean,
        key: String = ANONYMOUS_KEY,
    ) {
        conditionMap[key] = condition
    }

    fun resetConditions(excludingConditionKeys: List<String>) {
        conditionMap.keys
            .filter { it !in excludingConditionKeys }
            .forEach { conditionMap.remove(it) }
    }

    val flattend by lazy {
        if (conditionMap.values.count() == 0) { _: InterfaceInfo<T> -> true }
        else { info: InterfaceInfo<T> -> conditionMap.values.all { condition -> condition(info) } }
    }

    fun getMap(): Map<String, ((InterfaceInfo<T>) -> Boolean)> = conditionMap
    fun copy(): ConditionMap<T> = ConditionMap<T>(conditionMap, clazz)
}

class InterfaceItemList<T : Context>(itemStackList: List<InterfaceItem<T>>) {
    val get: (InterfaceInfo<T>) -> InterfaceItem<T>?

    init {
        val (staticItems, dynamicItems) = itemStackList.partition { it.staticPriority != null }
        val staticItemsCount = staticItems.count()
        val dynamicItemsCount = dynamicItems.count()

        val (conditionLess, conditionBased) = staticItems.partition {
            it.condition.getMap().count() == 0
        }
        val conditionBasedCount = conditionBased.count()
        val maxPriority = conditionBased.maxByOrNull { it.staticPriority!! }
        val maxPriorityConditionless = conditionLess.maxByOrNull { it.staticPriority!! }

        if (dynamicItemsCount == 0) {
            if (staticItemsCount == 0) get = { null }
            else if (staticItemsCount == 1) {
                val only = staticItems.first()
                if (only.condition.getMap().count() == 0) get = { only }
                else get = { if (only.condition.flattend(it)) only else null }
            } else {

                if (conditionBasedCount == 0) {
                    get = { maxPriorityConditionless }
                } else {
                    if (maxPriorityConditionless != null && maxPriorityConditionless.staticPriority!! > maxPriority!!.staticPriority!!) get =
                        { maxPriorityConditionless }
                    else {
                        val list = conditionBased.toMutableList()
                        if (maxPriorityConditionless != null) list.add(maxPriorityConditionless)
                        list.sortBy { it.staticPriority }
                        get = { info -> list.firstOrNull { it.condition.flattend(info) } }
                    }
                }
            }

        } else {
            val usableList = mutableListOf<InterfaceItem<T>>()
            if (conditionBasedCount == 0 && maxPriorityConditionless != null) usableList.add(maxPriorityConditionless)
            else {
                if (maxPriorityConditionless != null) usableList.add(maxPriorityConditionless)
                usableList.addAll(conditionBased)
            }
            val mappedStatic = staticItems.map { it to it.staticPriority }
            get = { info ->
                val solvedPriority = dynamicItems.map { it to it.priority(info) }
                val maxPriorityDynamic = solvedPriority.maxByOrNull { it.second }
                if (maxPriorityDynamic!!.second < (maxPriorityConditionless?.staticPriority ?: -1)) {
                    maxPriorityConditionless
                } else {
                    val list = (solvedPriority + mappedStatic).sortedBy { it.second }
                    list.firstOrNull { it.first.condition.flattend(info) }?.first
                }
            }
        }
    }
}