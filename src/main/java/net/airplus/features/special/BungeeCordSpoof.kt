/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.special

import net.airplus.event.Listenable
import net.airplus.event.PacketEvent
import net.airplus.event.handler
import net.airplus.utils.client.MinecraftInstance
import net.airplus.utils.kotlin.RandomUtils.nextInt
import net.minecraft.network.EnumConnectionState
import net.minecraft.network.handshake.client.C00Handshake

object BungeeCordSpoof : MinecraftInstance, Listenable {
    var enabled by ClientFixes.bungeeSpoofValue

    val onPacket = handler<PacketEvent> { event ->
        val packet = event.packet
        if (packet is C00Handshake && packet.requestedState == EnumConnectionState.LOGIN) {
            packet.ip = packet.ip + "\u0000" + String.format(
                "{0}.{1}.{2}.{3}", getRandomIpPart(), getRandomIpPart(), getRandomIpPart(), getRandomIpPart()
            ) + "\u0000" + mc.session.playerID.replace("-", "")
        }
    }

    private fun getRandomIpPart() = nextInt(endExclusive = 256).toString()

    override fun handleEvents() = enabled
}