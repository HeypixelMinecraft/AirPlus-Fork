/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.command.commands

import net.airplus.features.command.Command
import net.airplus.utils.kotlin.StringUtils

object SayCommand : Command("say") {
    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        if (args.size > 1) {
            mc.thePlayer.sendChatMessage(StringUtils.toCompleteString(args, 1))
            chat("Message was sent to the chat.")
            return
        }
        chatSyntax("say <message...>")
    }
}