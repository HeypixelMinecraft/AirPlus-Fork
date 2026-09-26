/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.render

import net.airplus.event.PacketEvent
import net.airplus.event.handler
import net.airplus.features.module.Category
import net.airplus.features.module.Module
import net.minecraft.network.play.server.S3FPacketCustomPayload

object AntiBlind : Module("AntiBlind", Category.RENDER, gameDetecting = false) {
    val confusionEffect by boolean("Confusion", true)
    val pumpkinEffect by boolean("Pumpkin", true)
    val fireEffect by float("FireAlpha", 0.3f, 0f..1f)
    val bossHealth by boolean("BossHealth", true)
    private val bookPage by boolean("BookPage", true)
    val achievements by boolean("Achievements", true)

    val onPacket = handler<PacketEvent> { event ->
        if (!bookPage) return@handler

        val packet = event.packet
        if (packet is S3FPacketCustomPayload && packet.channelName == "MC|BOpen") event.cancelEvent()
    }
}