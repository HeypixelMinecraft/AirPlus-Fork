/*
 * Air Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 */
package net.airplus.features.module.modules.render

import net.airplus.event.JumpEvent
import net.airplus.event.Render3DEvent
import net.airplus.event.WorldEvent
import net.airplus.event.handler
import net.airplus.features.module.Category
import net.airplus.features.module.Module
import net.airplus.utils.client.ClientThemesUtils
import net.airplus.utils.client.ClientUtils.runTimeTicks
import net.airplus.utils.extensions.lerpWith
import net.airplus.utils.render.ColorUtils.shiftHue
import net.airplus.utils.render.ColorUtils.withAlpha
import net.airplus.utils.render.RenderUtils.customRotatedObject2D
import net.airplus.utils.render.RenderUtils.drawHueCircle
import net.airplus.utils.render.RenderUtils.setupDrawCircles
import net.airplus.utils.render.animation.AnimationUtil.easeInOutElasticx
import net.airplus.utils.render.animation.AnimationUtil.easeInOutExpo
import net.airplus.utils.render.animation.AnimationUtil.easeOutBounce
import net.airplus.utils.render.animation.AnimationUtil.easeOutCirc
import net.airplus.utils.render.animation.AnimationUtil.easeOutElasticX
import net.airplus.utils.render.animation.AnimationUtil.easeWave
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.Entity
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraft.util.ResourceLocation
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11.*
import java.awt.Color

object JumpCircle : Module("JumpCircle", Category.RENDER, gameDetecting = false) {
    private val colorMode by choices("Color", arrayOf("Custom", "Theme"), "Theme")
    private val circleRadius by floatRange("CircleRadius", 0.15F..0.8F, 0F..3F)
    private val innerColor = color("InnerColor", Color(0, 0, 0, 50)) { colorMode == "Custom" }
    private val outerColor = color("OuterColor", Color(0, 111, 255, 255)) { colorMode == "Custom" }
    private val hueOffsetAnim by int("HueOffsetAnim", 63, -360..360)
    private val lifeTime by int("LifeTime", 20, 1..50, "Ticks")
    private val blackHole by boolean("BlackHole", false)
    private val useTexture by boolean("UseTexture", true)
    private val texture by choices("Texture", arrayOf("Supernatural", "Circle"), "Supernatural") { useTexture }
    private val deepestLight by boolean("Deepest Light", true) { useTexture }

    private val staticLoc = ResourceLocation("airplus/textures/jumpcircle/default")

    private val circleIcon = ResourceLocation("$staticLoc/circle1.png")
    private val supernaturalIcon = ResourceLocation("$staticLoc/circle2.png")

    // onUpdate/onRender3D/onWorld 均在主线程触发，无需 CopyOnWriteArrayList
    private val circles: MutableList<JumpData> = mutableListOf()
    // 复用过滤缓冲，避免每帧分配新的 ArrayList
    private val circlesRemaining: MutableList<JumpData> = mutableListOf()
    private var hasJumped = false

    private val tessellator = Tessellator.getInstance()
    private val worldRenderer = tessellator.worldRenderer
    // 缓存上次设置 blur/mipmap 的纹理，避免每帧重复调用 setBlurMipmap
    private var lastBlurTexture: ResourceLocation? = null

    private fun selectJumpTexture(): ResourceLocation = when (texture) {
        "Circle" -> circleIcon
        else -> supernaturalIcon
    }

    private fun createCircleForEntity(entity: Entity) {
        var entityPos = calculateEntityPosition(entity).addVector(0.0, 0.005, 0.0)
        val position = BlockPos(entityPos)
        if (mc.theWorld.getBlockState(position).block == Blocks.snow) {
            entityPos = entityPos.addVector(0.0, 0.125, 0.0)
        }
        circles += JumpData(entityPos, runTimeTicks + if (blackHole) lifeTime else 0)
    }

    val onJump = handler<JumpEvent> {
        hasJumped = true
    }

    val onRender3D = handler<Render3DEvent> {
        if(mc.thePlayer.onGround && hasJumped){
            createCircleForEntity(mc.thePlayer)
            hasJumped = false
        }
        if(circles.isEmpty()) return@handler
        val partialTick = it.partialTicks
        val lightFactor = if (deepestLight) 1f else 0f
        val effectStrength = when {
            lightFactor >= 1f / 255f -> when (texture) {
                "Circle" -> 0.1f
                "Supernatural" -> 0.075f
                else -> 0f
            }
            else -> 0f
        }
        // 保存渲染前的混合/光照状态，渲染结束后精确还原。
        // 之前无条件 disableBlend + enableLighting 会污染后续渲染状态：
        // 开启固定管线光照后，之后绘制的半透明元素（如 Scaffold 的标记、第三人称下的界面）
        // 颜色会被环境光衰减而变灰。
        val blendEnabled = glIsEnabled(GL_BLEND)
        val lightingEnabled = glIsEnabled(GL_LIGHTING)
        setupDrawCircles {
            // 复用缓冲：先渲染所有圆，再一次性重建列表，避免每帧分配新的 ArrayList
            circlesRemaining.clear()
            var anyExpired = false
            circles.forEach {
                val progress = ((runTimeTicks + partialTick) - it.endTime) / lifeTime
                if (progress >= 1F) {
                    anyExpired = true
                    return@forEach
                }
                val radius = circleRadius.lerpWith(progress)
                if(useTexture){
                    renderTexturedCircle(
                        it.pos,
                        radius.toDouble(),
                        1f - progress,
                        lightFactor,
                        effectStrength
                    )
                } else {
                    renderSimpleCircle(it.pos, radius, progress)
                }
                circlesRemaining.add(it)
            }
            if (anyExpired) {
                circles.clear()
                circles.addAll(circlesRemaining)
            }
        }
        GlStateManager.color(1f, 1f, 1f, 1f)
        GlStateManager.depthMask(true)
        if (blendEnabled) GlStateManager.enableBlend() else GlStateManager.disableBlend()
        if (lightingEnabled) GlStateManager.enableLighting() else GlStateManager.disableLighting()
    }

    override fun onDisable() {
        circles.clear()
        lastBlurTexture = null
    }

    val onWorld = handler<WorldEvent> {
        circles.clear()
        lastBlurTexture = null
    }

    private fun renderSimpleCircle(pos: Vec3, radius: Float, timeFraction: Float){
        val (color, color2) = when (colorMode) {
            "Theme" -> {
                val baseColor = ClientThemesUtils.getColor()
                val inner = baseColor.withAlpha((baseColor.alpha * (1 - timeFraction)).toInt().coerceIn(0, 255))
                val outer = baseColor.withAlpha((baseColor.alpha * (1 - timeFraction)).toInt().coerceIn(0, 255))
                Pair(inner, outer)
            }
            else -> {
                val inner = animateColor(innerColor.selectedColor(), 1f - timeFraction)
                val outer = animateColor(outerColor.selectedColor(), 1f - timeFraction)
                Pair(inner,outer)
            }
        }
        drawHueCircle(
            pos,
            radius,
            color,
            color2
        )
        GlStateManager.color(1f, 1f, 1f, 1f)
    }

    private fun renderTexturedCircle(pos: Vec3, maxRadius: Double, timeFraction: Float, shift: Float, intensity: Float) {
        val waveValue = 1f - timeFraction
        val wave = easeWave(waveValue)
        var alphaFraction = easeOutCirc(wave.toDouble()).toFloat()
        if (timeFraction < 0.5f) alphaFraction *= easeInOutExpo(alphaFraction.toDouble()).toFloat()
        val mainFactor = if (timeFraction > 0.5f) {
            easeOutElasticX((wave * wave).toDouble())
        } else {
            easeOutBounce(wave.toDouble())
        }
        val circleRadius = (mainFactor * maxRadius).toFloat()
        val rotation = (easeInOutElasticx(wave.toDouble()) * 90.0 / (1.0 + wave.toDouble()))
        val textureResource = selectJumpTexture()
        val (color, color2) = when (colorMode) {
            "Theme" -> {
                val baseColor = ClientThemesUtils.getColor()
                val inner = baseColor.withAlpha((baseColor.alpha * (1 - timeFraction)).toInt().coerceIn(0, 255))
                val outer = baseColor.withAlpha((baseColor.alpha * (1 - timeFraction)).toInt().coerceIn(0, 255))
                Pair(inner, outer)
            }
            else -> {
                val inner = animateColor(innerColor.selectedColor(), 1f - timeFraction)
                val outer = animateColor(outerColor.selectedColor(), 1f - timeFraction)
                Pair(inner,outer)
            }
        }
        val red = ((color.rgb shr 16) and 0xFF) / 255f
        val green = ((color.rgb shr 8) and 0xFF) / 255f
        val blue = (color.rgb and 0xFF) / 255f
        val alpha = ((color.rgb shr 24) and 0xFF) / 255f
        val red2 = ((color2.rgb shr 16) and 0xFF) / 255f
        val green2 = ((color2.rgb shr 8) and 0xFF) / 255f
        val blue2 = (color2.rgb and 0xFF) / 255f
        val alpha2 = ((color2.rgb shr 24) and 0xFF) / 255f
        mc.textureManager.bindTexture(textureResource)
        // 仅在纹理切换时调用 setBlurMipmap，避免每帧重复 GL 调用
        if (lastBlurTexture != textureResource) {
            mc.textureManager.getTexture(textureResource).setBlurMipmap(true, true)
            lastBlurTexture = textureResource
        }
        GlStateManager.pushMatrix()
        GlStateManager.translate(pos.xCoord - circleRadius / 2.0, pos.yCoord, pos.zCoord - circleRadius / 2.0)
        GlStateManager.rotate(90f, 1f, 0f, 0f)
        customRotatedObject2D(0f, 0f, circleRadius, circleRadius, rotation)
        worldRenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR)
        worldRenderer.pos(0.0, 0.0, 0.0).tex(0.0, 0.0).color(red, green, blue, alpha).endVertex()
        worldRenderer.pos(0.0, circleRadius.toDouble(), 0.0).tex(0.0, 1.0).color(red, green, blue, alpha).endVertex()
        worldRenderer.pos(circleRadius.toDouble(), circleRadius.toDouble(), 0.0).tex(1.0, 1.0).color(red, green, blue, alpha).endVertex()
        worldRenderer.pos(circleRadius.toDouble(), 0.0, 0.0).tex(1.0, 0.0).color(red, green, blue, alpha).endVertex()
        tessellator.draw()
        GlStateManager.color(1f, 1f, 1f, 1f)
        GlStateManager.popMatrix()
        if (shift >= 1f / 255f) {
            GlStateManager.pushMatrix()
            GlStateManager.translate(pos.xCoord, pos.yCoord, pos.zCoord)
            GlStateManager.rotate(rotation.toFloat(), 0f, 1f, 0f)
            worldRenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR)
            val polygons = 40
            val maxY = circleRadius / 3.5f
            val maxXZ = circleRadius / 7f
            for (i in 1 until polygons) {
                val fraction = i / polygons.toFloat()
                val fractionValue = fraction - (1.5f / polygons)
                val wave2 = easeWave(fractionValue)
                val circVal = easeOutCirc(wave2.toDouble()).toFloat()
                val alphaCheck = (alphaFraction * intensity * shift).takeIf { it * 255 >= 1 } ?: continue
                val alphaInt = (alphaCheck * 255).toInt()
                val variedRadius = circleRadius + circVal * maxXZ
                worldRenderer.pos((-variedRadius / 2f).toDouble(), (maxY * i / polygons - maxY / polygons).toDouble(), (-variedRadius / 2f).toDouble())
                    .tex(0.0, 0.0).color(red2, green2, blue2, alphaInt / 255f).endVertex()
                worldRenderer.pos((-variedRadius / 2f).toDouble(), (maxY * i / polygons - maxY / polygons).toDouble(), (variedRadius / 2f).toDouble())
                    .tex(0.0, 1.0).color(red2, green2, blue2, alphaInt / 255f).endVertex()
                worldRenderer.pos((variedRadius / 2f).toDouble(), (maxY * i / polygons - maxY / polygons).toDouble(), (variedRadius / 2f).toDouble())
                    .tex(1.0, 1.0).color(red2, green2, blue2, alphaInt / 255f).endVertex()
                worldRenderer.pos((variedRadius / 2f).toDouble(), (maxY * i / polygons - maxY / polygons).toDouble(), (-variedRadius / 2f).toDouble())
                    .tex(1.0, 0.0).color(red2, green2, blue2, alphaInt / 255f).endVertex()
            }
            tessellator.draw()
            GlStateManager.color(1f, 1f, 1f, 1f)
            GlStateManager.popMatrix()
        }
    }
    private fun animateColor(baseColor: Color, progress: Float): Color {
        val color = baseColor.withAlpha((baseColor.alpha * (1 - progress)).toInt().coerceIn(0, 255))
        if (hueOffsetAnim == 0) {
            return color
        }
        return shiftHue(color, (hueOffsetAnim * progress).toInt())
    }
    private fun calculateEntityPosition(entity: Entity): Vec3 {
        val partialTicks = mc.timer.renderPartialTicks
        val dx = entity.posX - entity.lastTickPosX
        val dy = entity.posY - entity.lastTickPosY
        val dz = entity.posZ - entity.lastTickPosZ
        return Vec3(
            entity.lastTickPosX + dx * partialTicks + dx * 2.0,
            entity.lastTickPosY + dy * partialTicks,
            entity.lastTickPosZ + dz * partialTicks + dz * 2.0
        )
    }
    data class JumpData(val pos: Vec3, val endTime: Int)
}
