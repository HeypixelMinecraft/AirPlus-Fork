/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.movement.nowebmodes.other

import net.airplus.features.module.modules.movement.nowebmodes.NoWebMode

object None : NoWebMode("None") {
    override fun onUpdate() {
        mc.thePlayer.isInWeb = false
    }
}
