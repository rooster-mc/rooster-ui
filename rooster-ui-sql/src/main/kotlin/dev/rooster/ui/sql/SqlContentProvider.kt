package dev.rooster.ui.sql

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import dev.rooster.ui.interfaces.Context
import dev.rooster.ui.interfaces.constructors.indexed_content.ContentProvidable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.TimeUnit

abstract class SqlContentProvider<ContextType : Context, DataType : Any>(
    val table: IntIdTable,
    val strategy: ContextStrategy<ContextType>,
    val cacheTtlSeconds: Long = 300,
) : ContentProvidable<ContextType, Int, DataType> {

    private val cache: Cache<Pair<Int, Int>, Any> = CacheBuilder.newBuilder()
        .expireAfterWrite(cacheTtlSeconds, TimeUnit.SECONDS)
        .build()

    protected fun cacheKey(id: Int, context: ContextType): Pair<Int, Int> =
        id to strategy.keys(context).contentHashCode()

    protected fun fetchRow(id: Int, context: ContextType): ResultRow? = transaction {
        val query = table.selectAll().where { table.id eq id }
        strategy.clause(context)?.let { query.andWhere { it } }
        query.singleOrNull()
    }

    fun invalidate(id: Int, context: ContextType) = cache.invalidate(cacheKey(id, context))
    fun invalidateAll() = cache.invalidateAll()

    protected fun rawGet(key: Pair<Int, Int>): Any? = cache.getIfPresent(key)
    protected fun rawPut(key: Pair<Int, Int>, value: Any) = cache.put(key, value)
}

class DirectSqlContentProvider<ContextType : Context, DataType : Any>(
    table: IntIdTable,
    val mapper: (ResultRow) -> DataType,
    strategy: ContextStrategy<ContextType> = ContextStrategy.none(),
    cacheTtlSeconds: Long = 300,
) : SqlContentProvider<ContextType, DataType>(table, strategy, cacheTtlSeconds) {

    @Suppress("UNCHECKED_CAST")
    override fun contentProvider(id: Int, context: ContextType): DataType? {
        val key = cacheKey(id, context)
        (rawGet(key) as DataType?)?.let { return it }
        val data = fetchRow(id, context)?.let(mapper) ?: return null
        rawPut(key, data)
        return data
    }
}

class IntermediateSqlContentProvider<ContextType : Context, IntermediateType : Any, DataType : Any>(
    table: IntIdTable,
    val rowToIntermediate: (ResultRow) -> IntermediateType,
    val intermediateToData: (IntermediateType) -> DataType,
    strategy: ContextStrategy<ContextType> = ContextStrategy.none(),
    cacheTtlSeconds: Long = 300,
) : SqlContentProvider<ContextType, DataType>(table, strategy, cacheTtlSeconds) {

    @Suppress("UNCHECKED_CAST")
    override fun contentProvider(id: Int, context: ContextType): DataType? {
        val key = cacheKey(id, context)
        val intermediate = (rawGet(key) as IntermediateType?)
            ?: fetchRow(id, context)?.let(rowToIntermediate)?.also { rawPut(key, it) }
            ?: return null
        return intermediateToData(intermediate)
    }
}
