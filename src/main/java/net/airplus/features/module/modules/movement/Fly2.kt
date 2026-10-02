/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 *
 * Ported from SilenceFix Fly.
 *
 * 与原版差异：
 * - Grim 模式原版会 mc.theWorld.skiptick++ 跳帧（SilenceFix 引擎字段），airplus 无此字段，已去掉。
 * - GrimGhostBlock 模式原版依赖 SilenceFix BlinkUtils 的 12 参数 blink 状态机，
 *   这里用本地发包缓冲实现同等语义（缓冲所有发包、脉冲释放）。
 *   脉冲释放时会保留最后一个 C08 放方块包（及其之后的包）到下一次脉冲：
 *   若全部释放，最后一个放方块包被 Grim 拒绝时其后的移动包全部悬空触发回弹，
 *   脚下刚放置的幽灵方块会被服务端校正抹掉，导致坠机。
 * - DoMCer 模式的 EventTeleport 用 S08PacketPlayerPosLook 接收包取消近似实现。
 * - Vanilla 模式原版为空实现（依赖引擎），这里实现为标准创造飞行。
 */
package net.airplus.features.module.modules.movement

import net.airplus.config.*
import net.airplus.event.*
import net.airplus.features.module.Category
import net.airplus.features.module.Module
import net.airplus.utils.client.PacketUtils.sendPacket
import net.airplus.utils.extensions.airTicks
import net.airplus.utils.movement.MovementUtils.strafe
import net.minecraft.network.Packet
import net.minecraft.network.play.client.C00PacketKeepAlive
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C16PacketClientStatus
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.LinkedList

object Fly2 : Module("Fly2", Category.MOVEMENT) {

    private val flyMode by choices("FlyMode", arrayOf("Vanilla", "DoMCer", "Grim", "GrimGhostBlock"), "Vanilla")
    private val timerValue by float("Timer", 0.45f, 0.1f..1f) { flyMode == "Grim" }

    private var started = false
    private var notUnder = false
    private var clipped = false
    private var teleport = false
    private val positions = LinkedList<DoubleArray>()
    private var pulseTimer = 0L

    private var ticks = 0
    private var exploited = false

    override val tag
        get() = if (flyMode == "GrimGhostBlock") "GhostBlock-Blink: $bufferSize" else flyMode

    // GrimGhostBlock 的发包缓冲
    private val packetBuffer = LinkedList<Packet<*>>()

    private val bufferSize
        get() = synchronized(packetBuffer) { packetBuffer.size }

    override fun onDisable() {
        mc.timer.ticksPerSecond = 20f
        mc.timer.timerSpeed = 1f
        ticks = 0
        exploited = false

        when (flyMode) {
            "GrimGhostBlock" -> {
                synchronized(positions) { positions.clear() }
                releaseBuffer(keepLastPlacement = false)
            }

            "Vanilla" -> {
                mc.thePlayer?.capabilities?.isFlying = false
            }
        }
    }

    override fun onEnable() {
        when (flyMode) {
            "DoMCer" -> {
                notUnder = false
                started = false
                clipped = false
                teleport = false
            }

            "GrimGhostBlock" -> {
                val player = mc.thePlayer ?: return
                packetBuffer.clear()
                synchronized(positions) {
                    positions.add(doubleArrayOf(player.posX, player.entityBoundingBox.minY + player.getEyeHeight() / 2.0, player.posZ))
                    positions.add(doubleArrayOf(player.posX, player.entityBoundingBox.minY, player.posZ))
                }
                pulseTimer = System.currentTimeMillis()
            }
        }
    }

    val onPacket = handler<PacketEvent> { event ->
        val player = mc.thePlayer ?: return@handler
        val packet = event.packet

        when (event.eventType) {
            EventState.SEND -> {
                // GrimGhostBlock: 缓冲所有发包（脉冲释放）
                if (flyMode == "GrimGhostBlock") {
                    event.cancelEvent()
                    synchronized(packetBuffer) { packetBuffer.add(packet) }
                }
            }

            EventState.RECEIVE -> {
                when {
                    flyMode == "Grim" && packet is S12PacketEntityVelocity && packet.entityID == player.entityId -> {
                        val str = Vec3(packet.motionX.toDouble(), 0.0, packet.motionZ.toDouble()).lengthVector()
                        if (str > 1) {
                            state = false
                        }
                    }

                    flyMode == "Grim" && packet is S08PacketPlayerPosLook -> {
                        exploited = true
                    }

                    // DoMCer: 用 S08 取消近似原版 EventTeleport
                    flyMode == "DoMCer" && teleport && packet is S08PacketPlayerPosLook -> {
                        event.cancelEvent()
                        teleport = false
                        state = false
                    }
                }
            }

            else -> { }
        }
    }

    val onTick = handler<GameTickEvent> {
        val player = mc.thePlayer ?: return@handler
        if (flyMode != "Grim") return@handler

        if (player.fallDistance > 2F) {
            state = false
            return@handler
        }

        if (ticks == 0) {
            if (player.onGround) {
                player.jump()
            }
        } else if (ticks <= 5) {
            mc.timer.ticksPerSecond = 20f / timerValue
        } else {
            mc.timer.ticksPerSecond = 20f
        }

        ticks++
        if (exploited || ticks == 2) {
            exploited = false
            sendPacket(
                C03PacketPlayer.C04PacketPlayerPosition(
                    player.posX + 114514, -1.0, player.posZ + 1919180, false
                ), false
            )
        }
        // 原版此处还有 mc.theWorld.skiptick++（引擎跳帧字段），airplus 无对应实现
    }

    val onStrafe = handler<StrafeEvent> { event ->
        val player = mc.thePlayer ?: return@handler
        if (flyMode != "DoMCer") return@handler

        val bb = player.entityBoundingBox.offset(0.0, 1.0, 0.0)
        if (mc.theWorld.getCollidingBoundingBoxes(player, bb).isNotEmpty() && !started) {
            // 头顶有方块：notUnder = true，尝试向下 clip
            notUnder = true
            if (clipped) {
                return@handler
            }
            clipped = true
            sendPacket(C03PacketPlayer.C06PacketPlayerPosLook(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch, false))
            sendPacket(C03PacketPlayer.C06PacketPlayerPosLook(player.posX, player.posY - 0.1, player.posZ, player.rotationYaw, player.rotationPitch, false))
            sendPacket(C03PacketPlayer.C06PacketPlayerPosLook(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch, false))
            teleport = true

            strafe()
            mc.timer.timerSpeed = 0.4f
            return@handler
        }

        when (player.airTicks) {
            0 -> {
                if (notUnder && clipped) {
                    started = true
                    // 原版 event.setSpeed(10.0)：取消原版 strafe 后手动设置速度
                    event.cancelEvent()
                    strafe(10.0f)
                    player.motionY = 0.42
                    notUnder = false
                }
            }

            1 -> {
                if (started) {
                    event.cancelEvent()
                    strafe(9.6f)
                }
            }
        }
    }

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler

        when (flyMode) {
            "Vanilla" -> {
                player.capabilities.isFlying = true
            }

            "GrimGhostBlock" -> {
                synchronized(positions) {
                    positions.add(doubleArrayOf(player.posX, player.entityBoundingBox.minY, player.posZ))
                }
                if (System.currentTimeMillis() - pulseTimer >= 2900) {
                    synchronized(positions) { positions.clear() }
                    releaseBuffer(keepLastPlacement = true)
                    pulseTimer = System.currentTimeMillis()
                }
            }
        }
    }

    val onRender3D = handler<Render3DEvent> {
        if (flyMode != "GrimGhostBlock") return@handler

        synchronized(positions) {
            GL11.glPushMatrix()
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            mc.entityRenderer.disableLightmap()
            GL11.glLineWidth(2.0f)
            GL11.glBegin(GL11.GL_LINE_STRIP)
            val color = Color(68, 131, 123, 255)
            GL11.glColor4f(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
            val renderPosX = mc.renderManager.viewerPosX
            val renderPosY = mc.renderManager.viewerPosY
            val renderPosZ = mc.renderManager.viewerPosZ
            for (pos in positions) {
                GL11.glVertex3d(pos[0] - renderPosX, pos[1] - renderPosY, pos[2] - renderPosZ)
            }
            GL11.glColor4d(1.0, 1.0, 1.0, 1.0)
            GL11.glEnd()
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GL11.glDisable(GL11.GL_LINE_SMOOTH)
            GL11.glDisable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glPopMatrix()
        }
    }

    // 保留最后一个放方块包时，其后最多允许滞留的包数（约半秒），防止玩家停止放方块时缓冲无限积压
    private const val KEEP_PLACE_MAX_TRAILING = 10

    private fun releaseBuffer(keepLastPlacement: Boolean) {
        val toRelease: List<Packet<*>>
        synchronized(packetBuffer) {
            var keepFromIndex = -1
            if (keepLastPlacement && packetBuffer.isNotEmpty()) {
                val lastPlaceIndex = packetBuffer.indexOfLast { it is C08PacketPlayerBlockPlacement }
                // 最后一个放方块包位于缓冲末段（其后仅有少量包）时才保留，
                // 保证服务端始终滞后一个待验证的方块，而不是整段缓冲无法释放
                if (lastPlaceIndex >= 0 && packetBuffer.size - 1 - lastPlaceIndex <= KEEP_PLACE_MAX_TRAILING) {
                    keepFromIndex = lastPlaceIndex
                }
            }
            if (keepFromIndex >= 0) {
                toRelease = ArrayList(packetBuffer.subList(0, keepFromIndex))
                val kept = ArrayList(packetBuffer.subList(keepFromIndex, packetBuffer.size))
                packetBuffer.clear()
                packetBuffer.addAll(kept)
            } else {
                toRelease = ArrayList(packetBuffer)
                packetBuffer.clear()
            }
        }
        // 与原版 blink 语义一致：C16 / C00 不释放
        toRelease.filterNot { it is C16PacketClientStatus || it is C00PacketKeepAlive }
            .forEach { sendPacket(it, false) }
    }
}
