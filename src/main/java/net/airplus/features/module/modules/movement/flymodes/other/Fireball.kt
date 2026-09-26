/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.movement.flymodes.other

import net.airplus.event.EventState
import net.airplus.event.MotionEvent
import net.airplus.features.module.modules.movement.Fly
import net.airplus.features.module.modules.movement.Fly.autoFireball
import net.airplus.features.module.modules.movement.Fly.options
import net.airplus.features.module.modules.movement.flymodes.FlyMode
import net.airplus.utils.block.center
import net.airplus.utils.client.PacketUtils.sendPacket
import net.airplus.utils.extensions.isMoving
import net.airplus.utils.extensions.isNearEdge
import net.airplus.utils.extensions.sendUseItem
import net.airplus.utils.extensions.tryJump
import net.airplus.utils.inventory.InventoryUtils
import net.airplus.utils.inventory.SilentHotbar
import net.airplus.utils.inventory.hotBarSlot
import net.airplus.utils.rotation.Rotation
import net.airplus.utils.rotation.RotationUtils
import net.airplus.utils.timing.TickedActions.nextTick
import net.minecraft.init.Items
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.util.BlockPos

object Fireball : FlyMode("Fireball") {

    override fun onMotion(event: MotionEvent) {
        val player = mc.thePlayer ?: return

        val fireballSlot = InventoryUtils.findItem(36, 44, Items.fire_charge) ?: return

        if (autoFireball != "Off") {
            SilentHotbar.selectSlotSilently(
                this,
                fireballSlot,
                immediate = true,
                render = autoFireball == "Pick",
                resetManually = true
            )
        }

        if (event.eventState != EventState.POST)
            return

        val customRotation = Rotation(
            if (Fly.invertYaw) RotationUtils.invertYaw(player.rotationYaw) else player.rotationYaw,
            Fly.rotationPitch
        )

        if (player.onGround && !mc.theWorld.isAirBlock(BlockPos(player.posX, player.posY - 1, player.posZ))) {
            Fly.firePosition = BlockPos(player.posX, player.posY - 1, player.posZ)
        }

        val smartRotation = Fly.firePosition?.center?.let { RotationUtils.toRotation(it, false, player) }
        val rotation = if (Fly.pitchMode == "Custom") customRotation else smartRotation

        if (options.rotationsActive && rotation != null) {
            RotationUtils.setTargetRotation(rotation, options, if (options.keepRotation) options.resetTicks else 1)
        }

        if (Fly.fireBallThrowMode == "Edge" && !player.isNearEdge(Fly.edgeThreshold))
            return

        if (Fly.autoJump && player.onGround && !Fly.wasFired) {
            player.tryJump()
        }
    }

    override fun onTick() {
        val player = mc.thePlayer ?: return

        val fireballSlot = InventoryUtils.findItem(36, 44, Items.fire_charge) ?: return

        val fireBall = player.hotBarSlot(fireballSlot).stack

        if (Fly.fireBallThrowMode == "Edge" && !player.isNearEdge(Fly.edgeThreshold))
            return

        if (Fly.wasFired) {
            return
        }

        if (player.isMoving) {
            Fly.nextTick {
                if (Fly.swing) player.swingItem() else sendPacket(C0APacketAnimation())

                // NOTE: You may increase max try to `2` if fireball doesn't work. (Ex: BlocksMC)
                repeat(Fly.fireballTry) {
                    player.sendUseItem(fireBall)
                }

                Fly.nextTick {
                    if (autoFireball != "Off") {
                        SilentHotbar.selectSlotSilently(
                            this,
                            fireballSlot,
                            immediate = true,
                            render = autoFireball == "Pick",
                            resetManually = true
                        )
                    }

                    Fly.wasFired = true
                }
            }
        }
    }

    override fun onDisable() {
        SilentHotbar.resetSlot(this)
    }
}
