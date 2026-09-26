/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.command.commands

import net.airplus.AirPlus.isStarting
import net.airplus.AirPlus.moduleManager
import net.airplus.features.command.Command
import net.airplus.features.command.CommandManager
import net.airplus.file.FileManager.accountsConfig
import net.airplus.file.FileManager.clickGuiConfig
import net.airplus.file.FileManager.friendsConfig
import net.airplus.file.FileManager.hudConfig
import net.airplus.file.FileManager.loadConfig
import net.airplus.file.FileManager.modulesConfig
import net.airplus.file.FileManager.valuesConfig
import net.airplus.file.FileManager.xrayConfig
import net.airplus.script.ScriptManager.disableScripts
import net.airplus.script.ScriptManager.reloadScripts
import net.airplus.script.ScriptManager.unloadScripts
import net.airplus.ui.font.Fonts

object ReloadCommand : Command("reload", "configreload") {
    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        chat("Reloading...")
        isStarting = true

        chat("§c§lReloading commands...")
        CommandManager.registerCommands()

        disableScripts()
        unloadScripts()

        for (module in moduleManager)
            moduleManager.generateCommand(module)

        chat("§c§lReloading scripts...")
        reloadScripts()

        chat("§c§lReloading fonts...")
        Fonts.loadFonts()

        chat("§c§lReloading modules...")
        loadConfig(modulesConfig)


        chat("§c§lReloading values...")
        loadConfig(valuesConfig)

        chat("§c§lReloading accounts...")
        loadConfig(accountsConfig)

        chat("§c§lReloading friends...")
        loadConfig(friendsConfig)

        chat("§c§lReloading xray...")
        loadConfig(xrayConfig)

        chat("§c§lReloading HUD...")
        loadConfig(hudConfig)

        chat("§c§lReloading ClickGUI...")
        loadConfig(clickGuiConfig)

        isStarting = false
        chat("Reloaded.")
    }
}
