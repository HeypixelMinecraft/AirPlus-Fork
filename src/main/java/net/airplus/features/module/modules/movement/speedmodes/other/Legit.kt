/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.movement.speedmodes.other

import net.airplus.features.module.modules.movement.speedmodes.SpeedMode
import net.airplus.utils.extensions.isMoving
import net.airplus.utils.extensions.tryJump

object Legit : SpeedMode("Legit") {
    override fun onStrafe() {
        val player = mc.thePlayer ?: return

        if (mc.thePlayer.onGround && player.isMoving) {
            player.tryJump()
        }
    }

    override fun onUpdate() {
        val player = mc.thePlayer ?: return

        player.isSprinting = player.movementInput.moveForward > 0.8
    }
}
