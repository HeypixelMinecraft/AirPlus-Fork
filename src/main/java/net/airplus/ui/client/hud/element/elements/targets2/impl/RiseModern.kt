package net.airplus.ui.client.hud.element.elements.targets2.impl

import net.airplus.ui.client.hud.element.Border
import net.airplus.ui.client.hud.element.elements.Target2
import net.airplus.ui.client.hud.element.elements.targets2.TargetStyle
import net.airplus.ui.font.Fonts
import net.airplus.utils.render.RenderUtils
import net.airplus.utils.render.RoundedUtil
import net.minecraft.entity.player.EntityPlayer
import java.awt.Color
import kotlin.math.max
import kotlin.math.round

class RiseModern(inst: Target2) : TargetStyle("RiseModern", inst, true) {

    private var smoothHurtTime = 0f
    private var prevHurtTime = 0

    override fun drawTarget(entity: EntityPlayer) {
        updateAnim(entity.health)

        val name = entity.name
        val health = round(entity.health.coerceIn(0F, entity.maxHealth))
        val healthText = health.toInt().toString()
        val healthTextWidth = Fonts.fontSemibold40.getStringWidth(healthText)
        val nameWidth = Fonts.fontSemibold40.getStringWidth(name)

        val edgeOffset = 8
        val padding = 7
        val indent = 4
        val faceScale = 32
        val healthBarWidth = max(nameWidth + 35 - healthTextWidth, 65).toFloat()
        val width = (edgeOffset + faceScale + edgeOffset + healthBarWidth + indent + healthTextWidth + edgeOffset).toFloat()
        val height = (faceScale + edgeOffset * 2).toFloat()

        // 背景
        RoundedUtil.drawRound(0F, 0F, width, height, 9F, targetInstance.bgColor)

        // 头像受击动画：受击瞬间 snap，然后按帧平滑衰减（匹配 Rise 表现）
        if (entity.hurtTime > prevHurtTime) {
            smoothHurtTime = entity.hurtTime.toFloat()
        }
        prevHurtTime = entity.hurtTime
        val decay = RenderUtils.deltaTime / 50f
        smoothHurtTime = (smoothHurtTime - decay).coerceAtLeast(0f)

        val playerInfo = mc.netHandler.getPlayerInfo(entity.uniqueID)
        if (playerInfo != null) {
            val hurtTime = smoothHurtTime * 0.5f
            val headX = edgeOffset + hurtTime / 2f
            val headY = edgeOffset + hurtTime / 2f
            val headSize = (faceScale - hurtTime).coerceAtLeast(1f)
            val hurtRatio = (smoothHurtTime / 10f).coerceIn(0f, 1f)
            val r = 1f
            val g = 1f - hurtRatio * 0.8f
            val b = 1f - hurtRatio * 0.8f
            val alpha = 1F - targetInstance.getFadeProgress()
            drawHead(playerInfo.locationSkin, headX, headY, 1f, headSize.toInt(), headSize.toInt(), r, g, b, alpha)
        }

        // 名字
        Fonts.fontSemibold40.drawString(
            name,
            (edgeOffset + faceScale + padding).toFloat(),
            (edgeOffset + indent + 2).toFloat(),
            targetInstance.barColor.rgb
        )

        // 生命条背景
        val barX = (edgeOffset + faceScale + padding).toFloat()
        val barY = (edgeOffset + faceScale - indent - 7).toFloat()
        RoundedUtil.drawRound(barX, barY, healthBarWidth, 6F, 3F, Color(0, 0, 0, 120))

        // 生命条
        val hpW = (easingHealth / entity.maxHealth.coerceAtLeast(1F) * healthBarWidth).coerceAtMost(healthBarWidth)
        RoundedUtil.drawRound(barX, barY, hpW, 6F, 3F, targetInstance.barColor)

        // 生命数值
        Fonts.fontSemibold40.drawString(
            healthText,
            (barX + healthBarWidth + indent).toFloat(),
            (barY - 1).toFloat(),
            targetInstance.barColor.rgb
        )
    }

    override fun getBorder(entity: EntityPlayer?): Border? {
        entity ?: return Border(0F, 0F, 120F, 48F)

        val name = entity.name
        val healthText = round(entity.health.coerceIn(0F, entity.maxHealth)).toInt().toString()
        val healthTextWidth = Fonts.fontSemibold40.getStringWidth(healthText)
        val nameWidth = Fonts.fontSemibold40.getStringWidth(name)

        val edgeOffset = 8
        val padding = 7
        val indent = 4
        val faceScale = 32
        val healthBarWidth = max(nameWidth + 35 - healthTextWidth, 65).toFloat()
        val width = (edgeOffset + faceScale + edgeOffset + healthBarWidth + indent + healthTextWidth + edgeOffset).toFloat()
        val height = (faceScale + edgeOffset * 2).toFloat()

        return Border(0F, 0F, width, height)
    }

    override fun handleBlur(entity: EntityPlayer) {
        val border = getBorder(entity) ?: return
        RoundedUtil.drawRound(0F, 0F, border.x2, border.y2, 9F, Color(255, 255, 255, 255))
    }

    override fun handleShadowCut(entity: EntityPlayer) = handleBlur(entity)

    override fun handleShadow(entity: EntityPlayer) {
        val border = getBorder(entity) ?: return
        RoundedUtil.drawRound(0F, 0F, border.x2, border.y2, 9F, shadowOpaque)
    }
}