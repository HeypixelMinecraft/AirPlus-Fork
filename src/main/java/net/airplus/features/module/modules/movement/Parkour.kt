/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.movement

import net.airplus.event.MovementInputEvent
import net.airplus.event.handler
import net.airplus.features.module.Category
import net.airplus.features.module.Module
import net.airplus.utils.extensions.isMoving
import net.airplus.utils.simulation.SimulatedPlayer

object Parkour : Module("Parkour", Category.MOVEMENT, subjective = true, gameDetecting = false) {

    val onMovementInput = handler<MovementInputEvent> { event ->
        val thePlayer = mc.thePlayer ?: return@handler

        val simPlayer = SimulatedPlayer.fromClientPlayer(event.originalInput)

        simPlayer.tick()

        if (thePlayer.isMoving && thePlayer.onGround && !thePlayer.isSneaking && !mc.gameSettings.keyBindSneak.isKeyDown && !simPlayer.onGround) {
            event.originalInput.jump = true
        }

    }
}
