/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.player.nofallmodes.aac

import net.airplus.features.module.modules.player.nofallmodes.NoFallMode
import net.airplus.utils.client.PacketUtils.sendPacket
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition

object AAC3315 : NoFallMode("AAC3.3.15") {
    override fun onUpdate() {
        val thePlayer = mc.thePlayer

        if (mc.isIntegratedServerRunning) return

        if (mc.thePlayer.fallDistance > 2) {
            sendPacket(C04PacketPlayerPosition(thePlayer.posX, Double.NaN, thePlayer.posZ, false))

            thePlayer.fallDistance = -9999f
        }
    }
}