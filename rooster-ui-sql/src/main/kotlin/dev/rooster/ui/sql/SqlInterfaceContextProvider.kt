package dev.rooster.ui.sql

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dev.rooster.core.RoosterServices
import dev.rooster.core.util.uuid
import dev.rooster.db.RoosterDb
import dev.rooster.db.findEntry
import dev.rooster.ui.context.InterfaceContextProvider
import dev.rooster.ui.interfaces.Context
import dev.rooster.ui.interfaces.RoosterInterface
import org.bukkit.entity.Player
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.vendors.currentDialect
import java.util.logging.Level

class SqlInterfaceContextProvider : InterfaceContextProvider() {
    init {
        RoosterDb.tables += InterfaceContexts
    }

    object InterfaceContexts : IntIdTable("RoosterInterfaceContexts") {
        val playerUUID = varchar("player_uuid", 50)
        val interfaceName = varchar("interface_name", 255)
        val content = text("content")
    }

    class InterfaceContext(
        id: EntityID<Int>
    ) : IntEntity(id) {
        companion object : IntEntityClass<InterfaceContext>(InterfaceContexts)

        val content by InterfaceContexts.content
    }

    fun isTableInitialized(): Boolean =
        transaction {
            currentDialect.tableExists(InterfaceContexts)
        }

    fun errorTableNotInitialized() {
        RoosterUISql.logger
            .log(Level.SEVERE, "InterfaceContexts table not initialized. Likely you initialized this service before RoosterSql initialized the database.")
    }

    override fun <T : Context> updateContext(player: Player, interfaceInstance: RoosterInterface<T>, context: T) {
        if (!isTableInitialized()) {
            errorTableNotInitialized()
            return
        }

        val jsonContent = Gson().toJson(context)
        transaction {
            val existingContext = InterfaceContexts
                .selectAll()
                .where {
                    (InterfaceContexts.playerUUID eq player.uuid()) and
                        (InterfaceContexts.interfaceName eq interfaceInstance.interfaceName)
                }.singleOrNull()

            if (existingContext != null) {
                InterfaceContexts.update({ InterfaceContexts.id eq existingContext[InterfaceContexts.id] }) {
                    it[content] = jsonContent
                }
            } else {
                InterfaceContexts.insert {
                    it[playerUUID] = player.uuid()
                    it[interfaceName] = interfaceInstance.interfaceName
                    it[content] = jsonContent
                }
            }
        }
    }

    override fun <T : Context> getContext(player: Player, interfaceInstance: RoosterInterface<T>): T? {
        if (!isTableInitialized()) {
            errorTableNotInitialized()
            return null
        }

        val data = InterfaceContext.findEntry(
            (InterfaceContexts.playerUUID eq player.uuid()) and
                (InterfaceContexts.interfaceName eq interfaceInstance.interfaceName)
        ) ?: return null

        val gson = GsonBuilder().create()
        return gson!!.fromJson(data.content, interfaceInstance.contextClass.java)
    }

    companion object {
        fun RoosterServices.addSqlInterfaceContextProvider() {
            setDelegate<InterfaceContextProvider>(SqlInterfaceContextProvider())
        }
    }
}
