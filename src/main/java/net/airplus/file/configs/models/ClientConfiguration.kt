package net.airplus.file.configs.models

import net.airplus.AirPlus
import net.airplus.config.Configurable
import net.airplus.utils.client.MinecraftInstance
import net.airplus.utils.render.IconUtils
import org.lwjgl.opengl.Display

object ClientConfiguration : Configurable("ClientConfiguration"), MinecraftInstance {
    var clientTitle by boolean("ClientTitle", true)
    var customBackground by boolean("CustomBackground", true)
    var particles by boolean("Particles", false)
    var stylisedAlts by boolean("StylisedAlts", true)
    var unformattedAlts by boolean("CleanAlts", true)
    var altsLength by int("AltsLength", 16, 4..20)
    var altsPrefix by text("AltsPrefix", "")
    // The game language can be overridden by the user. empty=default
    var overrideLanguage by text("OverrideLanguage","")

    // 主菜单布局风格（Flux = Flux 风格，其余见 MainMenuStyles；旧值 Default/Custom 由 MainMenuStyles.normalize 迁移）
    var mainMenuStyle by text("MainMenuStyle", "Flux")

    // 主菜单背景索引（0 = Flux 动态背景，其余对应 MainMenuStyles.BACKGROUND_IMAGES，取模避免越界）
    var customMenuBackgroundImageIndex by int("MainMenuBackgroundIndex", 0, 0..999)

    fun updateClientWindow() {
        if (clientTitle) {
            // Set LiquidBounce title
            Display.setTitle(AirPlus.clientTitle)
            // Update favicon
            IconUtils.favicon?.let { icons ->
                Display.setIcon(icons)
            }
        } else {
            // Set original title
            Display.setTitle("Minecraft 1.8.9")
            // Update favicon
            mc.setWindowIcon()
        }
    }

}