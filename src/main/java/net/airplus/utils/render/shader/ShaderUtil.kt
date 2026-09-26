// skid xylitol
package net.airplus.utils.render.shader

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
import java.io.ByteArrayInputStream
import java.io.IOException

/**
 * 通用 Shader 工具类，支持多种内置 shader 和自定义 shader。
 * 迁移自 xylitol 客户端的 ShaderUtil。
 */
class ShaderUtil {
    private val programID: Int

    constructor(fragmentShaderLoc: String, vertexShaderLoc: String) {
        val program = GL20.glCreateProgram()
        try {
            val fragmentShaderID = when (fragmentShaderLoc) {
                "kawaseUpGlow" -> createShader(ByteArrayInputStream(kawaseUpGlow.toByteArray()), GL20.GL_FRAGMENT_SHADER)
                "glow" -> createShader(ByteArrayInputStream(glowShader.toByteArray()), GL20.GL_FRAGMENT_SHADER)
                "chams" -> createShader(ByteArrayInputStream(chams.toByteArray()), GL20.GL_FRAGMENT_SHADER)
                "roundRectTexture" -> createShader(ByteArrayInputStream(roundRectTexture.toByteArray()), GL20.GL_FRAGMENT_SHADER)
                "roundRectOutline" -> createShader(ByteArrayInputStream(roundRectOutline.toByteArray()), GL20.GL_FRAGMENT_SHADER)
                "kawaseUpBloom" -> createShader(ByteArrayInputStream(kawaseUpBloom.toByteArray()), GL20.GL_FRAGMENT_SHADER)
                "kawaseDownBloom" -> createShader(ByteArrayInputStream(kawaseDownBloom.toByteArray()), GL20.GL_FRAGMENT_SHADER)
                "kawaseUp" -> createShader(ByteArrayInputStream(kawaseUp.toByteArray()), GL20.GL_FRAGMENT_SHADER)
                "kawaseDown" -> createShader(ByteArrayInputStream(kawaseDown.toByteArray()), GL20.GL_FRAGMENT_SHADER)
                "gradientMask" -> createShader(ByteArrayInputStream(gradientMask.toByteArray()), GL20.GL_FRAGMENT_SHADER)
                "mask" -> createShader(ByteArrayInputStream(mask.toByteArray()), GL20.GL_FRAGMENT_SHADER)
                "gradientround" -> createShader(ByteArrayInputStream(gradientround.toByteArray()), GL20.GL_FRAGMENT_SHADER)
                "gradient" -> createShader(ByteArrayInputStream(gradient.toByteArray()), GL20.GL_FRAGMENT_SHADER)
                "clickgui" -> createShader(ByteArrayInputStream(clickgui.toByteArray()), GL20.GL_FRAGMENT_SHADER)
                "roundedRect" -> createShader(ByteArrayInputStream(roundedRect.toByteArray()), GL20.GL_FRAGMENT_SHADER)
                "roundedRectGradient" -> createShader(ByteArrayInputStream(roundedRectGradient.toByteArray()), GL20.GL_FRAGMENT_SHADER)
                "arc" -> createShader(ByteArrayInputStream(arc.toByteArray()), GL20.GL_FRAGMENT_SHADER)
                "gaussian" -> createShader(ByteArrayInputStream(gaussian.toByteArray()), GL20.GL_FRAGMENT_SHADER)
                "bloom" -> createShader(ByteArrayInputStream(bloom.toByteArray()), GL20.GL_FRAGMENT_SHADER)
                else -> createShader(Minecraft.getMinecraft().resourceManager.getResource(ResourceLocation(fragmentShaderLoc)).inputStream, GL20.GL_FRAGMENT_SHADER)
            }
            GL20.glAttachShader(program, fragmentShaderID)

            val vertexShaderID = createShader(Minecraft.getMinecraft().resourceManager.getResource(ResourceLocation(vertexShaderLoc)).inputStream, GL20.GL_VERTEX_SHADER)
            GL20.glAttachShader(program, vertexShaderID)

        } catch (e: IOException) {
            e.printStackTrace()
        }

        GL20.glLinkProgram(program)
        val status = GL20.glGetProgrami(program, GL20.GL_LINK_STATUS)

        if (status == 0) {
            GL20.glDeleteProgram(program)
            throw IllegalStateException("Shader failed to link!")
        }
        this.programID = program
    }

    constructor(fragmentShaderSrc: String, notUsed: Boolean) {
            val program = GL20.glCreateProgram()
            val fragmentShaderID = createShader(ByteArrayInputStream(fragmentShaderSrc.toByteArray()), GL20.GL_FRAGMENT_SHADER)
            val vertexShaderID = try {
                // 注意：文件位于 assets/minecraft/airplus/shader/，domain 必须是 minecraft
                createShader(Minecraft.getMinecraft().resourceManager.getResource(ResourceLocation("minecraft", "airplus/shader/vertex.vert")).inputStream, GL20.GL_VERTEX_SHADER)
            } catch (e: IOException) {
                throw RuntimeException(e)
            }

        GL20.glAttachShader(program, fragmentShaderID)
        GL20.glAttachShader(program, vertexShaderID)
        GL20.glLinkProgram(program)
        val status = GL20.glGetProgrami(program, GL20.GL_LINK_STATUS)
        if (status == 0) {
            GL20.glDeleteProgram(program)
            throw IllegalStateException("Shader failed to link!")
        }
        this.programID = program
    }

    constructor(fragmentShaderLoc: String) : this(fragmentShaderLoc, "minecraft:airplus/shader/vertex.vert")

    fun init() {
        GL20.glUseProgram(programID)
    }

    fun unload() {
        GL20.glUseProgram(0)
    }

    fun getUniform(name: String): Int {
        return GL20.glGetUniformLocation(programID, name)
    }

    fun setUniformf(name: String, vararg args: Float) {
        val loc = GL20.glGetUniformLocation(programID, name)
        if (loc == -1) return
        when (args.size) {
            1 -> GL20.glUniform1f(loc, args[0])
            2 -> GL20.glUniform2f(loc, args[0], args[1])
            3 -> GL20.glUniform3f(loc, args[0], args[1], args[2])
            4 -> GL20.glUniform4f(loc, args[0], args[1], args[2], args[3])
        }
    }

    fun setUniformi(name: String, vararg args: Int) {
        val loc = GL20.glGetUniformLocation(programID, name)
        if (loc == -1) return
        if (args.size > 1) GL20.glUniform2i(loc, args[0], args[1])
        else GL20.glUniform1i(loc, args[0])
    }

    /**
     * 释放 Shader 程序资源。
     */
    fun cleanup() {
        if (programID != 0) {
            GL20.glUseProgram(0)
            GL20.glDeleteProgram(programID)
        }
    }

    companion object {
        private val mc = Minecraft.getMinecraft()

        fun drawQuads(x: Float, y: Float, width: Float, height: Float) {
            GL11.glBegin(GL11.GL_QUADS)
            GL11.glTexCoord2f(0f, 0f)
            GL11.glVertex2f(x, y)
            GL11.glTexCoord2f(0f, 1f)
            GL11.glVertex2f(x, y + height)
            GL11.glTexCoord2f(1f, 1f)
            GL11.glVertex2f(x + width, y + height)
            GL11.glTexCoord2f(1f, 0f)
            GL11.glVertex2f(x + width, y)
            GL11.glEnd()
        }

        fun drawQuads() {
            val sr = ScaledResolution(mc)
            val width = sr.scaledWidth_double.toFloat()
            val height = sr.scaledHeight_double.toFloat()
            GL11.glBegin(GL11.GL_QUADS)
            GL11.glTexCoord2f(0f, 1f)
            GL11.glVertex2f(0f, 0f)
            GL11.glTexCoord2f(0f, 0f)
            GL11.glVertex2f(0f, height)
            GL11.glTexCoord2f(1f, 0f)
            GL11.glVertex2f(width, height)
            GL11.glTexCoord2f(1f, 1f)
            GL11.glVertex2f(width, 0f)
            GL11.glEnd()
        }

        fun drawQuads(width: Float, height: Float) {
            GL11.glBegin(GL11.GL_QUADS)
            GL11.glTexCoord2f(0f, 1f)
            GL11.glVertex2f(0f, 0f)
            GL11.glTexCoord2f(0f, 0f)
            GL11.glVertex2f(0f, height)
            GL11.glTexCoord2f(1f, 0f)
            GL11.glVertex2f(width, height)
            GL11.glTexCoord2f(1f, 1f)
            GL11.glVertex2f(width, 0f)
            GL11.glEnd()
        }

        private fun createShader(inputStream: java.io.InputStream, shaderType: Int): Int {
            val shader = GL20.glCreateShader(shaderType)
            GL20.glShaderSource(shader, readInputStream(inputStream))
            GL20.glCompileShader(shader)

            if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == 0) {
                println(GL20.glGetShaderInfoLog(shader, 4096))
                throw IllegalStateException(String.format("Shader (%s) failed to compile!", shaderType))
            }

            return shader
        }

        private fun readInputStream(inputStream: java.io.InputStream): String {
            val scanner = java.util.Scanner(inputStream, "UTF-8").useDelimiter("\\A")
            return if (scanner.hasNext()) scanner.next() else ""
        }

        // Shader source strings (from original code)
        private val kawaseUpGlow = """#version 120

uniform sampler2D inTexture, textureToCheck;
uniform vec2 halfpixel, offset, iResolution;
uniform bool check;
uniform float lastPass;
uniform float exposure;

void main() {
    if(check && texture2D(textureToCheck, gl_TexCoord[0].st).a != 0.0) discard;
    vec2 uv = vec2(gl_FragCoord.xy / iResolution);

    vec4 sum = texture2D(inTexture, uv + vec2(-halfpixel.x * 2.0, 0.0) * offset);
    sum.rgb *= sum.a;
    vec4 smpl1 =  texture2D(inTexture, uv + vec2(-halfpixel.x, halfpixel.y) * offset);
    smpl1.rgb *= smpl1.a;
    sum += smpl1 * 2.0;
    vec4 smp2 = texture2D(inTexture, uv + vec2(0.0, halfpixel.y * 2.0) * offset);
    smp2.rgb *= smp2.a;
    sum += smp2;
    vec4 smp3 = texture2D(inTexture, uv + vec2(halfpixel.x, halfpixel.y) * offset);
    smp3.rgb *= smp3.a;
    sum += smp3 * 2.0;
    vec4 smp4 = texture2D(inTexture, uv + vec2(halfpixel.x * 2.0, 0.0) * offset);
    smp4.rgb *= smp4.a;
    sum += smp4;
    vec4 smp5 = texture2D(inTexture, uv + vec2(halfpixel.x, -halfpixel.y) * offset);
    smp5.rgb *= smp5.a;
    sum += smp5 * 2.0;
    vec4 smp6 = texture2D(inTexture, uv + vec2(0.0, -halfpixel.y * 2.0) * offset);
    smp6.rgb *= smp6.a;
    sum += smp6;
    vec4 smp7 = texture2D(inTexture, uv + vec2(-halfpixel.x, -halfpixel.y) * offset);
    smp7.rgb *= smp7.a;
    sum += smp7 * 2.0;
    vec4 result = sum / 12.0;
    gl_FragColor = vec4(result.rgb / result.a, mix(result.a, 1.0 - exp(-result.a * exposure), step(0.0, lastPass)));
}"""

        private val glowShader = """#version 120

uniform sampler2D textureIn, textureToCheck;
uniform vec2 texelSize, direction;
uniform vec3 color;
uniform bool avoidTexture;
uniform float exposure, radius;
uniform float weights[256];

#define offset direction * texelSize

void main() {
    if (direction.y == 1 && avoidTexture) {
        if (texture2D(textureToCheck, gl_TexCoord[0].st).a != 0.0) discard;
    }
    vec4 innerColor = texture2D(textureIn, gl_TexCoord[0].st);
    innerColor.rgb *= innerColor.a;
    innerColor *= weights[0];
    for (float r = 1.0; r <= radius; r++) {
        vec4 colorCurrent1 = texture2D(textureIn, gl_TexCoord[0].st + offset * r);
        vec4 colorCurrent2 = texture2D(textureIn, gl_TexCoord[0].st - offset * r);

        colorCurrent1.rgb *= colorCurrent1.a;
        colorCurrent2.rgb *= colorCurrent2.a;

        innerColor += (colorCurrent1 + colorCurrent2) * weights[int(r)];
    }

    gl_FragColor = vec4(innerColor.rgb / innerColor.a, mix(innerColor.a, 1.0 - exp(-innerColor.a * exposure), step(0.0, direction.y)));
}
"""

        private val chams = """#version 120

uniform sampler2D textureIn;
uniform vec4 color;
void main() {
    float alpha = texture2D(textureIn, gl_TexCoord[0].st).a;

    gl_FragColor = vec4(color.rgb, color.a * mix(0.0, alpha, step(0.0, alpha)));
}
"""

        private val roundRectTexture = """#version 120

uniform vec2 location, rectSize;
uniform sampler2D textureIn;
uniform float radius, alpha;

float roundedBoxSDF(vec2 centerPos, vec2 size, float radius) {
    return length(max(abs(centerPos) -size, 0.)) - radius;
}


void main() {
    float distance = roundedBoxSDF((rectSize * .5) - (gl_TexCoord[0].st * rectSize), (rectSize * .5) - radius - 1., radius);
    float smoothedAlpha =  (1.0-smoothstep(0.0, 2.0, distance)) * alpha;
    gl_FragColor = vec4(texture2D(textureIn, gl_TexCoord[0].st).rgb, smoothedAlpha);
}"""

        private val roundRectOutline = """#version 120

uniform vec2 location, rectSize;
uniform vec4 color, outlineColor;
uniform float radius, outlineThickness;

float roundedSDF(vec2 centerPos, vec2 size, float radius) {
    return length(max(abs(centerPos) - size + radius, 0.0)) - radius;
}

void main() {
    float distance = roundedSDF(gl_FragCoord.xy - location - (rectSize * .5), (rectSize * .5) + (outlineThickness *.5) - 1.0, radius);

    float blendAmount = smoothstep(0., 2., abs(distance) - (outlineThickness * .5));

    vec4 insideColor = (distance < 0.) ? color : vec4(outlineColor.rgb,  0.0);
    gl_FragColor = mix(outlineColor, insideColor, blendAmount);

}"""

        private val kawaseUpBloom = """#version 120

uniform sampler2D inTexture, textureToCheck;
uniform vec2 halfpixel, offset, iResolution;
uniform int check;

void main() {
  //  if(check && texture2D(textureToCheck, gl_TexCoord[0].st).a > 0.0) discard;
    vec2 uv = vec2(gl_FragCoord.xy / iResolution);

    vec4 sum = texture2D(inTexture, uv + vec2(-halfpixel.x * 2.0, 0.0) * offset);
    sum.rgb *= sum.a;
    vec4 smpl1 =  texture2D(inTexture, uv + vec2(-halfpixel.x, halfpixel.y) * offset);
    smpl1.rgb *= smpl1.a;
    sum += smpl1 * 2.0;
    vec4 smp2 = texture2D(inTexture, uv + vec2(0.0, halfpixel.y * 2.0) * offset);
    smp2.rgb *= smp2.a;
    sum += smp2;
    vec4 smp3 = texture2D(inTexture, uv + vec2(halfpixel.x, halfpixel.y) * offset);
    smp3.rgb *= smp3.a;
    sum += smp3 * 2.0;
    vec4 smp4 = texture2D(inTexture, uv + vec2(halfpixel.x * 2.0, 0.0) * offset);
    smp4.rgb *= smp4.a;
    sum += smp4;
    vec4 smp5 = texture2D(inTexture, uv + vec2(halfpixel.x, -halfpixel.y) * offset);
    smp5.rgb *= smp5.a;
    sum += smp5 * 2.0;
    vec4 smp6 = texture2D(inTexture, uv + vec2(0.0, -halfpixel.y * 2.0) * offset);
    smp6.rgb *= smp6.a;
    sum += smp6;
    vec4 smp7 = texture2D(inTexture, uv + vec2(-halfpixel.x, -halfpixel.y) * offset);
    smp7.rgb *= smp7.a;
    sum += smp7 * 2.0;
    vec4 result = sum / 12.0;
    gl_FragColor = vec4(result.rgb / result.a, mix(result.a, result.a * (1.0 - texture2D(textureToCheck, gl_TexCoord[0].st).a),check));
}"""

        private val kawaseDownBloom = """#version 120

uniform sampler2D inTexture;
uniform vec2 offset, halfpixel, iResolution;

void main() {
    vec2 uv = vec2(gl_FragCoord.xy / iResolution);
    vec4 sum = texture2D(inTexture, gl_TexCoord[0].st);
    sum.rgb *= sum.a;
    sum *= 4.0;
    vec4 smp1 = texture2D(inTexture, uv - halfpixel.xy * offset);
    smp1.rgb *= smp1.a;
    sum += smp1;
    vec4 smp2 = texture2D(inTexture, uv + halfpixel.xy * offset);
    smp2.rgb *= smp2.a;
    sum += smp2;
    vec4 smp3 = texture2D(inTexture, uv + vec2(halfpixel.x, -halfpixel.y) * offset);
    smp3.rgb *= smp3.a;
    sum += smp3;
    vec4 smp4 = texture2D(inTexture, uv - vec2(halfpixel.x, -halfpixel.y) * offset);
    smp4.rgb *= smp4.a;
    sum += smp4;
    vec4 result = sum / 8.0;
    gl_FragColor = vec4(result.rgb / result.a, result.a);
}"""

        private val kawaseUp = """#version 120

uniform sampler2D inTexture, textureToCheck;
uniform vec2 halfpixel, offset, iResolution;
uniform int check;

void main() {
    vec2 uv = vec2(gl_FragCoord.xy / iResolution);
    vec4 sum = texture2D(inTexture, uv + vec2(-halfpixel.x * 2.0, 0.0) * offset);
    sum += texture2D(inTexture, uv + vec2(-halfpixel.x, halfpixel.y) * offset) * 2.0;
    sum += texture2D(inTexture, uv + vec2(0.0, halfpixel.y * 2.0) * offset);
    sum += texture2D(inTexture, uv + vec2(halfpixel.x, halfpixel.y) * offset) * 2.0;
    sum += texture2D(inTexture, uv + vec2(halfpixel.x * 2.0, 0.0) * offset);
    sum += texture2D(inTexture, uv + vec2(halfpixel.x, -halfpixel.y) * offset) * 2.0;
    sum += texture2D(inTexture, uv + vec2(0.0, -halfpixel.y * 2.0) * offset);
    sum += texture2D(inTexture, uv + vec2(-halfpixel.x, -halfpixel.y) * offset) * 2.0;

    gl_FragColor = vec4(sum.rgb /12.0, mix(1.0, texture2D(textureToCheck, gl_TexCoord[0].st).a, check));
}
"""

        private val kawaseDown = """#version 120

uniform sampler2D inTexture;
uniform vec2 offset, halfpixel, iResolution;

void main() {
    vec2 uv = vec2(gl_FragCoord.xy / iResolution);
    vec4 sum = texture2D(inTexture, gl_TexCoord[0].st) * 4.0;
    sum += texture2D(inTexture, uv - halfpixel.xy * offset);
    sum += texture2D(inTexture, uv + halfpixel.xy * offset);
    sum += texture2D(inTexture, uv + vec2(halfpixel.x, -halfpixel.y) * offset);
    sum += texture2D(inTexture, uv - vec2(halfpixel.x, -halfpixel.y) * offset);
    gl_FragColor = vec4(sum.rgb * .125, 1.0);
}
"""

        private val gaussian = """#version 120

uniform sampler2D textureIn;
uniform vec2 texelSize, direction;
uniform float radius;
uniform float weights[256];

#define offset texelSize * direction

void main() {
    vec3 blr = texture2D(textureIn, gl_TexCoord[0].st).rgb * weights[0];

    for (float f = 1.0; f <= radius; f++) {
        blr += texture2D(textureIn, gl_TexCoord[0].st + f * offset).rgb * (weights[int(abs(f))]);
        blr += texture2D(textureIn, gl_TexCoord[0].st - f * offset).rgb * (weights[int(abs(f))]);
    }

    gl_FragColor = vec4(blr, 1.0);
}
"""

        private val bloom = """#version 120

uniform sampler2D inTexture, textureToCheck;
uniform vec2 texelSize, direction;
uniform float radius;
uniform float weights[256];

#define offset texelSize * direction

float smoothAlpha(sampler2D tex, vec2 uv, float threshold, float smoothing) {
    float alpha = texture2D(tex, uv).a;
    float edge0 = threshold * (1.0 - smoothing);
    float edge1 = threshold * (1.0 + smoothing);
    alpha = smoothstep(edge0, edge1, alpha);
    return alpha;
}

void main() {
    if (direction.y > 0 && texture2D(textureToCheck, gl_TexCoord[0].st).a != 0.0) discard;
    float blr = smoothAlpha(inTexture, gl_TexCoord[0].st, 0.5, 0.5) * weights[0];

    for (float f = 1.0; f <= radius; f++) {
        blr += smoothAlpha(inTexture, gl_TexCoord[0].st + f * offset, 0.5, 0.5) * (weights[int(abs(f))]);
        blr += smoothAlpha(inTexture, gl_TexCoord[0].st - f * offset, 0.5, 0.5) * (weights[int(abs(f))]);
    }

    gl_FragColor = vec4(0.0, 0.0, 0.0, blr);
}
"""

        private val gradientMask = """#version 120

uniform vec2 location, rectSize;
uniform sampler2D tex;
uniform vec3 color1, color2, color3, color4;
uniform float alpha;

#define NOISE .5/255.0

vec3 createGradient(vec2 coords, vec3 color1, vec3 color2, vec3 color3, vec3 color4){
    vec3 color = mix(mix(color1.rgb, color2.rgb, coords.y), mix(color3.rgb, color4.rgb, coords.y), coords.x);
    //Dithering the color from https://shader-tutorial.dev/advanced/color-banding-dithering/
    color += mix(NOISE, -NOISE, fract(sin(dot(coords.xy, vec2(12.9898,78.233))) * 43758.5453));
    return color;
}

void main() {
    vec2 coords = (gl_FragCoord.xy - location) / rectSize;
    float texColorAlpha = texture2D(tex, gl_TexCoord[0].st).a;
    gl_FragColor = vec4(createGradient(coords, color1, color2, color3, color4), texColorAlpha * alpha);
}"""

        private val mask = """#version 120

uniform vec2 location, rectSize;
uniform sampler2D u_texture, u_texture2;
void main() {
    vec2 coords = (gl_FragCoord.xy - location) / rectSize;
    float texColorAlpha = texture2D(u_texture, gl_TexCoord[0].st).a;
    vec3 tex2Color = texture2D(u_texture2, gl_TexCoord[0].st).rgb;
    gl_FragColor = vec4(tex2Color, texColorAlpha);
}"""

        private val gradient = """#version 120

uniform vec2 location, rectSize;
uniform sampler2D tex;
uniform vec4 color1, color2, color3, color4;
#define NOISE .5/255.0

vec4 createGradient(vec2 coords, vec4 color1, vec4 color2, vec4 color3, vec4 color4){
    vec4 color = mix(mix(color1, color2, coords.y), mix(color3, color4, coords.y), coords.x);
    //Dithering the color
    // from https://shader-tutorial.dev/advanced/color-banding-dithering/
    color += mix(NOISE, -NOISE, fract(sin(dot(coords.xy, vec2(12.9898, 78.233))) * 43758.5453));
    return color;
}

void main() {
    vec2 coords = (gl_FragCoord.xy - location) / rectSize;
    gl_FragColor = createGradient(coords, color1, color2, color3, color4);
}"""

        private val gradientround = """#version 120

uniform vec2 u_size;
uniform float u_radius;
uniform vec4 u_first_color;
uniform vec4 u_second_color;
uniform int u_direction;

void main(void)
{
    vec2 tex_coord = gl_TexCoord[0].st;
    vec4 color = mix(u_first_color, u_second_color, u_direction > 0.0 ? tex_coord.y : tex_coord.x);
    gl_FragColor = vec4(color.rgb, color.a * smoothstep(1.0, 0.0, length(max((abs(tex_coord - 0.5) + 0.5) * u_size - u_size + u_radius, 0.0)) - u_radius + 0.5));
}"""

        private val roundedRectGradient = """#version 120

uniform vec2 location, rectSize;
uniform vec4 color1, color2, color3, color4;
uniform float radius;

#define NOISE .5/255.0

float roundSDF(vec2 p, vec2 b, float r) {
    return length(max(abs(p) - b , 0.0)) - r;
}

vec4 createGradient(vec2 coords, vec4 color1, vec4 color2, vec4 color3, vec4 color4){
    vec4 color = mix(mix(color1, color2, coords.y), mix(color3, color4, coords.y), coords.x);
    //Dithering the color
    // from https://shader-tutorial.dev/advanced/color-banding-dithering/
    color += mix(NOISE, -NOISE, fract(sin(dot(coords.xy, vec2(12.9898, 78.233))) * 43758.5453));
    return color;
}

void main() {
    vec2 st = gl_TexCoord[0].st;
    vec2 halfSize = rectSize * .5;
    
   // use the bottom leftColor as the alpha
    float smoothedAlpha =  (1.0-smoothstep(0.0, 2., roundSDF(halfSize - (gl_TexCoord[0].st * rectSize), halfSize - radius - 1., radius)));
    vec4 gradient = createGradient(st, color1, color2, color3, color4);
    gl_FragColor = vec4(gradient.rgb, gradient.a * smoothedAlpha);
}"""

        private val roundedRect = """#version 120

uniform vec2 location, rectSize;
uniform vec4 color;
uniform float radius;
uniform bool blur;

float roundSDF(vec2 p, vec2 b, float r) {
    return length(max(abs(p) - b, 0.0)) - r;
}


void main() {
    vec2 rectHalf = rectSize * .5;
    // Smooth the result (free antialiasing).
    float smoothedAlpha =  (1.0-smoothstep(0.0, 1.0, roundSDF(rectHalf - (gl_TexCoord[0].st * rectSize), rectHalf - radius - 1., radius))) * color.a;
    gl_FragColor = vec4(color.rgb, smoothedAlpha);// mix(quadColor, shadowColor, 0.0);

}"""

        private val arc = """#version 120

#define PI 3.14159265359

uniform float radialSmoothness, radius, borderThickness, progress;
uniform int change;
uniform vec4 color;
uniform vec2 pos;

void main() {
    vec2 st = gl_FragCoord.xy - (pos + radius + borderThickness);
  //  vec2 rp = st * 2. - 1.;

    float circle = sqrt(dot(st,st));

    //Radius minus circle to get just the outline
    float smoothedAlpha = 1.0 - smoothstep(borderThickness, borderThickness + 3., abs(radius-circle));
    vec4 circleColor = vec4(color.rgb, smoothedAlpha * color.a);

    gl_FragColor = mix(vec4(circleColor.rgb, 0.0), circleColor, smoothstep(0., radialSmoothness, change * (atan(st.y,st.x) - (progress-.5) * PI * 2.5)));
}"""

        private val clickgui = """#define M_PI 3.1415926535897932384626433832795
#define M_TWO_PI (2.0 * M_PI)

uniform float iTime;
uniform vec2 iResolution;
uniform vec4 iMouse;
uniform vec2 rectsize;
float rand(vec2 n) {
    return fract(sin(dot(n, vec2(12.9898,12.1414))) * 83758.5453);
}

float noise(vec2 n) {
    const vec2 d = vec2(0.0, 1.0);
    vec2 b = floor(n);
    vec2 f = smoothstep(vec2(0.0), vec2(1.0), fract(n));
    return mix(mix(rand(b), rand(b + d.yx), f.x), mix(rand(b + d.xy), rand(b + d.yy), f.x), f.y);
}

vec3 ramp(float t) {
    return t <= .5 ? vec3( 1. - t * 1.4, .2, 1.05 ) / t : vec3( .3 * (1. - t) * 2., .2, 1.05 ) / t;
}
vec2 polarMap(vec2 uv, float shift, float inner) {

    uv = vec2(0.5) - uv;

    float px = 1.0 - fract(atan(uv.y, uv.x) / 6.28 + 0.25) + shift;
    float py = (sqrt(uv.x * uv.x + uv.y * uv.y) * (1.0 + inner * 2.0) - inner) * 2.0;

    return vec2(px, py);
}
float fire(vec2 n) {
    return noise(n) + noise(n * 2.1) * .6 + noise(n * 5.4) * .42;
}

float shade(vec2 uv, float t) {
    uv.x += uv.y < .5 ? 23.0 + t * .035 : -11.0 + t * .03;
    uv.y = abs(uv.y - .5);
    uv.x *= 35.0;

    float q = fire(uv - t * .013) / 2.0;
    vec2 r = vec2(fire(uv + q / 2.0 + t - uv.x - uv.y), fire(uv + q - t));

    return pow((r.y + r.y) * max(.0, uv.y) + .1, 4.0);
}

vec3 color(float grad) {

    float m2 = iMouse.z < 0.0001 ? 1.15 : iMouse.y * 3.0 / iResolution.y;
    grad =sqrt( grad);
    vec3 color = vec3(1.0 / (pow(vec3(0.5, 0.0, .1) + 2.61, vec3(2.0))));
    vec3 color2 = color;
    color = ramp(grad);
    color /= (m2 + max(vec3(0), color));

    return color;

}

void mainImage(out vec4 fragColor, in vec2 fragCoord) {

    float m1 = iMouse.z < 0.0001 ? 3.6 : iMouse.x * 5.0 / iResolution.x;

    float t = iTime;
    vec2 uv = fragCoord.xy / rectsize.xy;
    float ff = 1.0 - uv.y;
    uv.x -= (rectsize.x / rectsize.y - 1.0) / 2.0;
    vec2 uv2 = uv;
    uv2.y = 1.0 - uv2.y;
    uv = polarMap(uv, 1.3, m1);
    uv2 = polarMap(uv2, 1.9, m1);

    vec3 c1 = color(shade(uv, t)) * ff;
    vec3 c2 = color(shade(uv2, t)) * (1.0 - ff);

    fragColor = vec4(c1 + c2, 1.0);
}"""
    }
}
