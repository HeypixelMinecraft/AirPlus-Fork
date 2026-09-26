/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.command.commands

import net.airplus.AirPlus.commandManager
import net.airplus.features.command.Command
import net.airplus.file.FileManager.saveConfig
import net.airplus.file.FileManager.valuesConfig

object PrefixCommand : Command("prefix") {
    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        if (args.size <= 1) {
            chatSyntax("prefix <character>")
            return
        }

        val prefix = args[1]

        commandManager.prefix = prefix
        saveConfig(valuesConfig)

        chat("Successfully changed command prefix to '§8$prefix§3'")
    }
}