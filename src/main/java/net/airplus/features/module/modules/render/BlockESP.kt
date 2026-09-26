/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.render

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import net.airplus.event.Render3DEvent
import net.airplus.event.async.loopSequence
import net.airplus.event.handler
import net.airplus.features.module.Category
import net.airplus.features.module.Module
import net.airplus.utils.block.BlockUtils.getBlockName
import net.airplus.utils.block.BlockUtils.searchBlocks
import net.airplus.utils.block.block
import net.airplus.utils.extensions.component1
import net.airplus.utils.extensions.component2
import net.airplus.utils.extensions.component3
import net.airplus.utils.extensions.eyes
import net.airplus.utils.render.RenderUtils.draw2D
import net.airplus.utils.render.RenderUtils.drawBlockBox
import net.minecraft.block.Block
import net.minecraft.init.Blocks.air
import net.minecraft.util.BlockPos
import java.awt.Color
import java.util.concurrent.ConcurrentHashMap

object BlockESP : Module("BlockESP", Category.RENDER) {
    private val mode by choices("Mode", arrayOf("Box", "2D"), "Box")
    private val block by block("Block", 168)
    private val radius by int("Radius", 40, 5..120)
    private val blockLimit by int("BlockLimit", 256, 0..2056)

    private val color by color("Color", Color(255, 179, 72))

    private val posList = ConcurrentHashMap.newKeySet<BlockPos>()

    override fun onDisable() {
        posList.clear()
    }

    val onSearch = loopSequence(dispatcher = Dispatchers.Default) {
        val selectedBlock = Block.getBlockById(block)

        if (selectedBlock == null || selectedBlock == air) {
            delay(1000)
            return@loopSequence
        }

        val (x, y, z) = mc.thePlayer?.eyes ?: return@loopSequence
        val radiusSq = radius * radius

        posList.removeIf {
            it.distanceSqToCenter(x, y, z) >= radiusSq || it.block != selectedBlock
        }

        val listSpace = blockLimit - posList.size

        if (listSpace > 0) {
            posList += searchBlocks(radius, setOf(selectedBlock), listSpace).keys
        }

        delay(1000)
    }

    val onRender3D = handler<Render3DEvent> {
        when (mode) {
            "Box" -> posList.forEach { drawBlockBox(it, color, true) }
            "2D" -> posList.forEach { draw2D(it, color.rgb, Color.BLACK.rgb) }
        }
    }

    override val tag
        get() = getBlockName(block)
}