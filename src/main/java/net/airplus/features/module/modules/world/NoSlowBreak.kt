/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.world

import net.airplus.features.module.Category
import net.airplus.features.module.Module

object NoSlowBreak : Module("NoSlowBreak", Category.WORLD, gameDetecting = false) {
    val air by boolean("Air", true)
    val water by boolean("Water", false)
}
