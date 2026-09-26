/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.utils.render.shader

import net.airplus.AirPlus.CLIENT_NAME
import net.airplus.utils.client.MinecraftInstance.Companion.mc
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager.color
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.util.ResourceLocation
import java.io.File

sealed class Background(val backgroundFile: File) {
    companion object {
        fun fromFile(backgroundFile: File): Background {
            val background: Background = when (backgroundFile.extension) {
                "png" -> ImageBackground(backgroundFile)
                else -> throw IllegalArgumentException("Invalid background file extension")
            }
            background.initBackground()
            return background
        }
    }

    protected abstract fun initBackground()

    abstract fun drawBackground(width: Int, height: Int)
}

private class ImageBackground(backgroundFile: File) : Background(backgroundFile) {

    private val resourceLocation = ResourceLocation("${CLIENT_NAME.lowercase()}/background.png")

    override fun initBackground() {
        try {
            val image = javax.imageio.ImageIO.read(backgroundFile.inputStream())
            mc.textureManager.loadTexture(resourceLocation, DynamicTexture(image))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun drawBackground(width: Int, height: Int) {
        mc.textureManager.bindTexture(resourceLocation)
        color(1f, 1f, 1f, 1f)
        Gui.drawScaledCustomSizeModalRect(0, 0, 0f, 0f, width, height, width, height, width.toFloat(), height.toFloat())
    }
}
