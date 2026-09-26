// skid Leader-Lite
/*
 * Air Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 */
package net.airplus.features.module.modules.render

import net.airplus.features.module.Category
import net.airplus.features.module.Module

/**
 * BetterFPS
 *
 * Migrated from Leader-Lite. Skips forced [System.gc] calls during world load
 * and integrated server launch to reduce load-time stutter.
 *
 * The actual interception is performed in
 * [net.airplus.injection.forge.mixins.client.MixinMinecraft].
 */
object BetterFPS : Module("BetterFPS", Category.CLIENT, gameDetecting = false) {

    val fastLoad by boolean("FastLoad", true)

    var using = false
        private set

    override fun onEnable() {
        using = true
    }

    override fun onDisable() {
        using = false
    }
}
