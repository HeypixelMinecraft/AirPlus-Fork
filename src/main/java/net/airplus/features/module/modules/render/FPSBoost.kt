/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.render

import net.airplus.features.module.Category
import net.airplus.features.module.Module

object FPSBoost : Module(
    "FPSBoost", Category.CLIENT,
    defaultState = true, gameDetecting = false
) {
    // Chunk rebuild worker threads, replaces vanilla's hardcoded 2.
    // Applied on the first rendered frame of a world - changing it requires rejoining a world or restarting.
    // The underlying IntValue is exposed so the Sodium options GUI can bind a slider to it.
    val chunkWorkersSetting = int("ChunkWorkers", (Runtime.getRuntime().availableProcessors() - 1).coerceIn(2, 16), 2..16)
    val chunkWorkers by chunkWorkersSetting

    // Stop the per-tick torch flicker, lightmap only refreshes every 500ms fallback
    val staticLightmap by boolean("StaticLightmap", true)

    // Clouds: Off skips cloud geometry rebuild every frame
    val clouds by choices("Clouds", arrayOf("Off", "Vanilla"), "Off")

    // Skip rain/snow quads and rain particles
    val noWeather by boolean("NoWeather", true)

    // Cull TileEntity rendering beyond this distance (0 = vanilla unlimited)
    val tileEntityDistance by int("TileEntityDistance", 48, 0..128)

    // Cull entity rendering beyond this distance (0 = vanilla)
    val entityDistance by int("EntityDistance", 0, 0..256)

    // Block particle spawning beyond this distance
    val particleDistance by int("ParticleDistance", 16, 8..64)

    // Maximum amount of simultaneously existing particles
    val particleLimit by int("ParticleLimit", 1000, 100..4000)

    // Reuse raycast result while view, position and world state are unchanged
    val mouseOverCache by boolean("MouseOverCache", true)

    // Animated texture (water/lava/fire) update rate
    val animationThrottle by choices("AnimationThrottle", arrayOf("Half", "Vanilla", "Off"), "Half")
}
