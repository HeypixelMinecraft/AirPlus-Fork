/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.file

import com.google.gson.JsonArray
import net.airplus.file.configs.HudConfig
import net.airplus.utils.client.ClientUtils
import net.airplus.utils.io.readJson
import net.airplus.utils.io.writeJson
import java.io.File

/**
 * 命名 HUD 预设：保存在 <client dir>/hud-presets/<name>.json，
 * 供 HUD 编辑器的 Save / Load 对话框使用。
 */
object HudPresets {

    val presetsDir = File(FileManager.dir, "hud-presets")

    private fun presetFile(name: String): File {
        // 简单过滤文件名非法字符
        val safeName = name.trim().replace(Regex("[\\\\/:*?\"<>|]"), "_").ifEmpty { "Preset" }
        return File(presetsDir.apply { if (!exists()) mkdirs() }, "$safeName.json")
    }

    /** 已保存的预设名（不含 .json 后缀），按名称排序 */
    fun list(): List<String> {
        if (!presetsDir.isDirectory) return emptyList()
        return presetsDir.listFiles { f -> f.isFile && f.extension == "json" }
            ?.map { it.nameWithoutExtension }
            ?.sorted()
            ?: emptyList()
    }

    /** 把当前 HUD 保存为指定名称的预设 */
    fun save(name: String): Boolean {
        return try {
            presetFile(name).writeJson(HudConfig.serializeHud())
            ClientUtils.LOGGER.info("[HudPresets] Saved HUD preset: $name")
            true
        } catch (t: Throwable) {
            ClientUtils.LOGGER.error("[HudPresets] Failed to save HUD preset: $name", t)
            false
        }
    }

    /** 加载指定名称的预设，成功返回 true */
    fun load(name: String): Boolean {
        val file = presetFile(name)
        if (!file.isFile) return false
        return try {
            val array = file.readJson() as? JsonArray ?: return false
            HudConfig.applyHudJson(array)
            ClientUtils.LOGGER.info("[HudPresets] Loaded HUD preset: $name")
            true
        } catch (t: Throwable) {
            ClientUtils.LOGGER.error("[HudPresets] Failed to load HUD preset: $name", t)
            false
        }
    }

    /** 删除指定名称的预设，成功返回 true */
    fun delete(name: String): Boolean {
        val file = presetFile(name)
        if (!file.isFile) return false
        return try {
            file.delete()
            ClientUtils.LOGGER.info("[HudPresets] Deleted HUD preset: $name")
            true
        } catch (t: Throwable) {
            ClientUtils.LOGGER.error("[HudPresets] Failed to delete HUD preset: $name", t)
            false
        }
    }
}
