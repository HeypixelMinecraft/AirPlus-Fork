/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.misc

import net.airplus.event.Render2DEvent
import net.airplus.event.handler
import net.airplus.features.module.Category
import net.airplus.features.module.Module
import net.airplus.file.FileManager.friendsConfig
import net.airplus.file.FileManager.saveConfig
import net.airplus.utils.client.chat
import net.airplus.utils.render.ColorUtils.stripColor
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.input.Mouse

object MidClick : Module("MidClick", Category.MISC, subjective = true, gameDetecting = false) {
    private var wasDown = false

    val onRender = handler<Render2DEvent> {
        if (mc.currentScreen != null)
            return@handler

        if (!wasDown && Mouse.isButtonDown(2)) {
            val entity = mc.objectMouseOver.entityHit

            if (entity is EntityPlayer) {
                val playerName = stripColor(entity.name)

                if (!friendsConfig.isFriend(playerName)) {
                    friendsConfig.addFriend(playerName)
                    saveConfig(friendsConfig)
                    chat("§a§l$playerName§c was added to your friends.")
                } else {
                    friendsConfig.removeFriend(playerName)
                    saveConfig(friendsConfig)
                    chat("§a§l$playerName§c was removed from your friends.")
                }

            } else
                chat("§c§lError: §aYou need to select a player.")
        }
        wasDown = Mouse.isButtonDown(2)
    }
}