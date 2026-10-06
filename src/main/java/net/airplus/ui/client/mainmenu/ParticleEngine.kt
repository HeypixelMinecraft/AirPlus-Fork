/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.ui.client.mainmenu

import net.airplus.utils.client.MinecraftInstance
import net.airplus.utils.render.RenderUtils
import net.minecraft.client.gui.ScaledResolution
import java.awt.Color
import java.util.Random

class ParticleEngine : MinecraftInstance {
    private val particles = mutableListOf<Particle>()
    private var prevWidth = 0
    private var prevHeight = 0

    // 复用 Random 实例，避免每次 create 都构造新对象
    private val random = Random()

    init {
        create()
    }

    fun render() {
        if (particles.isEmpty() || prevWidth != mc.displayWidth || prevHeight != mc.displayHeight) {
            particles.clear()
            create()
        }

        prevWidth = mc.displayWidth
        prevHeight = mc.displayHeight

        for (particle in particles) {
            particle.fall()
            particle.interpolation()

            // Simple particle rendering without connections for now
            RenderUtils.drawFilledCircle(particle.x.toInt(), particle.y.toInt(), particle.size, Color(255, 255, 255, 180))
        }
    }

    private fun create() {
        for (i in 0 until 100) {
            particles.add(Particle(random.nextInt(mc.displayWidth).toFloat(), random.nextInt(mc.displayHeight).toFloat(), random))
        }
    }

    inner class Particle(var x: Float, var y: Float, random: Random) {
        val size: Float = genRandom()
        private val ySpeed = random.nextFloat() * 2
        private val xSpeed = random.nextFloat() * 2

        fun interpolation() {
            val f = 0.5f
            if (lint1(f) != lint2(f)) {
                y -= f
                x -= f
            }
        }

        fun fall() {
            // 仅在需要重置位置时构造 ScaledResolution
            y += ySpeed
            x += xSpeed

            if (y > mc.displayHeight.toFloat()) y = 1f
            if (x > mc.displayWidth.toFloat()) x = 1f
            if (x < 1f || y < 1f) {
                val scaledResolution = ScaledResolution(mc)
                if (x < 1) x = scaledResolution.scaledWidth.toFloat()
                if (y < 1) y = scaledResolution.scaledHeight.toFloat()
            }
        }

        private fun lint1(f: Float): Float {
            return 1.02f * (1f - f) + f
        }

        private fun lint2(f: Float): Float {
            return 1.02f + f * (1.0f - 1.02f)
        }

        private fun genRandom(): Float {
            return (0.3f + Math.random() * 0.3f).toFloat()
        }
    }
}
