/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.render

import net.airplus.event.Render3DEvent
import net.airplus.event.UpdateEvent
import net.airplus.event.handler
import net.airplus.features.module.Category
import net.airplus.features.module.Module
import net.airplus.features.module.modules.combat.KillAura
import net.airplus.ui.font.Fonts
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * 从 SilenceFix 迁移：目标实体（KillAura.target）血量变化时在其身上弹出
 * 变化数值 —— 掉血黄色 / 回血绿色，生命末期渐隐。
 */
object DamageParticle : Module("Damage Particles", Category.RENDER) {

    private val deleteAfter by int("Remove Ticks", 15, 1..60)

    private val hpData = HashMap<Int, Float>()
    private val particles = ArrayList<Particle>()

    val onUpdate = handler<UpdateEvent> {
        val world = mc.theWorld ?: return@handler

        // 推进所有粒子寿命
        for (p in particles) p.ticks++

        for (entity in world.loadedEntityList) {
            if (entity !is EntityLivingBase) continue

            val lastHp = hpData.getOrDefault(entity.entityId, entity.maxHealth)
            hpData[entity.entityId] = entity.health
            if (lastHp == entity.health) continue

            val diff = entity.health - lastHp
            val color = if (diff > 0) Color.GREEN else Color.YELLOW

            if (entity != KillAura.target) continue

            val str = BigDecimal(Math.abs(lastHp - entity.health).toDouble())
                .setScale(1, RoundingMode.HALF_EVEN)
                .toDouble()
                .toString()

            val textPos = Vec3(
                entity.posX + Math.random() * 0.5 * (if (Math.random() > 0.5) -1 else 1),
                entity.entityBoundingBox.minY + (entity.entityBoundingBox.maxY - entity.entityBoundingBox.minY) * 0.5,
                entity.posZ + Math.random() * 0.5 * (if (Math.random() > 0.5) -1 else 1)
            )
            particles.add(Particle(str, textPos, color))
        }
    }

    override fun onDisable() {
        particles.clear()
    }

    val onRender3D = handler<Render3DEvent> {
        if (mc.theWorld == null || mc.thePlayer == null) return@handler

        val font = Fonts.fontRegular40
        val renderPosX = mc.renderManager.renderPosX
        val renderPosY = mc.renderManager.renderPosY
        val renderPosZ = mc.renderManager.renderPosZ

        var canClear = true
        for (p in particles) {
            if (p.ticks > deleteAfter) continue
            canClear = false

            // 生命周期后 1/3 开始渐隐
            val fadeStart = deleteAfter * 2f / 3f
            val alpha = if (p.ticks > fadeStart) {
                (1f - (p.ticks - fadeStart) / (deleteAfter - fadeStart)).coerceIn(0f, 1f)
            } else 1f

            val argb = Color(p.color.red, p.color.green, p.color.blue, (alpha * 255f).toInt()).rgb

            GlStateManager.pushMatrix()
            GlStateManager.translate(
                p.textPos.xCoord - renderPosX,
                p.textPos.yCoord - renderPosY,
                p.textPos.zCoord - renderPosZ
            )
            GlStateManager.rotate(-mc.renderManager.playerViewY, 0.0f, 1.0f, 0.0f)
            GlStateManager.rotate(
                mc.renderManager.playerViewX,
                if (mc.gameSettings.thirdPersonView == 2) -1.0f else 1.0f, 0.0f, 0.0f
            )
            GlStateManager.scale(-0.03f, -0.03f, 0.03f)
            GL11.glDepthMask(false)
            font.drawStringWithShadow(
                p.str,
                -font.getStringWidth(p.str) / 2f,
                -font.height + 1f,
                argb
            )
            GL11.glDepthMask(true)
            GlStateManager.popMatrix()
        }

        if (canClear) {
            particles.clear()
        }
    }

    private class Particle(val str: String, val textPos: Vec3, val color: Color) {
        var ticks = 0
    }
}
