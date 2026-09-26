/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.render

import net.airplus.event.*
import net.airplus.features.module.Category
import net.airplus.features.module.Module
import net.airplus.utils.extensions.*
import net.airplus.utils.movement.MovementUtils.strafe
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.Vec3

object FreeCam : Module("FreeCam", Category.RENDER, gameDetecting = false) {

    private val speed by float("Speed", 0.8f, 0.1f..2f)

    private val allowCameraInteract by boolean("AllowCameraInteract", true)
    private val allowRotationChange by boolean("AllowRotationChange", true)

    data class PositionPair(var pos: Vec3, var lastPos: Vec3, var extraPos: Vec3 = lastPos) {
        operator fun plusAssign(velocity: Vec3) {
            extraPos = pos
            lastPos = pos
            pos += velocity
        }

        fun interpolate(tickDelta: Float) = Vec3(
            lastPos.xCoord + (pos.xCoord - lastPos.xCoord) * tickDelta,
            lastPos.yCoord + (pos.yCoord - lastPos.yCoord) * tickDelta,
            lastPos.zCoord + (pos.zCoord - lastPos.zCoord) * tickDelta
        )

    }

    override fun onEnable() {
        updatePosition(Vec3_ZERO)
    }

    override fun onDisable() {
        pos = null
        originalPos = null
        invalidateMemo()
    }

    val onInputEvent = handler<MovementInputEvent> { event ->
        val speed = speed.toDouble()

        val yAxisMovement = when {
            event.originalInput.jump -> 1.0f
            event.originalInput.sneak -> -1.0f
            else -> 0.0f
        }

        val velocity = Vec3_ZERO.apply {
            strafe(speed = speed, moveCheck = !event.originalInput.isMoving)

            this.yCoord = yAxisMovement * speed
        }

        updatePosition(velocity)

        event.originalInput.reset()
    }

    private var originalPos: PositionPair? = null
    private var pos: PositionPair? = null

    // Frame-level memoization: useModifiedPosition() is invoked per rendered entity,
    // so we cache the resolved position until the player's real position changes.
    private var memoValid = false
    private var memoCurrX = 0.0
    private var memoCurrY = 0.0
    private var memoCurrZ = 0.0
    private var memoPrevX = 0.0
    private var memoPrevY = 0.0
    private var memoPrevZ = 0.0
    private var memoLastX = 0.0
    private var memoLastY = 0.0
    private var memoLastZ = 0.0
    private var memoResult: PositionPair? = null

    private fun invalidateMemo() {
        memoValid = false
        memoResult = null
    }

    private fun updatePosition(velocity: Vec3) {
        val player = mc.thePlayer ?: return

        pos = (pos ?: PositionPair(player.currPos, player.currPos)).apply { this += velocity }
        invalidateMemo()
    }

    fun useModifiedPosition() {
        val player = mc.thePlayer ?: return

        val currX = player.posX
        val currY = player.posY
        val currZ = player.posZ
        val prevX = player.prevPosX
        val prevY = player.prevPosY
        val prevZ = player.prevPosZ
        val lastX = player.lastTickPosX
        val lastY = player.lastTickPosY
        val lastZ = player.lastTickPosZ

        if (!memoValid || currX != memoCurrX || currY != memoCurrY || currZ != memoCurrZ
            || prevX != memoPrevX || prevY != memoPrevY || prevZ != memoPrevZ
            || lastX != memoLastX || lastY != memoLastY || lastZ != memoLastZ
        ) {
            val event = CameraPositionEvent(player.currPos, player.prevPos, player.lastTickPos)
            EventManager.call(event)

            memoCurrX = currX; memoCurrY = currY; memoCurrZ = currZ
            memoPrevX = prevX; memoPrevY = prevY; memoPrevZ = prevZ
            memoLastX = lastX; memoLastY = lastY; memoLastZ = lastZ
            memoResult = event.result ?: pos
            memoValid = true
        }

        val data = memoResult ?: return

        originalPos = PositionPair(player.currPos, player.prevPos, player.lastTickPos)
        player.setPosAndPrevPos(data.pos, data.lastPos, data.extraPos)
    }

    fun restoreOriginalPosition() {
        val player = mc.thePlayer ?: return

        originalPos?.run { player.setPosAndPrevPos(pos, lastPos, extraPos) }
    }

    fun renderPlayerFromAllPerspectives(entity: EntityLivingBase) =
        handleEvents() && entity == mc.thePlayer || entity.isPlayerSleeping

    fun modifyRaycast(original: Vec3, entity: Entity, tickDelta: Float): Vec3 {
        if (!handleEvents() || entity != mc.thePlayer || !allowCameraInteract) {
            return original
        }

        return pos?.interpolate(tickDelta)?.apply { yCoord += entity.eyeHeight } ?: original
    }

    fun shouldDisableRotations() = handleEvents() && !allowRotationChange

    val onWorldChange = handler<WorldEvent> {
        // Disable when world changed
        state = false
    }

}