package net.airplus.ui.client

import net.airplus.lang.translationButton
import net.airplus.lang.translationMenu
import net.airplus.ui.font.Fonts
import net.airplus.utils.ui.AbstractScreen
import net.airplus.utils.ui.MenuButton
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiOptions
import net.minecraft.client.gui.GuiScreen

/**
 * Second-level settings menu of the main menu:
 * - Minecraft settings (vanilla options screen)
 * - Client configuration (old configuration screen)
 */
class GuiSettingsMenu(val prevGui: GuiScreen) : AbstractScreen() {

    override fun initGui() {
        val buttonWidth = 200
        val x = width / 2 - buttonWidth / 2
        var y = height / 4 + 30

        +MenuButton(1, x, y, buttonWidth, 24, translationMenu("gameSettings"))
        y += 28
        +MenuButton(2, x, y, buttonWidth, 24, translationMenu("configuration"))
        y += 28

        +MenuButton(0, x, y, buttonWidth, 24, translationButton("back"))
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawBackground(0)

        Fonts.fontBold180.drawCenteredString(
            translationMenu("settings"), width / 2F, height / 8F, 4673984, true
        )

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            0 -> mc.displayGuiScreen(prevGui)
            1 -> mc.displayGuiScreen(GuiOptions(this, mc.gameSettings))
            2 -> mc.displayGuiScreen(GuiClientConfiguration(this))
        }
    }
}
