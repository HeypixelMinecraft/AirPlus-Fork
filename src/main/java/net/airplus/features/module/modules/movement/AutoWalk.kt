/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.movement

import net.airplus.event.UpdateEvent
import net.airplus.event.handler
import net.airplus.features.module.Category
import net.airplus.features.module.Module
import net.minecraft.client.settings.GameSettings

object AutoWalk : Module("AutoWalk", Category.MOVEMENT, subjective = true, gameDetecting = false) {

    val onUpdate = handler<UpdateEvent> {
        mc.gameSettings.keyBindForward.pressed = true
    }

    override fun onDisable() {
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindForward))
            mc.gameSettings.keyBindForward.pressed = false
    }
}
