/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.features.command.commands

import net.airplus.AirPlus.isStarting
import net.airplus.AirPlus.moduleManager
import net.airplus.AirPlus.scriptManager
import net.airplus.features.command.Command
import net.airplus.features.command.CommandManager
import net.airplus.file.FileManager.clickGuiConfig
import net.airplus.file.FileManager.hudConfig
import net.airplus.file.FileManager.loadConfig
import net.airplus.file.FileManager.loadConfigs
import net.airplus.file.FileManager.modulesConfig
import net.airplus.file.FileManager.valuesConfig
import net.airplus.script.ScriptManager
import net.airplus.script.ScriptManager.reloadScripts
import net.airplus.script.ScriptManager.scriptsFolder
import net.airplus.utils.client.ClientUtils.LOGGER
import net.airplus.utils.io.FileFilters
import net.airplus.utils.io.MiscUtils
import net.airplus.utils.io.extractZipTo
import java.awt.Desktop

object ScriptManagerCommand : Command("scriptmanager", "scripts") {
    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        val usedAlias = args[0].lowercase()

        if (args.size < 2) {
            chatSyntax("$usedAlias <import/delete/reload/folder>")
            return
        }

        when (args[1].lowercase()) {
            "import" -> {
                try {
                    val file = MiscUtils.openFileChooser(FileFilters.JAVASCRIPT, FileFilters.ARCHIVE) ?: return

                    when (file.extension.lowercase()) {
                        "js" -> {
                            scriptManager.importScript(file)

                            loadConfig(clickGuiConfig)

                            chat("Successfully imported script.")
                        }

                        "zip" -> {
                            val existingFiles = ScriptManager.availableScriptFiles.toSet()

                            file.extractZipTo(scriptsFolder)

                            ScriptManager.availableScriptFiles.filterNot {
                                it in existingFiles
                            }.forEach(scriptManager::loadScript)

                            loadConfigs(clickGuiConfig, hudConfig)

                            chat("Successfully imported script.")
                        }

                        else -> chat("The file extension has to be .js or .zip")
                    }
                } catch (t: Throwable) {
                    LOGGER.error("Something went wrong while importing a script.", t)
                    chat("${t.javaClass.name}: ${t.message}")
                }
            }

            "delete" -> {
                try {
                    if (args.size <= 2) {
                        chatSyntax("$usedAlias delete <index>")
                        return
                    }

                    val scriptIndex = args[2].toInt()

                    if (scriptIndex >= ScriptManager.size) {
                        chat("Index $scriptIndex is too high.")
                        return
                    }

                    val script = ScriptManager[scriptIndex]

                    scriptManager.deleteScript(script)

                    loadConfigs(clickGuiConfig, hudConfig)

                    chat("Successfully deleted script.")
                } catch (numberFormat: NumberFormatException) {
                    chatSyntaxError()
                } catch (t: Throwable) {
                    LOGGER.error("Something went wrong while deleting a script.", t)
                    chat("${t.javaClass.name}: ${t.message}")
                }
            }

            "reload" -> {
                try {
                    CommandManager.registerCommands()

                    isStarting = true

                    reloadScripts()

                    for (module in moduleManager) moduleManager.generateCommand(module)
                    loadConfig(modulesConfig)

                    isStarting = false
                    loadConfigs(valuesConfig, clickGuiConfig, hudConfig)

                    chat("Successfully reloaded all scripts.")
                } catch (t: Throwable) {
                    LOGGER.error("Something went wrong while reloading all scripts.", t)
                    chat("${t.javaClass.name}: ${t.message}")
                }
            }

            "folder" -> {
                try {
                    Desktop.getDesktop().open(scriptsFolder)
                    chat("Successfully opened scripts folder.")
                } catch (t: Throwable) {
                    LOGGER.error("Something went wrong while trying to open your scripts folder.", t)
                    chat("${t.javaClass.name}: ${t.message}")
                }
            }
        }

        return

        val scriptManager = scriptManager

        if (scriptManager.isNotEmpty()) {
            chat("§c§lScripts")
            scriptManager.forEachIndexed { index, script ->
                chat(
                    "$index: §a§l${script.scriptName} §a§lv${script.scriptVersion} §3by §a§l${
                        script.scriptAuthors.joinToString(
                            ", "
                        )
                    }"
                )
            }
        }

        chatSyntax("$usedAlias <import/delete/reload/folder>")
    }

    override fun tabComplete(args: Array<String>): List<String> {
        if (args.isEmpty()) return emptyList()

        return when (args.size) {
            1 -> listOf("delete", "import", "folder", "reload")
                .filter { it.startsWith(args[0], true) }

            else -> emptyList()
        }
    }
}
