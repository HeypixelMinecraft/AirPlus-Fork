package net.airplus.utils.ui

import net.airplus.ui.font.Fonts
import net.airplus.utils.render.RenderUtils.drawRoundedBorderRect
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import java.awt.Color

/**
 * Modern rounded card button: dark translucent fill, brightens on hover
 * with a white outline and white text, smooth transition.
 */
class MenuButton(buttonId: Int, x: Int, y: Int, width: Int, height: Int, buttonText: String) :
    GuiButton(buttonId, x, y, width, height, buttonText) {

    private var hoverProgress = 0f

    override fun drawButton(mc: Minecraft, mouseX: Int, mouseY: Int) {
        if (!visible) {
            return
        }

        val hovered = enabled &&
                mouseX >= xPosition && mouseY >= yPosition &&
                mouseX < xPosition + width && mouseY < yPosition + height

        val target = if (hovered) 1f else 0f
        hoverProgress += (target - hoverProgress) * 0.18f
        if (hoverProgress < 0.001f) hoverProgress = 0f

        val fill = lerpColor(FILL_BASE, FILL_HOVER, hoverProgress)
        val border = lerpColor(BORDER_BASE, BORDER_HOVER, hoverProgress)
        val text = lerpColor(TEXT_BASE, TEXT_HOVER, hoverProgress)

        drawRoundedBorderRect(
            xPosition.toFloat(), yPosition.toFloat(),
            (xPosition + width).toFloat(), (yPosition + height).toFloat(),
            1f,
            fill,
            border,
            6f
        )

        val font = Fonts.fontSemibold35
        font.drawCenteredString(
            displayString,
            xPosition + width / 2f,
            yPosition + (height - font.fontHeight) / 2f,
            text,
            true
        )
    }

    private fun lerpColor(from: Int, to: Int, progress: Float): Int {
        val a = (from shr 24 and 0xFF) + ((to shr 24 and 0xFF) - (from shr 24 and 0xFF)) * progress
        val r = (from shr 16 and 0xFF) + ((to shr 16 and 0xFF) - (from shr 16 and 0xFF)) * progress
        val g = (from shr 8 and 0xFF) + ((to shr 8 and 0xFF) - (from shr 8 and 0xFF)) * progress
        val b = (from and 0xFF) + ((to and 0xFF) - (from and 0xFF)) * progress
        return Color(r.toInt().coerceIn(0, 255), g.toInt().coerceIn(0, 255), b.toInt().coerceIn(0, 255), a.toInt().coerceIn(0, 255)).rgb
    }

    private companion object {
        val FILL_BASE = Color(15, 15, 18, 150).rgb
        val FILL_HOVER = Color(255, 255, 255, 45).rgb
        val BORDER_BASE = Color(255, 255, 255, 40).rgb
        val BORDER_HOVER = Color(255, 255, 255, 170).rgb
        val TEXT_BASE = Color(190, 190, 195).rgb
        val TEXT_HOVER = Color(255, 255, 255).rgb
    }
}
