/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.movement

import net.airplus.event.UpdateEvent
import net.airplus.event.handler
import net.airplus.features.module.Category
import net.airplus.features.module.Module
import net.airplus.features.module.modules.movement.nowebmodes.aac.AAC
import net.airplus.features.module.modules.movement.nowebmodes.aac.LAAC
import net.airplus.features.module.modules.movement.nowebmodes.intave.IntaveNew
import net.airplus.features.module.modules.movement.nowebmodes.intave.IntaveOld
import net.airplus.features.module.modules.movement.nowebmodes.other.None
import net.airplus.features.module.modules.movement.nowebmodes.other.OldGrim
import net.airplus.features.module.modules.movement.nowebmodes.other.Rewi

object NoWeb : Module("NoWeb", Category.MOVEMENT) {

    private val noWebModes = arrayOf(
        // Vanilla
        None,

        // AAC
        AAC, LAAC,

        // Intave
        IntaveOld,
        IntaveNew,

        // Other
        Rewi,
        OldGrim
    )

    private val modes = noWebModes.map { it.modeName }.toTypedArray()

    val mode by choices(
        "Mode", modes, "None"
    )

    val onUpdate = handler<UpdateEvent> {
        modeModule.onUpdate()
    }

    override val tag
        get() = mode

    private val modeModule
        get() = noWebModes.find { it.modeName == mode }!!
}
