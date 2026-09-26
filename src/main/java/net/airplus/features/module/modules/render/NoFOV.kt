/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.render

import net.airplus.features.module.Category
import net.airplus.features.module.Module

object NoFOV : Module("NoFOV", Category.RENDER, gameDetecting = false) {
    val fov by float("FOV", 1f, 0f..1.5f)
}
