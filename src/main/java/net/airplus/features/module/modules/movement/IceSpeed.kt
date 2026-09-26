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
import net.airplus.utils.block.block
import net.airplus.utils.extensions.isMoving
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos

object IceSpeed : Module("IceSpeed", Category.MOVEMENT) {
    private val mode by choices("Mode", arrayOf("NCP", "AAC", "Spartan"), "NCP")
    override fun onEnable() {
        if (mode == "NCP") {
            Blocks.ice.slipperiness = 0.39f
            Blocks.packed_ice.slipperiness = 0.39f
        }
        super.onEnable()
    }

    val onUpdate = handler<UpdateEvent> {
        val mode = mode
        if (mode == "NCP") {
            Blocks.ice.slipperiness = 0.39f
            Blocks.packed_ice.slipperiness = 0.39f
        } else {
            Blocks.ice.slipperiness = 0.98f
            Blocks.packed_ice.slipperiness = 0.98f
        }

        val thePlayer = mc.thePlayer ?: return@handler

        if (!thePlayer.onGround || thePlayer.isOnLadder || thePlayer.isSneaking || !thePlayer.isSprinting || !thePlayer.isMoving) {
            return@handler
        }

        if (thePlayer.position.down().block.let { it != Blocks.ice && it != Blocks.packed_ice }) {
            return@handler
        }

        when (mode) {
            "AAC" -> {
                thePlayer.motionX *= 1.342
                thePlayer.motionZ *= 1.342
                Blocks.ice.slipperiness = 0.6f
                Blocks.packed_ice.slipperiness = 0.6f
            }

            "Spartan" -> {
                val upBlock = BlockPos(thePlayer).up(2).block

                if (upBlock != Blocks.air) {
                    thePlayer.motionX *= 1.342
                    thePlayer.motionZ *= 1.342
                } else {
                    thePlayer.motionX *= 1.18
                    thePlayer.motionZ *= 1.18
                }

                Blocks.ice.slipperiness = 0.6f
                Blocks.packed_ice.slipperiness = 0.6f
            }
        }
    }

    override fun onDisable() {
        Blocks.ice.slipperiness = 0.98f
        Blocks.packed_ice.slipperiness = 0.98f
        super.onDisable()
    }
}