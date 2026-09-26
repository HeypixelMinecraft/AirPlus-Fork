/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.utils.render.shader

import net.airplus.utils.client.ClientUtils
import org.lwjgl.opengl.ARBFragmentShader
import org.lwjgl.opengl.ARBShaderObjects.*
import org.lwjgl.opengl.ARBVertexShader
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20

/**
 * 桌面版 GLSL 着色器可用性探测。
 *
 * AirPlus 的所有着色器都基于桌面 OpenGL 的 `#version 120` 语法，并使用了固定管线内置变量
 * （gl_ModelViewProjectionMatrix / gl_TexCoord / gl_MultiTexCoord0）以及 ARB 着色器对象入口。
 * 在 Android 的 OpenGL ES（Krypton Wrapper 等）下这些着色器必然编译失败，会造成日志刷屏
 * 与渲染异常（背景、彩虹/渐变字体、模糊、发光描边等特效失效）。
 *
 * 这里做一次性探测并缓存结果，供着色器代码在不可用时整体降级。
 */
object ShaderSupport {

    @Volatile
    private var cached: Boolean? = null

    /**
     * 当前平台是否支持 AirPlus 所使用的桌面版着色器。
     * 必须在 GL 上下文就绪后调用（渲染线程内），结果会被缓存。
     */
    val isSupported: Boolean
        get() {
            cached?.let { return it }
            val result = try {
                detect()
            } catch (t: Throwable) {
                // 平台缺少相关 GL 入口时抛出的是 Error（如 NoSuchMethodError / LinkageError），必须用 Throwable
                false
            }
            cached = result
            if (!result) {
                ClientUtils.LOGGER.warn(
                    "[ShaderSupport] 当前平台不支持桌面版 GLSL 着色器，已自动降级；" +
                        "背景、彩虹/渐变字体、模糊、发光描边等特效将被跳过以保证画面正常。"
                )
            }
            return result
        }

    private fun detect(): Boolean {
        val version = GL11.glGetString(GL11.GL_VERSION) ?: return false
        // OpenGL ES 一律不支持桌面着色器
        if (version.contains("OpenGL ES", ignoreCase = true)) return false

        val glslVersion = GL11.glGetString(GL20.GL_SHADING_LANGUAGE_VERSION) ?: return false
        val glsl = parseVersionNumber(glslVersion) ?: return false
        // 桌面着色器最低要求 GLSL 1.20（OpenGL 2.1）
        if (glsl < 120) return false

        return probeCompile(glslVersion)
    }

    /**
     * 把 "1.20" / "4.60" / "1.2" 这类版本号统一换算成可比较的整数（120 / 460 / 120）。
     */
    private fun parseVersionNumber(raw: String): Int? {
        val match = Regex("(\\d+)\\.(\\d+)").find(raw) ?: return null
        val major = match.groupValues[1].toIntOrNull() ?: return null
        val minor = match.groupValues[2]
        val minorValue = if (minor.length == 1) minor.toInt() * 10 else minor.toInt()
        return major * 100 + minorValue
    }

    /**
     * 用探针着色器实际编译链接一次，确认固定管线内置变量与 ARB 入口真的可用。
     * 探测完成后立即释放资源，不残留 GL 对象。
     */
    private fun probeCompile(glslVersionText: String): Boolean {
        drainGlErrors()

        val vertexId = glCreateShaderObjectARB(ARBVertexShader.GL_VERTEX_SHADER_ARB)
        if (vertexId == 0) return false

        val fragmentId = glCreateShaderObjectARB(ARBFragmentShader.GL_FRAGMENT_SHADER_ARB)
        if (fragmentId == 0) {
            glDeleteObjectARB(vertexId)
            return false
        }

        var programId = 0
        return try {
            glShaderSourceARB(vertexId, PROBE_VERTEX)
            glCompileShaderARB(vertexId)
            if (glGetObjectParameteriARB(vertexId, GL_OBJECT_COMPILE_STATUS_ARB) == GL11.GL_FALSE) return false

            glShaderSourceARB(fragmentId, PROBE_FRAGMENT)
            glCompileShaderARB(fragmentId)
            if (glGetObjectParameteriARB(fragmentId, GL_OBJECT_COMPILE_STATUS_ARB) == GL11.GL_FALSE) return false

            programId = glCreateProgramObjectARB()
            if (programId == 0) return false

            glAttachObjectARB(programId, vertexId)
            glAttachObjectARB(programId, fragmentId)
            glLinkProgramARB(programId)

            val linked = glGetObjectParameteriARB(programId, GL_OBJECT_LINK_STATUS_ARB) != GL11.GL_FALSE
            if (linked) {
                ClientUtils.LOGGER.info("[ShaderSupport] 检测到 GLSL $glslVersionText，桌面着色器特效已启用。")
            }
            linked
        } finally {
            glDeleteObjectARB(vertexId)
            glDeleteObjectARB(fragmentId)
            if (programId != 0) glDeleteObjectARB(programId)
            drainGlErrors()
        }
    }

    /**
     * 清空探测过程可能残留的 GL 错误，避免污染后续的错误检查。
     */
    private fun drainGlErrors() {
        var guard = 0
        while (GL11.glGetError() != GL11.GL_NO_ERROR && guard++ < 16) {
            // 仅用于丢弃历史错误
        }
    }

    private val PROBE_VERTEX =
        "#version 120\nvoid main() { gl_TexCoord[0] = gl_MultiTexCoord0; gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex; }"

    private val PROBE_FRAGMENT =
        "#version 120\nuniform sampler2D textureIn;\nvoid main() { gl_FragColor = texture2D(textureIn, gl_TexCoord[0].xy); }"
}
