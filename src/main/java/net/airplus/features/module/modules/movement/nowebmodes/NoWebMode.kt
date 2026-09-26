package net.airplus.features.module.modules.movement.nowebmodes

import net.airplus.utils.client.MinecraftInstance

open class NoWebMode(val modeName: String) : MinecraftInstance {
    open fun onUpdate() {}
}
