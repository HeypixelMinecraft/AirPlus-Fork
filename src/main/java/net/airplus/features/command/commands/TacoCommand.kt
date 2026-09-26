/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.command.commands

import net.airplus.event.Listenable
import net.airplus.event.Render2DEvent
import net.airplus.event.UpdateEvent
import net.airplus.event.handler
import net.airplus.features.command.Command
import net.airplus.utils.extensions.component1
import net.airplus.utils.extensions.component2
import net.airplus.utils.render.RenderUtils.deltaTime
import net.airplus.utils.render.RenderUtils.drawImage
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.util.ResourceLocation

object TacoCommand : Command("taco"), Listenable {
    var tacoToggle = false
    private var image = 0
    private var running = 0f
    private val tacoTextures = arrayOf(
        ResourceLocation("airplus/taco/1.png"),
        ResourceLocation("airplus/taco/2.png"),
        ResourceLocation("airplus/taco/3.png"),
        ResourceLocation("airplus/taco/4.png"),
        ResourceLocation("airplus/taco/5.png"),
        ResourceLocation("airplus/taco/6.png"),
        ResourceLocation("airplus/taco/7.png"),
        ResourceLocation("airplus/taco/8.png"),
        ResourceLocation("airplus/taco/9.png"),
        ResourceLocation("airplus/taco/10.png"),
        ResourceLocation("airplus/taco/11.png"),
        ResourceLocation("airplus/taco/12.png")
    )

    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        tacoToggle = !tacoToggle
        chat(if (tacoToggle) "§aTACO TACO TACO. :)" else "§cYou made the little taco sad! :(")
    }

    val onRender2D = handler<Render2DEvent> {
        if (!tacoToggle)
            return@handler

        running += 0.15f * deltaTime
        val (width, height) = ScaledResolution(mc)
        drawImage(tacoTextures[image], running.toInt(), height - 60, 64, 32)
        if (width <= running)
            running = -64f
    }

    val onUpdate = handler<UpdateEvent> {
        if (!tacoToggle) {
            image = 0
            return@handler
        }

        image++
        if (image >= tacoTextures.size) image = 0
    }


    override fun tabComplete(args: Array<String>) = listOf("TACO")
}