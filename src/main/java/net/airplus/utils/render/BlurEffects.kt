package net.airplus.utils.render

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.shader.Framebuffer
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL11.*

object BlurEffects {
    private val mc = Minecraft.getMinecraft()
    
    private var gaussianProgram: Int = -1
    private var dualProgram: Int = -1
    
    private var outputBuffer: Framebuffer? = null
    private var tempBuffer: Framebuffer? = null
    
    private var gaussianUniforms = mutableMapOf<String, Int>()
    private var dualUniforms = mutableMapOf<String, Int>()
    
    enum class BlurMode {
        GAUSSIAN,
        DUAL,
        BETTER
    }
    
    fun blurArea(
        x: Float, y: Float, width: Float, height: Float,
        radius: Float,
        mode: BlurMode = BlurMode.GAUSSIAN
    ) {
        when (mode) {
            BlurMode.BETTER -> InternalBlurShader.blurArea(x, y, width, height, radius)
            else -> renderCustomBlur(x, y, width, height, radius, mode)
        }
    }
    
    private fun renderCustomBlur(x: Float, y: Float, width: Float, height: Float, radius: Float, mode: BlurMode) {
        val sr = ScaledResolution(mc)
        val factor = sr.scaleFactor
        ensureBuffers(mc.displayWidth, mc.displayHeight)

        val sX = (x * factor).toInt()
        val sY = (mc.displayHeight - (y * factor).toInt() - (height * factor).toInt())
        val sW = (width * factor).toInt()
        val sH = (height * factor).toInt()

        // 保存将被修改的 GL 状态，结束时恢复，避免纹理/blend 状态泄漏（奇怪的纹理）
        val prevBlend = GL11.glIsEnabled(GL11.GL_BLEND)
        val prevAlpha = GL11.glIsEnabled(GL11.GL_ALPHA_TEST)
        // 中间 pass 写入独立 FBO，不需要混合
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_ALPHA_TEST)

        // 保存 scissor 状态：结束时按原样恢复，避免关闭调用方外层已开启的 scissor
        val prevScissor = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST)
        glEnable(GL_SCISSOR_TEST)
        // Strictly limit scissor to blur area (no padding) to prevent blur from bleeding outside GUI bounds.
        // Texture sampling is not affected by scissor, so blur quality remains intact.
        glScissor(sX, sY, sW, sH)

        setupMatrix(sr)

        when (mode) {
            BlurMode.GAUSSIAN -> renderGaussianBlur(radius)
            BlurMode.DUAL -> renderDualBlur(radius)
            else -> {}
        }

        restoreMatrix()

        if (prevScissor) glEnable(GL_SCISSOR_TEST) else glDisable(GL_SCISSOR_TEST)

        // 恢复进入时的 GL 状态（blend / alpha / 纹理绑定 / 当前颜色）
        if (prevBlend) GL11.glEnable(GL11.GL_BLEND) else GL11.glDisable(GL11.GL_BLEND)
        if (prevAlpha) GL11.glEnable(GL11.GL_ALPHA_TEST) else GL11.glDisable(GL11.GL_ALPHA_TEST)
        GlStateManager.bindTexture(0)
        GlStateManager.color(1f, 1f, 1f, 1f)
    }
    
    private fun renderGaussianBlur(radius: Float) {
        ensureGaussianShader()
        val buffer = outputBuffer ?: return
        val mainBuffer = mc.framebuffer

        buffer.framebufferClear()
        buffer.bindFramebuffer(true)
        mainBuffer.bindFramebufferTexture()
        setupTextureParams()

        GL20.glUseProgram(gaussianProgram)
        setUniforms(gaussianUniforms, radius, 1.0f, 0.0f)
        GL11.glColor4f(1f, 1f, 1f, 1f)
        drawQuads()

        mainBuffer.bindFramebuffer(true)
        buffer.bindFramebufferTexture()
        setupTextureParams()

        // Save GL state before modifying blend/alpha
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT or GL11.GL_CURRENT_BIT)

        // Enable blending when writing back to main buffer to preserve transparency
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glDisable(GL_ALPHA_TEST)
        GL11.glColor4f(1f, 1f, 1f, 1f)
        setUniforms(gaussianUniforms, radius, 0.0f, 1.0f)
        drawQuads()

        GL20.glUseProgram(0)

        // Restore GL state
        GL11.glPopAttrib()
    }
    
    private fun renderDualBlur(radius: Float) {
        ensureDualShader()
        val buffer = outputBuffer ?: return
        val temp = tempBuffer ?: return
        val mainBuffer = mc.framebuffer

        for (i in 0 until 2) {
            buffer.framebufferClear()
            buffer.bindFramebuffer(true)
            if (i == 0) mainBuffer.bindFramebufferTexture() else temp.bindFramebufferTexture()
            setupTextureParams()

            GL20.glUseProgram(dualProgram)
            setUniforms(dualUniforms, radius, 1.0f, 0.0f)
            GL11.glColor4f(1f, 1f, 1f, 1f)
            drawQuads()

            temp.framebufferClear()
            temp.bindFramebuffer(true)
            buffer.bindFramebufferTexture()
            setupTextureParams()

            setUniforms(dualUniforms, radius, 0.0f, 1.0f)
            drawQuads()
        }

        mainBuffer.bindFramebuffer(true)
        temp.bindFramebufferTexture()
        setupTextureParams()

        // Save GL state before modifying blend/alpha
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT or GL11.GL_CURRENT_BIT)

        // Enable blending when writing back to main buffer to preserve transparency
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glDisable(GL_ALPHA_TEST)
        GL11.glColor4f(1f, 1f, 1f, 1f)
        drawQuads()

        GL20.glUseProgram(0)

        // Restore GL state
        GL11.glPopAttrib()
    }
    
    private fun ensureBuffers(w: Int, h: Int) {
        if (outputBuffer == null || outputBuffer!!.framebufferWidth != w || outputBuffer!!.framebufferHeight != h) {
            outputBuffer?.deleteFramebuffer()
            tempBuffer?.deleteFramebuffer()
            outputBuffer = Framebuffer(w, h, true)
            outputBuffer!!.setFramebufferFilter(9729)
            tempBuffer = Framebuffer(w, h, true)
            tempBuffer!!.setFramebufferFilter(9729)
        }
    }
    
    private fun setupMatrix(sr: ScaledResolution) {
        GL11.glMatrixMode(GL11.GL_MODELVIEW)
        GL11.glPushMatrix()
        GL11.glLoadIdentity()
        GL11.glMatrixMode(GL11.GL_PROJECTION)
        GL11.glPushMatrix()
        GL11.glLoadIdentity()
        GL11.glOrtho(0.0, sr.scaledWidth_double, sr.scaledHeight_double, 0.0, 1000.0, 3000.0)
        GL11.glTranslated(0.0, 0.0, -2000.0)
    }
    
    private fun restoreMatrix() {
        GL11.glMatrixMode(GL11.GL_PROJECTION)
        GL11.glPopMatrix()
        GL11.glMatrixMode(GL11.GL_MODELVIEW)
        GL11.glPopMatrix()
    }
    
    private fun setupTextureParams() {
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, 33071)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, 33071)
    }
    
    private fun setUniforms(uniforms: Map<String, Int>, radius: Float, dirX: Float, dirY: Float) {
        GL20.glUniform1f(uniforms["radius"] ?: 0, radius)
        GL20.glUniform2f(uniforms["direction"] ?: 0, dirX, dirY)
        GL20.glUniform2f(uniforms["texelSize"] ?: 0, 1.0f / mc.displayWidth, 1.0f / mc.displayHeight)
    }
    
    private fun ensureGaussianShader() {
        if (gaussianProgram != -1) return
        val vert = "#version 120\nvoid main() { gl_TexCoord[0] = gl_MultiTexCoord0; gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex; }"
        val frag = """
            #version 120
            uniform sampler2D textureIn;
            uniform vec2 texelSize;
            uniform vec2 direction;
            uniform float radius;
            
            float gaussian(float x, float sigma) {
                return exp(-(x*x) / (2.0 * sigma * sigma));
            }
            
            void main() {
                vec2 coord = gl_TexCoord[0].xy;
                vec4 sum = vec4(0.0);
                float totalWeight = 0.0;
                int range = int(min(radius, 50.0));
                float sigma = radius / 2.0;
                
                for (int i = -range; i <= range; i++) {
                    float weight = gaussian(float(i), sigma);
                    vec2 offset = float(i) * texelSize * direction;
                    sum += texture2D(textureIn, coord + offset) * weight;
                    totalWeight += weight;
                }
                gl_FragColor = sum / totalWeight;
            }
        """.trimIndent()
        
        gaussianProgram = createProgram(vert, frag)
        gaussianUniforms["radius"] = GL20.glGetUniformLocation(gaussianProgram, "radius")
        gaussianUniforms["direction"] = GL20.glGetUniformLocation(gaussianProgram, "direction")
        gaussianUniforms["texelSize"] = GL20.glGetUniformLocation(gaussianProgram, "texelSize")
    }
    
    private fun ensureDualShader() {
        if (dualProgram != -1) return
        val vert = "#version 120\nvoid main() { gl_TexCoord[0] = gl_MultiTexCoord0; gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex; }"
        val frag = """
            #version 120
            uniform sampler2D textureIn;
            uniform vec2 texelSize;
            uniform vec2 direction;
            uniform float radius;
            
            float gaussian(float x, float sigma) {
                return exp(-(x*x) / (2.0 * sigma * sigma));
            }
            
            void main() {
                vec2 coord = gl_TexCoord[0].xy;
                vec4 sum = vec4(0.0);
                float totalWeight = 0.0;
                int range = int(min(radius * 1.5, 60.0));
                float sigma = radius / 1.5;
                
                for (int i = -range; i <= range; i++) {
                    float weight = gaussian(float(i), sigma) * 1.2;
                    vec2 offset = float(i) * texelSize * direction * 1.5;
                    sum += texture2D(textureIn, coord + offset) * weight;
                    totalWeight += weight;
                }
                gl_FragColor = sum / totalWeight;
            }
        """.trimIndent()
        
        dualProgram = createProgram(vert, frag)
        dualUniforms["radius"] = GL20.glGetUniformLocation(dualProgram, "radius")
        dualUniforms["direction"] = GL20.glGetUniformLocation(dualProgram, "direction")
        dualUniforms["texelSize"] = GL20.glGetUniformLocation(dualProgram, "texelSize")
    }
    
    private fun createProgram(vertSrc: String, fragSrc: String): Int {
        val vertId = GL20.glCreateShader(GL20.GL_VERTEX_SHADER)
        GL20.glShaderSource(vertId, vertSrc)
        GL20.glCompileShader(vertId)

        val fragId = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER)
        GL20.glShaderSource(fragId, fragSrc)
        GL20.glCompileShader(fragId)

        val program = GL20.glCreateProgram()
        GL20.glAttachShader(program, vertId)
        GL20.glAttachShader(program, fragId)
        GL20.glLinkProgram(program)

        // 着色器已链接到程序，删除 shader 对象释放资源
        GL20.glDetachShader(program, vertId)
        GL20.glDetachShader(program, fragId)
        GL20.glDeleteShader(vertId)
        GL20.glDeleteShader(fragId)

        return program
    }

    /**
     * 释放所有 GPU 资源（Shader 程序、Framebuffer）。
     * 应在客户端关闭或 GL 上下文即将销毁时调用。
     */
    fun cleanup() {
        if (gaussianProgram != -1) {
            GL20.glDeleteProgram(gaussianProgram)
            gaussianProgram = -1
            gaussianUniforms.clear()
        }
        if (dualProgram != -1) {
            GL20.glDeleteProgram(dualProgram)
            dualProgram = -1
            dualUniforms.clear()
        }
        outputBuffer?.deleteFramebuffer()
        outputBuffer = null
        tempBuffer?.deleteFramebuffer()
        tempBuffer = null
    }
    
    private fun drawQuads() {
        val sr = ScaledResolution(mc)
        val w = sr.scaledWidth_double
        val h = sr.scaledHeight_double
        glBegin(GL_QUADS)
        glTexCoord2f(0f, 1f); glVertex2d(0.0, 0.0)
        glTexCoord2f(0f, 0f); glVertex2d(0.0, h)
        glTexCoord2f(1f, 0f); glVertex2d(w, h)
        glTexCoord2f(1f, 1f); glVertex2d(w, 0.0)
        glEnd()
    }
}
