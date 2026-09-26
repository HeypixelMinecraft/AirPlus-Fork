/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.command.commands

import net.airplus.features.command.Command
import net.airplus.utils.client.PacketUtils.sendPacket
import net.airplus.utils.client.PacketUtils.sendPackets
import net.airplus.utils.extensions.component1
import net.airplus.utils.extensions.component2
import net.airplus.utils.extensions.component3
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition

object HurtCommand : Command("hurt") {
    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        var damage = 1

        if (args.size > 1) {
            try {
                damage = args[1].toInt()
            } catch (ignored: NumberFormatException) {
                chatSyntaxError()
                return
            }
        }

        // Latest NoCheatPlus damage exploit
        val (x, y, z) = mc.thePlayer

        repeat(65 * damage) {
            sendPackets(
                C04PacketPlayerPosition(x, y + 0.049, z, false),
                C04PacketPlayerPosition(x, y, z, false)
            )
        }

        sendPacket(C04PacketPlayerPosition(x, y, z, true))

        // Output message
        chat("You were damaged.")
    }
}