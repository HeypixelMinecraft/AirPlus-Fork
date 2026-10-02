/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 *
 * Ported from SilenceFix FastLadder.
 */
package net.airplus.features.module.modules.movement

import net.airplus.event.UpdateEvent
import net.airplus.event.WorldEvent
import net.airplus.event.handler
import net.airplus.features.module.Category
import net.airplus.features.module.Module
import net.airplus.utils.client.PacketUtils.sendPacket
import net.minecraft.block.Block
import net.minecraft.block.BlockLadder
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing

object FastLadder : Module("FastLadder", Category.MOVEMENT) {

    private val blockLadder = ArrayList<BlockPos>()
    private var cancel = false
    private var normalClimb = false

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler
        val world = mc.theWorld ?: return@handler

        if (!player.isOnLadder) {
            normalClimb = false
            cancel = false
            blockLadder.clear()
        }
        if (!player.isOnLadder && !cancel) {
            return@handler
        }
        if (normalClimb && player.isOnLadder) {
            return@handler
        }
        if (player.isOnLadder && mc.gameSettings.keyBindJump.isKeyDown) {
            blockLadder.clear()
            cancel = false
            normalClimb = true
            return@handler
        }
        for ((block, value) in searchBlocks(4)) {
            if (value is BlockLadder && !blockLadder.contains(block)) {
                blockLadder.add(block)
            }
        }
        if (blockLadder.isNotEmpty()) {
            for (block in blockLadder) {
                sendPacket(
                    C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, block, EnumFacing.DOWN),
                    false
                )
            }
            if (player.isOnLadder) {
                cancel = true
            }
        }
    }

    val onWorld = handler<WorldEvent> {
        blockLadder.clear()
    }

    private fun getBlock(blockPos: BlockPos): Block? {
        val world = mc.theWorld ?: return null
        return world.getBlockState(blockPos)?.block
    }

    private fun searchBlocks(radius: Int): Map<BlockPos, Block> {
        val blocks = HashMap<BlockPos, Block>()

        val thePlayer = mc.thePlayer ?: return blocks

        for (x in radius downTo -radius + 1) {
            for (y in radius downTo -radius + 1) {
                for (z in radius downTo -radius + 1) {
                    val blockPos = BlockPos(thePlayer.posX + x, thePlayer.posY + y, thePlayer.posZ + z)
                    val block = getBlock(blockPos)
                    if (block != null) {
                        blocks[blockPos] = block
                    }
                }
            }
        }

        return blocks
    }
}
