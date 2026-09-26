/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.utils.extensions

fun String.toLowerCamelCase() = String(toCharArray().apply {
    this[0] = this[0].lowercaseChar()
})
