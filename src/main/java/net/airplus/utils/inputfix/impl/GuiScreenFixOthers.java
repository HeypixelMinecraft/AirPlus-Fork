/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 *
 * Ported from AirClient's Chinese input fix (net.ccbluex.liquidbounce.utils.inputfix).
 * Linux/Mac implementation: key 88 (K) opens a Swing input dialog and feeds the
 * typed characters into the screen, as a fallback for platforms where the LWJGL
 * keyboard does not deliver IME text.
 */
package net.airplus.utils.inputfix.impl;

import com.google.common.base.Strings;
import net.airplus.utils.inputfix.IGuiScreen;
import net.airplus.utils.inputfix.IGuiScreenFix;
import org.lwjgl.input.Keyboard;

import javax.swing.JOptionPane;

public class GuiScreenFixOthers implements IGuiScreenFix {

    @Override
    public void handleKeyboardInput(IGuiScreen gui) {
        char c = Keyboard.getEventCharacter();
        int k = Keyboard.getEventKey();
        if (Keyboard.getEventKeyState() || (k == 0 && Character.isDefined(c))) {
            if (k == 88) {
                for (char c1 : Strings.nullToEmpty(JOptionPane.showInputDialog("")).toCharArray())
                    gui.keyTyped(c1, 0);
                return;
            }
            gui.keyTyped(c, k);
        }
    }
}
