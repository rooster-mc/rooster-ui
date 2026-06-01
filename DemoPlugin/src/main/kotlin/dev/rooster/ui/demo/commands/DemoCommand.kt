package dev.rooster.ui.demo.commands

import dev.rooster.ui.demo.ui.TestInterface
import dev.rooster.ui.demo.ui.TestPageInterface
import dev.rooster.ui.demo.ui.TestScrollInterface
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.executors.CommandExecutor
import org.bukkit.entity.Player

val interfaces = listOf(TestInterface, TestPageInterface, TestScrollInterface)
fun demo() {
    CommandAPICommand("test-interface")
        .withArguments(
            StringArgument("interface").replaceSuggestions(
                ArgumentSuggestions.strings(
            interfaces.map { it.interfaceName }
        )))
        .executes(CommandExecutor { sender, context ->
            val target = interfaces.firstOrNull { it.interfaceName == context["interface"] }
            if (target == null) sender.sendMessage("Interface not found, ${context["interface"]}")
            target?.openInventory(sender as Player)
        })
        .register()
}
