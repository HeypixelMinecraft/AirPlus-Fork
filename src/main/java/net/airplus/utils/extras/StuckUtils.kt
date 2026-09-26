package net.airplus.utils.extras

import net.airplus.event.Listenable
import net.airplus.event.PacketEvent
import net.airplus.event.handler
import net.airplus.utils.client.MinecraftInstance
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S12PacketEntityVelocity

object StuckUtils : Listenable, MinecraftInstance {
    var moveTicks = 0
    var stuck = false
    var c03s = 0
    var skipTicks = 0

    fun stuck() {
        stuck = true
    }

    fun stopStuck() {
        stuck = false
    }

    val onPacket = handler<PacketEvent> { event ->
        if (event.packet is C03PacketPlayer && !event.packet.isMoving) {
            c03s++
            if (c03s >= 19) {
                c03s = 0
                if (stuck) moveTicks++
            }
        }

        if (event.packet is S12PacketEntityVelocity && stuck && event.packet.entityID == mc.thePlayer?.entityId) {
            moveTicks++
        }
    }
}
