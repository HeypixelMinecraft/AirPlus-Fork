/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.combat

import net.airplus.event.EventState
import net.airplus.event.PacketEvent
import net.airplus.event.handler
import net.airplus.features.module.Category
import net.airplus.features.module.Module
import net.airplus.features.module.modules.player.Gapple
import net.airplus.utils.client.PacketUtils.sendPacket
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper.getEnchantmentLevel
import net.minecraft.item.ItemAxe
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.client.C02PacketUseEntity.Action.ATTACK
import net.minecraft.network.play.client.C09PacketHeldItemChange

/**
 * 从 Flux 迁移：攻击时静默切换到热键栏最高锋利度斧头再攻击。
 * 开启 ArmorBreakMode 时仅配合 ArmorBreak 破甲循环结束后生效。
 */
object MoreDamage : Module("MoreDamage", Category.COMBAT) {

    private val armorBreakMode by boolean("ArmorBreakMode", false)

    private fun getAxeSlot(): Int {
        val player = mc.thePlayer ?: return -1
        var slot = -1
        var highestSharpness = -1
        for (i in 0..8) {
            val stack = player.inventoryContainer.getSlot(i + 36).stack ?: continue
            val sharpness = getEnchantmentLevel(Enchantment.sharpness.effectId, stack)
            if (stack.item is ItemAxe && sharpness > highestSharpness) {
                highestSharpness = sharpness
                slot = i
            }
        }
        if (slot == player.inventory.currentItem)
            slot = -1
        return slot
    }

    val onPacket = handler<PacketEvent> { event ->
        val player = mc.thePlayer ?: return@handler
        if (Gapple.isEating) return@handler
        if (event.eventType != EventState.SEND) return@handler

        val slot = getAxeSlot()
        if (slot == -1) return@handler

        val packet = event.packet
        if (packet is C02PacketUseEntity && packet.action == ATTACK) {
            if (ArmorBreak.isHighestWeapon && ArmorBreak.handleEvents() && armorBreakMode) {
                event.cancelEvent()
                sendPacket(C09PacketHeldItemChange(slot), triggerEvent = false)
                sendPacket(packet, triggerEvent = false)
                sendPacket(C09PacketHeldItemChange(player.inventory.currentItem), triggerEvent = false)
            } else if (ArmorBreak.handleEvents()) {
                // ArmorBreak 正在破甲循环中，交由 ArmorBreak 处理
                return@handler
            } else {
                event.cancelEvent()
                sendPacket(C09PacketHeldItemChange(slot), triggerEvent = false)
                sendPacket(packet, triggerEvent = false)
                sendPacket(C09PacketHeldItemChange(player.inventory.currentItem), triggerEvent = false)
            }
        }
    }
}
