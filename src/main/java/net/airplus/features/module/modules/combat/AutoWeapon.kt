/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.combat

import io.netty.buffer.Unpooled
import net.airplus.event.AttackEvent
import net.airplus.event.PacketEvent
import net.airplus.event.handler
import net.airplus.features.module.Category
import net.airplus.features.module.Module
import net.airplus.features.module.modules.movement.LiquidWalk
import net.airplus.features.module.modules.player.Gapple
import net.airplus.utils.client.PacketUtils.sendPacket
import net.airplus.utils.client.chat
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper.getEnchantmentLevel
import net.minecraft.item.ItemAxe
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemSword
import net.minecraft.network.PacketBuffer
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.client.C02PacketUseEntity.Action.ATTACK
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.network.play.client.C17PacketCustomPayload
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing

/**
 * 从 SilenceFix 迁移：攻击时自动切换到最佳武器破甲。
 * Normal  模式：物理切换热键栏槽位。
 * Silence 模式：静默切换（服务端短暂认为你手持高锋利度武器），支持 Old/New 两种 bypass 包。
 */
object AutoWeapon : Module("AutoWeapon", Category.COMBAT, subjective = true) {

    private val mode by choices("Mode", arrayOf("Normal", "Silence"), "Normal")
    private val healthValue by float("Health", 5f, 0.1f..20f)
    private val sendMode by choices("SendMode", arrayOf("Old", "New", "None"), "Old") { mode == "Silence" }

    private var hasSwitched = false
    private var silentSwitch = false
    private var projectileDisabledByUs = false

    val onAttack = handler<AttackEvent> {
        val player = mc.thePlayer ?: return@handler

        // Gapple 开启时不工作（原 SilenceFix shouldWork）
        if (Gapple.handleEvents()) return@handler

        when (mode) {
            "Normal" -> handleNormal(player)
            "Silence" -> handleSilence(player)
        }
    }

    val onPacket = handler<PacketEvent> { event ->
        if (!silentSwitch) return@handler

        // 攻击包发出后立即恢复原槽位（顺序：C09 切换 → C02 攻击 → C09 恢复）
        if (event.packet is C02PacketUseEntity && event.packet.action == ATTACK) {
            silentSwitch = false
            mc.thePlayer?.let { sendPacket(C09PacketHeldItemChange(it.inventory.currentItem)) }
        }
    }

    override fun onDisable() {
        hasSwitched = false
        silentSwitch = false
        projectileDisabledByUs = false
    }

    private fun handleNormal(player: net.minecraft.entity.player.EntityPlayer) {
        val bestSlot = getBestWeaponSlot() ?: return

        if (bestSlot != player.inventory.currentItem) {
            if (AutoProjectile.handleEvents()) {
                AutoProjectile.state = false
                projectileDisabledByUs = true
            }
            if (LiquidWalk.handleEvents()) {
                LiquidWalk.state = false
            }

            if (!hasSwitched) {
                chat("§f开始破甲")
                hasSwitched = true
            }

            player.inventory.currentItem = bestSlot
            mc.playerController.updateController()
        } else {
            if (projectileDisabledByUs) {
                AutoProjectile.state = true
                projectileDisabledByUs = false
                hasSwitched = false
            }
        }
    }

    private fun handleSilence(player: net.minecraft.entity.player.EntityPlayer) {
        val netHandler = mc.getNetHandler()
        if (player.isDead || netHandler == null || !netHandler.getNetworkManager().isChannelOpen()) {
            hasSwitched = false
            silentSwitch = false
            return
        }

        val heldItem = player.heldItem
        if (player.health > healthValue &&
            heldItem != null && (heldItem.item is ItemSword || heldItem.item is ItemAxe)
        ) {
            val goodWeaponSlot = getGoodWeapon() ?: return
            val newWeapon = player.inventory.getStackInSlot(goodWeaponSlot) ?: return

            // 只在斧头锋利度 >= 10 时静默切换
            if (newWeapon.item is ItemAxe &&
                getEnchantmentLevel(Enchantment.sharpness.effectId, newWeapon) < 10
            ) return

            if (LiquidWalk.handleEvents()) {
                LiquidWalk.state = false
            }

            if (!hasSwitched) {
                chat("§f开始破甲")
                hasSwitched = true
            }

            sendPacket(C09PacketHeldItemChange(goodWeaponSlot))
            silentSwitch = true

            when (sendMode) {
                "New" -> {
                    val buffer = PacketBuffer(Unpooled.buffer())
                    buffer.writeString("bypass_hyt")
                    sendPacket(C17PacketCustomPayload("bypass_hyt", buffer))
                }

                "Old" -> {
                    sendPacket(
                        C08PacketPlayerBlockPlacement(
                            BlockPos(-1, -1, -1), EnumFacing.DOWN.index,
                            player.heldItem, 0.0f, 0.0f, 0.0f
                        )
                    )
                }
            }
        } else {
            val bestSlot = getBestWeaponSlot() ?: return
            if (bestSlot != player.inventory.currentItem) {
                if (!hasSwitched) {
                    chat("§f开始破甲")
                    hasSwitched = true
                }

                player.inventory.currentItem = bestSlot
                mc.playerController.updateController()
            } else {
                hasSwitched = false
            }
        }
    }

    /**
     * 热键栏最佳武器槽位：完美斧（锋利10）> 低血量强斧（锋利>10）> 最佳剑
     */
    private fun getBestWeaponSlot(): Int? {
        val player = mc.thePlayer ?: return null
        if (player.isDead) return null

        var perfectAxeSlot = -1
        var strongAxeSlot = -1
        var bestSwordSlot = -1
        var hasPerfectAxe = false
        var hasStrongAxe = false

        for (i in 0..8) {
            val stack = player.inventory.getStackInSlot(i) ?: continue

            if (stack.item is ItemAxe) {
                val sharpness = getEnchantmentLevel(Enchantment.sharpness.effectId, stack)
                if (sharpness == 10) {
                    hasPerfectAxe = true
                    perfectAxeSlot = i
                } else if (sharpness > 10) {
                    hasStrongAxe = true
                    strongAxeSlot = i
                }
            } else if (stack.item is ItemSword && isBestSword(stack)) {
                bestSwordSlot = i
            }
        }

        if (hasPerfectAxe) return perfectAxeSlot
        if (player.health <= healthValue && hasStrongAxe) return strongAxeSlot
        return if (bestSwordSlot >= 0) bestSwordSlot else null
    }

    private fun isBestSword(stack: ItemStack): Boolean {
        val player = mc.thePlayer ?: return false
        val damage = getDamage(stack)
        for (i in 0 until player.inventory.sizeInventory) {
            val other = player.inventory.getStackInSlot(i) ?: continue
            if (other.item is ItemSword && getDamage(other) > damage) {
                return false
            }
        }
        return true
    }

    /**
     * Silence 模式：寻找比当前手持更高锋利度的武器（斧头需 >= 10）
     */
    private fun getGoodWeapon(): Int? {
        val player = mc.thePlayer ?: return null

        val currentStack = player.inventory.getStackInSlot(player.inventory.currentItem)
        val currentSharpness = currentStack?.let { getEnchantmentLevel(Enchantment.sharpness.effectId, it) } ?: 0

        for (i in 0..8) {
            val stack = player.inventory.getStackInSlot(i) ?: continue
            if (i == player.inventory.currentItem) continue

            val sharpness = getEnchantmentLevel(Enchantment.sharpness.effectId, stack)

            if (stack.item is ItemAxe && sharpness >= 10 && sharpness > currentSharpness) {
                return i
            }

            if (stack.item is ItemSword && sharpness > currentSharpness) {
                return i
            }
        }
        return null
    }

    private fun getDamage(stack: ItemStack): Float {
        var damage = 0f
        val item = stack.item

        if (item is ItemAxe) {
            damage += item.toolMaterial.damageVsEntity
        } else if (item is ItemSword) {
            damage += item.damageVsEntity
        }

        damage += getEnchantmentLevel(Enchantment.sharpness.effectId, stack) * 1.25f +
                getEnchantmentLevel(Enchantment.fireAspect.effectId, stack) * 0.01f
        return damage
    }
}
