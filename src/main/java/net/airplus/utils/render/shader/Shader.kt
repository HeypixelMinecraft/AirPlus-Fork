/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.utils.render.shader

import net.airplus.utils.client.ClientUtils.LOGGER
import net.airplus.utils.client.MinecraftInstance
import org.apache.commons.io.IOUtils
import org.lwjgl.opengl.*
import org.lwjgl.opengl.ARBShaderObjects.*
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL20.glGetUniformLocation
import org.lwjgl.opengl.GL20.glUseProgram
import java.io.File
import java.io.IOException
import java.nio.file.Files

abstract class Shader : MinecraftInstance {
    var programId = 0
        private set

    /**
     * 记录着色器程序是否真正处于启用状态。
     * 平台不支持时 [startShader] 会提前返回，此时必须跳过 [stopShader] 的 glPopMatrix，
     * 否则矩阵栈会失衡导致后续渲染错位。
     */
    protected var started = false
        private set

    /**
     * 着色器程序是否可用（编译并链接成功，且平台支持）。
     */
    val isUsable: Boolean
        get() = programId != 0
    
    private val uniformsMap = mutableMapOf<String, Int>()

    constructor(fragmentShader: String) {
        val vertexShaderID: Int
        val fragmentShaderID: Int

        // 平台不支持桌面版着色器（如 Android GLES）时直接放弃，避免着色器编译失败的日志刷屏
        if (!ShaderSupport.isSupported) return

        try {
            val vertexStream = javaClass.getResourceAsStream("/assets/minecraft/airplus/shader/vertex.vert")
            vertexShaderID = createShader(IOUtils.toString(vertexStream), ARBVertexShader.GL_VERTEX_SHADER_ARB)
            IOUtils.closeQuietly(vertexStream)
            
            val fragmentStream = javaClass.getResourceAsStream("/assets/minecraft/airplus/shader/fragment/$fragmentShader")
            fragmentShaderID = createShader(IOUtils.toString(fragmentStream), ARBFragmentShader.GL_FRAGMENT_SHADER_ARB)
            IOUtils.closeQuietly(fragmentStream)
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }
        
        if (vertexShaderID == 0 || fragmentShaderID == 0)
            return
        
        programId = glCreateProgramObjectARB()
        
        if (programId == 0)
            return
        
        glAttachObjectARB(programId, vertexShaderID)
        glAttachObjectARB(programId, fragmentShaderID)
        
        glLinkProgramARB(programId)
        glValidateProgramARB(programId)

        // 链接失败时把 programId 归零，统一由 isUsable 判定为不可用
        if (glGetObjectParameteriARB(programId, GL_OBJECT_LINK_STATUS_ARB) == GL_FALSE) {
            glDeleteObjectARB(programId)
            programId = 0
            LOGGER.warn("[Shader] 链接失败，已禁用该着色器: $fragmentShader")
            return
        }

        LOGGER.info("[Shader] Successfully loaded: $fragmentShader")
    }

    @Throws(IOException::class)
    constructor(fragmentShader: File) {
        val vertexShaderID: Int
        val fragmentShaderID: Int

        // 同上，不支持时直接放弃
        if (!ShaderSupport.isSupported) return

        val vertexStream = javaClass.getResourceAsStream("/assets/minecraft/airplus/shader/vertex.vert")
        vertexShaderID = createShader(IOUtils.toString(vertexStream), ARBVertexShader.GL_VERTEX_SHADER_ARB)
        IOUtils.closeQuietly(vertexStream)
        
        val fragmentStream = Files.newInputStream(fragmentShader.toPath())
        fragmentShaderID = createShader(IOUtils.toString(fragmentStream), ARBFragmentShader.GL_FRAGMENT_SHADER_ARB)
        IOUtils.closeQuietly(fragmentStream)
        
        if (vertexShaderID == 0 || fragmentShaderID == 0)
            return
        
        programId = glCreateProgramObjectARB()
        
        if (programId == 0)
            return
        
        glAttachObjectARB(programId, vertexShaderID)
        glAttachObjectARB(programId, fragmentShaderID)
        
        glLinkProgramARB(programId)
        glValidateProgramARB(programId)

        // 链接失败时把 programId 归零，统一由 isUsable 判定为不可用
        if (glGetObjectParameteriARB(programId, GL_OBJECT_LINK_STATUS_ARB) == GL_FALSE) {
            glDeleteObjectARB(programId)
            programId = 0
            LOGGER.warn("[Shader] 链接失败，已禁用该着色器: " + fragmentShader.name)
            return
        }

        LOGGER.info("[Shader] Successfully loaded: " + fragmentShader.name)
    }

    open fun startShader() {
        // 程序不可用时直接跳过，且不改变矩阵栈
        if (!isUsable || !ShaderSupport.isSupported) return

        glPushMatrix()
        glUseProgram(programId)
        started = true

        if (uniformsMap.isEmpty())
            setupUniforms()

        updateUniforms()
    }

    open fun stopShader() {
        // 未成功启用时不能弹栈，否则矩阵栈会失衡
        if (!started) return

        started = false
        glUseProgram(0)
        glPopMatrix()
    }

    abstract fun setupUniforms()
    abstract fun updateUniforms()
    private fun createShader(shaderSource: String, shaderType: Int): Int {
        var shader = 0

        return try {
            shader = glCreateShaderObjectARB(shaderType)

            if (shader == 0)
                return 0

            glShaderSourceARB(shader, shaderSource)
            glCompileShaderARB(shader)

            if (glGetObjectParameteriARB(shader, GL_OBJECT_COMPILE_STATUS_ARB) == GL_FALSE)
                throw RuntimeException("Error creating shader: " + getLogInfo(shader))

            shader
        } catch (e: Exception) {
            glDeleteObjectARB(shader)
            throw e
        }
    }

    private fun getLogInfo(i: Int) = glGetInfoLogARB(i, glGetObjectParameteriARB(i, GL_OBJECT_INFO_LOG_LENGTH_ARB))

    fun setUniform(uniformName: String, location: Int) {
        uniformsMap[uniformName] = location
    }

    fun setupUniform(uniformName: String) = setUniform(uniformName, glGetUniformLocation(programId, uniformName))

    fun getUniform(uniformName: String) = uniformsMap[uniformName]!!

    fun setUniformf(name: String, vararg args: Float) {
        val loc = glGetUniformLocation(programId, name)
        when (args.size) {
            1 -> org.lwjgl.opengl.GL20.glUniform1f(loc, args[0])
            2 -> org.lwjgl.opengl.GL20.glUniform2f(loc, args[0], args[1])
            3 -> org.lwjgl.opengl.GL20.glUniform3f(loc, args[0], args[1], args[2])
            4 -> org.lwjgl.opengl.GL20.glUniform4f(loc, args[0], args[1], args[2], args[3])
        }
    }

    companion object {
        fun drawQuad(x: Float, y: Float, width: Float, height: Float) {
            glBegin(GL_QUADS)
            glTexCoord2f(0.0f, 0.0f)
            glVertex2d(x.toDouble(), (y + height).toDouble())
            glTexCoord2f(1.0f, 0.0f)
            glVertex2d((x + width).toDouble(), (y + height).toDouble())
            glTexCoord2f(1.0f, 1.0f)
            glVertex2d((x + width).toDouble(), y.toDouble())
            glTexCoord2f(0.0f, 1.0f)
            glVertex2d(x.toDouble(), y.toDouble())
            glEnd()
        }
    }
}
