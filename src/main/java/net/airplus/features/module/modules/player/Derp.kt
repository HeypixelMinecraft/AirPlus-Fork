/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.player

import net.airplus.features.module.Category
import net.airplus.features.module.Module
import net.airplus.utils.kotlin.RandomUtils.nextFloat
import net.airplus.utils.rotation.Rotation
import net.airplus.utils.rotation.RotationUtils.currentRotation
import net.airplus.utils.rotation.RotationUtils.serverRotation
import net.airplus.utils.rotation.RotationUtils.syncSpecialModuleRotations

object Derp : Module("Derp", Category.PLAYER, subjective = true) {

    private val headless by boolean("Headless", false)
    private val spinny by boolean("Spinny", false)
    private val increment by float("Increment", 1F, 0F..50F) { spinny }

    override fun onDisable() {
        syncSpecialModuleRotations()
    }

    val rotation: Rotation
        get() {
            val rotationToUse = currentRotation ?: serverRotation

            val rot = Rotation(rotationToUse.yaw, nextFloat(-90f, 90f))

            if (headless)
                rot.pitch = 180F

            rot.yaw += if (spinny) increment else nextFloat(-180f, 180f)

            return rot.fixedSensitivity()
        }

}
