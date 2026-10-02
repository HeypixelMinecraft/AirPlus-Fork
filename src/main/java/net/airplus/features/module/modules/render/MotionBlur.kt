/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.render

import net.airplus.features.module.Module
import net.airplus.features.module.Category

/**
 * 从 SilenceFix 移植。实际效果在 MixinMinecraft 的 runTick TAIL 挂载：
 * 开启时加载 shaders/post/motion_blur.json，Phosphor uniform = 1 - min(amount/10, 0.9)。
 */
object MotionBlur : Module("MotionBlur", Category.RENDER) {

    val blurAmount by float("Amount", 7f, 0f..10f)
}
