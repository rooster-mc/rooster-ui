package dev.rooster.ui.sql

import dev.rooster.ui.interfaces.Context
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.and

interface ContextStrategy<ContextType : Context> {
    fun keys(context: ContextType): Array<Any?>
    fun clause(context: ContextType): Op<Boolean>?

    companion object {
        fun <ContextType : Context> none(): ContextStrategy<ContextType> =
            LambdaStrategy({ emptyArray() })
    }
}

class LambdaStrategy<ContextType : Context>(
    private val contextKeys: (ContextType) -> Array<Any?>,
    private val whereModifier: ((ContextType) -> Op<Boolean>?)? = null,
) : ContextStrategy<ContextType> {
    override fun keys(context: ContextType) = contextKeys(context)
    override fun clause(context: ContextType) = whereModifier?.invoke(context)
}

class ContextDimension<ContextType, V : Any>(
    private val extract: (ContextType) -> V,
    private val toClause: ((V) -> Op<Boolean>)? = null,
) {
    fun extractValue(context: ContextType): V = extract(context)
    fun buildClause(context: ContextType): Op<Boolean>? = toClause?.invoke(extract(context))
}

class DimensionStrategy<ContextType : Context>(
    private val dimensions: List<ContextDimension<ContextType, *>>,
) : ContextStrategy<ContextType> {
    override fun keys(context: ContextType): Array<Any?> =
        dimensions.map { it.extractValue(context) }.toTypedArray()

    override fun clause(context: ContextType): Op<Boolean>? =
        dimensions.mapNotNull { it.buildClause(context) }
            .reduceOrNull { acc, op -> acc and op }
}
