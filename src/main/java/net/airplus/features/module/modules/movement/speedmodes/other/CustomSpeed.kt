/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.movement.speedmodes.other

import net.airplus.features.module.modules.movement.Speed
import net.airplus.features.module.modules.movement.Speed.customAirStrafe
import net.airplus.features.module.modules.movement.Speed.customAirTimer
import net.airplus.features.module.modules.movement.Speed.customAirTimerTick
import net.airplus.features.module.modules.movement.Speed.customGroundStrafe
import net.airplus.features.module.modules.movement.Speed.customGroundTimer
import net.airplus.features.module.modules.movement.Speed.customY
import net.airplus.features.module.modules.movement.Speed.notOnConsuming
import net.airplus.features.module.modules.movement.Speed.notOnFalling
import net.airplus.features.module.modules.movement.Speed.notOnVoid
import net.airplus.features.module.modules.movement.speedmodes.SpeedMode
import net.airplus.utils.extensions.isMoving
import net.airplus.utils.extensions.stopXZ
import net.airplus.utils.extensions.stopY
import net.airplus.utils.extensions.tryJump
import net.airplus.utils.movement.FallingPlayer
import net.airplus.utils.movement.MovementUtils.strafe
import net.minecraft.item.ItemBucketMilk
import net.minecraft.item.ItemFood
import net.minecraft.item.ItemPotion

object CustomSpeed : SpeedMode("Custom") {

    override fun onMotion() {
        val player = mc.thePlayer ?: return
        val heldItem = player.heldItem

        val fallingPlayer = FallingPlayer()
        if (notOnVoid && fallingPlayer.findCollision(500) == null
            || notOnFalling && player.fallDistance > 2.5f
            || notOnConsuming && player.isUsingItem
            && (heldItem.item is ItemFood
                    || heldItem.item is ItemPotion
                    || heldItem.item is ItemBucketMilk)
        ) {

            if (player.onGround) player.tryJump()
            mc.timer.timerSpeed = 1f
            return
        }

        if (player.isMoving) {
            if (player.onGround) {
                if (customGroundStrafe > 0) {
                    strafe(customGroundStrafe)
                }

                mc.timer.timerSpeed = customGroundTimer
                player.motionY = customY.toDouble()
            } else {
                if (customAirStrafe > 0) {
                    strafe(customAirStrafe)
                }

                if (player.ticksExisted % customAirTimerTick == 0) {
                    mc.timer.timerSpeed = customAirTimer
                } else {
                    mc.timer.timerSpeed = 1f
                }
            }
        }
    }

    override fun onEnable() {
        val player = mc.thePlayer ?: return

        if (Speed.resetXZ) player.stopXZ()
        if (Speed.resetY) player.stopY()

        super.onEnable()
    }

}