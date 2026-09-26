/*
 * Air Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 */
package net.airplus.ui.client.clickgui.flat

import net.airplus.AirPlus.moduleManager
import net.airplus.config.*
import net.airplus.features.module.Category
import net.airplus.features.module.Module
import net.airplus.ui.font.Fonts
import net.airplus.ui.font.GameFontRenderer
import net.airplus.utils.render.RenderUtils
import net.airplus.utils.render.RenderUtils.drawRoundedRect
import net.airplus.utils.render.RoundedUtil
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.MathHelper
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.io.IOException
import kotlin.math.*

class FlatClickGui : GuiScreen() {

    // ==================== Theme System ====================

    enum class FlatTheme(val displayName: String, val colors: IntArray) {
        OCEAN("Ocean", intArrayOf(
            0xFFEEF1F5.toInt(), 0xFFFFFFFF.toInt(), 0xFFE6EAF0.toInt(), 0xFFF8F9FB.toInt(),
            0xFFE6EAF0.toInt(), 0xFF3498DB.toInt(), 0xFF3498DB.toInt(), 0xFF2C3E50.toInt(),
            0xFF7F8C9A.toInt(), 0xFFF0F2F5.toInt(), 0xFF3498DB.toInt(), 0xFF546575.toInt(),
            0xFFD0D8E0.toInt(), 0xFF3498DB.toInt(), 0xFFE6EAF0.toInt(), 0xFFF0F2F5.toInt(),
            0xFF3498DB.toInt(), 0xFFF8F9FB.toInt(), 0xFFE6EAF0.toInt(), 0xFFE6EAF0.toInt(),
            0x1A000000.toInt()
        )),
        MIDNIGHT("Midnight", intArrayOf(
            0xFF1A1D24.toInt(), 0xFF252A35.toInt(), 0xFF2E3542.toInt(), 0xFF2C323D.toInt(),
            0xFF363D4B.toInt(), 0xFF6C7B95.toInt(), 0xFF6C7B95.toInt(), 0xFFE4E7EC.toInt(),
            0xFF6B7280.toInt(), 0xFF2E3542.toInt(), 0xFF6C7B95.toInt(), 0xFF9CA3AF.toInt(),
            0xFF3D4555.toInt(), 0xFF6C7B95.toInt(), 0xFF2E3542.toInt(), 0xFF2E3542.toInt(),
            0xFF6C7B95.toInt(), 0xFF2C323D.toInt(), 0xFF363D4B.toInt(), 0xFF363D4B.toInt(),
            0x20000000.toInt()
        )),
        ROSE("Rose", intArrayOf(
            0xFFFFF0F3.toInt(), 0xFFFFFFFF.toInt(), 0xFFF0DDE0.toInt(), 0xFFFFF5F7.toInt(),
            0xFFF0DDE0.toInt(), 0xFFE84393.toInt(), 0xFFE84393.toInt(), 0xFF2D232E.toInt(),
            0xFF9A7B8C.toInt(), 0xFFF5E6EB.toInt(), 0xFFE84393.toInt(), 0xFF7B5565.toInt(),
            0xFFDDAAB8.toInt(), 0xFFE84393.toInt(), 0xFFF0DDE0.toInt(), 0xFFF5E6EB.toInt(),
            0xFFE84393.toInt(), 0xFFFFF5F7.toInt(), 0xFFF0DDE0.toInt(), 0xFFF0DDE0.toInt(),
            0x18000000.toInt()
        )),
        FOREST("Forest", intArrayOf(
            0xFFF0F9F4.toInt(), 0xFFFFFFFF.toInt(), 0xFFD5EDDF.toInt(), 0xFFF6FAF7.toInt(),
            0xFFD5EDDF.toInt(), 0xFF27AE60.toInt(), 0xFF27AE60.toInt(), 0xFF1A332A.toInt(),
            0xFF6B9080.toInt(), 0xFFE0F2E8.toInt(), 0xFF27AE60.toInt(), 0xFF4A7868.toInt(),
            0xFFB8DCC8.toInt(), 0xFF27AE60.toInt(), 0xFFD5EDDF.toInt(), 0xFFE0F2E8.toInt(),
            0xFF27AE60.toInt(), 0xFFF6FAF7.toInt(), 0xFFD5EDDF.toInt(), 0xFFD5EDDF.toInt(),
            0x15000000.toInt()
        )),
        SLATE("Slate", intArrayOf(
            0xFFF4F5F7.toInt(), 0xFFFFFFFF.toInt(), 0xFFE2E6EA.toInt(), 0xFFFAFBFC.toInt(),
            0xFFE2E6EA.toInt(), 0xFF546E7A.toInt(), 0xFF546E7A.toInt(), 0xFF242B33.toInt(),
            0xFF8896A4.toInt(), 0xFFEEF0F3.toInt(), 0xFF546E7A.toInt(), 0xFF64748B.toInt(),
            0xFFCBD5E1.toInt(), 0xFF546E7A.toInt(), 0xFFE2E6EA.toInt(), 0xFFEEF0F3.toInt(),
            0xFF546E7A.toInt(), 0xFFFAFBFC.toInt(), 0xFFE2E6EA.toInt(), 0xFFE2E6EA.toInt(),
            0x12000000.toInt()
        )),
        AMBER("Amber", intArrayOf(
            0xFFFFF8F0.toInt(), 0xFFFFFFFF.toInt(), 0xFFF0E0CC.toInt(), 0xFFFFFCF8.toInt(),
            0xFFF0E0CC.toInt(), 0xFFE67E22.toInt(), 0xFFE67E22.toInt(), 0xFF3D2B1A.toInt(),
            0xFFA07850.toInt(), 0xFFF5EBDA.toInt(), 0xFFE67E22.toInt(), 0xFF8B6914.toInt(),
            0xFFE0C8A0.toInt(), 0xFFE67E22.toInt(), 0xFFF0E0CC.toInt(), 0xFFF5EBDA.toInt(),
            0xFFE67E22.toInt(), 0xFFFFFCF8.toInt(), 0xFFF0E0CC.toInt(), 0xFFF0E0CC.toInt(),
            0x16000000.toInt()
        )),
        VIOLET("Violet", intArrayOf(
            0xFFF5F0FF.toInt(), 0xFFFFFFFF.toInt(), 0xFFE0D8F0.toInt(), 0xFFFAF7FF.toInt(),
            0xFFE0D8F0.toInt(), 0xFF7C4DFF.toInt(), 0xFF7C4DFF.toInt(), 0xFF2A1F38.toInt(),
            0xFF8B70B8.toInt(), 0xFFEEE6F7.toInt(), 0xFF7C4DFF.toInt(), 0xFF6B5099.toInt(),
            0xFFD0BBF0.toInt(), 0xFF7C4DFF.toInt(), 0xFFE0D8F0.toInt(), 0xFFEEE6F7.toInt(),
            0xFF7C4DFF.toInt(), 0xFFFAF7FF.toInt(), 0xFFE0D8F0.toInt(), 0xFFE0D8F0.toInt(),
            0x14000000.toInt()
        )),
        MINT("Mint", intArrayOf(
            0xFFF0FBF8.toInt(), 0xFFFFFFFF.toInt(), 0xFFC8EDE2.toInt(), 0xFFF7FCFA.toInt(),
            0xFFC8EDE2.toInt(), 0xFF00B894.toInt(), 0xFF00B894.toInt(), 0xFF0D3330.toInt(),
            0xFF5BA89A.toInt(), 0xFFD8F0E8.toInt(), 0xFF00B894.toInt(), 0xFF3D9980.toInt(),
            0xFFA8DDD0.toInt(), 0xFF00B894.toInt(), 0xFFC8EDE2.toInt(), 0xFFD8F0E8.toInt(),
            0xFF00B894.toInt(), 0xFFF7FCFA.toInt(), 0xFFC8EDE2.toInt(), 0xFFC8EDE2.toInt(),
            0x13000000.toInt()
        ));

        companion object {
            @JvmStatic
            fun themeNames() = entries.map { it.displayName }.toTypedArray()
        }
    }

    // Color indices
    private companion object {
        const val C_BG = 0
        const val C_WINDOW = 1
        const val C_TITLEBAR_B = 2
        const val C_CARD_BG = 3
        const val C_CARD_B = 4
        const val C_CARD_HOVER = 5
        const val C_ACCENT = 6
        const val C_TEXT_PRI = 7
        const val C_TEXT_DIM = 8
        const val C_PILL_BG = 9
        const val C_PILL_ACT = 10
        const val C_PILL_TXT = 11
        const val C_TOGGLE_OFF = 12
        const val C_TOGGLE_ON = 13
        const val C_SLIDER_TK = 14
        const val C_MODE_BG = 15
        const val C_MODE_ACT = 16
        const val C_INPUT_BG = 17
        const val C_INPUT_BR = 18
        const val C_DRAWER_BR = 19
        const val C_SHADOW = 20

        const val TITLE_BAR_HEIGHT = 38f
        const val TOOLBAR_HEIGHT = 32f
        const val CARD_MIN_WIDTH = 120f
        const val CARD_GAP = 8f
        const val CARD_PADDING = 10f
        const val DRAWER_WIDTH = 260f
        const val DRAWER_HEADER_HEIGHT = 48f
        const val SETTING_MARGIN_BOTTOM = 16f
        const val RESIZE_HANDLE_SIZE = 14f
    }

    // Dynamic Color Fields
    private var BG_COLOR = 0; private var WINDOW_BG = 0; private var TITLEBAR_BORDER = 0
    private var CARD_BG = 0; private var CARD_BORDER = 0; private var CARD_HOVER_BORDER = 0
    private var ACCENT = 0; private var TEXT_PRIMARY = 0; private var TEXT_DIM = 0
    private var PILL_BG = 0; private var PILL_ACTIVE = 0; private var PILL_TEXT = 0
    private var TOGGLE_OFF = 0; private var TOGGLE_ON = 0
    private var SLIDER_TRACK = 0; private var MODE_CHIP_BG = 0; private var MODE_CHIP_ACTIVE = 0
    private var INPUT_BG = 0; private var INPUT_BORDER = 0; private var DRAWER_BORDER = 0; private var SHADOW_COLOR = 0

    // === Position & Size ===
    private var posX = 0f; private var posY = 0f; private var winWidth = 600f; private var winHeight = 420f
    private var dragging = false; private var dragOffX = 0f; private var dragOffY = 0f
    private var resizing = false; private var resizeOffX = 0f; private var resizeOffY = 0f
    private var isClosing = false

    // === State ===
    private var selectedCat: Category = Category.COMBAT
    private var selectedModule: Module? = null

    // Drawer animation
    private var drawerAnim = 0f; private var targetDrawerAnim = 0f
    // Scroll
    private var gridScroll = 0f; private var targetGridScroll = 0f
    private var drawerScroll = 0f; private var targetDrawerScroll = 0f
    // Open animation
    private var openAnim = 0f; private var lastAnimTime = 0L
    // Smooth grid reflow
    private var effectiveDrawerWidth = 0f

    // Dragging states
    private var dragValue: Value<*>? = null; private var dragKind: String? = null
    private val smoothSliderVals = HashMap<Value<*>, Float>()
    private val modeHScroll = HashMap<Value<*>, Float>()

    // Text input
    private var focusedTextValue: TextValue? = null; private var textBuffer = ""

    // Theme selector
    private var currentThemeIndex = 0

    // Fonts
    private val titleFont get() = Fonts.fontSemibold40
    private val normalFont get() = Fonts.fontSemibold35
    private val smallFont get() = Fonts.fontRegular35

    // === Init ===
    init {
        applyTheme(FlatTheme.OCEAN.displayName)
        lastAnimTime = System.currentTimeMillis()
    }

    private fun applyTheme(themeName: String) {
        var target: FlatTheme? = null
        var idx = 0
        for (t in FlatTheme.entries) {
            if (t.displayName.equals(themeName, true)) { target = t; break }
            idx++
        }
        if (target == null) { target = FlatTheme.OCEAN; idx = 0 }
        currentThemeIndex = idx
        val c = target.colors
        BG_COLOR = c[C_BG]; WINDOW_BG = c[C_WINDOW]; TITLEBAR_BORDER = c[C_TITLEBAR_B]
        CARD_BG = c[C_CARD_BG]; CARD_BORDER = c[C_CARD_B]; CARD_HOVER_BORDER = c[C_CARD_HOVER]
        ACCENT = c[C_ACCENT]; TEXT_PRIMARY = c[C_TEXT_PRI]; TEXT_DIM = c[C_TEXT_DIM]
        PILL_BG = c[C_PILL_BG]; PILL_ACTIVE = c[C_PILL_ACT]; PILL_TEXT = c[C_PILL_TXT]
        TOGGLE_OFF = c[C_TOGGLE_OFF]; TOGGLE_ON = c[C_TOGGLE_ON]
        SLIDER_TRACK = c[C_SLIDER_TK]; MODE_CHIP_BG = c[C_MODE_BG]; MODE_CHIP_ACTIVE = c[C_MODE_ACT]
        INPUT_BG = c[C_INPUT_BG]; INPUT_BORDER = c[C_INPUT_BR]; DRAWER_BORDER = c[C_DRAWER_BR]
        SHADOW_COLOR = c[C_SHADOW]
    }

    // === Core Overrides ===
    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        updateAnimations()
        handle(mouseX, mouseY, -1, GuiEvent.DRAW)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        handle(mouseX, mouseY, mouseButton, GuiEvent.CLICK)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        handle(mouseX, mouseY, state, GuiEvent.RELEASE)
    }

    @Throws(IOException::class)
    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (keyCode == 1) {
            triggerCloseAnimation()
            return
        }
        val focused = focusedTextValue
        if (focused != null) {
            when (keyCode) {
                14 -> { // backspace
                    if (textBuffer.isNotEmpty()) {
                        textBuffer = textBuffer.substring(0, textBuffer.length - 1)
                        focused.set(textBuffer)
                    }
                }
                28 -> { // enter
                    focused.set(textBuffer)
                    focusedTextValue = null
                    textBuffer = ""
                }
                else -> {
                    if ((Character.isLetterOrDigit(typedChar) || typedChar == ' ' || typedChar.isValidTextChar()) && textBuffer.length < 64) {
                        textBuffer += typedChar
                        focused.set(textBuffer)
                    }
                }
            }
            return
        }
        super.keyTyped(typedChar, keyCode)
    }

    private fun Char.isValidTextChar() = code in 33..126 || code in 161..255

    override fun onGuiClosed() {
        super.onGuiClosed()
    }

    override fun doesGuiPauseGame() = false

    override fun initGui() {
        Keyboard.enableRepeatEvents(true)
        if (posX == 0f && posY == 0f) {
            winWidth = 600f; winHeight = 420f
            posX = width / 2f - winWidth / 2f
            posY = height / 2f - winHeight / 2f
            if (posX <= 0f) posX = 150f
            if (posY <= 0f) posY = 80f
        }
        if (isClosing) {
            isClosing = false
            openAnim = 0f
        }
        lastAnimTime = System.currentTimeMillis()
    }

    // === Animation ===
    private fun updateAnimations() {
        val now = System.currentTimeMillis()
        val dt = max(0f, min(0.1f, (now - lastAnimTime) / 1000f))
        lastAnimTime = now

        val target = if (isClosing) 0f else 1f

        openAnim = animate(target, openAnim, dt * 12.0)
        drawerAnim = animate(targetDrawerAnim, drawerAnim, dt * 10.0)
        val targetEffDW = drawerAnim * DRAWER_WIDTH
        effectiveDrawerWidth = animate(targetEffDW, effectiveDrawerWidth, dt * 8.0)
        gridScroll = animate(targetGridScroll, gridScroll, dt * 12f)
        drawerScroll = animate(targetDrawerScroll, drawerScroll, dt * 12f)

        for ((prop, current) in smoothSliderVals) {
            val sliderTarget = getSliderTarget(prop)
            if (dragValue == prop && dragKind == "slider") {
                smoothSliderVals[prop] = sliderTarget
            } else {
                smoothSliderVals[prop] = animate(sliderTarget, current, dt * 18.0)
            }
        }

        if (isClosing && openAnim < 0.01f) {
            mc.displayGuiScreen(null)
        }
    }

    private fun triggerCloseAnimation() {
        if (!isClosing) isClosing = true
    }

    private fun animate(target: Float, current: Float, speed: Float): Float {
        return current + (target - current) * min(1f, speed)
    }

    private fun animate(target: Float, current: Float, speed: Double): Float {
        return current + (target - current) * min(1f, speed.toFloat())
    }

    private fun getSliderTarget(value: Value<*>): Float = when (value) {
        is IntValue -> (value.get() - value.minimum) / (value.maximum - value.minimum).toFloat().coerceAtLeast(1f)
        is FloatValue -> (value.get() - value.minimum) / (value.maximum - value.minimum).coerceAtLeast(0.001f)
        is BlockValue -> (value.get() - value.minimum) / (value.maximum - value.minimum).toFloat().coerceAtLeast(1f)
        else -> 0f
    }

    // === Scissor helpers ===
    private fun scissorStart(x: Float, y: Float, w: Float, h: Float) {
        val sr = ScaledResolution(mc)
        val scale = sr.scaleFactor
        GL11.glScissor(
            (x * scale).toInt(),
            ((sr.scaledHeight - y - h) * scale).toInt(),
            (w * scale).toInt(),
            (h * scale).toInt()
        )
        GL11.glEnable(GL11.GL_SCISSOR_TEST)
    }

    private fun scissorEnd() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST)
    }

    // === Hover helper ===
    private fun isHovered(mx: Int, my: Int, x: Float, y: Float, w: Float, h: Float) =
        mx >= x && mx <= x + w && my >= y && my <= y + h

    // === Font helpers ===
    private fun drawFlatString(text: String, x: Float, y: Float, color: Int, font: GameFontRenderer) {
        font.drawString(text, x, y, color)
    }

    private fun getStringWidth(text: String, font: GameFontRenderer) = font.getStringWidth(text)

    private fun getFontHeight(font: GameFontRenderer) = font.height

    // === Fill circle ===
    private fun fillCircle(cx: Float, cy: Float, radius: Float, segments: Int, color: Int) {
        RenderUtils.drawFilledCircle(cx.toInt(), cy.toInt(), radius, Color(color, true))
    }

    // === Rounded rect (width+height style) ===
    private fun drawFlatRoundedRect(x: Float, y: Float, w: Float, h: Float, radius: Float, color: Int) {
        RoundedUtil.drawRound(x, y, w, h, radius, Color(color, true))
    }

    // === Draw line with color ===
    private fun drawFlatLine(x1: Float, y1: Float, x2: Float, y2: Float, width: Float, color: Int) {
        GL11.glPushMatrix()
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        RenderUtils.glColor(Color(color, true))
        GL11.glLineWidth(width)
        GL11.glBegin(GL11.GL_LINES)
        GL11.glVertex2f(x1, y1)
        GL11.glVertex2f(x2, y2)
        GL11.glEnd()
        GL11.glColor4f(1f, 1f, 1f, 1f)
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glPopMatrix()
    }

    // === Enum for event type ===
    private enum class GuiEvent { DRAW, CLICK, RELEASE }

    // === Main Handle ===
    private fun handle(mouseX: Int, mouseY: Int, mouseButton: Int, type: GuiEvent) {
        val animOffsetY = (1.0f - openAnim) * 150f

        if (type == GuiEvent.DRAW) {
            if (dragging && Mouse.isButtonDown(0)) {
                posX = mouseX - dragOffX
                posY = mouseY - dragOffY
            }
            if (resizing && Mouse.isButtonDown(0)) {
                winWidth = max(500f, mouseX - resizeOffX - posX)
                winHeight = max(350f, mouseY - resizeOffY - posY)
            }
            updateDragStates(mouseX, mouseY)
        }

        if (type == GuiEvent.RELEASE) {
            dragging = false
            resizing = false
            dragValue = null
            dragKind = null
        }

        // --- Draw outer container ---
        if (type == GuiEvent.DRAW) {
            val drawY = posY + animOffsetY
            // Shadow
            RenderUtils.drawRect(posX + 4, drawY + 4, posX + winWidth + 4, drawY + winHeight + 4, SHADOW_COLOR)
            // Window background
            drawFlatRoundedRect(posX, drawY, winWidth, winHeight, 12f, WINDOW_BG)

            // Title bar
            drawFlatRoundedRect(posX, drawY, winWidth, TITLE_BAR_HEIGHT, 12f, WINDOW_BG)
            // Cover bottom rounded corners of titlebar
            RenderUtils.drawRect(posX, drawY + TITLE_BAR_HEIGHT - 6, posX + winWidth, drawY + TITLE_BAR_HEIGHT + 6, WINDOW_BG)
            // Title bar bottom border
            RenderUtils.drawRect(posX, drawY + TITLE_BAR_HEIGHT - 1, posX + winWidth, drawY + TITLE_BAR_HEIGHT, TITLEBAR_BORDER)
            // Title text
            val titleStr = "CLICKGUI"
            drawFlatString(titleStr, posX + 16, drawY + TITLE_BAR_HEIGHT / 2f - getFontHeight(titleFont) / 2f, TEXT_PRIMARY, titleFont)

            // Theme selector button
            val themeBtnW = 80f; val themeBtnH = 24f
            val themeBtnX = posX + winWidth - themeBtnW - 16
            val themeBtnY = drawY + (TITLE_BAR_HEIGHT - themeBtnH) / 2f
            val currentName = FlatTheme.entries[currentThemeIndex].displayName
            val themeHovered = isHovered(mouseX, mouseY, themeBtnX, themeBtnY, themeBtnW, themeBtnH)
            drawFlatRoundedRect(themeBtnX, themeBtnY, themeBtnW, themeBtnH, 6f, if (themeHovered) PILL_ACTIVE else PILL_BG)
            drawFlatString(currentName, themeBtnX + (themeBtnW - getStringWidth(currentName, normalFont)) / 2f,
                themeBtnY + (themeBtnH - getFontHeight(normalFont)) / 2f,
                if (themeHovered) 0xFFFFFFFF.toInt() else TEXT_DIM, normalFont)

            // Resize handle indicator
            val rhX = posX + winWidth - RESIZE_HANDLE_SIZE - 2
            val rhY = drawY + winHeight - RESIZE_HANDLE_SIZE - 2
            for (i in 0..2) {
                drawFlatLine(
                    rhX + i * 4, rhY + RESIZE_HANDLE_SIZE,
                    rhX + RESIZE_HANDLE_SIZE, rhY + i * 4,
                    1f, TEXT_DIM
                )
            }
        }

        // Theme selector click
        if (type == GuiEvent.CLICK) {
            val themeBtnW = 80f; val themeBtnH = 24f
            val themeBtnX = posX + winWidth - themeBtnW - 16
            val themeBtnY = (posY + animOffsetY) + (TITLE_BAR_HEIGHT - themeBtnH) / 2f
            if (isHovered(mouseX, mouseY, themeBtnX, themeBtnY, themeBtnW, themeBtnH) && mouseButton == 0) {
                val themes = FlatTheme.entries
                val nextIndex = (currentThemeIndex + 1) % themes.size
                currentThemeIndex = nextIndex
                applyTheme(themes[nextIndex].displayName)
                return
            }
        }

        // --- Titlebar interaction ---
        val titlebarDrawY = posY + animOffsetY
        val overTitlebar = isHovered(mouseX, mouseY, posX, titlebarDrawY, winWidth, TITLE_BAR_HEIGHT)

        if (overTitlebar && type == GuiEvent.CLICK && mouseButton == 0) {
            val themeBtnW = 80f; val themeBtnH = 24f
            val themeBtnX = posX + winWidth - themeBtnW - 16
            val themeBtnY = titlebarDrawY + (TITLE_BAR_HEIGHT - themeBtnH) / 2f
            if (!isHovered(mouseX, mouseY, themeBtnX, themeBtnY, themeBtnW, themeBtnH)) {
                dragging = true
                dragOffX = mouseX - posX
                dragOffY = mouseY - posY
                return
            }
        }

        // Resize handle click
        val overResize = isHovered(mouseX, mouseY,
            posX + winWidth - RESIZE_HANDLE_SIZE - 4,
            titlebarDrawY + winHeight - RESIZE_HANDLE_SIZE - 4,
            RESIZE_HANDLE_SIZE + 4, RESIZE_HANDLE_SIZE + 4)
        if (overResize && type == GuiEvent.CLICK && mouseButton == 0) {
            resizing = true
            resizeOffX = mouseX - (posX + winWidth)
            resizeOffY = mouseY - (posY + winHeight)
            return
        }

        // --- Body area ---
        val bodyX = posX
        val bodyY = posY + TITLE_BAR_HEIGHT + animOffsetY
        val bodyW = winWidth
        val bodyH = winHeight - TITLE_BAR_HEIGHT

        val drawerActualWidth = drawerAnim * DRAWER_WIDTH
        val gridW = bodyW - effectiveDrawerWidth

        // === LEFT: Grid Area ===
        val cardAreaTop = bodyY + TOOLBAR_HEIGHT + 8
        val cardAreaH = bodyH - TOOLBAR_HEIGHT - 8
        val cardAreaBottom = cardAreaTop + cardAreaH

        // Grid scroll
        if (type == GuiEvent.DRAW && isHovered(mouseX, mouseY, bodyX, cardAreaTop, gridW, cardAreaH)) {
            val wheel = Mouse.getDWheel()
            if (wheel != 0) {
                targetGridScroll += wheel / 4f
            }
        }

        // Category toolbar
        val toolbarY = bodyY + 4
        var pillX = bodyX + 16
        for (cat in Category.entries) {
            if (!cat.shouldShow()) continue
            val catName = cat.displayName
            val pillWidth = getStringWidth(catName, normalFont) + 20f
            val isActive = cat == selectedCat

            if (type == GuiEvent.DRAW) {
                val bgColor = if (isActive) PILL_ACTIVE else PILL_BG
                val textColor = if (isActive) 0xFFFFFFFF.toInt() else PILL_TEXT
                drawFlatRoundedRect(pillX, toolbarY, pillWidth, 28f, 14f, bgColor)
                val txtX = pillX + pillWidth / 2f - getStringWidth(catName, normalFont) / 2f
                val txtY = toolbarY + 14f - getFontHeight(normalFont) / 2f
                drawFlatString(catName, txtX, txtY, textColor, normalFont)
            } else if (type == GuiEvent.CLICK) {
                if (isHovered(mouseX, mouseY, pillX, toolbarY, pillWidth, 28f)) {
                    selectedCat = cat
                    selectedModule = null
                    targetDrawerAnim = 0f
                    gridScroll = 0f
                    targetGridScroll = 0f
                }
            }
            pillX += pillWidth + 8
        }

        // Card area scissoring
        if (type == GuiEvent.DRAW) {
            scissorStart(bodyX, cardAreaTop, gridW, cardAreaH)
        }

        // Card grid
        val modules = moduleManager[selectedCat].filter { !it.isHidden }
        val gridCols = max(1, ((gridW - 32) / (CARD_MIN_WIDTH + CARD_GAP)).toInt())
        val cardW = (gridW - 32 - (gridCols - 1) * CARD_GAP) / gridCols
        val cardH = 70f
        val totalRows = ceil(modules.size.toFloat() / gridCols).toInt()
        val totalContentHeight = totalRows * (cardH + CARD_GAP)
        val maxGridScroll = max(0f, totalContentHeight - cardAreaH)
        targetGridScroll = max(-maxGridScroll, min(0f, targetGridScroll))

        val cardStartX = bodyX + 16
        val cardStartY = cardAreaTop + gridScroll
        var col = 0
        var row = 0

        for (mod in modules) {
            val cx = cardStartX + col * (cardW + CARD_GAP)
            val cy = cardStartY + row * (cardH + CARD_GAP)

            if (type == GuiEvent.DRAW) {
                // Visibility alpha for edge fade
                var cardAlpha = 1f
                val fadeMargin = cardH * 0.5f
                if (cy + cardH < cardAreaTop + fadeMargin) {
                    cardAlpha = max(0f, min(1f, (cardAreaTop - cy) / fadeMargin))
                } else if (cy > cardAreaBottom - fadeMargin) {
                    cardAlpha = max(0f, min(1f, (cardAreaBottom - (cy + cardH)) / fadeMargin))
                }

                if (cardAlpha <= 0.01f) {
                    col++; if (col >= gridCols) { col = 0; row++ }; continue
                }

                val isEnabled = mod.state
                val isSelected = mod == selectedModule
                val isCardHovered = isHovered(mouseX, mouseY, cx, cy, cardW, cardH)

                if (cardAlpha < 0.99f) {
                    GlStateManager.enableBlend()
                    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
                    GlStateManager.color(1f, 1f, 1f, cardAlpha)
                }

                // Hover shadow
                if (isCardHovered) {
                    RenderUtils.drawRect(cx + 1, cy + 2, cx + cardW + 1, cy + cardH + 2, 0x0D000000.toInt())
                }

                // Card bg
                val borderColor = when { isCardHovered -> CARD_HOVER_BORDER; isSelected -> ACCENT; else -> CARD_BORDER }
                drawFlatRoundedRect(cx, cy, cardW, cardH, 6f, CARD_BG)
                // Borders
                RenderUtils.drawRect(cx, cy, cx + cardW, cy + 1.5f, borderColor)
                RenderUtils.drawRect(cx, cy + cardH - 1.5f, cx + cardW, cy + cardH, borderColor)
                RenderUtils.drawRect(cx, cy, cx + 1.5f, cy + cardH, borderColor)
                RenderUtils.drawRect(cx + cardW - 1.5f, cy, cx + cardW, cy + cardH, borderColor)

                // Accent left border when enabled
                if (isEnabled) {
                    RenderUtils.drawRect(cx, cy + 4, cx + 3, cy + cardH - 4, ACCENT)
                }

                // Status dot
                val dotCx = cx + cardW - 10
                val dotCy = cy + 10
                fillCircle(dotCx, dotCy, 3.5f, 20, if (isEnabled) ACCENT else TOGGLE_OFF)

                // Title
                drawFlatString(mod.name, cx + CARD_PADDING, cy + CARD_PADDING, TEXT_PRIMARY, normalFont)

                // Meta info
                val keyName = try { if (mod.keyBind == 0) "None" else Keyboard.getKeyName(mod.keyBind) } catch (_: Exception) { "None" }
                val metaStr = "$keyName · ${if (isEnabled) "ON" else "OFF"}"
                drawFlatString(metaStr, cx + CARD_PADDING, cy + CARD_PADDING + getFontHeight(normalFont) + 4, TEXT_DIM, smallFont)

                if (cardAlpha < 0.99f) {
                    GlStateManager.color(1f, 1f, 1f, 1f)
                }

            } else if (type == GuiEvent.CLICK) {
                if (isHovered(mouseX, mouseY, cx, cy, cardW, cardH)) {
                    if (mouseButton == 0) {
                        if (drawerAnim > 0.5f) {
                            selectedModule = null
                            targetDrawerAnim = 0f
                        } else {
                            mod.toggle()
                        }
                    } else if (mouseButton == 1) {
                        selectedModule = mod
                        targetDrawerAnim = 1f
                        drawerScroll = 0f; targetDrawerScroll = 0f
                    }
                }
            }

            col++
            if (col >= gridCols) { col = 0; row++ }
        }

        if (type == GuiEvent.DRAW) {
            scissorEnd()
        }

        // === RIGHT: Drawer ===
        if (drawerAnim > 0.01f) {
            val drawerX = posX + winWidth - drawerActualWidth
            val drawerY = posY + TITLE_BAR_HEIGHT + animOffsetY
            val drawerH = winHeight - TITLE_BAR_HEIGHT

            // Drawer scroll
            if (type == GuiEvent.DRAW) {
                val drawerBodyH = winHeight - TITLE_BAR_HEIGHT - DRAWER_HEADER_HEIGHT
                val fullDrawerX = posX + winWidth - DRAWER_WIDTH
                if (isHovered(mouseX, mouseY, fullDrawerX, drawerY + DRAWER_HEADER_HEIGHT, DRAWER_WIDTH, drawerBodyH)) {
                    val wheel = Mouse.getDWheel()
                    if (wheel != 0) {
                        targetDrawerScroll += wheel / 4f
                        val maxDS = calculateDrawerContentHeight()
                        targetDrawerScroll = max(-maxDS, min(0f, targetDrawerScroll))
                    }
                }
            }

            if (type == GuiEvent.DRAW) {
                // Drawer panel
                RenderUtils.drawRect(drawerX, drawerY, drawerX + drawerActualWidth, drawerY + drawerH, WINDOW_BG)
                // Left border
                RenderUtils.drawRect(drawerX, drawerY, drawerX + 1.5f, drawerY + drawerH, DRAWER_BORDER)

                // Header
                RenderUtils.drawRect(drawerX, drawerY, drawerX + drawerActualWidth, drawerY + DRAWER_HEADER_HEIGHT, WINDOW_BG)
                RenderUtils.drawRect(drawerX, drawerY + DRAWER_HEADER_HEIGHT - 1, drawerX + drawerActualWidth, drawerY + DRAWER_HEADER_HEIGHT, TITLEBAR_BORDER)

                // Module name in header
                selectedModule?.let {
                    drawFlatString(it.name, drawerX + 16, drawerY + DRAWER_HEADER_HEIGHT / 2f - getFontHeight(normalFont) / 2f, TEXT_PRIMARY, normalFont)
                }

                // Close button
                val closeBtnX = drawerX + drawerActualWidth - 28
                val closeBtnY = drawerY + DRAWER_HEADER_HEIGHT / 2f - 10
                val closeHovered = isHovered(mouseX, mouseY, closeBtnX, closeBtnY, 24f, 24f)
                if (closeHovered) {
                    drawFlatRoundedRect(closeBtnX, closeBtnY, 24f, 24f, 6f, PILL_BG)
                }
                drawFlatString("\u00d7", closeBtnX + 6, closeBtnY + 3, if (closeHovered) TEXT_PRIMARY else TEXT_DIM, normalFont)

                // Drawer body content
                val contentX = drawerX + 16
                var contentY = drawerY + DRAWER_HEADER_HEIGHT + 12 + drawerScroll
                val contentW = drawerActualWidth - 32
                val visibleContentH = drawerH - DRAWER_HEADER_HEIGHT - 24

                scissorStart(drawerX, drawerY + DRAWER_HEADER_HEIGHT, drawerActualWidth, drawerH - DRAWER_HEADER_HEIGHT)

                selectedModule?.let { mod ->
                    // Meta info line
                    val keyName2 = try { if (mod.keyBind == 0) "None" else Keyboard.getKeyName(mod.keyBind) } catch (_: Exception) { "None" }
                    val metaLine = "Key: $keyName2  |  Hidden: ${if (mod.isHidden) "Yes" else "No"}"
                    drawFlatString(metaLine, contentX, contentY, TEXT_DIM, smallFont)
                    contentY += getFontHeight(smallFont) + SETTING_MARGIN_BOTTOM + 4

                    // Values
                    for (value in mod.values) {
                        try {
                            if (!value.shouldRender()) continue
                            contentY = drawValue(contentX, contentY, contentW, value, mouseX, mouseY, type,
                                drawerY + DRAWER_HEADER_HEIGHT, visibleContentH)
                        } catch (_: Exception) {
                            contentY += getFontHeight(smallFont) + SETTING_MARGIN_BOTTOM
                        }
                    }
                }

                scissorEnd()

            } else if (type == GuiEvent.CLICK) {
                // Close button check
                val closeBtnX = drawerX + drawerActualWidth - 28
                val closeBtnY = drawerY + DRAWER_HEADER_HEIGHT / 2f - 10
                if (isHovered(mouseX, mouseY, closeBtnX, closeBtnY, 24f, 24f)) {
                    targetDrawerAnim = 0f
                    selectedModule = null
                    return
                }

                // Click outside drawer
                if (mouseX < drawerX && mouseButton == 0) {
                    targetDrawerAnim = 0f
                    selectedModule = null
                    return
                }

                // Value clicks
                selectedModule?.let { mod ->
                    val contentX = drawerX + 16
                    var contentY = drawerY + DRAWER_HEADER_HEIGHT + 12 + targetDrawerScroll
                    val contentW = drawerActualWidth - 32
                    val visibleContentH = drawerH - DRAWER_HEADER_HEIGHT - 24

                    contentY += getFontHeight(smallFont) + SETTING_MARGIN_BOTTOM + 4

                    for (value in mod.values) {
                        try {
                            if (!value.shouldRender()) continue
                            contentY = handleValueClick(contentX, contentY, contentW, value, mouseX, mouseY, mouseButton, type)
                        } catch (_: Exception) {
                            contentY += getFontHeight(smallFont) + SETTING_MARGIN_BOTTOM
                        }
                    }
                }
            }
        }
    }

    // === Value Rendering ===
    private fun drawValue(x: Float, y: Float, w: Float, value: Value<*>, mx: Int, my: Int, type: GuiEvent, clipTop: Float, clipH: Float): Float {
        when (value) {
            is BoolValue -> return drawBoolValue(x, y, w, value, mx, my, type)
            is ListValue -> return drawListValue(x, y, w, value, mx, my, type)
            is IntValue -> return drawSlider(x, y, w, value, value.get().toDouble(), value.minimum.toDouble(), value.maximum.toDouble(), value.get().toString(), mx, my, type)
            is FloatValue -> {
                val valStr = (Math.round(value.get() * 100.0) / 100.0).toString()
                return drawSlider(x, y, w, value, value.get().toDouble(), value.minimum.toDouble(), value.maximum.toDouble(), valStr, mx, my, type)
            }
            is IntRangeValue -> return drawIntRangeValue(x, y, w, value, mx, my, type)
            is FloatRangeValue -> return drawFloatRangeValue(x, y, w, value, mx, my, type)
            is TextValue -> return drawTextValue(x, y, w, value, mx, my, type)
            is ColorValue -> return drawColorValue(x, y, w, value, mx, my, type)
            is FontValue -> return drawFontValue(x, y, w, value, mx, my, type)
            is BlockValue -> return drawSlider(x, y, w, value, value.get().toDouble(), value.minimum.toDouble(), value.maximum.toDouble(), value.get().toString(), mx, my, type)
            else -> {
                if (type == GuiEvent.DRAW) drawFlatString(value.name, x, y, TEXT_DIM, smallFont)
                return y + getFontHeight(smallFont) + SETTING_MARGIN_BOTTOM
            }
        }
    }

    private fun drawBoolValue(x: Float, y: Float, w: Float, value: BoolValue, mx: Int, my: Int, type: GuiEvent): Float {
        val val_ = value.get()
        if (type == GuiEvent.DRAW) {
            drawFlatString(value.name, x, y, TEXT_DIM, smallFont)
            val toggleW = 38f; val toggleH = 20f
            val toggleX = x + w - toggleW
            val toggleY = y + (getFontHeight(smallFont) - toggleH) / 2f - 1
            val trackColor = if (val_) TOGGLE_ON else TOGGLE_OFF
            drawFlatRoundedRect(toggleX, toggleY, toggleW, toggleH, 10f, trackColor)
            val knobRadius = 7f
            val knobX = if (val_) toggleX + toggleW - knobRadius - 3 else toggleX + knobRadius + 3
            val knobY = toggleY + toggleH / 2f
            fillCircle(knobX, knobY, knobRadius, 16, 0xFFFFFFFF.toInt())
        }
        return y + max(20f, getFontHeight(smallFont).toFloat()) + SETTING_MARGIN_BOTTOM
    }

    private fun drawListValue(x: Float, y: Float, w: Float, value: ListValue, mx: Int, my: Int, type: GuiEvent): Float {
        val modes = value.values
        if (modes.isEmpty()) return y + getFontHeight(smallFont) + SETTING_MARGIN_BOTTOM
        val currentVal = value.get()

        val chipWidths = FloatArray(modes.size)
        var totalW = 0f
        for (i in modes.indices) {
            chipWidths[i] = getStringWidth(modes[i], smallFont) + 14f
            totalW += chipWidths[i] + if (i < modes.size - 1) 6f else 0f
        }

        val arrowSize = 18f
        val arrowBtnW = arrowSize + 8
        val overflow = totalW > w
        val chipAreaW = if (overflow) w - 2 * arrowBtnW else w
        var scrollOffset = modeHScroll[value] ?: 0f
        if (totalW <= chipAreaW) scrollOffset = 0f
        else scrollOffset = max(0f, min(totalW - chipAreaW, scrollOffset))
        modeHScroll[value] = scrollOffset

        val chipStartX = if (overflow) x + arrowBtnW else x

        if (type == GuiEvent.DRAW) {
            drawFlatString(value.name, x, y, TEXT_DIM, smallFont)
            val chipY = y + getFontHeight(smallFont) + 6

            // Gradient fade
            if (overflow) {
                val fadeAlpha = 180
                if (scrollOffset > 0.1f) {
                    for (fx in 0..11) {
                        val a = (fadeAlpha * (1 - fx / 12f)).toInt()
                        RenderUtils.drawRect(chipStartX + fx, chipY, chipStartX + fx + 1, chipY + 24, (a shl 24) or (WINDOW_BG and 0x00FFFFFF))
                    }
                }
                if (scrollOffset < totalW - chipAreaW - 0.1f) {
                    for (fx in 0..11) {
                        val a = (fadeAlpha * (fx / 12f)).toInt()
                        RenderUtils.drawRect(chipStartX + chipAreaW - 12 + fx, chipY, chipStartX + chipAreaW - 12 + fx + 1, chipY + 24, (a shl 24) or (WINDOW_BG and 0x00FFFFFF))
                    }
                }
            }

            // Chips
            var chipX = chipStartX - scrollOffset
            for (i in modes.indices) {
                val mode = modes[i]
                val cw = chipWidths[i]
                val active = currentVal.equals(mode, true)

                if (chipX + cw < chipStartX - 1 || chipX > chipStartX + chipAreaW + 1) {
                    chipX += cw + 6; continue
                }

                val bgCol = if (active) MODE_CHIP_ACTIVE else MODE_CHIP_BG
                val txtCol = if (active) 0xFFFFFFFF.toInt() else TEXT_PRIMARY
                drawFlatRoundedRect(chipX, chipY, cw, 24f, 5f, bgCol)
                RenderUtils.drawRect(chipX, chipY, chipX + cw, chipY + 1, if (active) 0x30FFFFFF else INPUT_BORDER)
                RenderUtils.drawRect(chipX, chipY, chipX + 1, chipY + 24, if (active) 0x30FFFFFF else INPUT_BORDER)
                RenderUtils.drawRect(chipX + cw - 1, chipY, chipX + cw, chipY + 24, if (active) 0x30FFFFFF else INPUT_BORDER)
                RenderUtils.drawRect(chipX, chipY + 23, chipX + cw, chipY + 24, if (active) 0x30FFFFFF else INPUT_BORDER)

                val mTxtX = chipX + cw / 2f - getStringWidth(mode, smallFont) / 2f
                val mTxtY = chipY + 12f - getFontHeight(smallFont) / 2f
                drawFlatString(mode, mTxtX, mTxtY, txtCol, smallFont)

                chipX += cw + 6
            }

            // Arrow buttons
            if (overflow) {
                val ay = chipY + (24 - arrowSize) / 2f
                val canLeft = scrollOffset > 0.5f
                val canRight = scrollOffset < totalW - chipAreaW - 0.5f
                val leftCol = if (canLeft) (if (isHovered(mx, my, x, ay, arrowBtnW, arrowSize + 4)) MODE_CHIP_ACTIVE else PILL_BG) else (PILL_BG and 0x30FFFFFF)
                drawFlatRoundedRect(x, ay, arrowBtnW, arrowSize + 4, 4f, leftCol)
                drawFlatString("\u276E", x + (arrowBtnW - getStringWidth("\u276E", smallFont)) / 2f, ay + 2, if (canLeft) TEXT_PRIMARY else (TEXT_DIM and 0x60FFFFFF), smallFont)
                val rx = x + w - arrowBtnW
                val rightCol = if (canRight) (if (isHovered(mx, my, rx, ay, arrowBtnW, arrowSize + 4)) MODE_CHIP_ACTIVE else PILL_BG) else (PILL_BG and 0x30FFFFFF)
                drawFlatRoundedRect(rx, ay, arrowBtnW, arrowSize + 4, 4f, rightCol)
                drawFlatString("\u276F", rx + (arrowBtnW - getStringWidth("\u276F", smallFont)) / 2f, ay + 2, if (canRight) TEXT_PRIMARY else (TEXT_DIM and 0x60FFFFFF), smallFont)
            }
        }
        return y + getFontHeight(smallFont) + 6 + 24 + SETTING_MARGIN_BOTTOM
    }

    private fun drawSlider(x: Float, y: Float, w: Float, value: Value<*>, value_: Double, min: Double, max: Double, valueStr: String, mx: Int, my: Int, type: GuiEvent): Float {
        val sliderH = 8f; val thumbR = 7f

        if (type == GuiEvent.DRAW) {
            drawFlatString(value.name, x, y, TEXT_DIM, smallFont)
            val valW = getStringWidth(valueStr, smallFont)
            drawFlatString(valueStr, x + w - valW, y, TEXT_PRIMARY, smallFont)

            val sliderY = y + getFontHeight(smallFont) + 6
            drawFlatRoundedRect(x, sliderY, w, sliderH, 4f, SLIDER_TRACK)
            val ratio = ((value_ - min) / (max - min)).toFloat()
            var smoothed = smoothSliderVals[value] ?: ratio
            if (dragValue == value && dragKind == "slider") smoothed = ratio

            if (smoothed > 0.001f) {
                drawFlatRoundedRect(x, sliderY, w * smoothed, sliderH, 4f, ACCENT)
            }
            val thumbX = x + w * smoothed
            val thumbCY = sliderY + sliderH / 2f
            fillCircle(thumbX, thumbCY, thumbR, 16, 0xFFFFFFFF.toInt())
        }
        return y + getFontHeight(smallFont) + 6 + sliderH + SETTING_MARGIN_BOTTOM
    }

    private fun drawIntRangeValue(x: Float, y: Float, w: Float, value: IntRangeValue, mx: Int, my: Int, type: GuiEvent): Float {
        val sliderH = 8f; val thumbR = 7f
        val range = value.get()
        val min = value.minimum; val max = value.maximum
        val rangeW = (max - min).coerceAtLeast(1)

        if (type == GuiEvent.DRAW) {
            drawFlatString(value.name, x, y, TEXT_DIM, smallFont)
            val valStr = "${range.first}..${range.last}"
            val valW = getStringWidth(valStr, smallFont)
            drawFlatString(valStr, x + w - valW, y, TEXT_PRIMARY, smallFont)

            val sliderY = y + getFontHeight(smallFont) + 6
            drawFlatRoundedRect(x, sliderY, w, sliderH, 4f, SLIDER_TRACK)

            val leftRatio = (range.first - min) / rangeW.toFloat()
            val rightRatio = (range.last - min) / rangeW.toFloat()

            if (rightRatio - leftRatio > 0.001f) {
                drawFlatRoundedRect(x + w * leftRatio, sliderY, w * (rightRatio - leftRatio), sliderH, 4f, ACCENT)
            }

            fillCircle(x + w * leftRatio, sliderY + sliderH / 2f, thumbR, 16, 0xFFFFFFFF.toInt())
            fillCircle(x + w * rightRatio, sliderY + sliderH / 2f, thumbR, 16, 0xFFFFFFFF.toInt())
        }
        return y + getFontHeight(smallFont) + 6 + sliderH + SETTING_MARGIN_BOTTOM
    }

    private fun drawFloatRangeValue(x: Float, y: Float, w: Float, value: FloatRangeValue, mx: Int, my: Int, type: GuiEvent): Float {
        val sliderH = 8f; val thumbR = 7f
        val range = value.get()
        val min = value.minimum; val max = value.maximum
        val rangeW = (max - min).coerceAtLeast(0.001f)

        if (type == GuiEvent.DRAW) {
            drawFlatString(value.name, x, y, TEXT_DIM, smallFont)
            val valStr = "${(Math.round(range.start * 100.0) / 100.0)}..${(Math.round(range.endInclusive * 100.0) / 100.0)}"
            val valW = getStringWidth(valStr, smallFont)
            drawFlatString(valStr, x + w - valW, y, TEXT_PRIMARY, smallFont)

            val sliderY = y + getFontHeight(smallFont) + 6
            drawFlatRoundedRect(x, sliderY, w, sliderH, 4f, SLIDER_TRACK)

            val leftRatio = (range.start - min) / rangeW
            val rightRatio = (range.endInclusive - min) / rangeW

            if (rightRatio - leftRatio > 0.001f) {
                drawFlatRoundedRect(x + w * leftRatio, sliderY, w * (rightRatio - leftRatio), sliderH, 4f, ACCENT)
            }

            fillCircle(x + w * leftRatio, sliderY + sliderH / 2f, thumbR, 16, 0xFFFFFFFF.toInt())
            fillCircle(x + w * rightRatio, sliderY + sliderH / 2f, thumbR, 16, 0xFFFFFFFF.toInt())
        }
        return y + getFontHeight(smallFont) + 6 + sliderH + SETTING_MARGIN_BOTTOM
    }

    private fun drawTextValue(x: Float, y: Float, w: Float, value: TextValue, mx: Int, my: Int, type: GuiEvent): Float {
        if (type == GuiEvent.DRAW) {
            drawFlatString(value.name, x, y, TEXT_DIM, smallFont)
            val inputY = y + getFontHeight(smallFont) + 4
            val inputH = 26f
            drawFlatRoundedRect(x, inputY, w, inputH, 6f, INPUT_BG)
            RenderUtils.drawRect(x, inputY, x + w, inputY + 1, INPUT_BORDER)
            RenderUtils.drawRect(x, inputY, x + 1, inputY + inputH, INPUT_BORDER)
            RenderUtils.drawRect(x, inputY + inputH - 1, x + w, inputY + inputH, INPUT_BORDER)
            RenderUtils.drawRect(x + w - 1, inputY, x + w, inputY + inputH, INPUT_BORDER)

            var display = value.get()
            val isFocused = value == focusedTextValue
            if (display.isEmpty() && !isFocused) display = "Enter text..."
            val txtCol = if (display.isEmpty() && !isFocused) TEXT_DIM else TEXT_PRIMARY
            drawFlatString(display, x + 10, inputY + (inputH - getFontHeight(smallFont)) / 2f, txtCol, smallFont)

            if (isFocused && System.currentTimeMillis() % 1000 < 500) {
                val cursorX = x + 10 + getStringWidth(value.get(), smallFont)
                RenderUtils.drawRect(cursorX, inputY + 5, cursorX + 1, inputY + inputH - 5, TEXT_PRIMARY)
            }
        }
        return y + getFontHeight(smallFont) + 4 + 26 + SETTING_MARGIN_BOTTOM
    }

    private fun drawColorValue(x: Float, y: Float, w: Float, value: ColorValue, mx: Int, my: Int, type: GuiEvent): Float {
        val pickerW = min(w, 200f)
        val pickerH = 90f
        val barH = 11f
        val swatchSize = 24f
        val startY = y

        if (type == GuiEvent.DRAW) {
            drawFlatString(value.name, x, y, TEXT_DIM, smallFont)
            var py = y + getFontHeight(smallFont) + 6

            // Picker area
            val hueBase = Color.getHSBColor(value.hueSliderY, 1f, 1f)
            RenderUtils.drawRect(x, py, x + pickerW, py + pickerH, hueBase.rgb)

            // White gradient (saturation)
            for (sx in 0 until pickerW.toInt()) {
                val sat = sx / pickerW
                RenderUtils.drawRect(x + sx, py, x + sx + 1, py + pickerH,
                    Color(255, 255, 255, (255 * (1 - sat)).toInt()).rgb)
            }
            // Black gradient (brightness)
            for (sy in 0 until pickerH.toInt()) {
                val bri = 1f - sy / pickerH
                RenderUtils.drawRect(x, py + sy, x + pickerW, py + sy + 1,
                    Color(0, 0, 0, (255 * (1 - bri)).toInt()).rgb)
            }

            // Picker circle indicator
            val circPx = x + value.colorPickerPos.x * pickerW
            val circPy = py + value.colorPickerPos.y * pickerH
            RenderUtils.drawRect(circPx - 1, circPy - 7, circPx + 1, circPy + 7, 0xFFFFFFFF.toInt())
            RenderUtils.drawRect(circPx - 7, circPy - 1, circPx + 7, circPy + 1, 0xFFFFFFFF.toInt())

            // Hue bar
            val hy = py + pickerH + 5
            for (hx in 0 until pickerW.toInt()) {
                val hue = hx / pickerW
                RenderUtils.drawRect(x + hx, hy, x + hx + 1, hy + barH, Color.getHSBColor(hue, 1f, 1f).rgb)
            }
            val huePos = x + value.hueSliderY * pickerW
            RenderUtils.drawRect(huePos - 1, hy - 1, huePos + 1, hy + barH + 1, 0xFFFFFFFF.toInt())

            // Alpha bar
            val ay = hy + barH + 5
            for (ax in 0 until pickerW.toInt() step 4) {
                for (aby in 0 until barH.toInt() step 4) {
                    val light = (ax / 4 + aby / 4) % 2 == 0
                    RenderUtils.drawRect(x + ax, ay + aby, x + min(ax + 4, pickerW.toInt()), ay + min(aby + 4, barH.toInt()),
                        if (light) 0xFFCCCCCC.toInt() else 0xFF999999.toInt())
                }
            }
            val solidC = Color.getHSBColor(value.hueSliderY, value.colorPickerPos.x, 1 - value.colorPickerPos.y)
            for (ax in 0 until pickerW.toInt()) {
                val a = ax / pickerW
                RenderUtils.drawRect(x + ax, ay, x + ax + 1, ay + barH,
                    Color(solidC.red, solidC.green, solidC.blue, (255 * a).toInt()).rgb)
            }
            val alphaPos = x + value.opacitySliderY * pickerW
            RenderUtils.drawRect(alphaPos - 1, ay - 1, alphaPos + 1, ay + barH + 1, 0xFFFFFFFF.toInt())

            // Swatch
            val sy = ay + barH + 8
            val swatchColor = value.selectedColor()
            drawFlatRoundedRect(x, sy, swatchSize, swatchSize, 4f, swatchColor.rgb)

            // Rainbow indicator
            if (value.rainbow) {
                drawFlatString("Rainbow", x + swatchSize + 8, sy + 4, ACCENT, smallFont)
            }

            // Alpha label
            val aLabel = "A: ${(value.opacitySliderY * 100).toInt()}%"
            drawFlatString(aLabel, x + swatchSize + 8, sy + if (value.rainbow) getFontHeight(smallFont) + 2 else 4, TEXT_DIM, smallFont)
        }

        return startY + getFontHeight(smallFont) + 6 + pickerH + 5 + barH + 5 + barH + 8 + swatchSize + SETTING_MARGIN_BOTTOM
    }

    private fun drawFontValue(x: Float, y: Float, w: Float, value: FontValue, mx: Int, my: Int, type: GuiEvent): Float {
        if (type == GuiEvent.DRAW) {
            drawFlatString(value.name, x, y, TEXT_DIM, smallFont)
            val btnW = 28f; val btnH = 20f
            // Previous button
            val prevX = x + w - btnW * 2 - 8
            val prevY = y + (getFontHeight(smallFont) - btnH) / 2f - 1
            val prevHovered = isHovered(mx, my, prevX, prevY, btnW, btnH)
            drawFlatRoundedRect(prevX, prevY, btnW, btnH, 5f, if (prevHovered) PILL_ACTIVE else PILL_BG)
            drawFlatString("\u276E", prevX + (btnW - getStringWidth("\u276E", smallFont)) / 2f, prevY + (btnH - getFontHeight(smallFont)) / 2f, if (prevHovered) 0xFFFFFFFF.toInt() else TEXT_DIM, smallFont)

            // Next button
            val nextX = x + w - btnW
            val nextY = prevY
            val nextHovered = isHovered(mx, my, nextX, nextY, btnW, btnH)
            drawFlatRoundedRect(nextX, nextY, btnW, btnH, 5f, if (nextHovered) PILL_ACTIVE else PILL_BG)
            drawFlatString("\u276F", nextX + (btnW - getStringWidth("\u276F", smallFont)) / 2f, nextY + (btnH - getFontHeight(smallFont)) / 2f, if (nextHovered) 0xFFFFFFFF.toInt() else TEXT_DIM, smallFont)

            // Display name
            val displayName = value.displayName
            val nameW = getStringWidth(displayName, smallFont)
            drawFlatString(displayName, x, y, TEXT_PRIMARY, smallFont)
        }
        return y + max(20f, getFontHeight(smallFont).toFloat()) + SETTING_MARGIN_BOTTOM
    }

    // === Value Click Handling ===
    private fun handleValueClick(x: Float, y: Float, w: Float, value: Value<*>, mx: Int, my: Int, btn: Int, type: GuiEvent): Float {
        when (value) {
            is BoolValue -> return handleBoolClick(x, y, w, value, mx, my, btn, type)
            is ListValue -> return handleListClick(x, y, w, value, mx, my, btn, type)
            is IntValue -> return handleSliderClick(x, y, w, value, value.minimum.toDouble(), value.maximum.toDouble(), mx, my, btn, type) { v -> value.set(v.toInt()) }
            is FloatValue -> return handleSliderClick(x, y, w, value, value.minimum.toDouble(), value.maximum.toDouble(), mx, my, btn, type) { v -> value.set(v.toFloat()) }
            is BlockValue -> return handleSliderClick(x, y, w, value, value.minimum.toDouble(), value.maximum.toDouble(), mx, my, btn, type) { v -> value.set(v.toInt()) }
            is IntRangeValue -> return handleIntRangeClick(x, y, w, value, mx, my, btn, type)
            is FloatRangeValue -> return handleFloatRangeClick(x, y, w, value, mx, my, btn, type)
            is TextValue -> return handleTextClick(x, y, w, value, mx, my, btn, type)
            is ColorValue -> return handleColorClick(x, y, w, value, mx, my, btn, type)
            is FontValue -> return handleFontClick(x, y, w, value, mx, my, btn, type)
            else -> return y + getFontHeight(smallFont) + SETTING_MARGIN_BOTTOM
        }
    }

    private fun handleBoolClick(x: Float, y: Float, w: Float, value: BoolValue, mx: Int, my: Int, btn: Int, type: GuiEvent): Float {
        val toggleW = 38f; val toggleH = 20f
        val toggleX = x + w - toggleW
        val toggleY = y + (getFontHeight(smallFont) - toggleH) / 2f - 1
        if (type == GuiEvent.CLICK && btn == 0 && isHovered(mx, my, toggleX, toggleY, toggleW, toggleH)) {
            value.toggle()
        }
        return y + max(20f, getFontHeight(smallFont).toFloat()) + SETTING_MARGIN_BOTTOM
    }

    private fun handleListClick(x: Float, y: Float, w: Float, value: ListValue, mx: Int, my: Int, btn: Int, type: GuiEvent): Float {
        val modes = value.values
        if (modes.isEmpty()) return y + getFontHeight(smallFont) + SETTING_MARGIN_BOTTOM

        var totalW = 0f
        val chipWidths = FloatArray(modes.size)
        for (i in modes.indices) {
            chipWidths[i] = getStringWidth(modes[i], smallFont) + 14f
            totalW += chipWidths[i] + if (i < modes.size - 1) 6f else 0f
        }

        val arrowSize = 18f; val arrowBtnW = arrowSize + 8
        val overflow = totalW > w
        val chipAreaW = if (overflow) w - 2 * arrowBtnW else w
        val scrollOffset = modeHScroll[value] ?: 0f
        val chipStartX = if (overflow) x + arrowBtnW else x
        val chipY = y + getFontHeight(smallFont) + 6

        // Arrow button clicks
        if (type == GuiEvent.CLICK && btn == 0 && overflow) {
            val ay = chipY + (24 - arrowSize) / 2f
            val currentScroll = modeHScroll[value] ?: 0f
            if (isHovered(mx, my, x, ay, arrowBtnW, arrowSize + 4)) {
                modeHScroll[value] = max(0f, currentScroll - chipAreaW * 0.7f)
                return y + getFontHeight(smallFont) + 6 + 24 + SETTING_MARGIN_BOTTOM
            }
            val rx = x + w - arrowBtnW
            if (isHovered(mx, my, rx, ay, arrowBtnW, arrowSize + 4)) {
                modeHScroll[value] = min(totalW - chipAreaW, currentScroll + chipAreaW * 0.7f)
                return y + getFontHeight(smallFont) + 6 + 24 + SETTING_MARGIN_BOTTOM
            }
        }

        // Chip click detection
        var chipX = chipStartX - scrollOffset
        for (i in modes.indices) {
            val cw = chipWidths[i]
            if (type == GuiEvent.CLICK && btn == 0) {
                if (chipX + cw >= chipStartX && chipX <= chipStartX + chipAreaW) {
                    if (isHovered(mx, my, chipX, chipY, cw, 24f)) {
                        value.set(modes[i])
                    }
                }
            }
            chipX += cw + 6
        }
        return y + getFontHeight(smallFont) + 6 + 24 + SETTING_MARGIN_BOTTOM
    }

    private fun handleSliderClick(x: Float, y: Float, w: Float, value: Value<*>, min: Double, max: Double, mx: Int, my: Int, btn: Int, type: GuiEvent, setter: (Double) -> Unit): Float {
        val sliderY = y + getFontHeight(smallFont) + 6
        val sliderH = 8f

        if (type == GuiEvent.CLICK && btn == 0) {
            if (isHovered(mx, my, x, sliderY, w, sliderH + 8)) {
                dragValue = value
                dragKind = "slider"
                val raw = (mx - x) / w * (max - min) + min
                val clamped = MathHelper.clamp_double(raw, min, max)
                setter(clamped)
                smoothSliderVals[value] = ((clamped - min) / (max - min)).toFloat()
            }
        }
        return y + getFontHeight(smallFont) + 6 + sliderH + SETTING_MARGIN_BOTTOM
    }

    private fun handleIntRangeClick(x: Float, y: Float, w: Float, value: IntRangeValue, mx: Int, my: Int, btn: Int, type: GuiEvent): Float {
        val sliderY = y + getFontHeight(smallFont) + 6
        val sliderH = 8f; val thumbR = 7f
        val range = value.get()
        val rangeW = (value.maximum - value.minimum).coerceAtLeast(1)

        if (type == GuiEvent.CLICK && btn == 0) {
            if (isHovered(mx, my, x, sliderY, w, sliderH + thumbR * 2)) {
                val clickRatio = (mx - x) / w
                val clickVal = (value.minimum + clickRatio * rangeW).toInt()
                val distFirst = abs(clickRatio - (range.first - value.minimum) / rangeW.toFloat())
                val distLast = abs(clickRatio - (range.last - value.minimum) / rangeW.toFloat())

                dragValue = value
                if (distFirst < distLast) {
                    dragKind = "rangeLeft"
                    value.setFirst(clickVal.coerceIn(value.minimum, range.last))
                } else {
                    dragKind = "rangeRight"
                    value.setLast(clickVal.coerceIn(range.first, value.maximum))
                }
            }
        }
        return y + getFontHeight(smallFont) + 6 + sliderH + SETTING_MARGIN_BOTTOM
    }

    private fun handleFloatRangeClick(x: Float, y: Float, w: Float, value: FloatRangeValue, mx: Int, my: Int, btn: Int, type: GuiEvent): Float {
        val sliderY = y + getFontHeight(smallFont) + 6
        val sliderH = 8f; val thumbR = 7f
        val range = value.get()
        val rangeW = (value.maximum - value.minimum).coerceAtLeast(0.001f)

        if (type == GuiEvent.CLICK && btn == 0) {
            if (isHovered(mx, my, x, sliderY, w, sliderH + thumbR * 2)) {
                val clickRatio = (mx - x) / w
                val clickVal = value.minimum + clickRatio * rangeW
                val distFirst = abs(clickRatio - (range.start - value.minimum) / rangeW)
                val distLast = abs(clickRatio - (range.endInclusive - value.minimum) / rangeW)

                dragValue = value
                if (distFirst < distLast) {
                    dragKind = "rangeLeftF"
                    value.setFirst(clickVal.coerceIn(value.minimum, range.endInclusive))
                } else {
                    dragKind = "rangeRightF"
                    value.setLast(clickVal.coerceIn(range.start, value.maximum))
                }
            }
        }
        return y + getFontHeight(smallFont) + 6 + sliderH + SETTING_MARGIN_BOTTOM
    }

    private fun handleTextClick(x: Float, y: Float, w: Float, value: TextValue, mx: Int, my: Int, btn: Int, type: GuiEvent): Float {
        val inputY = y + getFontHeight(smallFont) + 4
        val inputH = 26f
        if (type == GuiEvent.CLICK && btn == 0) {
            if (isHovered(mx, my, x, inputY, w, inputH)) {
                focusedTextValue = value
                textBuffer = value.get()
            }
        }
        return y + getFontHeight(smallFont) + 4 + inputH + SETTING_MARGIN_BOTTOM
    }

    private fun handleColorClick(x: Float, y: Float, w: Float, value: ColorValue, mx: Int, my: Int, btn: Int, type: GuiEvent): Float {
        val pickerW = min(w, 200f)
        val pickerH = 90f
        val barH = 11f
        val swatchSize = 24f

        if (type == GuiEvent.CLICK && btn == 0) {
            val py = y + getFontHeight(smallFont) + 6
            val hBarY = py + pickerH + 5
            val aBarY = hBarY + barH + 5

            if (isHovered(mx, my, x, hBarY, pickerW, barH)) {
                dragValue = value
                dragKind = "hueBar"
                value.hueSliderY = MathHelper.clamp_float((mx - x) / pickerW, 0f, 1f)
                value.setupSliders(value.selectedColor())
            }
            if (isHovered(mx, my, x, aBarY, pickerW, barH)) {
                dragValue = value
                dragKind = "alphaBar"
                value.opacitySliderY = MathHelper.clamp_float((mx - x) / pickerW, 0f, 1f)
                value.setupSliders(value.selectedColor())
            }
            if (isHovered(mx, my, x, py, pickerW, pickerH)) {
                dragValue = value
                dragKind = "colorPick"
                value.colorPickerPos = javax.vecmath.Vector2f(
                    MathHelper.clamp_float((mx - x) / pickerW, 0f, 1f),
                    MathHelper.clamp_float((my - py) / pickerH, 0f, 1f)
                )
                value.setupSliders(value.selectedColor())
            }

            // Rainbow toggle (click on swatch area)
            val sy = aBarY + barH + 8
            if (isHovered(mx, my, x, sy, swatchSize, swatchSize)) {
                value.rainbow = !value.rainbow
            }
        }

        return y + getFontHeight(smallFont) + 6 + pickerH + 5 + barH + 5 + barH + 8 + swatchSize + SETTING_MARGIN_BOTTOM
    }

    private fun handleFontClick(x: Float, y: Float, w: Float, value: FontValue, mx: Int, my: Int, btn: Int, type: GuiEvent): Float {
        val btnW = 28f; val btnH = 20f
        val prevX = x + w - btnW * 2 - 8
        val prevY = y + (getFontHeight(smallFont) - btnH) / 2f - 1
        val nextX = x + w - btnW

        if (type == GuiEvent.CLICK && btn == 0) {
            if (isHovered(mx, my, prevX, prevY, btnW, btnH)) {
                value.previous()
            }
            if (isHovered(mx, my, nextX, prevY, btnW, btnH)) {
                value.next()
            }
        }
        return y + max(20f, getFontHeight(smallFont).toFloat()) + SETTING_MARGIN_BOTTOM
    }

    // === Drag State Updates ===
    private fun updateDragStates(mx: Int, my: Int) {
        val dv = dragValue ?: return
        val dk = dragKind ?: return

        if (dk == "slider" && Mouse.isButtonDown(0)) {
            val drawerX = posX + winWidth - drawerAnim * DRAWER_WIDTH
            val contentW = drawerAnim * DRAWER_WIDTH - 32
            val sliderX = drawerX + 16

            when (dv) {
                is IntValue -> {
                    val raw = (mx - sliderX) / contentW * (dv.maximum - dv.minimum) + dv.minimum
                    val clamped = MathHelper.clamp_int(raw.toInt(), dv.minimum, dv.maximum)
                    dv.set(clamped)
                    smoothSliderVals[dv] = ((clamped - dv.minimum) / (dv.maximum - dv.minimum).toFloat().coerceAtLeast(1f))
                }
                is FloatValue -> {
                    val raw = (mx - sliderX) / contentW * (dv.maximum - dv.minimum) + dv.minimum
                    val clamped = MathHelper.clamp_float(raw, dv.minimum, dv.maximum)
                    dv.set(clamped)
                    smoothSliderVals[dv] = ((clamped - dv.minimum) / (dv.maximum - dv.minimum).coerceAtLeast(0.001f))
                }
                is BlockValue -> {
                    val raw = (mx - sliderX) / contentW * (dv.maximum - dv.minimum) + dv.minimum
                    val clamped = MathHelper.clamp_int(raw.toInt(), dv.minimum, dv.maximum)
                    dv.set(clamped)
                    smoothSliderVals[dv] = ((clamped - dv.minimum) / (dv.maximum - dv.minimum).toFloat().coerceAtLeast(1f))
                }
                else -> {}
            }
        } else if (dk == "rangeLeft" && dv is IntRangeValue && Mouse.isButtonDown(0)) {
            val drawerX = posX + winWidth - drawerAnim * DRAWER_WIDTH
            val contentW = drawerAnim * DRAWER_WIDTH - 32
            val sliderX = drawerX + 16
            val rangeW = (dv.maximum - dv.minimum).coerceAtLeast(1)
            val raw = (dv.minimum + (mx - sliderX) / contentW * rangeW).toInt()
            val clamped = raw.coerceIn(dv.minimum, dv.get().last)
            dv.setFirst(clamped)
        } else if (dk == "rangeRight" && dv is IntRangeValue && Mouse.isButtonDown(0)) {
            val drawerX = posX + winWidth - drawerAnim * DRAWER_WIDTH
            val contentW = drawerAnim * DRAWER_WIDTH - 32
            val sliderX = drawerX + 16
            val rangeW = (dv.maximum - dv.minimum).coerceAtLeast(1)
            val raw = (dv.minimum + (mx - sliderX) / contentW * rangeW).toInt()
            val clamped = raw.coerceIn(dv.get().first, dv.maximum)
            dv.setLast(clamped)
        } else if (dk == "rangeLeftF" && dv is FloatRangeValue && Mouse.isButtonDown(0)) {
            val drawerX = posX + winWidth - drawerAnim * DRAWER_WIDTH
            val contentW = drawerAnim * DRAWER_WIDTH - 32
            val sliderX = drawerX + 16
            val rangeW = (dv.maximum - dv.minimum).coerceAtLeast(0.001f)
            val raw = dv.minimum + (mx - sliderX) / contentW * rangeW
            val clamped = raw.coerceIn(dv.minimum, dv.get().endInclusive)
            dv.setFirst(clamped)
        } else if (dk == "rangeRightF" && dv is FloatRangeValue && Mouse.isButtonDown(0)) {
            val drawerX = posX + winWidth - drawerAnim * DRAWER_WIDTH
            val contentW = drawerAnim * DRAWER_WIDTH - 32
            val sliderX = drawerX + 16
            val rangeW = (dv.maximum - dv.minimum).coerceAtLeast(0.001f)
            val raw = dv.minimum + (mx - sliderX) / contentW * rangeW
            val clamped = raw.coerceIn(dv.get().start, dv.maximum)
            dv.setLast(clamped)
        } else if (dk == "colorPick" && dv is ColorValue && Mouse.isButtonDown(0)) {
            val drawerX = posX + winWidth - drawerAnim * DRAWER_WIDTH
            val baseX = drawerX + 16
            val pickerW = min(drawerAnim * DRAWER_WIDTH - 32, 200f)
            val pickerH = 90f
            val baseY = findColorPickerY(dv) + getFontHeight(smallFont) + 6
            dv.colorPickerPos = javax.vecmath.Vector2f(
                MathHelper.clamp_float((mx - baseX) / pickerW, 0f, 1f),
                MathHelper.clamp_float((my - baseY) / pickerH, 0f, 1f)
            )
            dv.setupSliders(dv.selectedColor())
        } else if (dk == "hueBar" && dv is ColorValue && Mouse.isButtonDown(0)) {
            val drawerX = posX + winWidth - drawerAnim * DRAWER_WIDTH
            val baseX = drawerX + 16
            val pickerW = min(drawerAnim * DRAWER_WIDTH - 32, 200f)
            dv.hueSliderY = MathHelper.clamp_float((mx - baseX) / pickerW, 0f, 1f)
            dv.setupSliders(dv.selectedColor())
        } else if (dk == "alphaBar" && dv is ColorValue && Mouse.isButtonDown(0)) {
            val drawerX = posX + winWidth - drawerAnim * DRAWER_WIDTH
            val baseX = drawerX + 16
            val pickerW = min(drawerAnim * DRAWER_WIDTH - 32, 200f)
            dv.opacitySliderY = MathHelper.clamp_float((mx - baseX) / pickerW, 0f, 1f)
            dv.setupSliders(dv.selectedColor())
        }
    }

    private fun findColorPickerY(target: ColorValue): Float {
        val mod = selectedModule ?: return 0f
        var y = posY + TITLE_BAR_HEIGHT + (1.0f - openAnim) * 150 + DRAWER_HEADER_HEIGHT + 12 + targetDrawerScroll
        y += getFontHeight(smallFont) + SETTING_MARGIN_BOTTOM + 4
        for (value in mod.values) {
            if (!value.shouldRender()) continue
            if (value == target) return y
            y = getValueAdvance(y, value)
        }
        return y
    }

    private fun getValueAdvance(y: Float, value: Value<*>): Float = when (value) {
        is BoolValue -> y + max(20f, getFontHeight(smallFont).toFloat()) + SETTING_MARGIN_BOTTOM
        is ListValue -> y + getFontHeight(smallFont) + 6 + 24 + SETTING_MARGIN_BOTTOM
        is IntValue, is FloatValue, is BlockValue -> y + getFontHeight(smallFont) + 6 + 8 + SETTING_MARGIN_BOTTOM
        is IntRangeValue, is FloatRangeValue -> y + getFontHeight(smallFont) + 6 + 8 + SETTING_MARGIN_BOTTOM
        is TextValue -> y + getFontHeight(smallFont) + 4 + 26 + SETTING_MARGIN_BOTTOM
        is ColorValue -> y + getFontHeight(smallFont) + 6 + 90 + 5 + 11 + 5 + 11 + 8 + 24 + SETTING_MARGIN_BOTTOM
        is FontValue -> y + max(20f, getFontHeight(smallFont).toFloat()) + SETTING_MARGIN_BOTTOM
        else -> y + getFontHeight(smallFont) + SETTING_MARGIN_BOTTOM
    }

    private fun calculateDrawerContentHeight(): Float {
        val mod = selectedModule ?: return 0f
        var h = getFontHeight(smallFont) + SETTING_MARGIN_BOTTOM + 4f
        for (value in mod.values) {
            if (!value.shouldRender()) continue
            h = getValueAdvance(h, value)
        }
        val scissorH = winHeight - TITLE_BAR_HEIGHT - DRAWER_HEADER_HEIGHT
        val visibleContentH = scissorH - 12
        return max(0f, h - visibleContentH + 8)
    }
}
