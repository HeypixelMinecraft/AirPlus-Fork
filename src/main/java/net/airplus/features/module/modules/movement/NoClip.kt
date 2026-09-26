/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.movement

import net.airplus.event.MoveEvent
import net.airplus.event.handler
import net.airplus.features.module.Category
import net.airplus.features.module.Module
import net.airplus.utils.movement.MovementUtils.strafe

object NoClip : Module("NoClip", Category.MOVEMENT) {
    val speed by float("Speed", 0.5f, 0f..10f)

    override fun onDisable() {
        mc.thePlayer?.noClip = false
    }

    val onMove = handler<MoveEvent> { event ->
        val thePlayer = mc.thePlayer ?: return@handler

        strafe(speed, stopWhenNoInput = true, event)

        thePlayer.noClip = true
        thePlayer.onGround = false

        thePlayer.capabilities.isFlying = false

        var ySpeed = 0.0

        if (mc.gameSettings.keyBindJump.isKeyDown)
            ySpeed += speed

        if (mc.gameSettings.keyBindSneak.isKeyDown)
            ySpeed -= speed

        thePlayer.motionY = ySpeed
        event.y = ySpeed
    }
}
