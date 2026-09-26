/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.render

import net.airplus.features.module.Category
import net.airplus.features.module.Module

object Chams : Module("Chams", Category.RENDER) {
    val targets by boolean("Targets", true)
    val chests by boolean("Chests", true)
    val items by boolean("Items", true)
}
