/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.movement.longjumpmodes.other

import net.airplus.features.module.modules.movement.longjumpmodes.LongJumpMode

object Hycraft : LongJumpMode("Hycraft") {
    override fun onUpdate() {
        if (mc.thePlayer.motionY < 0) {
            mc.thePlayer.motionY *= 0.75f
            mc.thePlayer.jumpMovementFactor = 0.055f
        } else {
            mc.thePlayer.motionY += 0.02f
            mc.thePlayer.jumpMovementFactor = 0.08f
        }
    }
}