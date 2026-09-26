/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.render

import net.airplus.event.RotationSetEvent
import net.airplus.event.handler
import net.airplus.features.module.Category
import net.airplus.features.module.Module
import net.airplus.utils.extensions.prevRotation
import net.airplus.utils.extensions.rotation
import net.airplus.utils.rotation.Rotation
import org.lwjgl.opengl.Display

object FreeLook : Module("FreeLook", Category.RENDER) {

    private val autoF5 by boolean("AutoF5", true).subjective()

    // The module's rotations
    private var currRotation = Rotation.ZERO
    private var prevRotation = currRotation

    // The player's rotations
    private var savedCurrRotation = Rotation.ZERO
    private var savedPrevRotation = Rotation.ZERO

    private var modifySavedRotations = true

    override fun onEnable() {
        mc.thePlayer?.run {
            if (autoF5 && mc.gameSettings.thirdPersonView != 1) {
                mc.gameSettings.thirdPersonView = 1
            }

            currRotation = rotation
            prevRotation = prevRotation
        }
    }

    override fun onDisable() {
        if (autoF5) mc.gameSettings.thirdPersonView = 0
    }

    val onRotationSet = handler<RotationSetEvent> { event ->
        if (mc.gameSettings.thirdPersonView != 0) {
            event.cancelEvent()
        } else {
            currRotation = mc.thePlayer.rotation
            prevRotation = currRotation
        }

        prevRotation = currRotation
        currRotation += Rotation(event.yawDiff, -event.pitchDiff)

        currRotation.withLimitedPitch()
    }

    fun useModifiedRotation() {
        val player = mc.thePlayer ?: return

        if (mc.gameSettings.thirdPersonView == 0)
            return

        if (modifySavedRotations) {
            savedCurrRotation = player.rotation
            savedPrevRotation = player.prevRotation
        }

        if (!handleEvents())
            return

        if (!mc.inGameHasFocus || !Display.isActive()) {
            prevRotation = currRotation
        }

        player.rotation = currRotation
        player.prevRotation = prevRotation
    }

    fun restoreOriginalRotation() {
        val player = mc.thePlayer ?: return

        if (!handleEvents() || mc.gameSettings.thirdPersonView == 0)
            return

        player.rotation = savedCurrRotation
        player.prevRotation = savedPrevRotation
    }

    fun runWithoutSavingRotations(f: () -> Unit) {
        modifySavedRotations = false

        try {
            f()
        } catch (_: Exception) {
        }

        modifySavedRotations = true
    }
}