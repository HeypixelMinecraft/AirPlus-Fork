/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.render

import net.airplus.event.UpdateEvent
import net.airplus.event.handler
import net.airplus.features.module.Category
import net.airplus.features.module.Module
import net.minecraft.client.settings.GameSettings

object KeepTabList : Module("KeepTabList", Category.RENDER, gameDetecting = false) {

    val onUpdate = handler<UpdateEvent> {
        if (mc.thePlayer == null || mc.theWorld == null) return@handler

        mc.gameSettings.keyBindPlayerList.pressed = true
    }

    override fun onDisable() {
        mc.gameSettings.keyBindPlayerList.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindPlayerList)
    }
}