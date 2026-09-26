/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.movement.speedmodes.matrix

import net.airplus.features.module.modules.movement.speedmodes.SpeedMode
import net.airplus.utils.extensions.isInLiquid
import net.airplus.utils.extensions.isMoving
import net.airplus.utils.extensions.tryJump
import net.airplus.utils.movement.MovementUtils.strafe

object OldMatrixHop : SpeedMode("OldMatrixHop") {

    override fun onUpdate() {
        val player = mc.thePlayer ?: return
        if (player.isInLiquid || player.isInWeb || player.isOnLadder) return

        if (player.isMoving) {
            if (player.onGround) {
                player.tryJump()
                player.speedInAir = 0.02098f
                mc.timer.timerSpeed = 1.055f
            } else {
                strafe()
            }
        } else {
            mc.timer.timerSpeed = 1f
        }
    }
}
