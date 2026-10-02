/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 *
 * Ported from AirClient's Chinese input fix (net.ccbluex.liquidbounce.utils.inputfix).
 * Windows implementation: also forwards the LWJGL events carrying actual text
 * (key code 0 with a defined character) so IME input reaches text fields.
 */
package net.airplus.utils.inputfix.impl;

import net.airplus.utils.inputfix.IGuiScreen;
import net.airplus.utils.inputfix.IGuiScreenFix;
import org.lwjgl.input.Keyboard;

public class GuiScreenFixWindows implements IGuiScreenFix {

    @Override
    public void handleKeyboardInput(IGuiScreen gui) {
        char c = Keyboard.getEventCharacter();
        int k = Keyboard.getEventKey();
        if (Keyboard.getEventKeyState() || (k == 0 && Character.isDefined(c))) {
            gui.keyTyped(c, k);
        }
    }
}
