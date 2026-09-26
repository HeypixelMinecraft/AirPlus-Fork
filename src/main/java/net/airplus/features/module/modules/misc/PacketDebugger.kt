/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.misc

import net.airplus.AirPlus.hud
import net.airplus.event.PacketEvent
import net.airplus.event.handler
import net.airplus.features.module.Category
import net.airplus.features.module.Module
import net.airplus.script.remapper.Remapper
import net.airplus.ui.client.hud.element.elements.Notification
import net.airplus.utils.client.chat
import net.airplus.utils.timing.MSTimer

object PacketDebugger : Module("PacketDebugger", Category.MISC, gameDetecting = false) {

    private val notify by choices("Notify", arrayOf("Chat", "Notification"), "Chat")
    val packetType by choices("PacketType", arrayOf("Both", "Server", "Client", "Custom"), "Both")
    private val delay by int("Delay", 100, 0..1000)
    private val notificationStayTime by float(
        "NotificationStayTime", 3f, 0.5f..60f, suffix = "Seconds"
    ) { notify == "Notification" }

    private val timer = MSTimer()
    val selectedPackets = mutableSetOf<String>()

    val onPacket = handler<PacketEvent> { event ->
        if (mc.thePlayer == null || mc.theWorld == null) {
            return@handler
        }

        val packet = event.packet

        val isServerPacket = packet.javaClass.name.startsWith("net.minecraft.network.play.server")
        val isClientPacket = packet.javaClass.name.startsWith("net.minecraft.network.play.client")

        if (timer.hasTimePassed(delay)) {
            when (packetType.lowercase()) {
                "both" -> logPacket(event)
                "server" -> if (isServerPacket) logPacket(event)
                "client" -> if (isClientPacket) logPacket(event)
                "custom" -> if (selectedPackets.contains(packet.javaClass.simpleName)) logPacket(event)
            }
            timer.reset()
        }
    }

    private fun logPacket(event: PacketEvent) {
        val packet = event.packet

        val packetEvent = if (event.isCancelled) "§7(§cCancelled§7)" else ""

        val packetInfo = buildString {
            append("\n")
            append("§aPacket: §b${packet.javaClass.simpleName} $packetEvent\n")
            append("§aEventType: §b${event.eventType}\n")

            var clazz: Class<*>? = packet.javaClass

            while (clazz != null) {
                clazz.declaredFields.forEach { field ->
                    field.isAccessible = true

                    append("§a${Remapper.remapField(clazz!!, field.name)}: §b${field.get(packet)}\n")
                }

                clazz = clazz.superclass
            }
        }

        if (notify == "Chat") {
            chat(packetInfo)
        } else {
            // Not a good idea...
            hud.addNotification(Notification.informative(this, packetInfo, (notificationStayTime * 1000).toLong()))
        }
    }
}