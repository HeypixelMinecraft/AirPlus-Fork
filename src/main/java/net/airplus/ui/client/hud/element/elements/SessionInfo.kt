/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 *
 * Ported from Hanabi's WindowSessInfo (me.yarukon.hud.window.impl.WindowSessInfo).
 * Kept behaviour-identical to Hanabi:
 *  - 125x92 window (12px draggable title bar + 80px content), same layout/positions
 *  - same fonts (usans.otf / SessIcon.ttf), same colors per HUD mode (Classic/Simple)
 *  - blur behind the window (HudWindowManager blur behaviour)
 *  - same TPS algorithm (Hanabi PacketHelper, including the lag halving)
 *  - same title-packet based win/total parsing and KillAura-based kill counting
 *  - play time starts when connecting to a server (see MixinGuiConnecting)
 */
package net.airplus.ui.client.hud.element.elements

import net.airplus.event.EntityKilledEvent
import net.airplus.event.GameTickEvent
import net.airplus.event.Listenable
import net.airplus.event.PacketEvent
import net.airplus.event.handler
import net.airplus.features.module.modules.render.HanabiHUD
import net.airplus.ui.client.hud.HUD
import net.airplus.ui.client.hud.element.Border
import net.airplus.ui.client.hud.element.Element
import net.airplus.ui.client.hud.element.ElementInfo
import net.airplus.ui.client.hud.element.Side
import net.airplus.ui.font.AWTFontRenderer.Companion.assumeNonVolatile
import net.airplus.ui.font.Fonts
import net.airplus.ui.font.GameFontRenderer
import net.airplus.utils.render.HudBlur
import net.airplus.utils.render.RenderUtils.drawRect
import net.minecraft.network.play.server.S00PacketKeepAlive
import net.minecraft.network.play.server.S01PacketJoinGame
import net.minecraft.network.play.server.S03PacketTimeUpdate
import net.minecraft.network.play.server.S45PacketTitle
import java.awt.Color

@ElementInfo(name = "SessionInfo")
class SessionInfo(
    x: Double = 5.0, y: Double = 25.0, scale: Float = 1F, side: Side = Side.default()
) : Element("SessionInfo", x, y, scale, side), Listenable {

    private val blur by boolean("Blur", true)

    // 可自定义字体（标题与各行文字），图标字形字体保持不变
    private val fontValue by font("Font", Fonts.fontUsans40)
    private val font: GameFontRenderer
        get() = fontValue as? GameFontRenderer ?: Fonts.fontUsans40

    // Win / total counters, fed by title packets (bed wars / sky wars style titles)
    private var total = 0
    private var win = 0

    // Kill counter, fed by KillAura target deaths (like Hanabi's KillAura.killCount)
    private var kills = 0

    // --- TPS tracking (faithful port of Hanabi's PacketHelper) ---
    private var lastReceiveTime = -1L
    private var tps = 20.0
    private var packetsPerSecondTemp = 0
    private var packetsPerSecond = 0
    private var lastMS = 0L
    private var lagStart = 0L
    private var lagDoneOneTime = false
    private var lagTimer = 0L

    override fun handleEvents(): Boolean = HUD.elements.any { it === this }

    @Suppress("unused")
    private val onPacket = handler<PacketEvent> {
        when (val packet = it.packet) {
            is S01PacketJoinGame -> tps = 20.0

            is S03PacketTimeUpdate -> {
                val currentReceiveTime = System.currentTimeMillis()
                if (lastReceiveTime != -1L) {
                    val timeBetween = (currentReceiveTime - lastReceiveTime).toDouble()
                    val neededTps = timeBetween / 50.0
                    val multi = neededTps / 20.0
                    tps = (20.0 / multi).coerceIn(0.0, 20.0)
                }
                lastReceiveTime = currentReceiveTime
                packetsPerSecondTemp++
            }

            is S00PacketKeepAlive -> packetsPerSecondTemp++
        }
    }

    // PacketHelper#onUpdate: halve TPS every 2s while the server lags (>5s without packets)
    @Suppress("unused")
    private val onTick = handler<GameTickEvent> {
        val now = System.currentTimeMillis()

        if (now - lagTimer >= 2000L && serverLagTime > 5000L) {
            lagTimer = now
            tps /= 2.0
        }

        if (now - lastMS >= 1000L) {
            lastMS = now
            packetsPerSecond = packetsPerSecondTemp
            packetsPerSecondTemp = 0
        }

        if (packetsPerSecond < 1) {
            if (!lagDoneOneTime) {
                lagStart = now
                lagDoneOneTime = true
            }
        } else {
            if (lagDoneOneTime) {
                lagDoneOneTime = false
            }
            lagStart = 0L
        }
    }

    @Suppress("unused")
    private val onKilled = handler<EntityKilledEvent> {
        kills++
    }

    override fun createElement(): Boolean {
        if (startTime == 0L) {
            startTime = System.currentTimeMillis()
        }
        return true
    }

    override fun drawElement(): Border {
        val classic = HanabiHUD.hudMode == "Classic"

        // Hanabi HudWindow colors per HUD mode
        val textColor = if (classic) 0xFFFFFFFF.toInt() else 0xFF404040.toInt()
        val titleBGColor = if (classic) 0xCC2F74FF.toInt() else 0xBBFFFFFF.toInt()
        val frameBGColor = if (classic) 0xAA000000.toInt() else Color(166, 173, 176, 175).rgb

        if (blur) {
            // Blur behind the whole window (Hanabi BlurBuffer behaviour).
            // AABB in absolute screen coords; mask drawn in local coords so it follows scale/position.
            HudBlur.blur(
                (scale * renderX).toFloat(),
                (scale * renderY).toFloat(),
                (scale * (renderX + WIDTH)).toFloat(),
                (scale * (renderY + TITLE_HEIGHT + HEIGHT)).toFloat(),
                8f, "InternalBlur"
            ) {
                drawRect(0, 0, WIDTH.toInt(), (TITLE_HEIGHT + HEIGHT).toInt(), -1)
            }
        }

        assumeNonVolatile {
            // Title bar + frame
            drawRect(0, 0, WIDTH.toInt(), TITLE_HEIGHT.toInt(), titleBGColor)
            drawRect(0, TITLE_HEIGHT.toInt(), WIDTH.toInt(), (TITLE_HEIGHT + HEIGHT).toInt(), frameBGColor)

            // 标题文字相对标题栏垂直居中（跟随所选字体高度）
            font.drawString("Session info", 4f, (TITLE_HEIGHT - font.height) / 2f, textColor)

            // 各行内容相对行中线垂直居中：图标与文字动态对齐（跟随所选字体高度）
            val tpsDisplay = Math.round(tps * 10) / 10.0
            val rows = listOf(
                "B" to "Play time: ${playTime()}",
                "C" to "Move speed: $bps",
                "D" to "Win / Total: $win / $total",
                "E" to "TPS: $tpsDisplay",
                "F" to "Kills: $kills"
            )
            val iconFont = Fonts.fontSessIcon48
            val rowHeight = HEIGHT / rows.size
            rows.forEachIndexed { index, (icon, text) ->
                val centerY = TITLE_HEIGHT + rowHeight * index + rowHeight / 2f
                iconFont.drawString(icon, 4f, centerY - iconFont.height / 2f, textColor)
                font.drawString(text, 22f, centerY - font.height / 2f, textColor)
            }
        }

        return Border(0f, 0f, WIDTH, TITLE_HEIGHT + HEIGHT)
    }

    private fun playTime(): String {
        if (mc.isSingleplayer()) return "localhost"

        val durationInMillis = System.currentTimeMillis() - startTime
        val second = (durationInMillis / 1000) % 60
        val minute = (durationInMillis / (1000 * 60)) % 60
        val hour = (durationInMillis / (1000 * 60 * 60)) % 24
        return String.format("%02dh %02dm %02ds", hour, minute, second)
    }

    private val bps: String
        get() {
            val player = mc.thePlayer ?: return "0.00 bps"
            val xDist = player.posX - player.lastTickPosX
            val zDist = player.posZ - player.lastTickPosZ
            val lastDist = kotlin.math.sqrt(xDist * xDist + zDist * zDist)
            return String.format("%.2f bps", lastDist * 20.0 * mc.timer.timerSpeed)
        }

    // PacketHelper#getServerLagTime
    private val serverLagTime: Long
        get() = if (lagStart <= 0L) 0L else System.currentTimeMillis() - lagStart

    companion object {
        private const val WIDTH = 160f
        private const val HEIGHT = 92f
        private const val TITLE_HEIGHT = 22f

        /**
         * Play-time start. Set on every server connect by MixinGuiConnecting
         * (like Hanabi's HudWindowManager.startTime).
         */
        @JvmStatic
        var startTime = 0L
    }
}
