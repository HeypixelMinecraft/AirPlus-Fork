/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.cape

import net.airplus.features.module.modules.render.Cape
import net.airplus.file.FileManager.dir
import net.airplus.utils.client.MinecraftInstance
import net.minecraft.client.renderer.IImageBuffer
import net.minecraft.client.renderer.ThreadDownloadImageData
import net.minecraft.util.ResourceLocation
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import net.airplus.utils.client.ClientUtils.LOGGER

object CapeAPI : MinecraftInstance {

    private val capesCache = File(dir, "capes").apply {
        mkdir()
    }

    /**
     * 获取自定义披风（从Cape模块）
     * 每次调用都会重新获取，确保实时切换
     */
    fun getCustomCape(uuid: UUID): ResourceLocation? {
        return Cape.getCapeForPlayer(uuid)
    }

    /**
     * Load cape of user with uuid
     *
     * @param uuid
     * @return cape info
     */
    fun loadCape(uuid: UUID, success: (CapeInfo) -> Unit) {
        // 首先检查是否有自定义披风
        val customCape = getCustomCape(uuid)
        if (customCape != null) {
            val capeInfo = CapeInfo(customCape, true)
            success(capeInfo)
            return
        }

        // 如果没有自定义披风，尝试加载在线披风
        CapeService.refreshCapeCarriers {
            runCatching {
                // Get URL of cape from cape service
                val (name, url) = CapeService.getCapeDownload(uuid) ?: return@refreshCapeCarriers

                // Load cape
                val resourceLocation = ResourceLocation("capes/$name.png")
                val cacheFile = File(capesCache, "$name.png")
                val capeInfo = CapeInfo(resourceLocation)
                val threadDownloadImageData = ThreadDownloadImageData(cacheFile, url, null, object : IImageBuffer {

                    override fun parseUserSkin(image: BufferedImage?) = image

                    override fun skinAvailable() {
                        capeInfo.isCapeAvailable = true
                    }
                })

                mc.textureManager.loadTexture(resourceLocation, threadDownloadImageData)

                success(capeInfo)
            }.onFailure {
                LOGGER.error("Failed to load cape for UUID: $uuid", it)
            }
        }
    }
}

data class CapeInfo(val resourceLocation: ResourceLocation, var isCapeAvailable: Boolean = false)