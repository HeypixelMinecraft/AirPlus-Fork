/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.render

import net.airplus.event.MotionEvent
import net.airplus.event.handler
import net.airplus.features.module.Category
import net.airplus.features.module.Module

object NoBob : Module("NoBob", Category.RENDER, gameDetecting = false) {

    val onMotion = handler<MotionEvent> {
        mc.thePlayer?.distanceWalkedModified = -1f
    }
}
