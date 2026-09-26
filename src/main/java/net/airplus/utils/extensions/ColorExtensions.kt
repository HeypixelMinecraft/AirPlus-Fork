/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.utils.extensions

import java.awt.Color

fun Color.darker(factor: Float = 0.5F): Color {
    return Color(
        (red * factor).toInt().coerceIn(0, 255),
        (green * factor).toInt().coerceIn(0, 255),
        (blue * factor).toInt().coerceIn(0, 255),
        alpha
    )
}
