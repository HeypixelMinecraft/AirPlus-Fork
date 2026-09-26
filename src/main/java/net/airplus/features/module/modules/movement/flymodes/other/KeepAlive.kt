/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.movement.flymodes.other

import net.airplus.features.module.modules.movement.Fly.vanillaSpeed
import net.airplus.features.module.modules.movement.flymodes.FlyMode
import net.airplus.utils.client.PacketUtils.sendPacket
import net.airplus.utils.movement.MovementUtils.strafe
import net.minecraft.network.play.client.C00PacketKeepAlive

object KeepAlive : FlyMode("KeepAlive") {
    override fun onUpdate() {
        sendPacket(C00PacketKeepAlive())
        mc.thePlayer.capabilities.isFlying = false

        mc.thePlayer.motionY = when {
            mc.gameSettings.keyBindJump.isKeyDown -> vanillaSpeed.toDouble()
            mc.gameSettings.keyBindSneak.isKeyDown -> -vanillaSpeed.toDouble()
            else -> 0.0
        }

        strafe(vanillaSpeed, true)
    }
}
