/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.world

import net.airplus.event.UpdateEvent
import net.airplus.event.WorldEvent
import net.airplus.event.handler
import net.airplus.features.module.Category
import net.airplus.features.module.Module
import net.airplus.utils.extensions.isMoving

object Timer : Module("Timer", Category.WORLD, gameDetecting = false) {

    private val mode by choices("Mode", arrayOf("OnMove", "NoMove", "Always"), "OnMove")
    private val speed by float("Speed", 2F, 0.1F..10F)

    override fun onDisable() {
        if (mc.thePlayer == null)
            return

        mc.timer.timerSpeed = 1F
    }

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler

        if (mode == "Always" || mode == "OnMove" && player.isMoving || mode == "NoMove" && !player.isMoving) {
            mc.timer.timerSpeed = speed
            return@handler
        }

        mc.timer.timerSpeed = 1F
    }

    val onWorld = handler<WorldEvent> {
        if (it.worldClient == null)
            state = false
    }
}
