/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.player.nofallmodes.aac

import net.airplus.features.module.modules.player.nofallmodes.NoFallMode
import net.airplus.utils.client.PacketUtils.sendPackets
import net.airplus.utils.extensions.stopXZ
import net.airplus.utils.movement.MovementUtils.serverOnGround
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition

object AAC3311 : NoFallMode("AAC3.3.11") {
    override fun onUpdate() {
        val thePlayer = mc.thePlayer

        if (thePlayer.fallDistance > 2) {
            thePlayer.stopXZ()

            sendPackets(
                C04PacketPlayerPosition(thePlayer.posX, thePlayer.posY - 10E-4, thePlayer.posZ, serverOnGround),
                C03PacketPlayer(true)
            )
        }
    }
}