package net.airplus.utils.render

import net.airplus.utils.client.MinecraftInstance
import net.airplus.utils.render.RenderUtils.RoundedCorners
import net.airplus.utils.render.shader.KawaseBlur
import net.airplus.utils.render.shader.ShaderSupport
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.opengl.GL11.glLoadIdentity
import org.lwjgl.opengl.GL11.glMatrixMode
import org.lwjgl.opengl.GL11.glPopMatrix
import org.lwjgl.opengl.GL11.glPushMatrix
import org.lwjgl.opengl.GL11.GL_MODELVIEW

/**
 * 统一的 HUD 模糊入口。
 *
 * [blur] 的 x1/y1/x2/y2 为缩放 GUI 坐标系（ScaledResolution 空间）下的屏幕绝对坐标；
 * [mask] 在调用方当前 GL 矩阵下绘制模糊遮罩形状（可传局部坐标的圆角矩形等自定义形状），
 * 为 null 时按绝对坐标绘制圆角矩形（要求调用方当前矩阵为单位阵）。
 *
 * 模糊范围始终由 x1..y2（AABB）决定，形状由 [mask]（配合 stencil）决定。
 */
object HudBlur : MinecraftInstance {

    val MODES = arrayOf("InternalBlur", "Kawase", "ShaderGroup", "Gaussian", "Dual")

    fun blur(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        strength: Float,
        mode: String,
        radius: Float = 0F,
        corners: RoundedCorners = RoundedCorners.ALL,
        mask: (() -> Unit)? = null
    ) {
        if (!ShaderSupport.isSupported) return

        val left = minOf(x1, x2)
        val right = maxOf(x1, x2)
        val top = minOf(y1, y2)
        val bottom = maxOf(y1, y2)
        val width = right - left
        val height = bottom - top

        if (width <= 0F || height <= 0F) return

        // push/pop 保护当前矩阵：自定义遮罩可能修改矩阵（如 Target2 的动画变换），
        // 若不恢复会导致后续渲染（如 drawTarget）落在错误的变换上
        val drawMask: () -> Unit = {
            glPushMatrix()
            try {
                mask?.invoke() ?: drawDefaultMask(left, top, right, bottom, radius, corners)
            } finally {
                glPopMatrix()
            }
        }

        when (mode) {
            "ShaderGroup" -> BlurUtils.blur(left, top, right, bottom, strength, false, drawMask)

            "InternalBlur" -> {
                EmbeddedStencil.write(false)
                try {
                    // mask 绘制异常时也要恢复 colorMask/depthMask（erase 内部会还原）
                    try {
                        drawMask()
                    } finally {
                        EmbeddedStencil.erase(true)
                    }
                    InternalBlurShader.blurArea(left, top, width, height, strength)
                } finally {
                    // 异常路径同样还原 stencil 状态，避免残留 GL_EQUAL 导致后续渲染错乱
                    EmbeddedStencil.dispose()
                }
            }

            "Gaussian", "Dual" -> {
                val blurMode = if (mode == "Gaussian") BlurEffects.BlurMode.GAUSSIAN else BlurEffects.BlurMode.DUAL

                EmbeddedStencil.write(false)
                try {
                    try {
                        drawMask()
                    } finally {
                        EmbeddedStencil.erase(true)
                    }
                    BlurEffects.blurArea(left, top, width, height, strength, blurMode)
                } finally {
                    EmbeddedStencil.dispose()
                }
            }

            "Kawase" -> {
                val sr = ScaledResolution(mc)
                val factor = sr.scaleFactor
                val iterations = (strength / 3F).toInt().coerceIn(1, 5)
                val offset = (strength / iterations).toInt().coerceIn(1, 6)

                EmbeddedStencil.write(false)
                try {
                    try {
                        drawMask()
                    } finally {
                        EmbeddedStencil.erase(true)
                    }

                    // KawaseBlur 内部 ShaderUtil.drawQuads 依赖当前 modelview，必须重置为单位阵
                    glMatrixMode(GL_MODELVIEW)
                    glPushMatrix()
                    glLoadIdentity()
                    KawaseBlur.renderBlurScissor(
                        iterations,
                        offset,
                        (left * factor).toInt(),
                        mc.displayHeight - (bottom * factor).toInt(),
                        (width * factor).toInt(),
                        (height * factor).toInt()
                    )
                    glPopMatrix()
                } finally {
                    EmbeddedStencil.dispose()
                }
            }
        }
    }

    private fun drawDefaultMask(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        radius: Float,
        corners: RoundedCorners
    ) {
        RenderUtils.drawRoundedRect(x1, y1, x2, y2, -1, radius, corners)
    }
}
