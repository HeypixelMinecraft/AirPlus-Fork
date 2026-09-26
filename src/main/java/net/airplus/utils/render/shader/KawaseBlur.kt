// skid xylitol
package net.airplus.utils.render.shader

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.shader.Framebuffer
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL14

/**
 * Kawase Blur 实现
 * 迁移自 xylitol 客户端
 */
object KawaseBlur {
    private val mc = Minecraft.getMinecraft()

    private var kawaseDown: ShaderUtil? = null
    private var kawaseUp: ShaderUtil? = null

    @JvmField
    var framebuffer = Framebuffer(1, 1, false)

    private var currentIterations = 0
    private val framebufferList = mutableListOf<Framebuffer>()

    /** 着色器初始化失败后不再每帧重试（避免重复创建 GL 对象与报错刷屏） */
    private var initFailed = false

    private fun initShaders() {
        if (initFailed) return
        if (kawaseDown == null) {
            try { kawaseDown = ShaderUtil("kawaseDown") } catch (e: Exception) {
                e.printStackTrace()
                initFailed = true
            }
        }
        if (kawaseUp == null && !initFailed) {
            try { kawaseUp = ShaderUtil("kawaseUp") } catch (e: Exception) {
                e.printStackTrace()
                initFailed = true
            }
        }
    }

    fun setupUniforms(offset: Float) {
        kawaseDown?.setUniformf("offset", offset, offset)
        kawaseUp?.setUniformf("offset", offset, offset)
    }
    
    private fun initFramebuffers(iterations: Float) {
        // 清理旧的 framebuffer
        framebufferList.forEach { it.deleteFramebuffer() }
        framebufferList.clear()
        
        // 创建主 framebuffer (index 0)
        framebuffer = createFrameBuffer(framebuffer)
        framebufferList.add(framebuffer)
        
        // 创建多级 framebuffer (index 1 到 iterations+1)
        // 需要 iterations+1 个额外的framebuffer来支持 downsample 和 upsample
        // 因为 downsample 会访问到 framebufferList[iterations+1]
        for (i in 1..iterations.toInt() + 1) {
            val width = (mc.displayWidth / Math.pow(2.0, i.toDouble())).toInt()
            val height = (mc.displayHeight / Math.pow(2.0, i.toDouble())).toInt()
            val currentBuffer = Framebuffer(width, height, false)
            currentBuffer.setFramebufferFilter(GL11.GL_LINEAR)
            
            GlStateManager.bindTexture(currentBuffer.framebufferTexture)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL14.GL_MIRRORED_REPEAT)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL14.GL_MIRRORED_REPEAT)
            GlStateManager.bindTexture(0)
            
            framebufferList.add(currentBuffer)
        }
    }
    
    fun renderBlur(stencilFrameBufferTexture: Int, iterations: Int, offset: Int) {
        initShaders()
        val down = kawaseDown
        val up = kawaseUp
        if (down == null || up == null) {
            System.err.println("KawaseBlur shaders not initialized, skipping blur render")
            return
        }
        if (currentIterations != iterations || framebuffer.framebufferWidth != mc.displayWidth || framebuffer.framebufferHeight != mc.displayHeight) {
            initFramebuffers(iterations.toFloat())
            currentIterations = iterations
        }

        // 第一次 downsample
        renderFBO(framebufferList[1], mc.framebuffer.framebufferTexture, down, offset.toFloat())

        // Downsample 阶段
        for (i in 1 until iterations) {
            renderFBO(framebufferList[i + 1], framebufferList[i].framebufferTexture, down, offset.toFloat())
        }

        // Upsample 阶段
        for (i in iterations downTo 2) {
            renderFBO(framebufferList[i - 1], framebufferList[i].framebufferTexture, up, offset.toFloat())
        }

        // 最后一次 upsample，带 stencil 检查
        val lastBuffer = framebufferList[0]
        lastBuffer.framebufferClear()
        lastBuffer.bindFramebuffer(false)
        up.init()
        up.setUniformf("offset", offset.toFloat(), offset.toFloat())
        up.setUniformi("inTexture", 0)
        up.setUniformi("check", 1)
        up.setUniformi("textureToCheck", 16)
        up.setUniformf("halfpixel", 1.0f / lastBuffer.framebufferWidth, 1.0f / lastBuffer.framebufferHeight)
        up.setUniformf("iResolution", lastBuffer.framebufferWidth.toFloat(), lastBuffer.framebufferHeight.toFloat())

        GL13.glActiveTexture(GL13.GL_TEXTURE16)
        bindTexture(stencilFrameBufferTexture)
        GL13.glActiveTexture(GL13.GL_TEXTURE0)
        bindTexture(framebufferList[1].framebufferTexture)
        ShaderUtil.drawQuads()
        up.unload()

        // 渲染到主 framebuffer
        mc.framebuffer.bindFramebuffer(true)
        bindTexture(framebufferList[0].framebufferTexture)
        setAlphaLimit(0f)
        startBlend()
        ShaderUtil.drawQuads()
        GlStateManager.bindTexture(0)
    }
    
    /**
     * 渲染指定区域的模糊效果
     * 注意：此方法会先进行全屏模糊，然后在最终渲染到主framebuffer时使用scissor限制区域
     * @param x 区域左上角 X 坐标（像素坐标）
     * @param y 区域左上角 Y 坐标（像素坐标）
     * @param width 区域宽度（像素坐标）
     * @param height 区域高度（像素坐标）
     */
    fun renderBlur(stencilFrameBufferTexture: Int, iterations: Int, offset: Int, x: Int, y: Int, width: Int, height: Int) {
        initShaders()
        val down = kawaseDown
        val up = kawaseUp
        if (down == null || up == null) {
            System.err.println("KawaseBlur shaders not initialized, skipping blur render")
            return
        }
        // 检查是否需要重新初始化framebuffers（iterations改变或尺寸改变）
        // initFramebuffers 会创建 iterations+2 个（主 FBO + iterations+1 个半分辨率）
        val needsReinit = framebufferList.isEmpty() ||
                         framebuffer.framebufferWidth != mc.displayWidth ||
                         framebuffer.framebufferHeight != mc.displayHeight ||
                         framebufferList.size != iterations + 2

        if (needsReinit) {
            initFramebuffers(iterations.toFloat())
            currentIterations = iterations
        }

        // 保存将被修改的 GL 状态，结束时恢复，避免状态泄漏（奇怪的纹理 / 透明度异常）
        val prevBlend = GL11.glIsEnabled(GL11.GL_BLEND)
        val prevAlpha = GL11.glIsEnabled(GL11.GL_ALPHA_TEST)
        // 中间 pass 写入独立 FBO，不需要混合
        GlStateManager.disableBlend()
        GlStateManager.disableAlpha()

        // Downsample 阶段
        renderFBO(framebufferList[1], mc.framebuffer.framebufferTexture, down, offset.toFloat())
        for (i in 1 until iterations) {
            renderFBO(framebufferList[i + 1], framebufferList[i].framebufferTexture, down, offset.toFloat())
        }

        // Upsample 阶段
        for (i in iterations downTo 2) {
            renderFBO(framebufferList[i - 1], framebufferList[i].framebufferTexture, up, offset.toFloat())
        }

        // 最后一次 upsample，带 stencil 检查
        val lastBuffer = framebufferList[0]
        lastBuffer.framebufferClear()
        lastBuffer.bindFramebuffer(false)
        up.init()
        up.setUniformf("offset", offset.toFloat(), offset.toFloat())
        up.setUniformi("inTexture", 0)
        up.setUniformi("check", 1)
        up.setUniformi("textureToCheck", 16)
        up.setUniformf("halfpixel", 1.0f / lastBuffer.framebufferWidth, 1.0f / lastBuffer.framebufferHeight)
        up.setUniformf("iResolution", lastBuffer.framebufferWidth.toFloat(), lastBuffer.framebufferHeight.toFloat())

        GL13.glActiveTexture(GL13.GL_TEXTURE16)
        bindTexture(stencilFrameBufferTexture)
        GL13.glActiveTexture(GL13.GL_TEXTURE0)
        bindTexture(framebufferList[1].framebufferTexture)
        GlStateManager.color(1f, 1f, 1f, 1f)
        ShaderUtil.drawQuads()
        up.unload()

        // 渲染到主 framebuffer，使用 scissor 限制区域
        mc.framebuffer.bindFramebuffer(true)

        // 计算 scissor 坐标（OpenGL坐标系，原点在左下角）
        // 输入已经是像素坐标，直接转换到OpenGL坐标系
        val scissorX = x
        val scissorY = mc.displayHeight - (y + height)
        val scissorWidth = width
        val scissorHeight = height

        GL11.glEnable(GL11.GL_SCISSOR_TEST)
        GL11.glScissor(scissorX, scissorY, scissorWidth, scissorHeight)

        bindTexture(framebufferList[0].framebufferTexture)
        setAlphaLimit(0f)
        startBlend()
        GlStateManager.color(1f, 1f, 1f, 1f)
        ShaderUtil.drawQuads()

        GL11.glDisable(GL11.GL_SCISSOR_TEST)
        GlStateManager.bindTexture(0)

        // 恢复进入时的 GL 状态
        if (prevBlend) GlStateManager.enableBlend() else GlStateManager.disableBlend()
        if (prevAlpha) GlStateManager.enableAlpha() else GlStateManager.disableAlpha()
        GlStateManager.color(1f, 1f, 1f, 1f)
    }

    /**
     * 基于 Scissor 裁剪的区域模糊（不使用 stencil framebuffer）。
     *
     * 流程：
     *  1. 对主 framebuffer 全屏进行 Kawase 下采样 + 上采样，得到全屏模糊结果。
     *  2. 最后一次 upsample 使用 check=0（alpha=1.0），让模糊结果完全不透明。
     *  3. 将模糊结果渲染回主 framebuffer 时，用 glScissor 限制只在指定矩形区域内绘制，
     *     区域外保持原样，避免白屏。
     *
     * 坐标参数均为 framebuffer 像素坐标（原点在左下角）。
     *
     * @param iterations 下采样/上采样次数
     * @param offset     模糊偏移量
     * @param x          裁剪区域左下角 X（像素）
     * @param y          裁剪区域左下角 Y（像素）
     * @param width      裁剪区域宽度（像素）
     * @param height     裁剪区域高度（像素）
     */
    fun renderBlurScissor(iterations: Int, offset: Int, x: Int, y: Int, width: Int, height: Int) {
        if (width <= 0 || height <= 0) return
        initShaders()
        val down = kawaseDown
        val up = kawaseUp
        if (down == null || up == null) {
            System.err.println("KawaseBlur shaders not initialized, skipping blur render")
            return
        }

        val needsReinit = framebufferList.isEmpty() ||
                framebuffer.framebufferWidth != mc.displayWidth ||
                framebuffer.framebufferHeight != mc.displayHeight ||
                framebufferList.size != iterations + 2

        if (needsReinit) {
            initFramebuffers(iterations.toFloat())
            currentIterations = iterations
        }

        // 保存将被修改的 GL 状态，结束时恢复，避免状态泄漏（奇怪的纹理 / 透明度异常）
        val prevBlend = GL11.glIsEnabled(GL11.GL_BLEND)
        val prevAlpha = GL11.glIsEnabled(GL11.GL_ALPHA_TEST)
        // 中间 pass 写入独立 FBO，不需要混合
        GlStateManager.disableBlend()
        GlStateManager.disableAlpha()

        // Downsample 阶段：从主 framebuffer 逐级下采样
        renderFBO(framebufferList[1], mc.framebuffer.framebufferTexture, down, offset.toFloat())
        for (i in 1 until iterations) {
            renderFBO(framebufferList[i + 1], framebufferList[i].framebufferTexture, down, offset.toFloat())
        }

        // Upsample 阶段：逐级上采样（中间步骤，check=0）
        for (i in iterations downTo 2) {
            renderFBO(framebufferList[i - 1], framebufferList[i].framebufferTexture, up, offset.toFloat())
        }

        // 最后一次 upsample 到 framebufferList[0]，check=0 让 alpha=1.0（完全不透明）
        val lastBuffer = framebufferList[0]
        lastBuffer.framebufferClear()
        lastBuffer.bindFramebuffer(false)
        up.init()
        up.setUniformf("offset", offset.toFloat(), offset.toFloat())
        up.setUniformi("inTexture", 0)
        up.setUniformi("check", 0)
        up.setUniformf("halfpixel", 1.0f / lastBuffer.framebufferWidth, 1.0f / lastBuffer.framebufferHeight)
        up.setUniformf("iResolution", lastBuffer.framebufferWidth.toFloat(), lastBuffer.framebufferHeight.toFloat())
        bindTexture(framebufferList[1].framebufferTexture)
        GlStateManager.color(1f, 1f, 1f, 1f)
        ShaderUtil.drawQuads()
        up.unload()

        // 渲染到主 framebuffer，用 scissor 限制只在聊天区域绘制模糊结果
        mc.framebuffer.bindFramebuffer(true)

        GL11.glEnable(GL11.GL_SCISSOR_TEST)
        GL11.glScissor(x, y, width, height)

        bindTexture(framebufferList[0].framebufferTexture)
        setAlphaLimit(0f)
        startBlend()
        GlStateManager.color(1f, 1f, 1f, 1f)
        ShaderUtil.drawQuads()

        GL11.glDisable(GL11.GL_SCISSOR_TEST)
        GlStateManager.bindTexture(0)

        // 恢复进入时的 GL 状态，避免残留 alpha test / blend 影响后续渲染
        if (prevBlend) GlStateManager.enableBlend() else GlStateManager.disableBlend()
        if (prevAlpha) GlStateManager.enableAlpha() else GlStateManager.disableAlpha()
        GlStateManager.color(1f, 1f, 1f, 1f)
    }

    private fun renderFBO(framebuffer: Framebuffer, framebufferTexture: Int, shader: ShaderUtil, offset: Float) {
        framebuffer.framebufferClear()
        framebuffer.bindFramebuffer(false)
        shader.init()
        bindTexture(framebufferTexture)
        // 重置当前颜色，避免上一帧残留颜色把纹理调制出奇怪的颜色
        GlStateManager.color(1f, 1f, 1f, 1f)
        shader.setUniformf("offset", offset, offset)
        shader.setUniformi("inTexture", 0)
        shader.setUniformi("check", 0)
        shader.setUniformf("halfpixel", 1.0f / framebuffer.framebufferWidth, 1.0f / framebuffer.framebufferHeight)
        shader.setUniformf("iResolution", framebuffer.framebufferWidth.toFloat(), framebuffer.framebufferHeight.toFloat())
        ShaderUtil.drawQuads()
        shader.unload()
    }
    
    /**
     * 创建或重新创建 Framebuffer
     */
    fun createFrameBuffer(framebuffer: Framebuffer?): Framebuffer {
        var fb = framebuffer
        if (fb == null || fb.framebufferWidth != mc.displayWidth || fb.framebufferHeight != mc.displayHeight) {
            fb?.deleteFramebuffer()
            fb = Framebuffer(mc.displayWidth, mc.displayHeight, false)
        }
        fb.framebufferClear()
        fb.bindFramebuffer(false)
        return fb
    }
    
    /**
     * 绑定纹理
     */
    fun bindTexture(texture: Int) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture)
    }
    
    /**
     * 设置 Alpha 限制
     */
    fun setAlphaLimit(limit: Float) {
        GlStateManager.enableAlpha()
        GlStateManager.alphaFunc(GL11.GL_GREATER, limit)
    }
    
    /**
     * 开启混合
     */
    fun startBlend() {
        GlStateManager.enableBlend()
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
    }
    
    /**
     * 关闭混合
     */
    fun endBlend() {
        GlStateManager.disableBlend()
    }
}
