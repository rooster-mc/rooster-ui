package dev.rooster.ui.context

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dev.rooster.core.util.uuid
import dev.rooster.ui.interfaces.Context
import dev.rooster.ui.interfaces.RoosterInterface
import org.bukkit.entity.Player

// TODO: Save context to multiple ymls, not all in one file
class YmlInterfaceContextProvider :
    InterfaceContextProvider(),
    YmlOperations by YmlShell("interfaceContexts.yml") {
    private val gson = Gson()

    override fun <T : Context> updateContext(player: Player, interfaceInstance: RoosterInterface<T>, context: T) {
        changeConfig {
            config.set("Players.${player.uuid()}.${interfaceInstance.interfaceName}", gson.toJson(context.trackedValues()))
        }
    }

    override fun <T : Context> getContext(player: Player, interfaceInstance: RoosterInterface<T>): T? {
        val jsonString = config.getString("Players.${player.uuid()}.${interfaceInstance.interfaceName}")
            ?: return null
        val values: Map<String, Any> = gson.fromJson(jsonString, object : TypeToken<Map<String, Any>>() {}.type)
        val context = interfaceInstance.contextClass.java.getDeclaredConstructor().newInstance()
        context.restoreTrackedValues(values)
        return context
    }
}
