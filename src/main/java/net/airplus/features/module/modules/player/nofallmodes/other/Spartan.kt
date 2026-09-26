/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.player.nofallmodes.other

import net.airplus.features.module.modules.player.nofallmodes.NoFallMode
import net.airplus.utils.client.PacketUtils.sendPackets
import net.airplus.utils.timing.TickTimer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition

object Spartan : NoFallMode("Spartan") {
    private val spartanTimer = TickTimer()

    override fun onUpdate() {
        val thePlayer = mc.thePlayer

        spartanTimer.update()
        if (thePlayer.fallDistance > 1.5 && spartanTimer.hasTimePassed(10)) {
            sendPackets(
                C04PacketPlayerPosition(thePlayer.posX, thePlayer.posY + 10, thePlayer.posZ, true),
                C04PacketPlayerPosition(thePlayer.posX, thePlayer.posY - 10, thePlayer.posZ, true)
            )
            spartanTimer.reset()
        }
    }
}