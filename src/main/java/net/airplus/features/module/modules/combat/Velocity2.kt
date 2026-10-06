/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 *
 * Ported from SilenceFix Velocity (Grim mode).
 */
package net.airplus.features.module.modules.combat

import net.airplus.config.*
import net.airplus.event.*
import net.airplus.features.module.Category
import net.airplus.features.module.Module
import net.airplus.features.module.modules.world.scaffolds.Scaffold
import net.airplus.utils.client.PacketUtils.sendPacket
import net.airplus.utils.client.chat
import net.airplus.utils.client.realMotionX
import net.airplus.utils.client.realMotionY
import net.airplus.utils.client.realMotionZ
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.util.EnumChatFormatting

/**
 * Grim 反击退：收到 S12 后取消原始速度包，改用攻击目标 + 手动设置速度的方式补偿击退。
 *
 * 与原版差异：
 * - 原 SilenceFix 使用 AttackOrder.sendFixedAttack（ViaMCP 修复后的攻击顺序），
 *   这里用 C02攻击包 + C0A挥动手臂包代替。
 */
object Velocity2 : Module("Velocity2", Category.COMBAT) {

    private val mode by choices("Mode", arrayOf("Grim"), "Grim")

    private const val WARNING_DISTANCE = 2.95
    private var lastAttackerCheckTime = 0L

    override val tag
        get() = mode

    val onPacket = handler<PacketEvent>(priority = 1) { event ->
        if (event.eventType != EventState.RECEIVE) return@handler
        if (Scaffold.handleEvents()) return@handler
        val player = mc.thePlayer ?: return@handler

        val packet = event.packet

        if (packet is S12PacketEntityVelocity && packet.entityID == player.entityId) {
            val attacker = findRealAttacker()
            if (attacker != null) {
                showAttackDistance(attacker)
            }

            if (mode == "Grim") {
                var target = mc.objectMouseOver?.entityHit as? EntityLivingBase

                if (KillAura.target != null) {
                    target = KillAura.target
                }

                if (target != null) {
                    handleVelocity(packet, target)
                    event.cancelEvent()
                }
            }
        }
    }

    private fun findRealAttacker(): EntityLivingBase? {
        if (System.currentTimeMillis() - lastAttackerCheckTime < 100) return null
        lastAttackerCheckTime = System.currentTimeMillis()

        val player = mc.thePlayer ?: return null
        val world = mc.theWorld ?: return null

        return world.getEntitiesWithinAABB(EntityLivingBase::class.java, player.entityBoundingBox.expand(5.0, 3.0, 5.0)) { entity ->
            entity != null && entity !== player && entity.getDistanceToEntity(player) <= 3.0 && entity.canEntityBeSeen(player)
        }.minByOrNull { it.getDistanceToEntity(player) }
    }

    private fun showAttackDistance(attacker: EntityLivingBase) {
        val player = mc.thePlayer ?: return
        val playerEyes = player.getPositionEyes(1.0F)
        val attackerEyes = attacker.getPositionEyes(1.0F)
        val distance = playerEyes.distanceTo(attackerEyes)

        if (distance > WARNING_DISTANCE) {
            val message = "${EnumChatFormatting.GRAY}[对方封号距离] ${EnumChatFormatting.RED}" +
                String.format("%.2f", distance) + "m" +
                "${EnumChatFormatting.GRAY} (安全: ${WARNING_DISTANCE}m)"
            chat(message)
        }
    }

    private fun handleVelocity(packet: S12PacketEntityVelocity, target: EntityLivingBase) {
        val player = mc.thePlayer ?: return

        val needToggleSprint = !player.serverSprintState
        if (needToggleSprint) {
            sendPacket(C0BPacketEntityAction(player, C0BPacketEntityAction.Action.START_SPRINTING), false)
        }

        // 模拟 AttackOrder.sendFixedAttack：按固定顺序发送攻击与挥动手臂包
        repeat(6) {
            sendPacket(C02PacketUseEntity(target, C02PacketUseEntity.Action.ATTACK), false)
            sendPacket(C0APacketAnimation(), false)
        }

        player.isSprinting = true

        val motionX = packet.realMotionX * 0.07765
        val motionY = packet.realMotionY
        val motionZ = packet.realMotionZ * 0.07765

        player.setVelocity(motionX, motionY, motionZ)

        if (needToggleSprint) {
            sendPacket(C0BPacketEntityAction(player, C0BPacketEntityAction.Action.STOP_SPRINTING), false)
        }
    }
}
