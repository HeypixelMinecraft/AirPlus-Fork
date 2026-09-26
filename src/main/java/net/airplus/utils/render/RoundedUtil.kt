/*
 * Air Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * skid FDP Client
 * https://github.com/SkidderMC/FDPClient
 */
package net.airplus.utils.render

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import org.lwjgl.opengl.GL11
import java.awt.Color

object RoundedUtil {

    // 预计算单位圆 sin/cos 表（0-360 度，步长 5 度），避免每帧重复 Math.sin/cos 调用
    private const val ANGLE_STEP = 5
    private const val TABLE_SIZE = 360 / ANGLE_STEP
    private val SIN_TABLE = DoubleArray(TABLE_SIZE + 1) { i ->
        Math.sin(Math.toRadians((i * ANGLE_STEP).toDouble()))
    }
    private val COS_TABLE = DoubleArray(TABLE_SIZE + 1) { i ->
        Math.cos(Math.toRadians((i * ANGLE_STEP).toDouble()))
    }

    private fun sinAt(angleDeg: Int): Double = SIN_TABLE[angleDeg / ANGLE_STEP]
    private fun cosAt(angleDeg: Int): Double = COS_TABLE[angleDeg / ANGLE_STEP]

    @JvmStatic
    fun drawRound(x: Float, y: Float, width: Float, height: Float, radius: Float, color: Color) {
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.shadeModel(GL11.GL_SMOOTH)

        val alpha = color.alpha / 255f
        val red = color.red / 255f
        val green = color.green / 255f
        val blue = color.blue / 255f

        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer

        worldRenderer.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION_TEX_COLOR)

        // 4 个圆弧：每个 90 度，步长 5 度（共 72 个顶点而非 360+）
        // 第一段：0..90
        var deg = 0
        while (deg <= 90) {
            val sinV = sinAt(deg)
            val cosV = cosAt(deg)
            val x1 = x + width - radius + sinV * radius
            val y1 = y + height - radius + cosV * radius
            worldRenderer.pos(x1, y1, 0.0).tex(0.0, 0.0).color(red, green, blue, alpha).endVertex()
            deg += ANGLE_STEP
        }

        // 第二段：90..180
        deg = 90
        while (deg <= 180) {
            val sinV = sinAt(deg)
            val cosV = cosAt(deg)
            val x1 = x + width - radius + sinV * radius
            val y1 = y + radius + cosV * radius
            worldRenderer.pos(x1, y1, 0.0).tex(0.0, 0.0).color(red, green, blue, alpha).endVertex()
            deg += ANGLE_STEP
        }

        // 第三段：180..270
        deg = 180
        while (deg <= 270) {
            val sinV = sinAt(deg)
            val cosV = cosAt(deg)
            val x1 = x + radius + sinV * radius
            val y1 = y + radius + cosV * radius
            worldRenderer.pos(x1, y1, 0.0).tex(0.0, 0.0).color(red, green, blue, alpha).endVertex()
            deg += ANGLE_STEP
        }

        // 第四段：270..360
        deg = 270
        while (deg <= 360) {
            val sinV = sinAt(deg)
            val cosV = cosAt(deg)
            val x1 = x + radius + sinV * radius
            val y1 = y + height - radius + cosV * radius
            worldRenderer.pos(x1, y1, 0.0).tex(0.0, 0.0).color(red, green, blue, alpha).endVertex()
            deg += ANGLE_STEP
        }

        tessellator.draw()

        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.shadeModel(GL11.GL_FLAT)
    }

    @JvmStatic
    fun applyGradientHorizontal(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float,
        c1: Color,
        c2: Color,
        callback: Runnable
    ) {
        GlStateManager.pushMatrix()
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.shadeModel(GL11.GL_SMOOTH)

        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer

        worldRenderer.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION_TEX_COLOR)

        val alpha1 = c1.alpha / 255f
        val red1 = c1.red / 255f
        val green1 = c1.green / 255f
        val blue1 = c1.blue / 255f
        val alpha2 = c2.alpha / 255f
        val red2 = c2.red / 255f
        val green2 = c2.green / 255f
        val blue2 = c2.blue / 255f

        var deg = 0
        while (deg <= 90) {
            val sinV = sinAt(deg)
            val cosV = cosAt(deg)
            val x1 = x + width - radius + sinV * radius
            val y1 = y + height - radius + cosV * radius
            worldRenderer.pos(x1, y1, 0.0).tex(0.0, 0.0).color(red2, green2, blue2, alpha2).endVertex()
            deg += ANGLE_STEP
        }

        deg = 90
        while (deg <= 180) {
            val sinV = sinAt(deg)
            val cosV = cosAt(deg)
            val x1 = x + width - radius + sinV * radius
            val y1 = y + radius + cosV * radius
            worldRenderer.pos(x1, y1, 0.0).tex(0.0, 0.0).color(red2, green2, blue2, alpha2).endVertex()
            deg += ANGLE_STEP
        }

        deg = 180
        while (deg <= 270) {
            val sinV = sinAt(deg)
            val cosV = cosAt(deg)
            val x1 = x + radius + sinV * radius
            val y1 = y + radius + cosV * radius
            worldRenderer.pos(x1, y1, 0.0).tex(0.0, 0.0).color(red1, green1, blue1, alpha1).endVertex()
            deg += ANGLE_STEP
        }

        deg = 270
        while (deg <= 360) {
            val sinV = sinAt(deg)
            val cosV = cosAt(deg)
            val x1 = x + radius + sinV * radius
            val y1 = y + height - radius + cosV * radius
            worldRenderer.pos(x1, y1, 0.0).tex(0.0, 0.0).color(red1, green1, blue1, alpha1).endVertex()
            deg += ANGLE_STEP
        }

        tessellator.draw()

        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.shadeModel(GL11.GL_FLAT)
        GlStateManager.popMatrix()

        callback.run()
    }
}
