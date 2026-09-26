/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.player

import net.airplus.event.UpdateEvent
import net.airplus.event.handler
import net.airplus.features.module.Category
import net.airplus.features.module.Module
import net.airplus.utils.timing.MSTimer
import net.minecraft.item.ItemFishingRod

object AutoFish : Module("AutoFish", Category.PLAYER, subjective = true, gameDetecting = false) {

    private val rodOutTimer = MSTimer()

    val onUpdate = handler<UpdateEvent> {
        val thePlayer = mc.thePlayer

        if (thePlayer?.heldItem == null || mc.thePlayer.heldItem.item !is ItemFishingRod)
            return@handler

        val fishEntity = thePlayer.fishEntity

        if (rodOutTimer.hasTimePassed(500) && fishEntity == null || (fishEntity != null && fishEntity.motionX == 0.0 && fishEntity.motionZ == 0.0 && fishEntity.motionY != 0.0)) {
            mc.rightClickMouse()
            rodOutTimer.reset()
        }
    }
}
