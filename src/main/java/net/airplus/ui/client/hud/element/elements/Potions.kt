/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 *
 * Ported from Hanabi's HUD potion status rendering (HUD#renderPotionStatus).
 * Replaces the old "Effects" element. Shows a progress bar per active potion
 * effect with the vanilla status icon, effect name and remaining duration.
 * Stacks upward from the element anchor (default: bottom right, above the hotbar).
 */
package net.airplus.ui.client.hud.element.elements

import net.airplus.ui.font.AWTFontRenderer.Companion.assumeNonVolatile
import net.airplus.ui.font.Fonts
import net.airplus.ui.client.hud.element.Border
import net.airplus.ui.client.hud.element.Element
import net.airplus.ui.client.hud.element.ElementInfo
import net.airplus.ui.client.hud.element.Side
import net.airplus.utils.render.HudBlur
import net.airplus.utils.render.RenderUtils.drawRect
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.resources.I18n
import net.minecraft.potion.Potion
import net.minecraft.util.ResourceLocation
import java.awt.Color
import kotlin.math.max

@ElementInfo(name = "Potions", single = true)
class Potions(
    x: Double = 115.0, y: Double = 60.0, scale: Float = 1F,
    side: Side = Side(Side.Horizontal.RIGHT, Side.Vertical.DOWN)
) : Element("Potions", x, y, scale, side) {

    private val nameColor by color("Color", Color(33, 170, 47))
    private val fontRenderer by font("Font", Fonts.minecraftFont)
    private val blur by boolean("Blur", false)
    private val blurStrength by float("Blur-Strength", 8f, 1f..20f) { blur }

    // Longest seen duration per potion id, so the bar doesn't reset on re-applied effects
    private val potionMaxDurations = mutableMapOf<Int, Int>()

    override fun drawElement(): Border {
        val player = mc.thePlayer ?: return Border(0f, -ROW_HEIGHT, BAR_WIDTH, 0f)
        val effects = player.activePotionEffects

        // Clean up effects that are gone and track the max seen duration per potion
        potionMaxDurations.keys.removeAll { id ->
            val potion = Potion.potionTypes.getOrNull(id)
            potion == null || player.getActivePotionEffect(potion) == null
        }
        for (effect in effects) {
            val max = potionMaxDurations[effect.potionID]
            if (max == null || max < effect.duration) potionMaxDurations[effect.potionID] = effect.duration
        }

        // Optional blur behind the potion rows (absolute AABB: screen = scale * (render + local))
        val rowsTop = -ROW_HEIGHT - max(0, effects.size - 1) * ROW_SPACING
        if (blur && effects.isNotEmpty()) {
            val s = scale
            val originX = renderX.toFloat()
            val originY = renderY.toFloat()
            HudBlur.blur(
                originX * s, (originY + rowsTop) * s,
                (originX + BAR_WIDTH) * s, originY * s,
                blurStrength, "InternalBlur"
            ) {
                drawRect(0f, rowsTop, BAR_WIDTH, 0f, -1)
            }
        }

        assumeNonVolatile {
            for ((i, effect) in effects.withIndex()) {
                val potion = Potion.potionTypes[effect.potionID] ?: continue
                // Rows stack upward from the anchor (like Hanabi: x -= 35)
                val rowTop = -ROW_HEIGHT - i * ROW_SPACING
                val maxDuration = potionMaxDurations[effect.potionID] ?: effect.duration
                val ratio = (effect.duration / maxDuration.toFloat()).coerceIn(0f, 1f)
                val progressX = BAR_WIDTH * ratio

                // Black progress bar + grey remainder (Hanabi style)
                drawRect(0f, rowTop, progressX, rowTop + ROW_HEIGHT, Color(0, 0, 0, 100).rgb)
                drawRect(progressX, rowTop, BAR_WIDTH, rowTop + ROW_HEIGHT, Color(50, 50, 50, 100).rgb)

                // Vanilla status icon
                if (potion.hasStatusIcon()) {
                    GlStateManager.color(1f, 1f, 1f, 1f)
                    mc.textureManager.bindTexture(ResourceLocation("textures/gui/container/inventory.png"))
                    val iconIndex = potion.statusIconIndex
                    Gui().drawTexturedModalRect(
                        4, (rowTop + 6f).toInt(),
                        iconIndex % 8 * 18, 198 + iconIndex / 8 * 18, 18, 18
                    )
                }

                // Effect name with roman numeral amplifier
                var potionName = I18n.format(potion.getName()).replace(Regex("§."), "")
                potionName += when (effect.amplifier) {
                    0 -> " I"
                    1 -> " II"
                    2 -> " III"
                    3 -> " IV"
                    else -> " " + (effect.amplifier + 1)
                }
                fontRenderer.drawStringWithShadow(potionName, 30f, rowTop + 5f, nameColor.rgb)

                // Remaining duration
                val duration = Potion.getDurationString(effect).replace(Regex("§."), "")
                fontRenderer.drawStringWithShadow(duration, 30f, rowTop + 17f, Color(255, 255, 255, 204).rgb)
            }
        }

        val bottom = if (effects.isEmpty()) 0f else -1f
        val top = if (effects.isEmpty()) -ROW_HEIGHT else (-ROW_HEIGHT - (effects.size - 1) * ROW_SPACING)
        return Border(0f, top, BAR_WIDTH, bottom)
    }

    companion object {
        private const val BAR_WIDTH = 110f
        private const val ROW_HEIGHT = 30f
        private const val ROW_SPACING = 35f
    }
}
