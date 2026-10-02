/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 *
 * Ported from SilenceFix MoBendsMod（"MoreBends"，平滑动作）。
 *
 * 动画引擎（dev.xinxin.utils.mobends 包）从 SilenceFix 原样迁移；
 * 渲染接管点在 MixinRendererLivingEntity（原版在补丁版 RendererLivingEntity 中调用）。
 */
package net.airplus.features.module.modules.render

import net.airplus.event.Render3DEvent
import net.airplus.event.TickEndEvent
import net.airplus.event.handler
import net.airplus.features.module.Category
import net.airplus.features.module.Module
import dev.xinxin.utils.mobends.AnimatedEntity
import dev.xinxin.utils.mobends.client.renderer.entity.RenderBendsPlayer
import dev.xinxin.utils.mobends.client.renderer.entity.RenderBendsSpider
import dev.xinxin.utils.mobends.client.renderer.entity.RenderBendsZombie
import dev.xinxin.utils.mobends.data.Data_Player
import dev.xinxin.utils.mobends.data.Data_Spider
import dev.xinxin.utils.mobends.data.Data_Zombie
import dev.xinxin.utils.mobends.data.EntityData
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.monster.EntitySpider
import net.minecraft.entity.monster.EntityZombie
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ResourceLocation
import org.lwjgl.util.vector.ReadableVector3f
import org.lwjgl.util.vector.Vector3f

object MoBendsMod : Module("MoreBends", Category.RENDER) {

    @JvmField
    val texture_NULL = ResourceLocation("mobends", "textures/white.png")

    @JvmField
    var partialTicks = 0f

    @JvmField
    var ticks = 0f

    @JvmField
    var ticksPerFrame = 0f

    override fun onEnable() {
        AnimatedEntity.register()
    }

    val onRender3D = handler<Render3DEvent> { e ->
        val world = mc.theWorld ?: return@handler

        for (i in Data_Player.dataList.indices) {
            Data_Player.dataList[i].update(e.partialTicks)
        }
        for (i in Data_Zombie.dataList.indices) {
            Data_Zombie.dataList[i].update(e.partialTicks)
        }
        for (i in Data_Spider.dataList.indices) {
            Data_Spider.dataList[i].update(e.partialTicks)
        }

        val player = mc.thePlayer
        if (player != null) {
            val newTicks = player.ticksExisted + e.partialTicks
            if (!world.isRemote || !mc.isGamePaused()) {
                ticksPerFrame = Math.min(Math.max(0f, newTicks - ticks), 1f)
                ticks = newTicks
            } else {
                ticksPerFrame = 0f
            }
        }
    }

    val onTick = handler<TickEndEvent> {
        val world = mc.theWorld ?: return@handler

        // Player
        val playerIterator = Data_Player.dataList.iterator()
        while (playerIterator.hasNext()) {
            val data = playerIterator.next()
            val entity = world.getEntityByID(data.entityID)
            if (entity != null) {
                if (!data.entityType.equals(entity.name, ignoreCase = true)) {
                    playerIterator.remove()
                    Data_Player.add(Data_Player(entity.entityId))
                    continue
                }
                data.motion_prev.set(data.motion as ReadableVector3f)
                data.motion.x = (entity.posX - data.position.x).toFloat()
                data.motion.y = (entity.posY - data.position.y).toFloat()
                data.motion.z = (entity.posZ - data.position.z).toFloat()
                data.position = Vector3f(entity.posX.toFloat(), entity.posY.toFloat(), entity.posZ.toFloat())
            } else {
                playerIterator.remove()
            }
        }

        // Zombie
        val zombieIterator = Data_Zombie.dataList.iterator()
        while (zombieIterator.hasNext()) {
            val data = zombieIterator.next()
            val entity = world.getEntityByID(data.entityID)
            if (entity != null) {
                if (!data.entityType.equals(entity.name, ignoreCase = true)) {
                    zombieIterator.remove()
                    Data_Zombie.add(Data_Zombie(entity.entityId))
                    continue
                }
                data.motion_prev.set(data.motion as ReadableVector3f)
                data.motion.x = (entity.posX - data.position.x).toFloat()
                data.motion.y = (entity.posY - data.position.y).toFloat()
                data.motion.z = (entity.posZ - data.position.z).toFloat()
                data.position = Vector3f(entity.posX.toFloat(), entity.posY.toFloat(), entity.posZ.toFloat())
            } else {
                zombieIterator.remove()
            }
        }

        // Spider
        val spiderIterator = Data_Spider.dataList.iterator()
        while (spiderIterator.hasNext()) {
            val data = spiderIterator.next()
            val entity = world.getEntityByID(data.entityID)
            if (entity != null) {
                if (!data.entityType.equals(entity.name, ignoreCase = true)) {
                    spiderIterator.remove()
                    Data_Spider.add(Data_Spider(entity.entityId))
                    continue
                }
                data.motion_prev.set(data.motion as ReadableVector3f)
                data.motion.x = (entity.posX - data.position.x).toFloat()
                data.motion.y = (entity.posY - data.position.y).toFloat()
                data.motion.z = (entity.posZ - data.position.z).toFloat()
                data.position = Vector3f(entity.posX.toFloat(), entity.posY.toFloat(), entity.posZ.toFloat())
            } else {
                spiderIterator.remove()
            }
        }
    }

    /**
     * 由 MixinRendererLivingEntity 在 doRender HEAD 调用。
     * 返回 true 表示已由 MoBends 渲染器接管，应取消原版渲染。
     */
    fun onRenderLivingEvent(
        renderer: net.minecraft.client.renderer.entity.RendererLivingEntity<*>,
        entity: EntityLivingBase,
        x: Double,
        y: Double,
        z: Double,
        entityYaw: Float,
        partialTicks: Float,
    ): Boolean {
        if (!state || renderer is RenderBendsPlayer || renderer is RenderBendsZombie || renderer is RenderBendsSpider) {
            return false
        }

        val animatedEntity = AnimatedEntity.getByEntity(entity)
        if (animatedEntity != null && (entity is EntityPlayer || entity is EntityZombie) || entity is EntitySpider) {
            if (entity is AbstractClientPlayer) {
                AnimatedEntity.getPlayerRenderer(entity).doRender(entity, x, y, z, entityYaw, partialTicks)
            } else if (entity is EntityZombie) {
                AnimatedEntity.zombieRenderer.doRender(entity, x, y, z, entityYaw, partialTicks)
            } else if (entity is EntitySpider) {
                AnimatedEntity.spiderRenderer.doRender(entity, x, y, z, entityYaw, partialTicks)
            }
            return true
        }
        return false
    }
}
