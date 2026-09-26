/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.movement.nowebmodes.aac

import net.airplus.features.module.modules.movement.nowebmodes.NoWebMode

object AAC : NoWebMode("AAC") {
    override fun onUpdate() {
        if (!mc.thePlayer.isInWeb) {
            return
        }

        mc.thePlayer.jumpMovementFactor = 0.59f

        if (!mc.gameSettings.keyBindSneak.isKeyDown)
            mc.thePlayer.motionY = 0.0
    }
}
