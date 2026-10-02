/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.utils.misc

import net.airplus.utils.client.MinecraftInstance
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagList
import net.minecraft.potion.Potion

/**
 * 从 SilenceFix 移植的花雨庭道具检测工具（NameTags2 等使用）。
 */
object HYTUtils : MinecraftInstance {

    private const val GOD_AXE_MAX_DURABILITY = 2
    private const val GOD_AXE_ENCHANT_ID = 16
    private const val GOD_AXE_MIN_LEVEL = 666
    private const val KB_BALL_ENCHANT_ID = 19
    private const val KB_BALL_MIN_LEVEL = 2
    private const val FIRE_ENCHANT_BALL_ENCHANT_ID = 20
    private const val FIRE_ENCHANT_BALL_MIN_LEVEL = 1
    private const val REGEN_POTION_AMPLIFIER = 4

    private val LOBBY_KEYWORDS = arrayOf("问题反馈", "练习场", "单人模式")

    fun isInLobby(): Boolean {
        val world = mc.theWorld ?: return false
        return LOBBY_KEYWORDS.any { keyword ->
            world.playerEntities.any { player ->
                (player as? EntityPlayer)?.name?.contains(keyword) == true
            }
        }
    }

    fun isGoodItem(stack: ItemStack?): Boolean {
        if (stack == null) return false

        val item = stack.item
        return item == Items.ender_pearl ||
            item == Items.arrow ||
            item == Items.lava_bucket ||
            item == Items.water_bucket ||
            isGodAxe(stack) ||
            isKBBall(stack)
    }

    fun isHoldingGodAxe(player: EntityPlayer): Boolean =
        isGodAxe(player.getEquipmentInSlot(0))

    fun isGodAxe(stack: ItemStack?): Boolean {
        if (stack == null || stack.item != Items.golden_axe) return false

        val durability = stack.maxDamage - stack.itemDamage
        if (durability > GOD_AXE_MAX_DURABILITY) return false

        val enchantmentTagList = stack.enchantmentTagList ?: return false
        return hasSpecificEnchantment(enchantmentTagList, GOD_AXE_ENCHANT_ID, GOD_AXE_MIN_LEVEL)
    }

    fun isKBBall(stack: ItemStack?): Boolean {
        if (stack == null || stack.item != Items.slime_ball) return false

        val enchantmentTagList = stack.enchantmentTagList ?: return false
        return hasSpecificEnchantment(enchantmentTagList, KB_BALL_ENCHANT_ID, KB_BALL_MIN_LEVEL)
    }

    fun isFireEnchantBall(stack: ItemStack?): Boolean {
        if (stack == null || stack.item != Items.magma_cream) return false

        val enchantmentTagList = stack.enchantmentTagList ?: return false
        return hasSpecificEnchantment(enchantmentTagList, FIRE_ENCHANT_BALL_ENCHANT_ID, FIRE_ENCHANT_BALL_MIN_LEVEL)
    }

    fun isHoldingEnchantedGoldenApple(player: EntityPlayer): Boolean {
        val holdingItem = player.getEquipmentInSlot(0)
        return holdingItem != null &&
            holdingItem.item == Items.golden_apple &&
            holdingItem.hasEffect()
    }

    fun hasEatenGoldenApple(player: EntityPlayer): Int {
        val regenPotion = player.getActivePotionEffect(Potion.regeneration)
        if (regenPotion == null || regenPotion.amplifier < REGEN_POTION_AMPLIFIER) return -1
        return regenPotion.duration
    }

    fun isRegen(player: EntityPlayer): Int {
        val regenPotion = player.getActivePotionEffect(Potion.regeneration)
        return regenPotion?.duration ?: -1
    }

    fun isStrength(player: EntityPlayer): Int {
        val strengthPotion = player.getActivePotionEffect(Potion.damageBoost)
        return strengthPotion?.duration ?: -1
    }

    private fun hasSpecificEnchantment(enchantmentTagList: NBTTagList, enchantId: Int, minLevel: Int): Boolean {
        for (i in 0 until enchantmentTagList.tagCount()) {
            val nbt = enchantmentTagList.getCompoundTagAt(i)
            if (nbt.hasKey("id") && nbt.hasKey("lvl") &&
                nbt.getInteger("id") == enchantId && nbt.getInteger("lvl") >= minLevel
            ) return true
        }
        return false
    }
}
