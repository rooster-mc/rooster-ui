package dev.cypdashuhn.rooster.ui.context

import com.google.gson.Gson
import dev.cypdashuhn.rooster.common.util.uuid
import dev.cypdashuhn.rooster.ui.interfaces.Context
import dev.cypdashuhn.rooster.ui.interfaces.RoosterInterface
import org.bukkit.entity.Player

//TODO: Save context to multiple ymls, not all in one file
class YmlInterfaceContextProvider : InterfaceContextProvider(), YmlOperations by YmlShell("interfaceContexts.yml") {
    override fun <T : Context> updateContext(player: Player, interfaceInstance: RoosterInterface<T>, context: T) {
        changeConfig {
            config.set("Players.${player.uuid()}.${interfaceInstance.interfaceName}", Gson().toJson(context))
        }
    }

    override fun <T : Context> getContext(player: Player, interfaceInstance: RoosterInterface<T>): T? {
        val jsonString = config.getString("Players.${player.uuid()}.${interfaceInstance.interfaceName}")
        return Gson().fromJson(jsonString, interfaceInstance.contextClass.java)
    }
}