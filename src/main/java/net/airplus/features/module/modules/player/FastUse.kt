/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.player

import net.airplus.event.MoveEvent
import net.airplus.event.UpdateEvent
import net.airplus.event.handler
import net.airplus.features.module.Category
import net.airplus.features.module.Module
import net.airplus.utils.client.PacketUtils.sendPacket
import net.airplus.utils.inventory.ItemUtils.isConsumingItem
import net.airplus.utils.movement.MovementUtils.serverOnGround
import net.airplus.utils.timing.MSTimer
import net.minecraft.network.play.client.C03PacketPlayer

object FastUse : Module("FastUse", Category.PLAYER) {

    private val mode by choices("Mode", arrayOf("Instant", "NCP", "AAC", "Custom"), "NCP")

    private val delay by int("CustomDelay", 0, 0..300) { mode == "Custom" }
    private val customSpeed by int("CustomSpeed", 2, 1..35) { mode == "Custom" }
    private val customTimer by float("CustomTimer", 1.1f, 0.5f..2f) { mode == "Custom" }

    private val noMove by boolean("NoMove", false)

    private val msTimer = MSTimer()
    private var usedTimer = false

    val onUpdate = handler<UpdateEvent> {
        val thePlayer = mc.thePlayer ?: return@handler

        if (usedTimer) {
            mc.timer.timerSpeed = 1F
            usedTimer = false
        }

        if (!isConsumingItem()) {
            msTimer.reset()
            return@handler
        }

        when (mode.lowercase()) {
            "instant" -> {
                repeat(35) {
                    sendPacket(C03PacketPlayer(serverOnGround))
                }

                mc.playerController.onStoppedUsingItem(thePlayer)
            }

            "ncp" -> if (thePlayer.itemInUseDuration > 14) {
                repeat(20) {
                    sendPacket(C03PacketPlayer(serverOnGround))
                }

                mc.playerController.onStoppedUsingItem(thePlayer)
            }

            "aac" -> {
                mc.timer.timerSpeed = 1.22F
                usedTimer = true
            }

            "custom" -> {
                mc.timer.timerSpeed = customTimer
                usedTimer = true

                if (!msTimer.hasTimePassed(delay))
                    return@handler

                repeat(customSpeed) {
                    sendPacket(C03PacketPlayer(serverOnGround))
                }

                msTimer.reset()
            }
        }
    }

    val onMove = handler<MoveEvent> { event ->
        mc.thePlayer ?: return@handler

        if (!isConsumingItem() || !noMove)
            return@handler

        event.zero()
    }

    override fun onDisable() {
        if (usedTimer) {
            mc.timer.timerSpeed = 1F
            usedTimer = false
        }
    }

    override val tag
        get() = mode
}
