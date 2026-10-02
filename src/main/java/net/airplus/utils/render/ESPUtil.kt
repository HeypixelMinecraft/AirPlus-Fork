/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.utils.render

import net.airplus.utils.client.MinecraftInstance
import net.minecraft.client.renderer.GLAllocation
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.entity.Entity
import net.minecraft.util.AxisAlignedBB
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.util.glu.GLU
import org.lwjgl.util.vector.Vector3f
import org.lwjgl.util.vector.Vector4f
import java.nio.FloatBuffer
import java.nio.IntBuffer

/**
 * 从 SilenceFix 移植的 2D 投影工具（NameTags2 使用）。
 */
object ESPUtil : MinecraftInstance {

    private val frustum = Frustum()
    private val windPos = BufferUtils.createFloatBuffer(4)
    private val intBuffer = GLAllocation.createDirectIntBuffer(16)
    private val floatBuffer1 = GLAllocation.createDirectFloatBuffer(16)
    private val floatBuffer2 = GLAllocation.createDirectFloatBuffer(16)

    fun isInView(ent: Entity): Boolean {
        val view = mc.renderViewEntity ?: return false
        frustum.setPosition(view.posX, view.posY, view.posZ)
        return frustum.isBoundingBoxInFrustum(ent.entityBoundingBox) || ent.ignoreFrustumCheck
    }

    fun projectOn2D(x2: Float, y2: Float, z: Float, scaleFactor: Int): Vector3f? {
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, floatBuffer1)
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, floatBuffer2)
        GL11.glGetInteger(GL11.GL_VIEWPORT, intBuffer)
        if (GLU.gluProject(x2, y2, z, floatBuffer1, floatBuffer2, intBuffer, windPos)) {
            return Vector3f(
                windPos.get(0) / scaleFactor.toFloat(),
                (mc.displayHeight - windPos.get(1)) / scaleFactor.toFloat(),
                windPos.get(2)
            )
        }
        return null
    }

    fun getInterpolatedPos(entity: Entity): DoubleArray {
        val ticks = mc.timer.renderPartialTicks
        val renderManager = mc.renderManager
        return doubleArrayOf(
            interpolate(entity.lastTickPosX, entity.posX, ticks) - renderManager.renderPosX,
            interpolate(entity.lastTickPosY, entity.posY, ticks) - renderManager.renderPosY,
            interpolate(entity.lastTickPosZ, entity.posZ, ticks) - renderManager.renderPosZ
        )
    }

    fun getInterpolatedBoundingBox(entity: Entity): AxisAlignedBB {
        val renderingEntityPos = getInterpolatedPos(entity)
        val entityRenderWidth = (entity.width / 1.5f).toDouble()
        return AxisAlignedBB(
            renderingEntityPos[0] - entityRenderWidth, renderingEntityPos[1], renderingEntityPos[2] - entityRenderWidth,
            renderingEntityPos[0] + entityRenderWidth, renderingEntityPos[1] + entity.height + (if (entity.isSneaking) -0.3 else 0.18),
            renderingEntityPos[2] + entityRenderWidth
        ).expand(0.15, 0.15, 0.15)
    }

    fun getEntityPositionsOn2D(entity: Entity): Vector4f {
        val bb = getInterpolatedBoundingBox(entity)
        val yOffset = 0.0f
        val vectors = listOf(
            Vector3f(bb.minX.toFloat(), bb.minY.toFloat(), bb.minZ.toFloat()),
            Vector3f(bb.minX.toFloat(), bb.maxY.toFloat() - yOffset, bb.minZ.toFloat()),
            Vector3f(bb.maxX.toFloat(), bb.minY.toFloat(), bb.minZ.toFloat()),
            Vector3f(bb.maxX.toFloat(), bb.maxY.toFloat() - yOffset, bb.minZ.toFloat()),
            Vector3f(bb.minX.toFloat(), bb.minY.toFloat(), bb.maxZ.toFloat()),
            Vector3f(bb.minX.toFloat(), bb.maxY.toFloat() - yOffset, bb.maxZ.toFloat()),
            Vector3f(bb.maxX.toFloat(), bb.minY.toFloat(), bb.maxZ.toFloat()),
            Vector3f(bb.maxX.toFloat(), bb.maxY.toFloat() - yOffset, bb.maxZ.toFloat())
        )
        val entityPos = Vector4f(Float.MAX_VALUE, Float.MAX_VALUE, -1.0f, -1.0f)
        val sr = net.minecraft.client.gui.ScaledResolution(mc)
        for (vector3f in vectors) {
            val projected = projectOn2D(vector3f.x, vector3f.y, vector3f.z, sr.scaleFactor)
                ?: continue
            if (projected.z < 0.0 || projected.z >= 1.0) continue
            entityPos.x = minOf(projected.x, entityPos.x)
            entityPos.y = minOf(projected.y, entityPos.y)
            entityPos.z = maxOf(projected.x, entityPos.z)
            entityPos.w = maxOf(projected.y, entityPos.w)
        }
        return entityPos
    }

    private fun interpolate(old: Double, now: Double, partialTicks: Float): Double =
        old + (now - old) * partialTicks
}
