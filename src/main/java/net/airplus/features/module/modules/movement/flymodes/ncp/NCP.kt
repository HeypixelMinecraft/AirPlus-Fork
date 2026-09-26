/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.movement.flymodes.ncp

import net.airplus.event.PacketEvent
import net.airplus.features.module.modules.movement.Fly.ncpMotion
import net.airplus.features.module.modules.movement.flymodes.FlyMode
import net.airplus.utils.client.PacketUtils.sendPacket
import net.airplus.utils.client.PacketUtils.sendPackets
import net.airplus.utils.extensions.component1
import net.airplus.utils.extensions.component2
import net.airplus.utils.extensions.component3
import net.airplus.utils.movement.MovementUtils.strafe
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition

object NCP : FlyMode("NCP") {
    override fun onEnable() {
        if (!mc.thePlayer.onGround) return

        val (x, y, z) = mc.thePlayer

        repeat(65) {
            sendPackets(
                C04PacketPlayerPosition(x, y + 0.049, z, false),
                C04PacketPlayerPosition(x, y, z, false)
            )
        }

        sendPacket(C04PacketPlayerPosition(x, y + 0.1, z, true))

        mc.thePlayer.motionX *= 0.1
        mc.thePlayer.motionZ *= 0.1
        mc.thePlayer.swingItem()
    }

    override fun onUpdate() {
        mc.thePlayer.motionY =
            if (mc.gameSettings.keyBindSneak.isKeyDown) -0.5
            else -ncpMotion.toDouble()

        strafe()
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is C03PacketPlayer)
            packet.onGround = true
    }

}
