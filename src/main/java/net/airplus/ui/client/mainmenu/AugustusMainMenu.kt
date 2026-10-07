/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.ui.client.mainmenu

import net.airplus.AirPlus.CLIENT_NAME
import net.airplus.AirPlus.clientVersionText
import net.airplus.features.module.ModuleManager
import net.airplus.features.module.modules.render.ClickGUI
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
 * Augustus 风格主菜单（复刻 Augustus 客户端 b4.20.3 布局，品牌适配 AirPlus）。
 *
 * 布局：左侧深色竖排边栏（顶部客户端标识 + 用户名，中部蓝色按钮组），
 * 主区为统一背景 + 青灰雾蒙层，居中像素风大 logo。
 * 右上角模块/设置统计，左下版本号，右下制作组。
 */
class AugustusMainMenu : AbstractScreen() {

    private val timer = MSTimer()

    private data class Entry(val label: String, val action: () -> Unit)

    private val entries = listOf(
        Entry("Singleplayer") { mc.displayGuiScreen(GuiSelectWorld(this)) },
        Entry("Multiplayer") { mc.displayGuiScreen(GuiMultiplayer(this)) },
        Entry("AirPlus") { ModuleManager[ClickGUI::class.java]?.toggle() },
        Entry("Options...") { mc.displayGuiScreen(GuiOptions(this, mc.gameSettings)) },
        Entry("Quit Game") { mc.shutdown() }
    )

    private val hoverProgress = FloatArray(entries.size)

    /** 模块/设置总数统计（惰性缓存，避免每帧遍历） */
    private val statsText by lazy {
        val modules = ModuleManager.size
        val settings = ModuleManager.sumOf { it.values.size }
        "$modules modules and $settings settings loaded"
    }

    /** 边栏布局数据（drawScreen 与 mouseClicked 共用同一套公式） */
    private data class Layout(
        val sidebarW: Float,
        val padX: Float,
        val btnW: Float,
        val btnH: Float,
        val gap: Float,
        val btnY0: Float
    )

    private fun layout(): Layout {
        val sidebarW = (width * 0.175f).coerceIn(100f, 200f)
        val padX = (sidebarW * 0.09f).coerceIn(8f, 16f)
        val btnW = sidebarW - padX * 2f
        val btnH = (height * 0.036f).coerceIn(20f, 26f)
        val gap = 4f
        val totalH = entries.size * btnH + (entries.size - 1) * gap
        val btnY0 = height / 2f - totalH / 2f - 20f
        return Layout(sidebarW, padX, btnW, btnH, gap, btnY0)
    }

    override fun initGui() {
        timer.reset()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val sr = ScaledResolution(mc)
        width = sr.scaledWidth
        height = sr.scaledHeight
        val l = layout()

        // ===== 1. 统一背景 + 青灰雾蒙层 =====
        MainMenuStyles.drawMenuBackground(width, height, 0)
        RenderUtils.drawRect(0f, 0f, width.toFloat(), height.toFloat(), Color(58, 88, 102, 110).rgb)

        // ===== 2. 左侧不透明深色边栏 + 右缘蓝色亮线 =====
        RenderUtils.drawRect(0f, 0f, l.sidebarW, height.toFloat(), Color(12, 22, 27, 245).rgb)
        RenderUtils.drawRect(l.sidebarW, 0f, l.sidebarW + 1f, height.toFloat(), Color(59, 125, 216, 90).rgb)

        // ===== 3. 边栏顶部：客户端标识 + 用户名 =====
        val topFont = Fonts.fontSemibold35
        val smallFont = Fonts.fontRegular30
        topFont.drawString(CLIENT_NAME, l.padX, 14f, Color(59, 125, 216).rgb)
        smallFont.drawString(mc.session.username, l.padX, 14f + topFont.height + 3f, Color(170, 180, 186).rgb)

        // ===== 4. 边栏中部蓝色按钮组 =====
        val labelFont = Fonts.fontFluxSans
        val labelH = labelFont.height
        for (i in entries.indices) {
            val bx = l.padX
            val by = l.btnY0 + i * (l.btnH + l.gap)
            val hovered = mouseX >= bx && mouseX <= bx + l.btnW && mouseY >= by && mouseY <= by + l.btnH

            val target = if (hovered) 1f else 0f
            hoverProgress[i] += (target - hoverProgress[i]) * 0.18f
            val p = hoverProgress[i]

            RenderUtils.drawRoundedRect(bx, by, bx + l.btnW, by + l.btnH, lerpColor(BTN_COLOR, BTN_HOVER, p).rgb, 2f)

            val tx = bx + l.btnW / 2f - labelFont.getStringWidth(entries[i].label) / 2f
            val ty = by + (l.btnH - labelH) / 2f
            labelFont.drawString(entries[i].label, tx, ty, Color(240, 244, 248).rgb)
        }

        // ===== 5. 主区居中像素风大 logo（原版字体放大 3 倍）=====
        val logoScale = 3f
        val textW = mc.fontRendererObj.getStringWidth(LOGO) * logoScale
        val logoX = l.sidebarW + (width - l.sidebarW) / 2f - textW / 2f
        val logoY = height * 0.34f
        GL11.glPushMatrix()
        GL11.glScalef(logoScale, logoScale, 1f)
        mc.fontRendererObj.drawStringWithShadow(LOGO, logoX / logoScale, logoY / logoScale, Color(228, 236, 240).rgb)
        GL11.glPopMatrix()

        // ===== 6. 右上角统计 =====
        val stW = smallFont.getStringWidth(statsText)
        smallFont.drawString(statsText, width - 10f - stW, 10f, Color(200, 210, 216).rgb)

        // ===== 7. 左下版本号 / 右下制作组（避让右下 Menu Settings 按钮）=====
        smallFont.drawString("b$clientVersionText", 10f, height - 14f, Color(200, 210, 216).rgb)
        val creditW = smallFont.getStringWidth(CREDIT)
        smallFont.drawString(CREDIT, width - 10f - creditW, height - 40f, Color(180, 190, 196, 200).rgb)

        // ===== 8. 统一设置入口 =====
        MainMenuStyles.drawSettingsButton(width, height, mouseX, mouseY)

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (MainMenuStyles.handleSettingsClick(width, height, mouseX, mouseY, mouseButton)) {
            timer.reset()
            return
        }

        if (mouseButton == 0 && timer.hasTimePassed(200)) {
            val l = layout()
            for (i in entries.indices) {
                val by = l.btnY0 + i * (l.btnH + l.gap)
                if (mouseX >= l.padX && mouseX <= l.padX + l.btnW && mouseY >= by && mouseY <= by + l.btnH) {
                    entries[i].action()
                    timer.reset()
                    return
                }
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    private fun lerpColor(a: Color, b: Color, t: Float): Color {
        val tt = t.coerceIn(0f, 1f)
        return Color(
            (a.red + (b.red - a.red) * tt).toInt(),
            (a.green + (b.green - a.green) * tt).toInt(),
            (a.blue + (b.blue - a.blue) * tt).toInt()
        )
    }

    private companion object {
        const val LOGO = "Augustus"
        const val CREDIT = "© AirPlus Team"
        val BTN_COLOR = Color(59, 125, 216)
        val BTN_HOVER = Color(72, 148, 245)
    }
}
