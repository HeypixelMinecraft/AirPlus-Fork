/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.combat

import net.airplus.event.UpdateEvent
import net.airplus.event.handler
import net.airplus.features.module.Category
import net.airplus.features.module.Module
import net.airplus.utils.attack.EntityUtils.isSelected
import net.airplus.utils.inventory.InventoryUtils
import net.airplus.utils.inventory.hotBarSlot
import net.airplus.utils.rotation.RaycastUtils.raycastEntity
import net.airplus.utils.timing.MSTimer
import net.minecraft.init.Items.egg
import net.minecraft.init.Items.snowball

object AutoProjectile : Module("AutoProjectile", Category.COMBAT) {
    private val facingEnemy by boolean("FacingEnemy", true)

    private val range by float("Range", 8F, 1F..20F)
    private val throwDelay by intRange("ThrowDelay", 1000..1500, 50..2000)

    private val switchBackDelay by int("SwitchBackDelay", 500, 50..2000)

    private val throwTimer = MSTimer()
    private val projectilePullTimer = MSTimer()

    private var projectileInUse = false
    private var switchBack = -1

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler
        val usingProjectile =
            (player.isUsingItem && (player.heldItem?.item == snowball || player.heldItem?.item == egg)) || projectileInUse

        if (usingProjectile) {
            if (projectilePullTimer.hasTimePassed(switchBackDelay)) {
                if (switchBack != -1 && player.inventory.currentItem != switchBack) {
                    player.inventory.currentItem = switchBack

                    mc.playerController.syncCurrentPlayItem()
                } else {
                    player.stopUsingItem()
                }

                switchBack = -1
                projectileInUse = false

                throwTimer.reset()
            }
        } else {
            var throwProjectile = false

            if (facingEnemy) {
                var facingEntity = mc.objectMouseOver?.entityHit

                if (facingEntity == null) {
                    facingEntity = raycastEntity(range.toDouble()) { isSelected(it, true) }
                }

                if (isSelected(facingEntity, true)) {
                    throwProjectile = true
                }
            } else {
                throwProjectile = true
            }

            if (throwProjectile) {
                val randomThrowDelay = throwDelay.random()

                if (throwTimer.hasTimePassed(randomThrowDelay)) {
                    if (player.heldItem?.item != snowball && player.heldItem?.item != egg) {
                        val projectile = InventoryUtils.findItemArray(36, 44, arrayOf(snowball, egg)) ?: return@handler

                        switchBack = player.inventory.currentItem

                        player.inventory.currentItem = projectile
                        mc.playerController.syncCurrentPlayItem()
                    }

                    throwProjectile()
                }
            }
        }
    }

    /**
     * Throw projectile (snowball/egg)
     */
    private fun throwProjectile() {
        val player = mc.thePlayer ?: return
        val projectile = InventoryUtils.findItemArray(36, 44, arrayOf(snowball, egg)) ?: return

        player.inventory.currentItem = projectile

        mc.playerController.sendUseItem(player, mc.theWorld, player.hotBarSlot(projectile).stack)

        projectileInUse = true
        projectilePullTimer.reset()
    }

    /**
     * Reset everything when disabled
     */
    override fun onDisable() {
        throwTimer.reset()
        projectilePullTimer.reset()
        projectileInUse = false
        switchBack = -1
    }
}
