package dev.rooster.ui.demo.commands

import dev.rooster.ui.demo.ui.TestInterface
import dev.rooster.ui.demo.ui.TestPageInterface
import dev.rooster.ui.demo.ui.TestScrollInterface
import dev.rooster.ui.interfaces.RoosterInterface
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.executors.CommandExecutor
import org.bukkit.entity.Player

private val interfaceMap: Map<String, RoosterInterface<*>> = listOf(
    TestInterface, TestPageInterface, TestScrollInterface
).associateBy { it.interfaceName }

fun demo() {
    CommandAPICommand("test-interface")
        .withArguments(
            StringArgument("interface").replaceSuggestions(
                ArgumentSuggestions.strings(interfaceMap.keys.toList())
            )
        )
        .executes(CommandExecutor { sender, args ->
            val target = interfaceMap[args["interface"] as String]
            if (target == null) sender.sendMessage("Interface not found, ${args["interface"]}")
            target?.openInventory(sender as Player)
        })
        .register()
}
