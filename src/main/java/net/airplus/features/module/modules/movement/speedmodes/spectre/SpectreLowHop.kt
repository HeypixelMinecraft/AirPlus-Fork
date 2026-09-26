/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.movement.speedmodes.spectre

import net.airplus.features.module.modules.movement.speedmodes.SpeedMode
import net.airplus.utils.extensions.isMoving
import net.airplus.utils.movement.MovementUtils.strafe

object SpectreLowHop : SpeedMode("SpectreLowHop") {
    override fun onMotion() {
        if (!mc.thePlayer.isMoving || mc.thePlayer.movementInput.jump) return
        if (mc.thePlayer.onGround) {
            strafe(1.1f)
            mc.thePlayer.motionY = 0.15
            return
        }
        strafe()
    }

}