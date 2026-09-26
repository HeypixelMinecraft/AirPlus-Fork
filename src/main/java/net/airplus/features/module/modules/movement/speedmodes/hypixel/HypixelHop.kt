/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.movement.speedmodes.hypixel

import net.airplus.features.module.modules.movement.speedmodes.SpeedMode
import net.airplus.utils.extensions.isInLiquid
import net.airplus.utils.extensions.isMoving
import net.airplus.utils.extensions.tryJump
import net.airplus.utils.movement.MovementUtils.strafe

object HypixelHop : SpeedMode("HypixelHop") {
    override fun onStrafe() {
        val player = mc.thePlayer ?: return
        if (player.isInLiquid)
            return

        if (player.onGround && player.isMoving) {
            if (player.isUsingItem) {
                player.tryJump()
            } else {
                player.tryJump()
                strafe(0.4f)
            }
        }

    }
}
