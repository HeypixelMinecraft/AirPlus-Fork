/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.misc

import net.airplus.features.module.Category
import net.airplus.features.module.Module
import net.airplus.utils.extensions.rotation
import net.airplus.utils.rotation.AlwaysRotationSettings
import net.airplus.utils.rotation.Rotation
import net.airplus.utils.rotation.RotationUtils.currentRotation
import net.airplus.utils.rotation.RotationUtils.setTargetRotation
import net.airplus.utils.timing.WaitTickUtils
import net.minecraft.entity.player.EntityPlayer

object NoRotateSet : Module("NoRotateSet", Category.MISC, gameDetecting = false) {
    var savedRotation = Rotation.ZERO

    private val ignoreOnSpawn by boolean("IgnoreOnSpawn", false)
    val affectRotation by boolean("AffectRotation", true)

    private val ticksUntilStart = intRange("TicksUntilStart", 0..0, 0..20) { affectRotation }

    private val options = AlwaysRotationSettings(this) { affectRotation }.apply {
        withoutKeepRotation()
        applyServerSideValue.hideWithState(true)
        resetTicksValue.excludeWithState(1)
    }

    fun shouldModify(player: EntityPlayer) = handleEvents() && (!ignoreOnSpawn || player.ticksExisted != 0)

    fun rotateBackToPlayerRotation() {
        val player = mc.thePlayer ?: return

        currentRotation = player.rotation

        WaitTickUtils.schedule(ticksUntilStart.random, this)

        setTargetRotation(savedRotation, options = options)
    }
}