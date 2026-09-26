// skid LeaderClient
package net.airplus.features.module.modules.render

import net.airplus.event.AttackEvent
import net.airplus.event.UpdateEvent
import net.airplus.event.WorldEvent
import net.airplus.event.handler
import net.airplus.features.module.Category
import net.airplus.features.module.Module
import net.airplus.utils.client.asResourceLocation
import net.airplus.utils.client.playSound
import net.minecraft.block.Block
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.effect.EntityLightningBolt
import net.minecraft.init.Blocks
import net.minecraft.util.EnumParticleTypes

object KillEffectV2 : Module("KillEffectV2", Category.RENDER) {

    var killedTimes = 0
        private set

    private val lightning by boolean("Lightning", true)
    private val explosion by boolean("Explosion", true)
    private val blood by boolean("Blood", true)

    private var target: EntityLivingBase? = null
    private var lastEffectTime = 0L

    val onUpdate = handler<UpdateEvent> {
        val t = target ?: return@handler
        if (!mc.theWorld.loadedEntityList.contains(t) || t.health <= 0) {
            val now = System.currentTimeMillis()
            // 1s 冷却（不可开关），1s 内不会重复触发
            if (handleEvents() && now - lastEffectTime >= 1000) {
                lastEffectTime = now
                if (lightning) {
                    val bolt = EntityLightningBolt(mc.theWorld, t.posX, t.posY, t.posZ)
                    mc.theWorld.addEntityToWorld((-Math.random() * 100000).toInt(), bolt)
                    mc.playSound("ambient.weather.thunder".asResourceLocation())
                }

                if (explosion) {
                    for (i in 0..8) {
                        mc.effectRenderer.emitParticleAtEntity(t, EnumParticleTypes.FLAME)
                    }
                    mc.playSound("item.fireCharge.use".asResourceLocation())
                }

                if (blood) {
                    for (i in 0 until 10) {
                        mc.effectRenderer.spawnEffectParticle(
                            EnumParticleTypes.BLOCK_CRACK.particleID,
                            t.posX,
                            t.posY + t.height / 2.0,
                            t.posZ,
                            t.motionX + nextFloat(-0.5f, 0.5f),
                            t.motionY + nextFloat(-0.5f, 0.5f),
                            t.motionZ + nextFloat(-0.5f, 0.5f),
                            Block.getStateId(Blocks.redstone_block.defaultState)
                        )
                    }
                }
            }
            target = null
            killedTimes++
        }
    }

    val onWorld = handler<WorldEvent> {
        target = null
    }

    val onAttack = handler<AttackEvent> { event ->
        val entity = event.targetEntity ?: return@handler
        if (entity is EntityLivingBase) {
            target = entity
        }
    }

    override fun onDisable() {
        target = null
        lastEffectTime = 0
    }

    private fun nextFloat(startInclusive: Float, endInclusive: Float): Float {
        if (startInclusive == endInclusive || endInclusive - startInclusive <= 0f) {
            return startInclusive
        }
        return startInclusive + (endInclusive - startInclusive) * Math.random().toFloat()
    }
}
