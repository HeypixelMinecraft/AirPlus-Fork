/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.script.api.global

import net.airplus.utils.client.chat

/**
 * Object used by the script API to provide an easier way of calling chat-related methods.
 */
object Chat {

    /**
     * Prints a message to the chat (client-side)
     * @param message Message to be printed
     */
    @Suppress("unused")
    @JvmStatic
    fun print(message : String) = chat(message)
}