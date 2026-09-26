/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.movement.speedmodes.other

import net.airplus.event.MoveEvent
import net.airplus.features.module.modules.movement.Speed
import net.airplus.features.module.modules.movement.speedmodes.SpeedMode
import net.airplus.utils.extensions.isMoving
import net.airplus.utils.movement.MovementUtils.direction
import net.airplus.utils.timing.MSTimer
import kotlin.math.cos
import kotlin.math.sin

object TeleportCubeCraft : SpeedMode("TeleportCubeCraft") {
    private val timer = MSTimer()
    override fun onMove(event: MoveEvent) {
        if (mc.thePlayer.isMoving && mc.thePlayer.onGround && timer.hasTimePassed(300)) {
            val yaw = direction
            val length = Speed.cubecraftPortLength
            event.x = -sin(yaw) * length
            event.z = cos(yaw) * length
            timer.reset()
        }
    }
}