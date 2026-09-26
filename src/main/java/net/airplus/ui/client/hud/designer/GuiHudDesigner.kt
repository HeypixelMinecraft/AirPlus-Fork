/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.ui.client.hud.designer

import net.airplus.file.FileManager.hudConfig
import net.airplus.file.FileManager.saveConfig
import net.airplus.ui.client.hud.HUD
import net.airplus.ui.client.hud.element.Element
import net.minecraft.client.gui.GuiScreen
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse

class GuiHudDesigner : GuiScreen() {

    private var editorPanel = EditorPanel(this, 2, 2)

    var selectedElement: Element? = null
        set(value) {
            field = value
        }
    private var buttonAction = false

    override fun initGui() {
        Keyboard.enableRepeatEvents(true)
        editorPanel = EditorPanel(this, width / 2, height / 2)
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        HUD.render(true)
        HUD.handleMouseMove(mouseX, mouseY)

        if (selectedElement !in HUD.elements)
            selectedElement = null

        val wheel = Mouse.getDWheel()

        editorPanel.drawPanel(mouseX, mouseY, wheel)

        if (wheel != 0 && !editorPanel.hitTest(mouseX, mouseY)) {
            for (element in HUD.elements) {
                if (element.isInBorder(
                        mouseX / element.scale - element.renderX,
                        mouseY / element.scale - element.renderY
                    )
                ) {
                    element.scale += if (wheel > 0) 0.05f else -0.05f
                    break
                }
            }
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        super.mouseClicked(mouseX, mouseY, mouseButton)

        if (buttonAction) {
            buttonAction = false
            return
        }

        // 点击落在编辑器 UI（工具条 / 卡片 / 确认框）上时，不影响 HUD 元素的选择与拖动
        if (editorPanel.hitTest(mouseX, mouseY)) {
            return
        }

        HUD.handleMouseClick(mouseX, mouseY, mouseButton)

        selectedElement = null
        editorPanel.create = false

        if (mouseButton == 0) {
            for (element in HUD.elements) {
                if (element.isInBorder(
                        mouseX / element.scale - element.renderX,
                        mouseY / element.scale - element.renderY
                    )
                ) {
                    selectedElement = element
                    break
                }
            }
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        super.mouseReleased(mouseX, mouseY, state)

        HUD.handleMouseReleased()
    }

    override fun onGuiClosed() {
        Keyboard.enableRepeatEvents(false)
        saveConfig(hudConfig)

        super.onGuiClosed()
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        // Save / Load 预设对话框打开时优先处理其键盘输入
        if (editorPanel.handleDialogKey(typedChar, keyCode)) {
            return
        }

        when (keyCode) {
            Keyboard.KEY_DELETE -> if (selectedElement != null) {
                HUD.removeElement(this, selectedElement!!)
            }

            Keyboard.KEY_ESCAPE -> {
                selectedElement = null
                editorPanel.create = false
            }

            else -> HUD.handleKey(typedChar, keyCode)
        }

        super.keyTyped(typedChar, keyCode)
    }
}