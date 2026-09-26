/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.ui.client.hud.element.elements


import net.airplus.features.module.Module
import net.airplus.ui.client.hud.HUD.addNotification
import net.airplus.ui.client.hud.HUD.notifications
import net.airplus.ui.client.hud.designer.GuiHudDesigner
import net.airplus.ui.client.hud.element.Border
import net.airplus.ui.client.hud.element.Element
import net.airplus.ui.client.hud.element.ElementInfo
import net.airplus.ui.client.hud.element.Side
import net.airplus.ui.client.hud.element.elements.Notification.Companion.maxTextLength
import net.airplus.ui.font.Fonts
import net.airplus.utils.client.ClientThemesUtils
import net.airplus.utils.client.ClientUtils
import net.airplus.utils.extensions.lerpWith
import net.airplus.utils.render.ColorUtils.withAlpha
import net.airplus.utils.render.RenderUtils
import net.airplus.utils.render.RenderUtils.deltaTime
import net.airplus.utils.render.RenderUtils.drawRoundedBorder
import net.airplus.utils.render.RenderUtils.drawRoundedRect
import net.minecraft.util.ResourceLocation
import java.awt.Color
import kotlin.math.sin

/**
 * CustomHUD Notification element
 */
@ElementInfo(name = "Notifications", single = true, priority = -1)
class Notifications(
    x: Double = 0.0, y: Double = 30.0, scale: Float = 1F, side: Side = Side(Side.Horizontal.RIGHT, Side.Vertical.DOWN)
) : Element("Notifications", x, y, scale, side) {

    val style by choices("Style", arrayOf("Classic", "Modern", "Compact"), "Modern")
    val horizontalFade by choices("HorizontalFade", arrayOf("InOnly", "OutOnly", "Both", "None"), "OutOnly")
    val padding by int("Padding", 5, 1..20)
    val roundRadius by float("RoundRadius", 3f, 0f..10f)
    val color by color("BackgroundColor", Color.BLACK.withAlpha(128))
    val renderBorder by boolean("RenderBorder", false)
    val borderColor by color("BorderColor", Color.BLUE.withAlpha(255)) { renderBorder }
    val borderWidth by float("BorderWidth", 2f, 0.5F..5F) { renderBorder }

    private val exampleNotification = Notification("Example Title", "Example Description")

    private var index = 0

    override fun updateElement() {
        if (mc.currentScreen is GuiHudDesigner && ClientUtils.runTimeTicks % 60 == 0) {
            exampleNotification.severityType = SeverityType.entries[++index % SeverityType.entries.size]
        }
    }

    override fun drawElement(): Border? {
        var verticalOffset = 0f

        maxTextLength = maxOf(100, notifications.maxOfOrNull { it.textLength } ?: 0)

        notifications.removeIf { notification ->
            if (notification != exampleNotification) {
                notification.y = (notification.y..verticalOffset).lerpWith(RenderUtils.deltaTimeNormalized())
            }

            notification.drawNotification(this).also { if (!it) verticalOffset += Notification.MAX_HEIGHT + padding }
        }

        if (mc.currentScreen is GuiHudDesigner) {
            if (exampleNotification !in notifications) {
                index = 0
                addNotification(exampleNotification)
            }

            exampleNotification.fadeState = Notification.FadeState.STAY
            exampleNotification.textLength = Fonts.fontSemibold40.getStringWidth(exampleNotification.longestString)

            val notificationHeight = Notification.MAX_HEIGHT

            exampleNotification.y = 0F

            return Border(
                -(maxTextLength.toFloat() + 24 + 20), -notificationHeight.toFloat(), 0F, 0F
            )
        }

        return null
    }

    enum class SeverityType(val path: ResourceLocation) {
        SUCCESS(ResourceLocation("airplus/notifications/success.png")), RED_SUCCESS(ResourceLocation("airplus/notifications/redsuccess.png")), INFO(
            ResourceLocation("airplus/notifications/info.png")
        ),
        WARNING(ResourceLocation("airplus/notifications/warning.png")), ERROR(ResourceLocation("airplus/notifications/error.png"))
    }
}

class Notification(
    var title: String,
    var description: String,
    private val delay: Long = 2000L,
    var severityType: Notifications.SeverityType = Notifications.SeverityType.INFO
) {
    var x = 0F

    // Spawn the notification 32 pixels above the last one - if exists.
    var y: Float = (notifications.lastOrNull()?.y ?: 0F) + MAX_HEIGHT * 2
    var textLength = 0

    val longestString
        get() = arrayOf(title, description).maxBy { Fonts.fontSemibold40.getStringWidth(it) }

    private var stay = delay
    private var fadeStep = 0F
    var fadeState = FadeState.IN

    /**
     * Used when the same module state changes within the fade in/stay time window.
     */
    fun replaceModuleNotification(title: String, description: String, severityType: Notifications.SeverityType) {
        if (fadeState.ordinal > 1) {
            return
        }

        // Re-setup every important information
        stay = delay
        this.severityType = severityType
        this.title = title
        this.description = description

        textLength = Fonts.fontSemibold40.getStringWidth(longestString)
        maxTextLength = maxOf(textLength, maxTextLength)

        notifications.sortBy { it.stay }
    }

    companion object {
        fun informative(title: String, message: String, delay: Long = 2000L) =
            Notification(title, message, delay, Notifications.SeverityType.INFO)

        fun informative(title: Module, message: String, delay: Long = 2000L) =
            Notification(title.spacedName, message, delay, Notifications.SeverityType.INFO)

        fun error(title: Module, message: String, delay: Long = 2000L) =
            Notification(title.spacedName, message, delay, Notifications.SeverityType.ERROR)

        fun warning(title: Module, message: String, delay: Long = 2000L) =
            Notification(title.spacedName, message, delay, Notifications.SeverityType.WARNING)

        var maxTextLength = 0
        const val MAX_HEIGHT = 32
        const val ICON_SIZE = 24
        private val MODERN_BG = Color(16, 16, 20, 215)
        private val COMPACT_BG = Color(14, 14, 18, 200)
    }

    enum class FadeState {
        IN, STAY, OUT, END
    }

    init {
        textLength = Fonts.fontSemibold40.getStringWidth(longestString)
        maxTextLength = maxOf(maxTextLength, textLength)
    }

    fun drawNotification(element: Notifications): Boolean {
        val notificationWidth = maxTextLength + ICON_SIZE + 16F
        val extraSpace = 4F

        val currentX = when (fadeState) {
            FadeState.IN -> if (element.horizontalFade in arrayOf("InOnly", "Both")) x else notificationWidth
            FadeState.OUT -> if (element.horizontalFade in arrayOf("OutOnly", "Both")) x else notificationWidth
            else -> x
        }

        if (element.style == "Modern") {
            drawModern(element, currentX, extraSpace)
        } else if (element.style == "Compact") {
            drawCompact(element, currentX, extraSpace)
        } else {
            drawClassic(element, currentX, extraSpace)
        }

        val delta = deltaTime

        when (fadeState) {
            FadeState.IN -> {
                if (x < notificationWidth) {
                    x += delta
                }
                if (x >= notificationWidth) {
                    fadeState = FadeState.STAY
                    x = notificationWidth
                    fadeStep = notificationWidth
                }
                stay = delay
            }

            FadeState.STAY -> {
                if (textLength != maxTextLength) {
                    maxTextLength = maxOf(textLength, maxTextLength)
                    x = maxTextLength + ICON_SIZE + 16F
                    fadeStep = x
                }
                stay -= delta
                if (stay <= 0) {
                    fadeState = FadeState.OUT
                }
            }

            FadeState.OUT -> if (x > 0) {
                x -= delta
                y -= delta / 4F
            } else {
                fadeState = FadeState.END
            }

            FadeState.END -> return true
        }

        return false
    }

    private fun drawClassic(element: Notifications, currentX: Float, extraSpace: Float) {
        drawRoundedRect(0F, -y - MAX_HEIGHT, -currentX - extraSpace, -y, element.color.rgb, element.roundRadius)

        if (element.renderBorder) {
            drawRoundedBorder(
                0F,
                -y - MAX_HEIGHT,
                -currentX - extraSpace,
                -y,
                element.borderWidth,
                element.borderColor.rgb,
                element.roundRadius
            )
        }

        val nearTopSpot = -y - MAX_HEIGHT + 10

        Fonts.fontSemibold40.drawString(title, ICON_SIZE + 8F - currentX, nearTopSpot - 5, Color.WHITE.rgb)
        Fonts.fontSemibold35.drawString(
            description, ICON_SIZE + 8F - currentX, nearTopSpot + Fonts.fontSemibold40.fontHeight - 2, Int.MAX_VALUE
        )

        RenderUtils.drawImage(
            severityType.path, -currentX + 2, -y - MAX_HEIGHT + 4, ICON_SIZE, ICON_SIZE, radius = element.roundRadius
        )
    }

    /**
     * Modern 样式：深色背景 + 左侧 severity 强调条 + 居中图标 + 白色标题/灰色描述 + 底部剩余时间进度条
     */
    private fun drawModern(element: Notifications, currentX: Float, extraSpace: Float) {
        val radius = element.roundRadius
        val cardLeft = -currentX - extraSpace
        val cardTop = -y - MAX_HEIGHT
        val cardBottom = -y

        // 深色背景
        drawRoundedRect(cardLeft, cardTop, 0F, cardBottom, MODERN_BG.rgb, radius)

        // 左侧强调条（severity 颜色）
        drawRoundedRect(cardLeft, cardTop, cardLeft + 3F, cardBottom, accentColor.rgb, radius)

        // 居中 severity 图标
        val iconX = cardLeft + 9F
        val iconY = cardTop + (MAX_HEIGHT - 16) / 2F
        RenderUtils.drawImage(severityType.path, iconX, iconY, 16, 16, radius = radius)

        // 标题 + 描述
        val textX = cardLeft + 31F
        Fonts.fontSemibold40.drawString(title, textX, cardTop + 5F, Color.WHITE.rgb)
        Fonts.fontSemibold35.drawString(
            description, textX, cardTop + 5F + Fonts.fontSemibold40.fontHeight + 1F, Color(170, 170, 170).rgb
        )

        // 底部剩余时间进度条（仅在滑入/停留阶段显示）
        if (fadeState == FadeState.IN || fadeState == FadeState.STAY) {
            val progress = (stay / delay.toFloat()).coerceIn(0F, 1F)
            val barLeft = cardLeft + 4F
            val barRight = -4F
            val barY = cardBottom - 3F
            drawRoundedRect(barLeft, barY, barRight, barY + 1.5F, Color(255, 255, 255, 40).rgb, 0F)
            drawRoundedRect(barLeft, barY, barLeft + (barRight - barLeft) * progress, barY + 1.5F, accentColor.rgb, 0F)
        }
    }

    private val accentColor: Color
        get() = when (severityType) {
            Notifications.SeverityType.SUCCESS, Notifications.SeverityType.RED_SUCCESS -> Color(76, 175, 80)
            Notifications.SeverityType.INFO -> ClientThemesUtils.getColor()
            Notifications.SeverityType.WARNING -> Color(255, 152, 0)
            Notifications.SeverityType.ERROR -> Color(244, 67, 54)
        }

    /**
     * Compact 样式：无图标的紧凑胶囊。severity 用一枚带呼吸动画的圆点表达，
     * 背景为深色全圆角胶囊并带一层主题色描边，底部保留剩余时间进度条。
     */
    private fun drawCompact(element: Notifications, currentX: Float, extraSpace: Float) {
        val height = MAX_HEIGHT.toFloat()
        val cardLeft = -currentX - extraSpace
        val cardTop = -y - height
        val cardBottom = -y
        val radius = height / 2F

        // 深色全圆角胶囊背景
        drawRoundedRect(cardLeft, cardTop, 0F, cardBottom, COMPACT_BG.rgb, radius)

        // 呼吸圆点（severity 颜色）：滑入时更亮，停留阶段轻柔呼吸
        val breath = 0.5F + 0.5F * sin((System.currentTimeMillis() % 1600L) / 1600F * 2F * Math.PI.toFloat())
        val entryBoost = if (fadeState == FadeState.IN) (currentX / (maxTextLength + ICON_SIZE + 16F)).coerceIn(0F, 1F) else 1F
        val glow = (0.35F + 0.65F * breath) * entryBoost

        val dotCX = cardLeft + 10F
        val dotCY = cardTop + height / 2F

        // 外圈柔光
        drawRoundedRect(dotCX - 7F * glow, dotCY - 7F * glow, dotCX + 7F * glow, dotCY + 7F * glow, accentColor.withAlpha((60 * glow).toInt()).rgb, 7F * glow)
        // 圆点本体
        drawRoundedRect(dotCX - 2.5F, dotCY - 2.5F, dotCX + 2.5F, dotCY + 2.5F, accentColor.rgb, 2.5F)

        // 标题 + 描述
        val textX = cardLeft + 20F
        Fonts.fontSemibold40.drawString(title, textX, cardTop + 5F, Color.WHITE.rgb)
        Fonts.fontSemibold35.drawString(
            description, textX, cardTop + 5F + Fonts.fontSemibold40.fontHeight + 1F, Color(165, 165, 170).rgb
        )

        // 底部剩余时间进度条（仅在滑入/停留阶段显示）
        if (fadeState == FadeState.IN || fadeState == FadeState.STAY) {
            val progress = (stay / delay.toFloat()).coerceIn(0F, 1F)
            val barLeft = textX
            val barRight = -10F
            val barY = cardBottom - 3.5F
            drawRoundedRect(barLeft, barY, barRight, barY + 1.2F, Color(255, 255, 255, 30).rgb, 0.6F)
            drawRoundedRect(barLeft, barY, barLeft + (barRight - barLeft) * progress, barY + 1.2F, accentColor.withAlpha(220).rgb, 0.6F)
        }
    }
}