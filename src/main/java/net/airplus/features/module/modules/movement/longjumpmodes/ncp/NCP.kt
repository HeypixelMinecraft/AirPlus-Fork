/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.movement.longjumpmodes.ncp

import net.airplus.event.MoveEvent
import net.airplus.features.module.modules.movement.LongJump.canBoost
import net.airplus.features.module.modules.movement.LongJump.jumped
import net.airplus.features.module.modules.movement.LongJump.ncpBoost
import net.airplus.features.module.modules.movement.longjumpmodes.LongJumpMode
import net.airplus.utils.extensions.isMoving
import net.airplus.utils.movement.MovementUtils.speed

object NCP : LongJumpMode("NCP") {
    override fun onUpdate() {
        speed *= if (canBoost) ncpBoost else 1f
        canBoost = false
    }

    override fun onMove(event: MoveEvent) {
        if (!mc.thePlayer.isMoving && jumped) {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
            event.zeroXZ()
        }
    }
}