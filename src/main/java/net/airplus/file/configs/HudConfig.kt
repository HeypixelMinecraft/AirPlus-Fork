/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.file.configs

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.airplus.config.FontValue
import net.airplus.file.FileConfig
import net.airplus.ui.client.hud.HUD
import net.airplus.ui.client.hud.element.Side
import net.airplus.utils.client.ClientUtils
import net.airplus.utils.io.json
import net.airplus.utils.io.jsonArray
import net.airplus.utils.io.readJson
import net.airplus.utils.io.writeJson
import java.io.File
import java.io.IOException

class HudConfig(file: File) : FileConfig(file) {

    override fun loadDefault() = HUD.setDefault()

    /**
     * Load config from file
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    override fun loadConfig() {
        val jsonArray = file.readJson() as? JsonArray ?: return
        applyHudJson(jsonArray)
    }

    /**
     * Save config to file
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    override fun saveConfig() {
        file.writeJson(serializeHud())
    }

    companion object {

        /**
         * 把当前 HUD 序列化成 JSON 数组（与 hud.json 格式一致）。
         * 供 hud.json 以及 HUD 预设（HudPresets）共用。
         */
        fun serializeHud(): JsonArray {
            return jsonArray {
                for (element in HUD.elements) {
                    +json {
                        "Type" to element.name
                        "X" to element.x
                        "Y" to element.y
                        "Scale" to element.scale
                        "HorizontalFacing" to element.side.horizontal.sideName
                        "VerticalFacing" to element.side.vertical.sideName

                        element.values.forEach {
                            it.name to it.toJson()
                        }
                    }
                }
            }
        }

        /**
         * 应用一段 HUD JSON（先清空现有元素，再逐个恢复，最后补上 force 元素）。
         */
        fun applyHudJson(jsonArray: JsonArray) {
            HUD.clearElements()

            try {
                for (jsonObject in jsonArray) {
                    if (jsonObject !is JsonObject)
                        continue

                    if (!jsonObject.has("Type"))
                        continue

                    val type = jsonObject["Type"].asString

                    try {
                        val elementClass = HUD.ELEMENTS.entries.find { it.value.name == type }?.key

                        if (elementClass == null) {
                            ClientUtils.LOGGER.warn("Unrecognized HUD element: '$type'")
                            continue
                        }

                        val element = elementClass.newInstance()

                        element.x = jsonObject["X"].asDouble
                        element.y = jsonObject["Y"].asDouble
                        element.scale = jsonObject["Scale"].asFloat
                        element.side = Side(
                            Side.Horizontal.getByName(jsonObject["HorizontalFacing"].asString) ?: Side.Horizontal.RIGHT,
                            Side.Vertical.getByName(jsonObject["VerticalFacing"].asString) ?: Side.Vertical.UP
                        )

                        for (value in element.values) {
                            if (jsonObject.has(value.name))
                                value.fromJson(jsonObject[value.name])
                        }

                        // Support for old HUD files
                        if (jsonObject.has("font"))
                            element.values.find { it is FontValue }?.fromJson(jsonObject["font"])

                        HUD.addElement(element)
                    } catch (e: Exception) {
                        ClientUtils.LOGGER.error("Error while loading custom HUD element '$type' from config.", e)
                    }
                }

                // Add forced elements when missing
                for ((elementClass, info) in HUD.ELEMENTS) {
                    if (info.force && HUD.elements.none { it.javaClass == elementClass }) {
                        HUD.addElement(elementClass.newInstance())
                    }
                }
            } catch (e: Exception) {
                ClientUtils.LOGGER.error("Error while loading custom hud config.", e)
                HUD.setDefault()
            }
        }
    }
}
