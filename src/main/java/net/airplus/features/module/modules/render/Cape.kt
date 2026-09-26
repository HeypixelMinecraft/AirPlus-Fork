//skid from airclient
package net.airplus.features.module.modules.render

import net.airplus.config.ListValue
import net.airplus.features.module.Category
import net.airplus.features.module.Module
import net.airplus.file.FileManager
import net.airplus.utils.client.ClientUtils
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.util.ResourceLocation
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*
import java.util.zip.ZipFile
import javax.imageio.ImageIO

object Cape : Module("Cape", Category.CLIENT) {

    private val capeFolder: File by lazy {
        val folder = File(FileManager.dir, "capes")
        if (!folder.exists()) {
            folder.mkdirs()
        }
        folder
    }

    // LRU 缓存：限制最大容量为 16，超过时自动清理最旧的纹理
    private val capeMap = object : LinkedHashMap<String, ResourceLocation>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, ResourceLocation>): Boolean {
            return if (size > MAX_CAPE_CACHE_SIZE) {
                // 清理纹理资源
                eldest.value?.let { loc ->
                    try {
                        mc.textureManager.deleteTexture(loc)
                    } catch (e: Exception) {
                        ClientUtils.LOGGER.warn("[Cape] Failed to delete texture: ${loc.resourcePath}")
                    }
                }
                true
            } else {
                false
            }
        }
    }

    // LRU 最大缓存数量
    private val MAX_CAPE_CACHE_SIZE = 16

    private val capeFiles = mutableListOf<File>()
    private var initialized = false

    private var capeModeValue: ListValue? = null
    private var currentCape: ResourceLocation? = null
    private var lastCapeMode: String = "None"

    val capeMode: String
        get() = capeModeValue?.get() ?: "None"

    init {
        initializeCapes()
    }

    private fun initializeCapes() {
        if (initialized) return
        
        try {
            ClientUtils.LOGGER.info("[Cape] Starting initialization...")
            
            extractCapesFromJar()
            scanCapes()
            
            val capeNames = mutableListOf("None")
            capeNames.addAll(capeFiles.map { it.nameWithoutExtension })
            
            capeModeValue = choices("Cape", capeNames.toTypedArray(), capeNames.first())
            
            // 注册onChanged监听器
            capeModeValue?.onChanged { newValue ->
                ClientUtils.LOGGER.info("[Cape] onChanged triggered: $newValue")
                lastCapeMode = newValue
                updateCurrentCape(newValue)
            }
            
            // 初始化当前披风
            lastCapeMode = capeMode
            updateCurrentCape(capeMode)
            
            ClientUtils.LOGGER.info("[Cape] Initialization complete. Found ${capeFiles.size} capes, current: ${capeMode}")
            initialized = true
        } catch (e: Exception) {
            ClientUtils.LOGGER.error("[Cape] Initialization failed: ${e.message}")
            e.printStackTrace()
            capeModeValue = choices("Cape", arrayOf("None"), "None")
        }
    }

    private fun updateCurrentCape(selectedCape: String) {
        ClientUtils.LOGGER.info("[Cape] updateCurrentCape called with: $selectedCape")
        
        if (selectedCape == "None") {
            currentCape = null
            ClientUtils.LOGGER.info("[Cape] Cape disabled")
            return
        }
        
        // 检查缓存
        if (capeMap.containsKey(selectedCape)) {
            currentCape = capeMap[selectedCape]
            ClientUtils.LOGGER.info("[Cape] Using cached cape: $selectedCape -> $currentCape")
            return
        }
        
        // 加载新披风
        val file = capeFiles.find { it.nameWithoutExtension == selectedCape }
        if (file != null) {
            val texture = loadCapeTexture(file)
            if (texture != null) {
                capeMap[selectedCape] = texture
                currentCape = texture
                ClientUtils.LOGGER.info("[Cape] Loaded and cached cape: $selectedCape -> $texture")
            } else {
                currentCape = null
                ClientUtils.LOGGER.error("[Cape] Failed to load cape: $selectedCape")
            }
        } else {
            currentCape = null
            ClientUtils.LOGGER.error("[Cape] Cape file not found: $selectedCape")
        }
    }

    private fun extractCapesFromJar() {
        try {
            capeFolder.mkdirs()
            
            val jarPath = javaClass.protectionDomain.codeSource.location.toURI().path
            ClientUtils.LOGGER.info("[Cape] Jar path: $jarPath")
            
            if (!jarPath.endsWith(".jar")) {
                ClientUtils.LOGGER.info("[Cape] Not running from jar, copying from resources...")
                copyCapesFromResources()
                return
            }

            val zipFile = ZipFile(jarPath)
            val entries = zipFile.entries()
            var extractedCount = 0

            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                val name = entry.name
                if (name.startsWith("assets/minecraft/airclient/cape/") && name.endsWith(".png")) {
                    val fileName = File(name).name
                    val targetFile = File(capeFolder, fileName)
                    
                    if (!targetFile.exists()) {
                        try {
                            zipFile.getInputStream(entry).use { input ->
                                FileOutputStream(targetFile).use { output ->
                                    input.copyTo(output)
                                }
                            }
                            extractedCount++
                            ClientUtils.LOGGER.info("[Cape] Extracted: $fileName")
                        } catch (e: Exception) {
                            ClientUtils.LOGGER.error("[Cape] Failed to extract $fileName: ${e.message}")
                        }
                    }
                }
            }
            zipFile.close()
            ClientUtils.LOGGER.info("[Cape] Extracted $extractedCount capes from jar")
        } catch (e: Exception) {
            ClientUtils.LOGGER.error("[Cape] Failed to extract capes from jar: ${e.message}")
            e.printStackTrace()
            copyCapesFromResources()
        }
    }

    private fun copyCapesFromResources() {
        try {
            capeFolder.mkdirs()

            // 与 assets/minecraft/airclient/cape 下的实际资源保持一致（overlay.png 为披风覆盖层纹理，不算独立样式）
            val knownCapes = listOf(
                "white.png", "vape.png", "styles.png", "styles2.png",
                "Sherry.png", "ravenxd.png", "ravenanime.png",
                "opal.png", "LB+R.png", "LB+.png",
                "Hatch.png", "FDP.png", "Cry.png",
                "Chicken.png", "Chick.png",
                "bilibili.png", "Augustus.png", "Astolfotrap.png",
                "among us.png"
            )

            var copiedCount = 0
            for (capeName in knownCapes) {
                val targetFile = File(capeFolder, capeName)
                if (!targetFile.exists()) {
                    try {
                        val resource = javaClass.classLoader.getResourceAsStream("assets/minecraft/airclient/cape/$capeName")
                        if (resource != null) {
                            FileOutputStream(targetFile).use { output ->
                                resource.copyTo(output)
                            }
                            resource.close()
                            copiedCount++
                            ClientUtils.LOGGER.info("[Cape] Copied from resources: $capeName")
                        }
                    } catch (e: Exception) {
                        ClientUtils.LOGGER.error("[Cape] Failed to copy $capeName: ${e.message}")
                    }
                }
            }
            ClientUtils.LOGGER.info("[Cape] Copied $copiedCount capes from resources")
        } catch (e: Exception) {
            ClientUtils.LOGGER.error("[Cape] Failed to copy capes from resources: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun scanCapes() {
        capeFiles.clear()
        capeMap.clear()

        if (!capeFolder.exists() || !capeFolder.isDirectory) {
            ClientUtils.LOGGER.warn("[Cape] Cape folder does not exist: ${capeFolder.absolutePath}")
            return
        }

        ClientUtils.LOGGER.info("[Cape] Scanning folder: ${capeFolder.absolutePath}")

        capeFolder.walk()
            .filter { file ->
                file.isFile && file.extension.equals("png", ignoreCase = true)
                        && !file.nameWithoutExtension.equals("overlay", ignoreCase = true)
            }
            .sortedBy { it.nameWithoutExtension.lowercase() }
            .forEach { file ->
                capeFiles.add(file)
                ClientUtils.LOGGER.info("[Cape] Found cape file: ${file.name}")
            }

        ClientUtils.LOGGER.info("[Cape] Total capes found: ${capeFiles.size}")
    }

    private fun loadCapeTexture(file: File): ResourceLocation? {
        return try {
            ClientUtils.LOGGER.info("[Cape] Loading texture for: ${file.name}")

            // 使用 use 确保 FileInputStream 在任何情况下都被关闭
            val bufferedImage = FileInputStream(file).use { fis ->
                ImageIO.read(fis)
            }
            if (bufferedImage != null) {
                val resourceLocation = ResourceLocation("airclient_capes_${file.nameWithoutExtension.lowercase()}")
                mc.textureManager.loadTexture(resourceLocation, DynamicTexture(bufferedImage))
                ClientUtils.LOGGER.info("[Cape] Successfully loaded texture: ${file.name} -> $resourceLocation")
                resourceLocation
            } else {
                ClientUtils.LOGGER.error("[Cape] Failed to read image: ${file.name}")
                null
            }
        } catch (e: Exception) {
            ClientUtils.LOGGER.error("[Cape] Failed to load cape texture ${file.name}: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    fun getCapeForPlayer(uuid: UUID): ResourceLocation? {
        if (!state) return null
        
        val currentPlayer = mc.thePlayer ?: return null
        
        if (uuid != currentPlayer.uniqueID) return null
        
        // 检查是否需要更新（实时切换支持）
        val currentMode = capeMode
        if (currentMode != lastCapeMode) {
            ClientUtils.LOGGER.info("[Cape] Detected mode change: $lastCapeMode -> $currentMode")
            lastCapeMode = currentMode
            updateCurrentCape(currentMode)
        }
        
        return currentCape
    }

    override val tag
        get() = capeMode

    fun refreshCapes() {
        ClientUtils.LOGGER.info("[Cape] Refreshing capes...")
        initialized = false
        // 清理所有缓存的纹理资源
        capeMap.values.forEach { loc ->
            try {
                mc.textureManager.deleteTexture(loc)
            } catch (e: Exception) {
                ClientUtils.LOGGER.warn("[Cape] Failed to delete texture during refresh: ${loc.resourcePath}")
            }
        }
        capeMap.clear()
        currentCape = null
        initializeCapes()

        capeModeValue?.let { value ->
            val capeNames = mutableListOf("None")
            capeNames.addAll(capeFiles.map { it.nameWithoutExtension })
            value.updateValues(capeNames.toTypedArray())
            ClientUtils.LOGGER.info("[Cape] Refreshed cape list: ${capeFiles.size} capes")
        }
    }

    override fun onDisable() {
        // 模块禁用时清理所有缓存的纹理资源
        capeMap.values.forEach { loc ->
            try {
                mc.textureManager.deleteTexture(loc)
            } catch (e: Exception) {
                ClientUtils.LOGGER.warn("[Cape] Failed to delete texture on disable: ${loc.resourcePath}")
            }
        }
        capeMap.clear()
        currentCape = null
    }
}
