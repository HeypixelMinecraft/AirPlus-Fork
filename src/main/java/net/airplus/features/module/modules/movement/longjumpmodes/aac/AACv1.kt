/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.movement.longjumpmodes.aac

import net.airplus.features.module.modules.movement.longjumpmodes.LongJumpMode
import net.airplus.utils.movement.MovementUtils

object AACv1 : LongJumpMode("AACv1") {
    override fun onUpdate() {
        mc.thePlayer.motionY += 0.05999
        MovementUtils.speed *= 1.08f
    }
}