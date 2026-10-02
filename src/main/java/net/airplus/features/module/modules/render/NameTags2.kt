/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.render

import net.airplus.event.Render2DEvent
import net.airplus.event.Render3DEvent
import net.airplus.event.handler
import net.airplus.features.module.Category
import net.airplus.features.module.Module
import net.airplus.features.module.modules.combat.KillAura
import net.airplus.features.module.modules.misc.Teams
import net.airplus.ui.font.Fonts
import net.airplus.utils.misc.HYTUtils
import net.airplus.utils.render.ESPUtil
import net.minecraft.client.gui.Gui
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.passive.EntityAnimal
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumChatFormatting
import org.lwjgl.util.vector.Vector4f
import java.awt.Color
import java.text.DecimalFormat
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * 从 SilenceFix 移植（原 NameTags，因客户端已有 NameTags 而改名 NameTags2）。
 * 原版的 EventRenderNameTag 取消逻辑由 MixinRendererLivingEntity#canRenderName 注入完成。
 */
object NameTags2 : Module("NameTags2", Category.RENDER) {

    private val playersValue by boolean("Players", true)
    private val animalsValue by boolean("Animals", true)
    private val mobsValue by boolean("Mobs", false)
    private val invisibleValue by boolean("Invisible", false)
    private val itemsValue by boolean("Items", true)

    private val entityPosition = HashMap<Entity, Vector4f>()
    private val itemPosition = HashMap<EntityItem, Vector4f>()
    private val distanceFormat = DecimalFormat("0.0")

    private val font get() = Fonts.fontRegular35

    val onRender3D = handler<Render3DEvent> {
        entityPosition.clear()
        itemPosition.clear()

        val world = mc.theWorld ?: return@handler

        for (entity in world.loadedEntityList) {
            if (shouldRender(entity) && ESPUtil.isInView(entity)) {
                entityPosition[entity] = ESPUtil.getEntityPositionsOn2D(entity)
            }

            if (itemsValue && shouldRenderItem(entity) && ESPUtil.isInView(entity)) {
                itemPosition[entity as EntityItem] = ESPUtil.getEntityPositionsOn2D(entity)
            }
        }
    }

    val onRender2D = handler<Render2DEvent> {
        for ((item, pos) in itemPosition) {
            drawItemTag(item, pos.x, pos.y + 10, pos.z)
        }

        for ((entity, pos) in entityPosition) {
            if (entity !is EntityLivingBase) continue
            drawOpponentNameTag(entity, pos.x, pos.y, pos.z)
        }
    }

    private fun drawOpponentNameTag(entity: EntityLivingBase, x: Float, y: Float, width: Float) {
        val f = font
        var name = entity.displayName.formattedText
        name = getRank(entity) + EnumChatFormatting.WHITE.toString() + name
        val sep = " | "
        val hpStr = max(0f, entity.health).roundToInt().toString() + "HP"
        val nameW = f.getStringWidth(name).toFloat()
        val sepW = f.getStringWidth(sep).toFloat()
        val hpW = f.getStringWidth(hpStr).toFloat()
        val textW = nameW + sepW + hpW
        val padX = 4f
        val padY = 2f
        val tagW = textW + padX * 2f
        val tagH = f.FONT_HEIGHT + padY * 2f
        val mid = x + (width - x) / 2.0f
        val boxL = mid - tagW / 2.0f
        val boxT = y - (tagH + 6.0f)
        val boxR = boxL + tagW
        val boxB = boxT + tagH
        val bg = Color(20, 20, 20, 130).rgb
        val cName = 0xFFFFFFFF.toInt()
        val cSep = Color(190, 190, 190).rgb
        val cHP = Color(230, 70, 70).rgb
        Gui.drawRect(boxL.toInt(), boxT.toInt(), boxR.toInt(), boxB.toInt(), bg)
        var tx = boxL + padX
        val ty = boxT + (tagH - f.FONT_HEIGHT) / 2f + 2.0f
        f.drawString(name, tx, ty, cName); tx += nameW
        f.drawString(sep, tx, ty, cSep); tx += sepW
        f.drawString(hpStr, tx, ty, cHP)
    }

    private fun drawItemTag(item: EntityItem, x: Float, y: Float, width: Float) {
        val stack = item.entityItem ?: return
        val f = font

        val itemName = getItemDisplayName(stack)
        if (itemName.isNullOrEmpty()) return

        val count = if (stack.stackSize > 1) " x" + stack.stackSize else ""
        val sep = " | "
        val distStr = String.format("%.1fm", mc.thePlayer.getDistanceToEntity(item))

        val special = isTargetItem(stack)

        val nameW = f.getStringWidth(itemName + count).toFloat()
        val sepW = if (special) f.getStringWidth(sep).toFloat() else 0f
        val dW = if (special) f.getStringWidth(distStr).toFloat() else 0f
        val textW = nameW + sepW + dW

        val padX = 4f
        val padY = 2f
        val tagW = textW + padX * 2f
        val tagH = f.FONT_HEIGHT + padY * 2f

        val mid = x + (width - x) / 2.0f
        val boxL = mid - tagW / 2.0f
        val boxT = y - (tagH + 5.0f)
        val boxR = boxL + tagW
        val boxB = boxT + tagH

        val bg = Color(20, 20, 20, 130).rgb
        val cSep = Color(190, 190, 190).rgb
        val cRed = Color(230, 70, 70).rgb
        val cName = if (special) getItemColor(stack) else 0xFFFFFFFF.toInt()

        if (special) {
            Gui.drawRect(boxL.toInt(), boxT.toInt(), boxR.toInt(), boxB.toInt(), bg)
        }

        val ty = boxT + (tagH - f.FONT_HEIGHT) / 2f + 2.0f
        var tx = if (special) boxL + padX else mid - textW / 2.0f

        f.drawString(itemName + count, tx, ty, cName)
        if (special) {
            tx += nameW
            f.drawString(sep, tx, ty, cSep)
            tx += sepW
            f.drawString(distStr, tx, ty, cRed)
        }
    }

    private fun getItemDisplayName(stack: ItemStack): String? {
        return when (stack.item) {
            Items.golden_apple ->
                if (stack.metadata == 1)
                    EnumChatFormatting.GOLD.toString() + "附魔金苹果"
                else
                    EnumChatFormatting.YELLOW.toString() + "金苹果"
            Items.gold_ingot -> EnumChatFormatting.GOLD.toString() + "金锭"
            Items.iron_ingot -> EnumChatFormatting.GRAY.toString() + "铁锭"
            Items.diamond -> EnumChatFormatting.AQUA.toString() + "钻石"
            else -> stack.displayName
        }
    }

    private fun getItemColor(stack: ItemStack): Int {
        return when (stack.item) {
            Items.golden_apple ->
                if (stack.metadata == 1) Color(255, 170, 0).rgb else Color(255, 255, 85).rgb
            Items.gold_ingot -> Color(255, 255, 85).rgb
            Items.iron_ingot -> Color(200, 200, 200).rgb
            Items.diamond -> Color(85, 255, 255).rgb
            else -> -1
        }
    }

    private fun getRank(entity: EntityLivingBase): String {
        if (entity is EntityPlayer) {
            if (entity == mc.thePlayer) {
                return EnumChatFormatting.GREEN.toString() + "[You] "
            }
            if (entity == KillAura.target) {
                return EnumChatFormatting.DARK_RED.toString() + "[杀戮目标] "
            }
            if (Teams.handleEvents() && Teams.isInYourTeam(entity)) {
                return EnumChatFormatting.GREEN.toString() + "[队伍] "
            }
            if (HYTUtils.isStrength(entity) > 0) {
                return EnumChatFormatting.DARK_RED.toString() + "[力量狗] "
            }
            if (HYTUtils.isRegen(entity) > 0) {
                return EnumChatFormatting.DARK_RED.toString() + "[生命恢复狗] "
            }
            if (HYTUtils.isHoldingGodAxe(entity)) {
                return EnumChatFormatting.DARK_RED.toString() + "[秒人斧] "
            }
            if (HYTUtils.isKBBall(entity.heldItem)) {
                return EnumChatFormatting.DARK_RED.toString() + "[击退粘液球] "
            }
            if (HYTUtils.hasEatenGoldenApple(entity) > 0) {
                return EnumChatFormatting.DARK_RED.toString() + "[金苹果] "
            }
        }
        return ""
    }

    private fun shouldRender(entity: Entity): Boolean {
        if (entity.isDead) {
            return false
        }
        if (entity == mc.thePlayer && playersValue) {
            return mc.gameSettings.thirdPersonView != 0
        }

        if (entity is EntityPlayer && playersValue) {
            return true
        }

        if (entity is EntityAnimal && animalsValue) {
            return true
        }

        if (entity is EntityMob && mobsValue) {
            return true
        }

        if (entity.isInvisible() && invisibleValue) {
            return true
        }

        return false
    }

    private fun shouldRenderItem(entity: Entity): Boolean =
        entity is EntityItem && !entity.isDead

    private fun isTargetItem(stack: ItemStack?): Boolean =
        stack != null && (
            stack.item == Items.golden_apple ||
                stack.item == Items.gold_ingot ||
                stack.item == Items.iron_ingot ||
                stack.item == Items.diamond
            )
}
