/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.combat

import io.netty.buffer.Unpooled
import net.airplus.event.AttackEvent
import net.airplus.event.handler
import net.airplus.features.module.Category
import net.airplus.features.module.Module
import net.airplus.features.module.modules.player.Gapple
import net.airplus.features.module.modules.world.scaffolds.Scaffold
import net.airplus.utils.client.PacketUtils.sendPacket
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper.getEnchantmentLevel
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemAxe
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemSword
import net.minecraft.item.ItemTool
import net.minecraft.network.PacketBuffer
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C17PacketCustomPayload

/**
 * 从 Flux 迁移：攻击时按武器伤害从低到高循环切换（破甲）。
 * 目标 HurtTime 低于 HurtTime 值时重置循环，可包含斧头 / 最佳击退武器。
 */
object ArmorBreak : Module("ArmorBreak", Category.COMBAT) {

    private val hurtTime by int("HurtTime", 4, 0..10)
    private val includeAxe by boolean("IncludeAxe", false)
    private val includeKBWeapon by boolean("IncludeKBWeapon", false)

    private var lastSwordDamage = -1.0
    private var switchEnd = false
    var isHighestWeapon = false
        private set

    val onAttack = handler<AttackEvent> { event ->
        val entity = event.targetEntity as? EntityLivingBase ?: return@handler
        val player = mc.thePlayer ?: return@handler

        if (Scaffold.handleEvents()) return@handler
        if (Gapple.isEating && Gapple.handleEvents()) return@handler

        val slot = getSwitchSlot(entity.hurtTime)
        if (slot == -1) return@handler

        // 将低伤害武器与当前手持槽位交换（mode 2 = SWAP）
        mc.playerController.windowClick(
            player.inventoryContainer.windowId, slot, player.inventory.currentItem, 2, player
        )
        sendPacket(C17PacketCustomPayload("test", PacketBuffer(Unpooled.wrappedBuffer(byteArrayOf(1)))), triggerEvent = false)
        sendPacket(C08PacketPlayerBlockPlacement(player.heldItem))
    }

    /**
     * 在背包中寻找比 lastSwordDamage 高、且伤害最低的武器槽位；
     * 找不到时表示已切换到最高伤害武器，循环结束。
     */
    private fun getSwitchSlot(entityHurtTime: Int): Int {
        val player = mc.thePlayer ?: return -1

        if (entityHurtTime <= hurtTime) {
            lastSwordDamage = -1.0
            switchEnd = false
            isHighestWeapon = false
        }
        if (switchEnd) {
            lastSwordDamage = -1.0
            isHighestWeapon = false
        }

        var minSwordDamage = 10000.0
        var minSwordSlot = -1
        for (i in 9 until 45) {
            val stack = player.inventoryContainer.getSlot(i).stack ?: continue
            if (!isWeapon(stack, i)) continue

            val damage = getDamage(stack)
            if (damage > lastSwordDamage && damage < minSwordDamage) {
                minSwordDamage = damage
                minSwordSlot = i
            }
        }

        if (minSwordSlot != -1) {
            lastSwordDamage = minSwordDamage
            return minSwordSlot
        }
        switchEnd = true
        isHighestWeapon = true
        return -1
    }

    private fun isWeapon(stack: ItemStack, slot: Int): Boolean {
        val item = stack.item
        return item is ItemSword ||
                (item is ItemAxe && includeAxe) ||
                (includeKBWeapon && isBestKBWeapon(stack, slot))
    }

    /**
     * 该武器是否为背包中击退附魔最高的武器
     */
    private fun isBestKBWeapon(stack: ItemStack, slot: Int): Boolean {
        val player = mc.thePlayer ?: return false
        val knockback = getEnchantmentLevel(Enchantment.knockback.effectId, stack)
        for (i in 9 until 45) {
            if (i == slot) continue
            val other = player.inventoryContainer.getSlot(i).stack ?: continue
            if (knockback <= getEnchantmentLevel(Enchantment.knockback.effectId, other))
                return false
        }
        return true
    }

    private fun getDamage(stack: ItemStack): Double {
        var damage = 0f
        val item = stack.item

        if (item is ItemSword) {
            damage += item.damageVsEntity
        } else if (item is ItemTool) {
            damage += item.toolMaterial.damageVsEntity
        }

        damage += getEnchantmentLevel(Enchantment.sharpness.effectId, stack) * 1.25f +
                getEnchantmentLevel(Enchantment.knockback.effectId, stack) * 0.01f
        return damage.toDouble()
    }
}
