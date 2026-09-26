/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.world

import net.airplus.features.module.Category
import net.airplus.features.module.Module

object FastPlace : Module("FastPlace", Category.WORLD) {
    val speed by int("Speed", 0, 0..4)
    val onlyBlocks by boolean("OnlyBlocks", true)
    val facingBlocks by boolean("OnlyWhenFacingBlocks", true)
}
