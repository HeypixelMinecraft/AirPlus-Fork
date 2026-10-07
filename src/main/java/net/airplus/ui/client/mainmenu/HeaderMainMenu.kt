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
import java.awt.Color

/**
 * 顶部条形主菜单（迁移自 AirClient HeaderMainMenu）。
 *
 * 风格：杂志报头 / 高级时装画册风格，极简横线分割，大量留白。
 * 布局：顶部完整横向导航条（左 LOGO，中水平按钮，右版本）；下方大面积展示区显示超大标题与细装饰线。
 * 配色：深紫红 #3D2A35 蒙层，暖灰 #D4C5C9 文字，强调色 #8B6478 玫瑰紫。
 */
class HeaderMainMenu : AbstractScreen() {

    private val timer = MSTimer()

    private data class Entry(val label: String, val action: () -> Unit)

    private val entries = listOf(
        Entry("Singleplayer") { mc.displayGuiScreen(GuiSelectWorld(this)) },
        Entry("Multiplayer") { mc.displayGuiScreen(GuiMultiplayer(this)) },
        Entry("Alt Manager") { mc.displayGuiScreen(GuiAltManager(this)) },
        Entry("Options") { mc.displayGuiScreen(GuiSettingsMenu(this)) },
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

        // 统一背景 + 深紫红蒙层
        MainMenuStyles.drawMenuBackground(width, height, Color(48, 32, 42, 160).rgb)

        val accent = Color(139, 100, 120)
        val headerBg = Color(30, 20, 26, 210)
        val headerH = 64

        val titleFont = Fonts.fontBold180
        val labelFont = Fonts.fontSemibold35
        val smallFont = Fonts.fontRegular35
        val descFont = Fonts.fontRegular30

        // ===== 顶部导航条 =====
        RenderUtils.drawRect(0f, 0f, width.toFloat(), headerH.toFloat(), headerBg.rgb)
        // 底部强调线
        RenderUtils.drawRect(0f, headerH.toFloat(), width.toFloat(), (headerH + 1).toFloat(), accent.rgb)

        // 左侧 LOGO
        val padX = 40f
        val logoY = 18f
        RenderUtils.drawRoundedRect(padX, logoY, padX + 28f, logoY + 28f, accent.rgb, 5f)
        val logoChar = "A"
        Fonts.fontExtraBold35.drawString(
            logoChar, padX + 14f - Fonts.fontExtraBold35.getStringWidth(logoChar) / 2f,
            logoY + 14f - Fonts.fontExtraBold35.height / 2f + 1f,
            Color(245, 240, 232).rgb
        )
        labelFont.drawString(CLIENT_NAME, padX + 40f, logoY + 1f, Color(232, 222, 226).rgb)
        descFont.drawString("v" + clientVersionText, padX + 40f, logoY + 4f + labelFont.height, Color(180, 170, 174).rgb)

        // 右侧版本信息
        val rightText = "MINECRAFT 1.8.9"
        descFont.drawString(
            rightText, width - padX - descFont.getStringWidth(rightText), logoY + 6f,
            Color(180, 170, 174).rgb
        )
        val rightText2 = "FORGE"
        descFont.drawString(
            rightText2, width - padX - descFont.getStringWidth(rightText2), logoY + 6f + descFont.height + 4f,
            Color(139, 100, 120, 200).rgb
        )

        // 中间水平按钮组
        val btnW = 110
        val btnH = 32
        val btnGap = 8
        val totalBtnW = entries.size * btnW + (entries.size - 1) * btnGap
        var bx = width / 2f - totalBtnW / 2f
        val by = (headerH - btnH) / 2f

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
                    Color(139, 100, 120, (60 * p).toInt()).rgb, 4f
                )
            }

            // 文字
            labelFont.drawString(
                e.label, bx + btnW / 2f - labelFont.getStringWidth(e.label) / 2f,
                by + (btnH - labelFont.height) / 2f,
                mixColor(Color(220, 214, 218), Color(245, 240, 232), p).rgb
            )

            // 悬停底部短横线
            if (p > 0.05f) {
                val lineW = 24f * p
                RenderUtils.drawRect(
                    bx + btnW / 2f - lineW / 2f, by + btnH - 4f,
                    bx + btnW / 2f + lineW / 2f, by + btnH - 3f,
                    Color(139, 100, 120, (220 * p).toInt()).rgb
                )
            }

            bx += btnW + btnGap
        }

        // ===== 下方展示区：超大标题居中 + 装饰 =====
        val showY = headerH + (height - headerH) * 0.32f
        val bigTitle = CLIENT_NAME
        val bigW = titleFont.getStringWidth(bigTitle)
        val maxW = width - 120f
        val useBig = bigW <= maxW
        val usedFont = if (useBig) titleFont else labelFont
        val usedW = usedFont.getStringWidth(bigTitle)
        usedFont.drawString(
            bigTitle, width / 2f - usedW / 2f + 3f, showY + 3f,
            Color(0, 0, 0, 50).rgb
        )
        usedFont.drawString(
            bigTitle, width / 2f - usedW / 2f, showY,
            Color(232, 222, 226, 220).rgb
        )

        // 标题下方装饰：细横线 + 副标题 + 细横线（对称居中）
        val decoY = showY + usedFont.height + 24f
        val lineLen = 60f
        val subText = "AIRPLUS · CLIENT"
        val subW = smallFont.getStringWidth(subText)
        val totalDecoW = lineLen * 2 + 16f + subW
        val decoStart = width / 2f - totalDecoW / 2f
        RenderUtils.drawRect(decoStart, decoY + smallFont.height / 2f, decoStart + lineLen, decoY + smallFont.height / 2f + 1f, accent.rgb)
        smallFont.drawString(
            subText, decoStart + lineLen + 8f, decoY,
            Color(180, 170, 174).rgb
        )
        RenderUtils.drawRect(decoStart + lineLen + 16f + subW, decoY + smallFont.height / 2f, decoStart + lineLen * 2 + 16f + subW, decoY + smallFont.height / 2f + 1f, accent.rgb)

        // 装饰副标题
        descFont.drawCenteredString(
            "An open source mixin-based client", width / 2f, decoY + smallFont.height + 16f,
            Color(160, 150, 154, 200).rgb, false
        )

        // 左下角装饰：竖向小标记
        val markX = padX
        val markY = height - 80f
        RenderUtils.drawRect(markX, markY, markX + 2f, markY + 40f, Color(139, 100, 120, 180).rgb)
        smallFont.drawString(
            "© AirPlus", markX + 10f, markY + 12f,
            Color(160, 150, 154, 200).rgb
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
            val headerH = 64
            val btnW = 110
            val btnH = 32
            val btnGap = 8
            val totalBtnW = entries.size * btnW + (entries.size - 1) * btnGap
            var bx = width / 2f - totalBtnW / 2f
            val by = (headerH - btnH) / 2f

            for (i in entries.indices) {
                if (mouseX >= bx && mouseX <= bx + btnW && mouseY >= by && mouseY <= by + btnH) {
                    entries[i].action()
                    timer.reset()
                    return
                }
                bx += btnW + btnGap
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
