/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.command.commands

import net.airplus.features.command.Command

object PingCommand : Command("ping") {

    override fun execute(args: Array<String>) {
        chat("§3Your ping is §a${mc.netHandler.getPlayerInfo(mc.thePlayer.uniqueID).responseTime}ms§3.")
    }

}