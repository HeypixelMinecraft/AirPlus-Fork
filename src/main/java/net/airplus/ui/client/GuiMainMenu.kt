/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.ui.client

import net.airplus.AirPlus.CLIENT_NAME
import net.airplus.AirPlus.clientVersionText
import net.airplus.api.ClientUpdate
import net.airplus.api.ClientUpdate.hasUpdate
import net.airplus.file.FileManager
import net.airplus.file.FileManager.valuesConfig
import net.airplus.lang.translationMenu
import net.airplus.ui.font.Fonts
import net.airplus.utils.client.JavaVersion
import net.airplus.utils.client.javaVersion
import net.airplus.utils.io.MiscUtils
import net.airplus.utils.render.MenuBackground
import net.airplus.utils.ui.AbstractScreen
import net.airplus.utils.ui.MenuButton
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiMultiplayer
import net.minecraft.client.gui.GuiSelectWorld
import net.minecraft.client.resources.I18n
import org.lwjgl.input.Mouse
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit

class GuiMainMenu : AbstractScreen() {

    private var popup: PopupScreen? = null

    companion object {
        private var popupOnce = false
        var lastWarningTime: Long? = null
        private val warningInterval = TimeUnit.DAYS.toMillis(7)

        fun shouldShowWarning() = lastWarningTime == null || Instant.now().toEpochMilli() - lastWarningTime!! > warningInterval
    }

    init {
        if (!popupOnce) {
            javaVersion?.let {
                when {
                    it.major == 1 && it.minor == 8 && it.update < 100 -> showOutdatedJava8Warning()
                    it.major > 8 -> showJava11Warning()
                }
            }
            when {
                FileManager.firstStart -> showWelcomePopup()
                hasUpdate() -> showUpdatePopup()
                shouldShowWarning() -> showDiscontinuedWarning()
            }
            popupOnce = true
        }
    }

    override fun initGui() {
        val buttonWidth = 200
        val buttonHeight = 24
        val buttonSpacing = 28
        val x = width / 2 - buttonWidth / 2

        // 四个按钮（3 个间距）整组在屏幕垂直居中
        var y = height / 2 - (buttonHeight * 4 + buttonSpacing * 3) / 2

        +MenuButton(1, x, y, buttonWidth, buttonHeight, I18n.format("menu.singleplayer"))
        y += buttonSpacing
        +MenuButton(2, x, y, buttonWidth, buttonHeight, I18n.format("menu.multiplayer"))
        y += buttonSpacing
        +MenuButton(3, x, y, buttonWidth, buttonHeight, translationMenu("settings"))
        y += buttonSpacing
        +MenuButton(4, x, y, buttonWidth, buttonHeight, I18n.format("menu.quit"))

        // Unobtrusive background switcher in the bottom right corner
        +MenuButton(5, width - 96, height - 24, 88, 18, translationMenu("changeBackground"))
    }

    private fun showWelcomePopup() {
        popup = PopupScreen {
            title("§a§l欢迎使用AirPlus!")
            message("""
            感谢下载和使用 §b$CLIENT_NAME§e!
            当前客户端版本: §b$clientVersionText§e

            获取支持或报告问题:加入QQ群聊722573066
            """.trimIndent())
            button("§aOK")
            onClose { popup = null }
        }
    }

    private fun showUpdatePopup() {
        val newestVersion = ClientUpdate.newestVersion ?: return

        val dateFormatter = SimpleDateFormat("EEEE, MMMM dd, yyyy, h a z", Locale.ENGLISH)
        val newestVersionDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH).parse(newestVersion.publishedAt)
        val formattedNewestDate = newestVersionDate?.let { dateFormatter.format(it) } ?: newestVersion.publishedAt
        popup = PopupScreen {
            title("§bNew Update Available!")
            message("""
                §eA new ${if (newestVersion.prerelease) "pre-release" else "version"} of $CLIENT_NAME is available!

                - §aVersion:§r ${newestVersion.tagName}
                - §aDate:§r $formattedNewestDate

                §6Changes:§r
                ${newestVersion.body ?: ""}

                §bUpgrade now to enjoy the latest features and improvements!§r
            """.trimIndent())
            button("§aDownload") { MiscUtils.showURL(newestVersion.htmlUrl) }
            onClose { popup = null }
        }
    }

    private fun showDiscontinuedWarning() {
        popup = PopupScreen {
            title("§c§lUnsupported version")
            message("""
                §6§lThis version is discontinued and unsupported.§r

                §eWe strongly recommend switching to §b$CLIENT_NAME Nextgen§e,
                which offers the following benefits:

                §a- §fSupports all Minecraft versions from §71.7§f to §71.21+§f.
                §a- §fFrequent updates with the latest bypasses and features.
                §a- §fActive development and official support.
                §a- §fImproved performance and compatibility.

                §cWhy upgrade?§r
                - No new bypasses or features will be introduced in this version.
                - Auto config support will not be actively maintained.
                - Unofficial forks of this version are discouraged as they lack the full feature set of Nextgen and cannot be trusted.

                §9Upgrade to LiquidBounce Nextgen today for a better experience!§r
            """.trimIndent())
            button("§aDownload Nextgen") { MiscUtils.showURL("https://liquidbounce.net/download") }
            button("§eInstallation Tutorial") { MiscUtils.showURL("https://www.youtube.com/watch?v=i_r1i4m-NZc") }
            onClose {
                popup = null
                lastWarningTime = Instant.now().toEpochMilli()
                FileManager.saveConfig(valuesConfig)
            }
        }
    }

    private fun showOutdatedJava8Warning() {
        popup = PopupScreen {
            title("§c§lOutdated Java Runtime Environment")
            message("""
                §6§lYou are using an outdated version of Java 8 (${javaVersion!!.raw}).§r

                §fThis might cause unexpected §c§lBUGS§f.
                Please update it to 8u101+, or get a new one from the Internet.
            """.trimIndent())
            button("§aDownload Java") { MiscUtils.showURL(JavaVersion.DOWNLOAD_PAGE) }
            button("§eI realized")
            onClose { popup = null }
        }
    }

    private fun showJava11Warning() {
        popup = PopupScreen {
            title("§c§lInappropriate Java Runtime Environment")
            message("""
                §6§lThis version of $CLIENT_NAME is designed for Java 8 environment.§r

                §fHigher versions of Java might cause bug or crash.
                You can get JRE 8 from the Internet.
            """.trimIndent())
            button("§aDownload Java") { MiscUtils.showURL(JavaVersion.DOWNLOAD_PAGE) }
            button("§eI realized")
            onClose { popup = null }
        }
    }


    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawBackground(0)

        // 标题：「Air」白色 +「Plus」黑色，整体水平居中
        val titleFont = Fonts.fontBold180
        val airWidth = titleFont.getStringWidth("Air").toFloat()
        val plusWidth = titleFont.getStringWidth("Plus").toFloat()
        val titleX = width / 2F - (airWidth + plusWidth) / 2F
        val titleY = height / 8F
        titleFont.drawStringWithShadow("Air", titleX, titleY, 0xffffff)
        titleFont.drawStringWithShadow("Plus", titleX + airWidth, titleY, 0x000000)

        // 版本号：灰色小字，居中在标题正下方
        Fonts.fontSemibold35.drawCenteredString(
            clientVersionText,
            width / 2F,
            titleY + titleFont.fontHeight + 4F,
            0xAAAAAA,
            true
        )

        super.drawScreen(mouseX, mouseY, partialTicks)

        if (popup != null) {
            popup!!.drawScreen(width, height, mouseX, mouseY)
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (popup != null) {
            popup!!.mouseClicked(mouseX, mouseY, mouseButton)
            return
        }

        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun actionPerformed(button: GuiButton) {
        if (popup != null) {
            return
        }

        when (button.id) {
            1 -> mc.displayGuiScreen(GuiSelectWorld(this))
            2 -> mc.displayGuiScreen(GuiMultiplayer(this))
            3 -> mc.displayGuiScreen(GuiSettingsMenu(this))
            4 -> mc.shutdown()
            5 -> MenuBackground.nextBackground()
        }
    }

    override fun handleMouseInput() {
        if (popup != null) {
            val eventDWheel = Mouse.getEventDWheel()
            if (eventDWheel != 0) {
                popup!!.handleMouseWheel(eventDWheel)
            }
        }

        super.handleMouseInput()
    }
}
