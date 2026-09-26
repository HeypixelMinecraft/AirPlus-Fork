/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.movement.flymodes.vanilla

import net.airplus.features.module.modules.movement.Fly.handleVanillaKickBypass
import net.airplus.features.module.modules.movement.flymodes.FlyMode

object SmoothVanilla : FlyMode("SmoothVanilla") {
    override fun onUpdate() {
        mc.thePlayer.capabilities.isFlying = true
        handleVanillaKickBypass()
    }
}
