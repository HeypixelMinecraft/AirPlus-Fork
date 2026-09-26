/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.world

import net.airplus.event.UpdateEvent
import net.airplus.event.handler
import net.airplus.features.module.Category
import net.airplus.features.module.Module
import net.airplus.utils.block.block
import net.minecraft.client.settings.GameSettings
import net.minecraft.init.Blocks

object AutoBreak : Module("AutoBreak", Category.WORLD, subjective = true, gameDetecting = false) {

    val onUpdate = handler<UpdateEvent> {
        mc.theWorld ?: return@handler

        val target = mc.objectMouseOver?.blockPos ?: return@handler

        mc.gameSettings.keyBindAttack.pressed = target.block != Blocks.air
    }

    override fun onDisable() {
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindAttack))
            mc.gameSettings.keyBindAttack.pressed = false
    }
}
