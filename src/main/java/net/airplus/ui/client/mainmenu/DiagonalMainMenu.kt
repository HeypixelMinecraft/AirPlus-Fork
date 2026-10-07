/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.ui.client.mainmenu

import net.airplus.AirPlus.CLIENT_NAME
import net.airplus.AirPlus.clientVersionText
import net.airplus.ui.client.GuiModsMenu
import net.airplus.ui.client.GuiSettingsMenu
import net.airplus.ui.client.altmanager.GuiAltManager
import net.airplus.ui.font.Fonts
import net.airplus.utils.render.RenderUtils
import net.airplus.utils.timing.MSTimer
import net.airplus.utils.ui.AbstractScreen
import net.minecraft.client.gui.GuiMultiplayer
import net.minecraft.client.gui.GuiOptions
import net.minecraft.client.gui.GuiSelectWorld
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.opengl.GL11
import java.awt.Color

/**
 * 对角线主菜单（迁移自 AirClient DiagonalMainMenu）。
 *
 * 风格：复古画册 / 皮革质感美学，非对称对角分布，沉静优雅。
 * 布局：左上 LOGO + 标题；右下垂直按钮组（右对齐）；中间倾斜大字水印 + 对角细斜线装饰。
 * 配色：深棕 #3D2E26 蒙层，奶白 #EDE0D4 文字，强调色 #A0784F 暖棕。
 */
class DiagonalMainMenu : AbstractScreen() {

    private val timer = MSTimer()

    private data class Entry(val label: String, val desc: String, val action: () -> Unit)

    private val entries = listOf(
        Entry("Singleplayer", "Local adventure") { mc.displayGuiScreen(GuiSelectWorld(this)) },
        Entry("Multiplayer", "Online servers") { mc.displayGuiScreen(GuiMultiplayer(this)) },
        Entry("Alt Manager", "Accounts") { mc.displayGuiScreen(GuiAltManager(this)) },
        Entry("Options", "Settings") { mc.displayGuiScreen(GuiSettingsMenu(this)) },
        Entry("Mods", "Modules") { mc.displayGuiScreen(GuiModsMenu(this)) },
        Entry("Quit", "Exit game") { mc.shutdown() }
    )

    private val hoverProgress = FloatArray(entries.size)

    override fun initGui() {
        timer.reset()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val sr = ScaledResolution(mc)
        width = sr.scaledWidth
        height = sr.scaledHeight

        // 统一背景 + 深棕蒙层
        MainMenuStyles.drawMenuBackground(width, height, Color(50, 38, 32, 165).rgb)

        val accent = Color(160, 120, 79)
        val lineColor = Color(160, 120, 79, 70)

        val titleFont = Fonts.fontBold180
        val labelFont = Fonts.fontSemibold35
        val smallFont = Fonts.fontRegular35
        val descFont = Fonts.fontRegular30
        val idxFont = Fonts.fontRegular30

        // ===== 左上：LOGO + 标题 =====
        val padX = 50f
        val topY = 46f
        RenderUtils.drawRoundedRect(padX, topY, padX + 30f, topY + 30f, accent.rgb, 5f)
        val logoChar = "A"
        Fonts.fontExtraBold40.drawString(
            logoChar, padX + 15f - Fonts.fontExtraBold40.getStringWidth(logoChar) / 2f,
            topY + 15f - Fonts.fontExtraBold40.height / 2f + 1f,
            Color(245, 240, 232).rgb
        )
        labelFont.drawString(CLIENT_NAME, padX + 42f, topY + 1f, Color(237, 224, 212).rgb)
        descFont.drawString(clientVersionText, padX + 42f, topY + 4f + labelFont.height, Color(170, 158, 146).rgb)

        // 左上 LOGO 下方装饰：短横线 + 小字
        val decoY1 = topY + 50f
        RenderUtils.drawRect(padX, decoY1, padX + 36f, decoY1 + 1f, accent.rgb)
        smallFont.drawString(
            "AirPlus · 2026", padX, decoY1 + 8f,
            Color(160, 120, 79, 200).rgb
        )

        // ===== 中间：倾斜大字水印 + 对角斜线 =====
        GL11.glPushMatrix()
        val centerX = width / 2f
        val centerY = height / 2f
        GL11.glTranslatef(centerX, centerY, 0f)
        GL11.glRotatef(-18f, 0f, 0f, 1f)

        val bigTitle = CLIENT_NAME
        val bigW = titleFont.getStringWidth(bigTitle)
        titleFont.drawString(
            bigTitle, -bigW / 2f + 4f, -titleFont.height / 2f + 4f,
            Color(0, 0, 0, 40).rgb
        )
        titleFont.drawString(
            bigTitle, -bigW / 2f, -titleFont.height / 2f,
            Color(160, 120, 79, 55).rgb
        )
        GL11.glPopMatrix()

        // 对角细斜线装饰（左下到右上若干平行线）
        for (i in -3..3) {
            val offset = i * 24f
            drawDiagonalLine(
                60f, height - 120f + offset,
                width - 60f, 120f + offset,
                lineColor.rgb
            )
        }

        // ===== 右下：垂直按钮组（右对齐） =====
        val btnW = 240f
        val btnH = 44f
        val btnGap = 8f
        val totalH = entries.size * btnH + (entries.size - 1) * btnGap
        val rightPad = 50f
        val bx = width - rightPad - btnW
        var by = height - 80f - totalH

        for (i in entries.indices) {
            val e = entries[i]
            val hovered = mouseX >= bx && mouseX <= bx + btnW &&
                    mouseY >= by && mouseY <= by + btnH

            val target = if (hovered) 1f else 0f
            hoverProgress[i] += (target - hoverProgress[i]) * 0.2f
            val p = hoverProgress[i]

            // 悬停背景
            if (p > 0.02f) {
                RenderUtils.drawRoundedRect(
                    bx, by, bx + btnW, by + btnH,
                    Color(245, 232, 218, (40 * p).toInt()).rgb, 5f
                )
            }

            // 右侧强调竖条（悬停时）
            if (p > 0.05f) {
                RenderUtils.drawRect(
                    bx + btnW - 3f * p, by + 8f, bx + btnW, by + btnH - 8f,
                    Color(accent.red, accent.green, accent.blue, (220 * p).toInt()).rgb
                )
            }

            // 序号（左侧，垂直居中）
            val idxStr = String.format("%02d", i + 1)
            idxFont.drawString(
                idxStr, bx + 12f, by + (btnH - idxFont.height) / 2f,
                mixColor(Color(150, 138, 126), accent, p).rgb
            )
            // 序号右侧分隔竖线
            RenderUtils.drawRect(bx + 40f, by + 10f, bx + 41f, by + btnH - 10f, Color(160, 120, 79, 60).rgb)

            // 标签（左对齐，上方）
            labelFont.drawString(
                e.label, bx + 50f, by + (btnH - labelFont.height - descFont.height - 2f) / 2f,
                mixColor(Color(225, 218, 206), Color(248, 242, 232), p).rgb
            )
            // 描述（左对齐，下方）
            descFont.drawString(
                e.desc, bx + 50f, by + (btnH - labelFont.height - descFont.height - 2f) / 2f + labelFont.height + 2f,
                Color(150, 142, 132).rgb
            )

            by += btnH + btnGap
        }

        // 右下角装饰小字
        smallFont.drawString(
            "© AirPlus",
            width - rightPad - smallFont.getStringWidth("© AirPlus"),
            height - 32f,
            Color(160, 120, 79, 160).rgb
        )

        // 右下角切换按钮
        MainMenuStyles.drawSettingsButton(width, height, mouseX, mouseY)

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    private fun drawDiagonalLine(x1: Float, y1: Float, x2: Float, y2: Float, color: Int) {
        val steps = 40
        val dx = (x2 - x1) / steps
        val dy = (y2 - y1) / steps
        for (i in 0 until steps) {
            val sx = x1 + dx * i
            val sy = y1 + dy * i
            RenderUtils.drawRect(sx, sy, sx + 1.5f, sy + 1.5f, color)
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (MainMenuStyles.handleSettingsClick(width, height, mouseX, mouseY, mouseButton)) {
            timer.reset()
            return
        }

        if (mouseButton == 0 && timer.hasTimePassed(200)) {
            val btnW = 240f
            val btnH = 44f
            val btnGap = 8f
            val totalH = entries.size * btnH + (entries.size - 1) * btnGap
            val rightPad = 50f
            val bx = width - rightPad - btnW
            var by = height - 80f - totalH

            for (i in entries.indices) {
                if (mouseX >= bx && mouseX <= bx + btnW && mouseY >= by && mouseY <= by + btnH) {
                    entries[i].action()
                    timer.reset()
                    return
                }
                by += btnH + btnGap
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
