/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.command.shortcuts

open class Token

class Literal(val literal: String) : Token()

class StatementEnd : Token()
