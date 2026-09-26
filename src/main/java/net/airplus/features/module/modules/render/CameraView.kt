/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.render

import net.airplus.event.CameraPositionEvent
import net.airplus.event.EventState
import net.airplus.event.MotionEvent
import net.airplus.event.handler
import net.airplus.features.module.Category
import net.airplus.features.module.Module
import net.airplus.features.module.modules.world.scaffolds.Scaffold

object CameraView : Module("CameraView", Category.RENDER, gameDetecting = false) {

    private val customY by float("CustomY", 0f, -10f..10f)
    private val saveLastGroundY by boolean("SaveLastGroundY", true)
    private val onScaffold by boolean("OnScaffold", true)
    private val onF5 by boolean("OnF5", true)

    private var launchY: Double? = null

    override fun onEnable() {
        mc.thePlayer?.run {
            launchY = posY
        }
    }

    val onMotion = handler<MotionEvent> { event ->
        if (event.eventState != EventState.POST) return@handler

        mc.thePlayer?.run {
            if (!saveLastGroundY || (onGround || ticksExisted == 1)) {
                launchY = posY
            }
        }
    }

    val onCameraUpdate = handler<CameraPositionEvent> { event ->
        mc.thePlayer?.run {
            val currentLaunchY = launchY ?: return@handler
            if (onScaffold && !Scaffold.handleEvents()) return@handler
            if (onF5 && mc.gameSettings.thirdPersonView == 0) return@handler

            event.withY(currentLaunchY + customY)
        }
    }
}