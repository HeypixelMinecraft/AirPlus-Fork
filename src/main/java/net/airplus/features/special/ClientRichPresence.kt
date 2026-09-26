/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.special

import com.jagrosh.discordipc.IPCClient
import com.jagrosh.discordipc.IPCListener
import com.jagrosh.discordipc.entities.RichPresence
import com.jagrosh.discordipc.entities.pipe.PipeStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.airplus.AirPlus.CLIENT_CLOUD
import net.airplus.AirPlus.CLIENT_NAME
import net.airplus.AirPlus.MINECRAFT_VERSION
import net.airplus.AirPlus.clientCommit
import net.airplus.AirPlus.clientVersionText
import net.airplus.AirPlus.moduleManager
import net.airplus.config.Configurable
import net.airplus.event.ClientShutdownEvent
import net.airplus.event.Listenable
import net.airplus.event.handler
import net.airplus.utils.client.ClientUtils.LOGGER
import net.airplus.utils.client.MinecraftInstance
import net.airplus.utils.client.ServerUtils
import net.airplus.utils.io.HttpClient
import net.airplus.utils.io.get
import net.airplus.utils.io.jsonBody
import net.airplus.utils.kotlin.SharedScopes
import org.json.JSONObject
import java.io.IOException
import java.time.OffsetDateTime

object ClientRichPresence : Configurable("DiscordRPC"), MinecraftInstance, Listenable {

    var showRPCValue by boolean("ShowRichPresence", true)
    var showRPCServerIP by boolean("ShowRichPresenceServerIP", true)
    var showRPCModulesCount by boolean("ShowRichPresenceModulesCount", true)
    var customRPCText by text("RichPresenceCustomText", "")

    // IPC Client
    private var ipcClient: IPCClient? = null

    private var appID = 0L
    private val assets = mutableMapOf<String, String>()
    private val timestamp = OffsetDateTime.now()

    // Status of running
    private var running = false

    /**
     * Setup Discord RPC
     */
    fun setup() {
        try {
            running = true

            loadConfiguration()

            ipcClient = IPCClient(appID).apply {
                setListener(object : IPCListener {

                    /**
                     * Fired whenever an [IPCClient] is ready and connected to Discord.
                     *
                     * @param client The now ready IPCClient.
                     */
                    override fun onReady(client: IPCClient?) {
                        SharedScopes.IO.launch {
                            while (running) {
                                update()
                                delay(1000L)
                            }
                        }
                    }

                    /**
                     * Fired whenever an [IPCClient] has closed.
                     *
                     * @param client The now closed IPCClient.
                     * @param json A [JSONObject] with close data.
                     */
                    override fun onClose(client: IPCClient?, json: JSONObject?) {
                        running = false
                    }

                })
            }
            ipcClient?.connect()
        } catch (e: Throwable) {
            LOGGER.error("Failed to setup Discord RPC.", e)
        }

    }

    /**
     * Update rich presence
     */
    fun update() {
        if (ipcClient?.status != PipeStatus.CONNECTED) return

        val builder = RichPresence.Builder().apply {
            // Set playing time
            setStartTimestamp(timestamp)

            // Check assets contains logo and set logo
            assets["logo"]?.let {
                setLargeImage(it, "MC $MINECRAFT_VERSION - $CLIENT_NAME $clientVersionText $clientCommit")
            }

            // Check user is in-game
            mc.thePlayer?.let {
                val serverData = mc.currentServerData

                // Set server info
                if (showRPCServerIP) {
                    setDetails(customRPCText.ifEmpty {
                        "Server: ${
                            if (mc.isIntegratedServerRunning || serverData == null) "Singleplayer"
                            else ServerUtils.hideSensitiveInformation(serverData.serverIP)
                        }"
                    })
                }

                // Set modules count info
                if (showRPCModulesCount) {
                    setState("Enabled ${moduleManager.count { it.state }} of ${moduleManager.size} modules")
                }
            }
        }

        // Check ipc client is connected and send rpc
        if (ipcClient?.status == PipeStatus.CONNECTED)
            ipcClient?.sendRichPresence(builder.build())
    }

    /**
     * Shutdown ipc client
     */
    fun shutdown() {
        if (ipcClient?.status != PipeStatus.CONNECTED) {
            return
        }

        try {
            ipcClient?.close()
        } catch (e: Throwable) {
            LOGGER.error("Failed to close Discord RPC.", e)
        }
    }

    private val onClientShutdown = handler<ClientShutdownEvent> {
        shutdown()
    }

    /**
     * Load configuration from web
     *
     * @throws IOException If reading failed
     */
    private fun loadConfiguration() {
        val discordConf = HttpClient.get("$CLIENT_CLOUD/discord.json").jsonBody<DiscordConfiguration>() ?: return

        // Check has app id
        discordConf.appID?.let { appID = it }

        // Import all asset names
        assets += discordConf.assets
    }
}

private class DiscordConfiguration(val appID: Long?, val assets: Map<String, String>)
