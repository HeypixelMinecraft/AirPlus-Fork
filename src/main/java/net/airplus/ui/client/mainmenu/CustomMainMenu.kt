/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.ui.client.mainmenu

import net.airplus.AirPlus.CLIENT_NAME
import net.airplus.AirPlus.clientVersionText
import net.airplus.ui.client.altmanager.GuiAltManager
import net.airplus.ui.font.Fonts
import net.airplus.utils.render.RenderUtils
import net.airplus.utils.timing.MSTimer
import net.airplus.utils.ui.AbstractScreen
import net.minecraft.client.gui.GuiMultiplayer
import net.minecraft.client.gui.GuiOptions
import net.minecraft.client.gui.GuiSelectWorld
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.input.Mouse
import java.awt.Color

/**
 * 面板选择式主菜单（迁移自 AirClient CustomMainMenu）。
 * 原 AirClient 使用贴图素材（selectBG/selection/图标），此处改为圆角矩形 + Flux 图标字形绘制，效果一致且无需外部素材。
 */
class CustomMainMenu : AbstractScreen() {
    private var particleEngine: ParticleEngine? = null

    private val timer = MSTimer()

    private var firstInit = false
    private var selectionAnimY = 0f
    private var i1 = 0
    private var cur = ""

    // 标签 -> Flux Icon 字形（K=单人游戏 L=多人游戏 M=Alt N=设置）
    private val glyphs = mapOf(
        "Single Player" to "K",
        "Multi Player" to "L",
        "Settings" to "N",
        "Alts Manager" to "M"
    )

    init {
        if (!firstInit) firstInit = true
    }

    override fun initGui() {
        if (particleEngine == null) particleEngine = ParticleEngine()
        timer.reset()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val sr = ScaledResolution(mc)
        width = sr.scaledWidth
        height = sr.scaledHeight

        MainMenuStyles.drawMenuBackground(width, height, Color(0, 0, 0, 90).rgb)

        // Render particles
        particleEngine?.render()

        // Draw logo
        val logoFont = Fonts.fontBold180
        logoFont.drawCenteredString(CLIENT_NAME, width / 2f, height / 2f - 130f, Color(245, 245, 245).rgb, false)
        Fonts.fontRegular35.drawCenteredString(
            clientVersionText,
            width / 2f + logoFont.getStringWidth(CLIENT_NAME) / 2f + 8f,
            height / 2f - 130f + logoFont.height - Fonts.fontRegular35.height,
            Color(220, 220, 220).rgb, false
        )

        // Draw main panel
        val x = (width - 269 / 2) / 2
        val y = (height - 200 / 2) / 2
        val width1 = 269 / 2
        val height1 = 244 / 2
        RenderUtils.drawRoundedRect(
            x.toFloat() - 10f, y.toFloat() - 12f,
            (x + width1).toFloat() + 10f, (y + height1).toFloat() + 12f,
            Color(250, 250, 250, 235).rgb, 8f
        )

        // Draw buttons with selection indicator
        val height2 = 92 / 2
        val strs = arrayOf("Single Player", "Multi Player", "Settings", "Alts Manager")

        // 悬停高亮条（原 selection.png 贴图的替代绘制）
        if (selectionAnimY == 0f) {
            selectionAnimY = y - 10f
            i1 = (y - 10f).toInt()
        }
        selectionAnimY = i1 + (selectionAnimY - i1) * 0.6f
        RenderUtils.drawRoundedRect(
            (x - 10).toFloat(), selectionAnimY,
            (x - 10 + 309 / 2f).toFloat(), selectionAnimY + height2,
            Color(232, 232, 232, 160).rgb, 6f
        )

        var buttonY = y.toInt()
        for (i in 0 until 4) {
            val str = strs[i]
            val font = Fonts.fontSemibold35
            val glyph = glyphs[str] ?: ""
            val hoveringAppend = isHoveringAppend(mouseX, mouseY, x, buttonY, width1, 32)

            if (hoveringAppend || cur == str) {
                i1 = buttonY - 10
                cur = str
                Fonts.fontFluxIcon20.drawStringWithShadow(
                    glyph, x + 30f, buttonY + 8f + (13f - Fonts.fontFluxIcon20.height) / 2f, Color(90, 90, 90).rgb
                )
                font.drawString(str, (x + 50).toFloat(), (buttonY + 10).toFloat(), Color(90, 90, 90).rgb)

                if (hoveringAppend && Mouse.isButtonDown(0) && timer.hasTimePassed(200)) {
                    when (i) {
                        0 -> mc.displayGuiScreen(GuiSelectWorld(this))
                        1 -> mc.displayGuiScreen(GuiMultiplayer(this))
                        2 -> mc.displayGuiScreen(GuiOptions(this, mc.gameSettings))
                        3 -> mc.displayGuiScreen(GuiAltManager(this))
                    }
                    timer.reset()
                }
            } else {
                font.drawString(str, (x + 50).toFloat(), (buttonY + 10).toFloat(), Color(197, 197, 197).rgb)
                Fonts.fontFluxIcon20.drawString(
                    glyph, x + 30f, buttonY + 8f + (13f - Fonts.fontFluxIcon20.height) / 2f, Color(197, 197, 197).rgb
                )
            }
            buttonY += 32
        }

        MainMenuStyles.drawSettingsButton(width, height, mouseX, mouseY)

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (MainMenuStyles.handleSettingsClick(width, height, mouseX, mouseY, mouseButton)) {
            timer.reset()
            return
        }

        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    /**
     * Check if mouse is hovering over a region
     */
    private fun isHoveringAppend(mouseX: Int, mouseY: Int, x: Int, y: Int, width: Int, height: Int): Boolean {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height
    }

    override fun handleMouseInput() {
        super.handleMouseInput()
    }

}
