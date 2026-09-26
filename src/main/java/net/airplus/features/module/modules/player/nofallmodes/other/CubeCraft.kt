/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.player.nofallmodes.other

import net.airplus.features.module.modules.player.nofallmodes.NoFallMode
import net.airplus.utils.client.PacketUtils.sendPacket
import net.minecraft.network.play.client.C03PacketPlayer

object CubeCraft : NoFallMode("CubeCraft") {
    override fun onUpdate() {
        if (mc.thePlayer.fallDistance > 2f) {
            mc.thePlayer.onGround = false
            sendPacket(C03PacketPlayer(true))
        }
    }
}