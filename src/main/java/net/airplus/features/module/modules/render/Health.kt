/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.render

import net.airplus.event.Render2DEvent
import net.airplus.event.handler
import net.airplus.features.module.Category
import net.airplus.features.module.Module
import net.airplus.utils.render.ColorUtils.blendColors
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.gui.inventory.GuiContainerCreative
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.potion.Potion
import net.minecraft.util.MathHelper
import java.awt.Color
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import java.util.Random

/**
 * 从 SilenceFix 迁移：血量显示（数值 + 原版风格血条）。
 * 打开背包/箱子时渲染在容器界面上，否则渲染在屏幕下方（HUD）。
 */
object Health : Module("Health", Category.RENDER) {

    private val decimalFormat = DecimalFormat("0.#", DecimalFormatSymbols(Locale.ENGLISH))
    private val random = Random()
    private val gui = Gui()
    private var width = 0

    val onRender2D = handler<Render2DEvent> {
        if (mc.thePlayer != null && mc.theWorld != null) {
            renderHealth()
        }
    }

    private fun renderHealth() {
        val player = mc.thePlayer ?: return
        val world = mc.theWorld ?: return

        val scaledResolution = ScaledResolution(mc)
        val screen = mc.currentScreen
        val absorptionHealth = player.absorptionAmount

        val text = decimalFormat.format(player.health / 2.0f) + "§c❤ " +
                (if (absorptionHealth <= 0.0f) "" else "§e" + decimalFormat.format(absorptionHealth / 2.0f) + "§6❤")

        if (player.health >= 0.0f && player.health < 10.0f || player.health >= 10.0f && player.health < 100.0f) {
            width = 3
        }

        // 渲染位置：背包/箱子界面内或屏幕下方
        // GuiChest: ySize = 114 + inventoryRows * 18（ySize 未公开，用 inventoryRows 等价计算）
        val offsetY = when (screen) {
            is GuiInventory -> 70
            is GuiContainerCreative -> 80
            is GuiChest -> 42 + screen.inventoryRows * 9
            else -> 0
        }

        val x2 = scaledResolution.scaledWidth / 2 - width
        val y2 = scaledResolution.scaledHeight / 2 + 25 + offsetY

        val color = blendColors(
            floatArrayOf(0.0f, 0.5f, 1.0f),
            arrayOf(Color(255, 37, 0), Color.YELLOW, Color.GREEN),
            player.health / player.maxHealth
        )

        val textX = if (absorptionHealth > 0.0f) x2 - 15.5f else x2 - 3.5f
        mc.fontRendererObj.drawString(text, textX, y2.toFloat(), color.rgb, true)

        GlStateManager.pushMatrix()
        mc.textureManager.bindTexture(Gui.icons)
        random.setSeed(mc.ingameGUI.updateCounter * 312871L)

        val barWidth = scaledResolution.scaledWidth / 2.0f - player.maxHealth / 2.5f * 10.0f / 2.0f
        val maxHealth = player.maxHealth
        val lastPlayerHealth = mc.ingameGUI.lastPlayerHealth
        val healthInt = MathHelper.ceiling_float_int(player.health)
        var regenIndex = -1
        val flash = mc.ingameGUI.healthUpdateCounter > mc.ingameGUI.updateCounter &&
                (mc.ingameGUI.healthUpdateCounter - mc.ingameGUI.updateCounter) / 3L % 2L == 1L

        if (player.isPotionActive(Potion.regeneration)) {
            regenIndex = mc.ingameGUI.updateCounter % MathHelper.ceiling_float_int(maxHealth + 5.0f)
        }

        var i = MathHelper.ceiling_float_int(maxHealth / 2.0f) - 1
        while (i >= 0) {
            var xOffset = 16
            if (player.isPotionActive(Potion.poison)) {
                xOffset += 36
            } else if (player.isPotionActive(Potion.wither)) {
                xOffset += 72
            }

            val k = if (flash) 1 else 0
            var renX = barWidth + (i % 10 * 8)
            var renY = scaledResolution.scaledHeight / 2.0f + 15.0f + offsetY

            if (healthInt <= 4) {
                renY += random.nextInt(2)
            }
            if (i == regenIndex) {
                renY -= 2.0f
            }

            val yOffset = if (world.worldInfo.isHardcoreModeEnabled) 5 else 0

            gui.drawTexturedModalRect(renX, renY, 16 + k * 9, 9 * yOffset, 9, 9)
            if (flash) {
                if (i * 2 + 1 < lastPlayerHealth) {
                    gui.drawTexturedModalRect(renX, renY, xOffset + 54, 9 * yOffset, 9, 9)
                }
                if (i * 2 + 1 == lastPlayerHealth) {
                    gui.drawTexturedModalRect(renX, renY, xOffset + 63, 9 * yOffset, 9, 9)
                }
            }
            if (i * 2 + 1 < healthInt) {
                gui.drawTexturedModalRect(renX, renY, xOffset + 36, 9 * yOffset, 9, 9)
            }
            if (i * 2 + 1 == healthInt) {
                gui.drawTexturedModalRect(renX, renY, xOffset + 45, 9 * yOffset, 9, 9)
            }
            i--
        }
        GlStateManager.popMatrix()
    }
}
