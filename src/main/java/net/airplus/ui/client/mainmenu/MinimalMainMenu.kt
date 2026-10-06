/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.ui.client.mainmenu

import net.airplus.AirPlus.CLIENT_NAME
import net.airplus.AirPlus.clientVersionText
import net.airplus.ui.client.GuiModsMenu
import net.airplus.ui.client.altmanager.GuiAltManager
import net.airplus.ui.font.Fonts
import net.airplus.utils.render.RenderUtils
import net.airplus.utils.timing.MSTimer
import net.airplus.utils.ui.AbstractScreen
import net.minecraft.client.gui.GuiMultiplayer
import net.minecraft.client.gui.GuiOptions
import net.minecraft.client.gui.GuiSelectWorld
import net.minecraft.client.gui.ScaledResolution
import java.awt.Color

/**
 * 极简居中主菜单（迁移自 AirClient MinimalMainMenu）。
 *
 * 风格：纸质极简、编辑器式留白美学。
 * 布局：完全居中对称，顶部超大细体衬线标题，中央单列细长按钮，底部小字脚注。
 * 配色：暖白半透明面板，深炭灰文字，悬停浅米色高亮。
 */
class MinimalMainMenu : AbstractScreen() {

    private val timer = MSTimer()

    private data class Entry(val label: String, val action: () -> Unit)

    private val entries = listOf(
        Entry("Singleplayer") { mc.displayGuiScreen(GuiSelectWorld(this)) },
        Entry("Multiplayer") { mc.displayGuiScreen(GuiMultiplayer(this)) },
        Entry("Alt Manager") { mc.displayGuiScreen(GuiAltManager(this)) },
        Entry("Options") { mc.displayGuiScreen(GuiOptions(this, mc.gameSettings)) },
        Entry("Mods") { mc.displayGuiScreen(GuiModsMenu(this)) },
        Entry("Quit") { mc.shutdown() }
    )

    private val hoverProgress = FloatArray(entries.size)

    override fun initGui() {
        timer.reset()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val sr = ScaledResolution(mc)
        width = sr.scaledWidth
        height = sr.scaledHeight

        // 统一背景 + 暖白半透明蒙层
        MainMenuStyles.drawMenuBackground(width, height, Color(245, 242, 235, 150).rgb)

        val titleFont = Fonts.fontNeutonExtraLight40
        val labelFont = Fonts.fontSemibold35
        val smallFont = Fonts.fontRegular35
        val labelH = labelFont.height
        val smallH = smallFont.height

        // 顶部标题区
        val titleY = height / 5f
        titleFont.drawCenteredString(
            CLIENT_NAME, width / 2f, titleY, Color(38, 38, 40).rgb, false
        )

        // 标题下方细横线 + 版本号
        val lineY = titleY + titleFont.height + 14f
        val lineWidth = 60f
        RenderUtils.drawRect(
            width / 2f - lineWidth / 2f, lineY,
            width / 2f + lineWidth / 2f, lineY + 1f,
            Color(180, 170, 150, 160).rgb
        )
        smallFont.drawCenteredString(
            clientVersionText, width / 2f, lineY + 10f,
            Color(120, 116, 108).rgb, false
        )

        // 中央按钮列
        val btnW = 240
        val btnH = 36
        val gap = 8
        val totalH = entries.size * btnH + (entries.size - 1) * gap
        val by0 = height / 2f - totalH / 2f + 40f

        val panelPadX = 28f
        val panelPadY = 20f
        // 整体淡色背板
        RenderUtils.drawRoundedRect(
            width / 2f - btnW / 2f - panelPadX, by0 - panelPadY,
            width / 2f + btnW / 2f + panelPadX, by0 + totalH + panelPadY,
            Color(245, 242, 235, 120).rgb, 8f
        )

        var by = by0
        for (i in entries.indices) {
            val e = entries[i]
            val bx = width / 2f - btnW / 2f
            val hovered = mouseX >= bx && mouseX <= bx + btnW &&
                    mouseY >= by && mouseY <= by + btnH

            val target = if (hovered) 1f else 0f
            hoverProgress[i] += (target - hoverProgress[i]) * 0.18f
            val p = hoverProgress[i]

            // 悬停背景
            val bgAlpha = (110 * p).toInt()
            if (bgAlpha > 1) {
                RenderUtils.drawRoundedRect(
                    bx, by, bx + btnW, by + btnH,
                    Color(232, 224, 205, bgAlpha).rgb, 5f
                )
            }

            // 左侧小圆点（悬停时出现）
            if (p > 0.05f) {
                val dotR = 2.4f * p
                val dotX = bx + 16f
                val dotY = by + btnH / 2f
                RenderUtils.drawRoundedRect(
                    dotX - dotR, dotY - dotR, dotX + dotR, dotY + dotR,
                    Color(150, 110, 70, (220 * p).toInt()).rgb, dotR
                )
            }

            // 文字垂直居中（用 fontHeight）
            val textY = by + (btnH - labelH) / 2f
            val textColor = mixColor(Color(70, 68, 64), Color(40, 38, 34), p)
            labelFont.drawString(
                e.label, bx + 28f, textY, textColor.rgb
            )

            // 右侧箭头（悬停时出现）
            if (p > 0.05f) {
                val arrow = "→"
                val aw = labelFont.getStringWidth(arrow)
                labelFont.drawString(
                    arrow, bx + btnW - aw - 16f, textY,
                    Color(150, 110, 70, (220 * p).toInt()).rgb
                )
            }

            by += btnH + gap
        }

        // 底部脚注
        val footY = height - 32f
        smallFont.drawCenteredString(
            "© AirPlus",
            width / 2f, footY, Color(110, 106, 98).rgb, false
        )

        // 右下角切换按钮
        MainMenuStyles.drawSettingsButton(width, height, mouseX, mouseY)

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (MainMenuStyles.handleSettingsClick(width, height, mouseX, mouseY, mouseButton)) {
            timer.reset()
            return
        }

        if (mouseButton == 0 && timer.hasTimePassed(200)) {
            val btnW = 240
            val btnH = 36
            val gap = 8
            val totalH = entries.size * btnH + (entries.size - 1) * gap
            var by = height / 2f - totalH / 2f + 40f
            for (i in entries.indices) {
                val bx = width / 2f - btnW / 2f
                if (mouseX >= bx && mouseX <= bx + btnW && mouseY >= by && mouseY <= by + btnH) {
                    entries[i].action()
                    timer.reset()
                    return
                }
                by += btnH + gap
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    private fun mixColor(a: Color, b: Color, t: Float): Color {
        val tt = t.coerceIn(0f, 1f)
        return Color(
            (a.red + (b.red - a.red) * tt).toInt(),
            (a.green + (b.green - a.green) * tt).toInt(),
            (a.blue + (b.blue - a.blue) * tt).toInt()
        )
    }
}
