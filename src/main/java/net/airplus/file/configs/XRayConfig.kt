/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.file.configs

import com.google.gson.JsonArray
import net.airplus.features.module.modules.render.XRay
import net.airplus.file.FileConfig
import net.airplus.utils.block.blockById
import net.airplus.utils.block.id
import net.airplus.utils.io.readJson
import net.airplus.utils.io.writeJson
import net.minecraft.init.Blocks
import java.io.*

class XRayConfig(file: File) : FileConfig(file) {

    /**
     * Load config from file
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    override fun loadConfig() {
        val json = file.readJson() as? JsonArray ?: return

        XRay.xrayBlocks.clear()

        json.mapNotNullTo(XRay.xrayBlocks) {
            it.asInt.blockById.takeIf { b -> b != Blocks.air }
        }
    }

    /**
     * Save config to file
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    override fun saveConfig() {
        file.writeJson(XRay.xrayBlocks.map { it.id }.sorted())
    }
}