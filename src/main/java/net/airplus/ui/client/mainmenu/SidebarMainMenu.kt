/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.ui.client.mainmenu

import net.airplus.AirPlus.CLIENT_NAME
import net.airplus.AirPlus.clientVersionText
import net.airplus.ui.client.GuiModsMenu
import net.airplus.ui.client.altmanager.GuiAltManager
import net.airplus.ui.font.Fonts
import net.airplus.utils.render.RenderUtils
import net.airplus.utils.timing.MSTimer
import net.airplus.utils.ui.AbstractScreen
import net.minecraft.client.gui.GuiMultiplayer
import net.minecraft.client.gui.GuiOptions
import net.minecraft.client.gui.GuiSelectWorld
import net.minecraft.client.gui.ScaledResolution
import java.awt.Color

/**
 * 左侧边栏主菜单（迁移自 AirClient SidebarMainMenu）。
 *
 * 风格：现代杂志 / 工作室风格，强烈非对称。
 * 布局：左 38% 深炭半透明侧边栏（垂直导航 + Logo + 底部状态），右 62% 大块装饰区显示超大半透明标题与几何线条。
 * 配色：侧边栏深炭 #1F1F22，主区域暖米白半透明 #F0EAE0，强调色暖橙 #C08552。
 */
class SidebarMainMenu : AbstractScreen() {

    private val timer = MSTimer()

    private data class Entry(val label: String, val desc: String, val action: () -> Unit)

    private val entries = listOf(
        Entry("Singleplayer", "Local world") { mc.displayGuiScreen(GuiSelectWorld(this)) },
        Entry("Multiplayer", "Join server") { mc.displayGuiScreen(GuiMultiplayer(this)) },
        Entry("Alt Manager", "Accounts") { mc.displayGuiScreen(GuiAltManager(this)) },
        Entry("Options", "Game settings") { mc.displayGuiScreen(GuiOptions(this, mc.gameSettings)) },
        Entry("Mods", "Client modules") { mc.displayGuiScreen(GuiModsMenu(this)) },
        Entry("Quit", "Exit game") { mc.shutdown() }
    )

    private val hoverProgress = FloatArray(entries.size)

    override fun initGui() {
        timer.reset()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val sr = ScaledResolution(mc)
        width = sr.scaledWidth
        height = sr.scaledHeight

        // 统一背景
        MainMenuStyles.drawMenuBackground(width, height, 0)

        val sidebarW = (width * 0.38f).toInt().coerceIn(280, 440)
        val accent = Color(192, 133, 82)
        val sidebarBg = Color(31, 31, 34, 230)
        val mainBg = Color(240, 234, 224, 70)

        // 右侧主区域淡色蒙层
        RenderUtils.drawRect(
            sidebarW.toFloat(), 0f, width.toFloat(), height.toFloat(),
            mainBg.rgb
        )

        // 左侧侧边栏
        RenderUtils.drawRect(0f, 0f, sidebarW.toFloat(), height.toFloat(), sidebarBg.rgb)
        // 侧边栏右侧细分割线（强调色）
        RenderUtils.drawRect(
            (sidebarW - 2).toFloat(), 0f, sidebarW.toFloat(), height.toFloat(),
            accent.rgb
        )

        val titleFont = Fonts.fontBold180
        val labelFont = Fonts.fontSemibold35
        val smallFont = Fonts.fontRegular35
        val descFont = Fonts.fontRegular30
        val labelH = labelFont.height
        val descH = descFont.height
        val smallH = smallFont.height

        // 侧边栏顶部 Logo 区
        val padX = 28f
        val logoY = 38f
        // 小方块 logo
        RenderUtils.drawRoundedRect(padX, logoY, padX + 26f, logoY + 26f, accent.rgb, 5f)
        val logoChar = "A"
        Fonts.fontExtraBold35.drawString(
            logoChar, padX + 13f - Fonts.fontExtraBold35.getStringWidth(logoChar) / 2f,
            logoY + 13f - Fonts.fontExtraBold35.height / 2f + 1f,
            Color(245, 240, 232).rgb
        )
        labelFont.drawString(CLIENT_NAME, padX + 38f, logoY + 2f, Color(238, 232, 222).rgb)
        descFont.drawString(clientVersionText, padX + 38f, logoY + 4f + labelH, Color(150, 144, 134).rgb)

        // 顶部下方分隔线
        val sepY = logoY + 44f
        RenderUtils.drawRect(padX, sepY, sidebarW - padX, sepY + 1f, Color(80, 76, 70, 160).rgb)

        // 导航按钮列表
        val listY = sepY + 22f
        val itemH = 52f
        val itemW = sidebarW - padX * 2

        for (i in entries.indices) {
            val e = entries[i]
            val iy = listY + i * itemH
            val hovered = mouseX >= padX && mouseX <= padX + itemW &&
                    mouseY >= iy && mouseY <= iy + itemH - 6f

            val target = if (hovered) 1f else 0f
            hoverProgress[i] += (target - hoverProgress[i]) * 0.2f
            val p = hoverProgress[i]

            // 悬停背景
            if (p > 0.02f) {
                RenderUtils.drawRoundedRect(
                    padX, iy, padX + itemW, iy + itemH - 6f,
                    Color(255, 255, 255, (20 * p).toInt()).rgb, 5f
                )
            }

            // 选中/悬停左侧强调竖条
            if (p > 0.02f) {
                val barH = (itemH - 6f) * 0.6f
                val barY = iy + ((itemH - 6f) - barH) / 2f
                RenderUtils.drawRect(
                    padX, barY, padX + 3f * p, barY + barH,
                    Color(accent.red, accent.green, accent.blue, (220 * p).toInt()).rgb
                )
            }

            // 序号（右侧小字）
            val idxStr = String.format("%02d", i + 1)
            val idxW = descFont.getStringWidth(idxStr)
            descFont.drawString(
                idxStr, padX + itemW - idxW - 4f, iy + 8f,
                Color(120, 116, 108, (180 + 60 * p).toInt()).rgb
            )

            // 标签（左对齐，上方）
            labelFont.drawString(
                e.label, padX + 16f, iy + 7f,
                mixColor(Color(210, 204, 194), Color(250, 246, 238), p).rgb
            )
            // 描述（左对齐，下方，紧跟标签）
            descFont.drawString(
                e.desc, padX + 16f, iy + 9f + labelH,
                Color(130, 126, 118).rgb
            )
        }

        // 侧边栏底部状态（上移避免与左下角按钮重叠）
        val footY = height - 98f
        RenderUtils.drawRect(padX, footY, sidebarW - padX, footY + 1f, Color(80, 76, 70, 140).rgb)
        // 状态点
        val dotX = padX + 4f
        val dotY = footY + 16f
        RenderUtils.drawRoundedRect(dotX - 3f, dotY - 3f, dotX + 3f, dotY + 3f, Color(120, 200, 130).rgb, 3f)
        smallFont.drawString("Ready to play", padX + 14f, footY + 9f, Color(170, 166, 158).rgb)

        // ===== 右侧大装饰区 =====
        val mainX = sidebarW.toFloat()
        val mainW = width - sidebarW

        // 超大半透明背景标题
        val bigTitle = CLIENT_NAME.uppercase()
        val bigFont = titleFont
        val bigW = bigFont.getStringWidth(bigTitle)
        val maxBigW = mainW * 0.9f
        val useBig = bigW <= maxBigW
        val usedFont = if (useBig) bigFont else labelFont
        val usedW = usedFont.getStringWidth(bigTitle)
        val bigX = mainX + mainW * 0.5f - usedW / 2f
        val bigY = height * 0.26f

        usedFont.drawString(bigTitle, bigX + 3f, bigY + 3f, Color(0, 0, 0, 30).rgb)
        usedFont.drawString(bigTitle, bigX, bigY, Color(60, 56, 50, 80).rgb)

        // 副标题
        Fonts.fontSemibold40.drawCenteredString(
            "Minecraft 1.8.9",
            mainX + mainW * 0.5f, bigY + usedFont.height + 18f,
            Color(90, 86, 80, 200).rgb, false
        )

        // 装饰几何线（右侧斜线组）
        val lineX = mainX + mainW - 80f
        var lineY = height * 0.18f
        for (i in 0 until 6) {
            RenderUtils.drawRect(
                lineX, lineY + i * 8f, lineX + 50f, lineY + i * 8f + 1f,
                Color(192, 133, 82, 60 + i * 20).rgb
            )
        }

        // 右下角版本号
        Fonts.fontRegular30.drawString(
            clientVersionText,
            width - 70f, height - 40f,
            Color(120, 116, 108, 200).rgb
        )

        // 右下角切换按钮
        MainMenuStyles.drawSettingsButton(width, height, mouseX, mouseY)

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (MainMenuStyles.handleSettingsClick(width, height, mouseX, mouseY, mouseButton)) {
            timer.reset()
            return
        }

        if (mouseButton == 0 && timer.hasTimePassed(200)) {
            val sidebarW = (width * 0.38f).toInt().coerceIn(280, 440)
            val padX = 28f
            val sepY = 38f + 44f
            val listY = sepY + 22f
            val itemH = 52f
            val itemW = sidebarW - padX * 2

            for (i in entries.indices) {
                val iy = listY + i * itemH
                if (mouseX >= padX && mouseX <= padX + itemW &&
                    mouseY >= iy && mouseY <= iy + itemH - 6f) {
                    entries[i].action()
                    timer.reset()
                    return
                }
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    private fun mixColor(a: Color, b: Color, t: Float): Color {
        val tt = t.coerceIn(0f, 1f)
        return Color(
            (a.red + (b.red - a.red) * tt).toInt(),
            (a.green + (b.green - a.green) * tt).toInt(),
            (a.blue + (b.blue - a.blue) * tt).toInt()
        )
    }
}
