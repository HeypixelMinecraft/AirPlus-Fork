package net.airplus.utils.render

import net.airplus.file.FileManager.saveConfig
import net.airplus.file.FileManager.valuesConfig
import net.airplus.file.configs.models.ClientConfiguration.menuBackgroundIndex
import net.airplus.utils.client.MinecraftInstance.Companion.mc
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager.color
import net.minecraft.util.ResourceLocation
import javax.imageio.ImageIO

// Built-in menu backgrounds (assets/minecraft/airplus/bg/*).
// Cycles through the bundled images and persists the selection.
object MenuBackground {

    private val names = arrayOf("miku.jpeg", "miku2.jpeg", "muzimi.jpeg", "nigu.jpeg", "nina.jpeg")
    private val imageSizes = HashMap<String, Pair<Int, Int>>()

    private fun location(name: String) = ResourceLocation("minecraft", "airplus/bg/$name")

    val currentIndex: Int
        get() = menuBackgroundIndex.coerceIn(0, names.lastIndex)

    val currentName: String
        get() = names[currentIndex]

    val count: Int
        get() = names.size

    fun nextBackground() {
        menuBackgroundIndex = (currentIndex + 1) % names.size
        saveConfig(valuesConfig)
    }

    fun drawBackground(width: Int, height: Int) {
        val name = currentName
        val size = imageSizes.getOrPut(name) {
            runCatching {
                val image = ImageIO.read(mc.resourceManager.getResource(location(name)).inputStream)
                image.width to image.height
            }.getOrDefault(width to height)
        }

        // Cover the whole screen while keeping the aspect ratio
        val scale = maxOf(width / size.first.toFloat(), height / size.second.toFloat())
        val drawW = (size.first * scale).toInt().coerceAtLeast(width)
        val drawH = (size.second * scale).toInt().coerceAtLeast(height)
        val x = (width - drawW) / 2
        val y = (height - drawH) / 2

        mc.textureManager.bindTexture(location(name))
        color(1f, 1f, 1f, 1f)
        Gui.drawScaledCustomSizeModalRect(x, y, 0f, 0f, drawW, drawH, drawW, drawH, drawW.toFloat(), drawH.toFloat())
    }
}
