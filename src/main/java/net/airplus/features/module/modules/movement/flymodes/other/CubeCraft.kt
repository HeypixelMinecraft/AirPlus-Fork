/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.movement.flymodes.other

import net.airplus.event.MoveEvent
import net.airplus.features.module.modules.movement.flymodes.FlyMode
import net.airplus.utils.extensions.toRadiansD
import net.airplus.utils.timing.TickTimer
import kotlin.math.cos
import kotlin.math.sin

object CubeCraft : FlyMode("CubeCraft") {
    private val tickTimer = TickTimer()

    override fun onUpdate() {
        mc.timer.timerSpeed = 0.6f
        tickTimer.update()
    }

    override fun onMove(event: MoveEvent) {
        val yaw = mc.thePlayer.rotationYaw.toRadiansD()

        if (tickTimer.hasTimePassed(2)) {
            event.x = -sin(yaw) * 2.4
            event.z = cos(yaw) * 2.4
            tickTimer.reset()
        } else {
            event.x = -sin(yaw) * 0.2
            event.z = cos(yaw) * 0.2
        }
    }
}
