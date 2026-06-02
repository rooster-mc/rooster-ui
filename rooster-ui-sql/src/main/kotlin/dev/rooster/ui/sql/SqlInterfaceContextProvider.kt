package dev.rooster.ui.sql
class SqlInterfaceContextProvider : InterfaceContextProvider() {
    init {
        Rooster.dynamicTables += InterfaceContexts
    }

    object InterfaceContexts : IntIdTable("RoosterInterfaceContexts") {
        val playerUUID = varchar("player_uuid", 50)
        val interfaceName = varchar("interface_name", 50)
        val content = text("content")
    }

    class InterfaceContext(id: EntityID<Int>) : IntEntity(id) {
        companion object : IntEntityClass<InterfaceContext>(InterfaceContexts)

        val content by InterfaceContexts.content
    }

    override fun <T : Context> updateContext(player: Player, interfaceInstance: RoosterInterface<T>, context: T) {
        val jsonContent = Gson().toJson(context)
        transaction {
            val existingContext = InterfaceContexts.selectAll()
                .where { (InterfaceContexts.playerUUID eq player.uuid()) and (InterfaceContexts.interfaceName eq interfaceInstance.interfaceName) }
                .singleOrNull()

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
        val data = InterfaceContext.findEntry(
            (InterfaceContexts.playerUUID eq player.uuid()) and
                    (InterfaceContexts.interfaceName eq interfaceInstance.interfaceName)
        ) ?: return null

        val gson = GsonBuilder().create()
        return gson!!.fromJson(data.content, interfaceInstance.contextClass.java)
    }
}
