/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.module.modules.render

import net.airplus.features.module.Category
import net.airplus.features.module.Module
import net.airplus.ui.client.theme.ThemeSelector
import net.airplus.utils.client.ClientThemesUtils
import org.lwjgl.input.Keyboard
import java.awt.Color

/**
 * Central theme settings, migrated from AirClient. All values sync into
 * [ClientThemesUtils] (the single color source) whenever they change, so
 * config loading and runtime changes behave the same.
 *
 * Enabling opens the theme selector GUI; the module itself never turns on.
 */
object ThemeManager : Module("ThemeManager", Category.CLIENT, Keyboard.KEY_NONE, canBeEnabled = false) {

    private val themeValue = choices("Theme", ClientThemesUtils.themeNames, "Moon Purple")
        .onChanged { syncToClientThemesUtils() }
    private val themeFadeSpeedValue = int("ThemeFadeSpeed", 7, 1..10)
        .onChanged { syncToClientThemesUtils() }
    private val updownValue = boolean("UpDown", false)
        .onChanged { syncToClientThemesUtils() }
    private val backgroundModeValue = choices("BackgroundMode", arrayOf("Synced", "Dark", "Custom", "Neverlose", "None"), "Synced")
        .onChanged { syncToClientThemesUtils() }
    private val customBgColorValue = color("CustomBgColor", Color(32, 32, 64)) { backgroundMode == "Custom" }
    private val panelColorValue = color("PanelColor", Color(35, 35, 35, 200))

    val theme: String
        get() = themeValue.get()

    val panelColor: Color
        get() = panelColorValue.get()

    val themeFadeSpeed: Int
        get() = themeFadeSpeedValue.get()

    val updown: Boolean
        get() = updownValue.get()

    val backgroundMode: String
        get() = backgroundModeValue.get()

    override fun onEnable() {
        mc.displayGuiScreen(ThemeSelector())
    }

    /** Push all settings into the theme engine. */
    fun syncToClientThemesUtils() {
        ClientThemesUtils.ClientColorMode = themeValue.get()
        ClientThemesUtils.ThemeFadeSpeed = themeFadeSpeedValue.get()
        ClientThemesUtils.updown = updownValue.get()
        ClientThemesUtils.BackgroundMode = backgroundModeValue.get()
        ClientThemesUtils.customBgColor = customBgColorValue.get()
    }

    /** Programmatically change the theme (used by the theme selector GUI). */
    fun setTheme(newTheme: String) {
        themeValue.set(newTheme)
        ClientThemesUtils.ClientColorMode = newTheme
    }
}
