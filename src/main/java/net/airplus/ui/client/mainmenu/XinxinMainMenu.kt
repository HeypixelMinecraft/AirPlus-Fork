/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.ui.client.mainmenu

import net.airplus.ui.client.altmanager.GuiAltManager
import net.airplus.ui.font.Fonts
import net.airplus.utils.render.RenderUtils
import net.airplus.utils.ui.AbstractScreen
import net.minecraft.client.gui.GuiMultiplayer
import net.minecraft.client.gui.GuiOptions
import net.minecraft.client.gui.GuiSelectWorld
import java.awt.Color

/**
 * Xinxin 卡片式主菜单（迁移自 SilenceFix BetterMainMenu，风格名 Xinxin）。
 * 统一背景（默认 xinxin_bg 可在 Menu Settings 自定义）+ 暗色渐变蒙层 + 中央半透明圆角卡片（发光描边）+ 竖排紫色圆角按钮 + 点击淡出转场。
 */
class XinxinMainMenu : AbstractScreen() {

    private class MenuButton(val text: String, val action: () -> Unit) {
        var x = 0f
        var y = 0f
    }

    // 与原版一致：内置进服按钮文字为黄色加粗，点击进入 Alt Manager
    private val buttons = listOf(
        MenuButton("单人世界") { mc.displayGuiScreen(GuiSelectWorld(this)) },
        MenuButton("多人世界") { mc.displayGuiScreen(GuiMultiplayer(this)) },
        MenuButton("§l§e账号管理") { mc.displayGuiScreen(GuiAltManager(this)) },
        MenuButton("游戏设置") { mc.displayGuiScreen(GuiOptions(this, mc.gameSettings)) },
        MenuButton("退出游戏") { mc.shutdown() }
    )

    // 点击按钮后的淡出转场（原 DecelerateAnimation 900ms）
    private var pendingAction: (() -> Unit)? = null
    private var fadeStartMs = 0L

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        // 统一背景（可在 Menu Settings 选择/自定义）+ 暗色渐变蒙层
        MainMenuStyles.drawMenuBackground(width, height, 0)
        RenderUtils.drawGradientRect(
            0, 0, width, height,
            Color(40, 40, 40, 80).rgb, Color(20, 20, 20, 120).rgb, 0f
        )

        // 中央半透明圆角卡片 + 四层发光描边
        val cardWidth = 420f
        val cardHeight = 380f
        val midX = width / 2f - cardWidth / 2f
        val midY = height / 2f - cardHeight / 2f
        RenderUtils.drawRoundedRect(midX, midY, midX + cardWidth, midY + cardHeight, Color(255, 255, 255, 60).rgb, 20f)
        for (i in 1..4) {
            RenderUtils.drawRoundedRect(
                midX - i, midY - i, midX + cardWidth + i, midY + cardHeight + i,
                Color(255, 255, 255, 8).rgb, 20f + i
            )
        }

        Fonts.fontFluxRobotoL40.drawCenteredString("Xinxin Menu", width / 2f, midY + 40f, Color(255, 255, 255, 230).rgb)

        // 竖排按钮（140×40，间距 15）
        var buttonY = midY + 100f
        for (b in buttons) {
            b.x = width / 2f - BUTTON_WIDTH / 2f
            b.y = buttonY
            drawModernButton(b, mouseX, mouseY)
            buttonY += BUTTON_HEIGHT + BUTTON_SPACING
        }

        MainMenuStyles.drawSettingsButton(width, height, mouseX, mouseY)

        super.drawScreen(mouseX, mouseY, partialTicks)

        // 淡出转场：变黑后跳转目标界面
        val action = pendingAction
        if (action != null) {
            val t = (System.currentTimeMillis() - fadeStartMs) / FADE_DURATION_MS.toFloat()
            if (t >= 1f) {
                pendingAction = null
                action.invoke()
            } else {
                // Decelerate 减速缓动
                val alpha = 1f - (1f - t) * (1f - t) * (1f - t)
                drawRect(0, 0, width, height, Color(0, 0, 0, (alpha * 255f).toInt()).rgb)
            }
        }
    }

    private fun drawModernButton(b: MenuButton, mouseX: Int, mouseY: Int) {
        val hovered = isHovering(mouseX, mouseY, b.x, b.y, b.x + BUTTON_WIDTH, b.y + BUTTON_HEIGHT)
        val buttonColor = if (hovered) Color(78, 7, 246, 160) else Color(161, 131, 237, 120)

        RenderUtils.drawRoundedRect(b.x, b.y, b.x + BUTTON_WIDTH, b.y + BUTTON_HEIGHT, buttonColor.rgb, 12f)
        if (hovered) {
            for (i in 1..2) {
                RenderUtils.drawRoundedRect(
                    b.x - i, b.y - i, b.x + BUTTON_WIDTH + i, b.y + BUTTON_HEIGHT + i,
                    Color(255, 255, 255, 20).rgb, 12f + i
                )
            }
        }

        Fonts.fontSF40.drawCenteredString(
            b.text,
            b.x + BUTTON_WIDTH / 2f,
            b.y + (BUTTON_HEIGHT - Fonts.fontSF40.height) / 2f + 1f,
            Color(255, 255, 255, 240).rgb
        )
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (MainMenuStyles.handleSettingsClick(width, height, mouseX, mouseY, mouseButton)) {
            return
        }

        // 转场进行中忽略点击
        if (mouseButton == 0 && pendingAction == null) {
            for (b in buttons) {
                if (isHovering(mouseX, mouseY, b.x, b.y, b.x + BUTTON_WIDTH, b.y + BUTTON_HEIGHT)) {
                    pendingAction = b.action
                    fadeStartMs = System.currentTimeMillis()
                    break
                }
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun doesGuiPauseGame(): Boolean = false

    private fun isHovering(mouseX: Int, mouseY: Int, x: Float, y: Float, x2: Float, y2: Float): Boolean =
        mouseX >= x && mouseX < x2 && mouseY >= y && mouseY < y2

    companion object {
        private const val BUTTON_WIDTH = 140f
        private const val BUTTON_HEIGHT = 40f
        private const val BUTTON_SPACING = 15f
        private const val FADE_DURATION_MS = 900f
    }
}
