/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 *
 * Ported from AirClient's Chinese input fix (net.ccbluex.liquidbounce.utils.inputfix).
 * Selects the platform specific keyboard input implementation at client startup.
 */
package net.airplus.utils.inputfix;

import net.airplus.utils.inputfix.impl.GuiScreenFixOthers;
import net.airplus.utils.inputfix.impl.GuiScreenFixWindows;
import net.minecraft.util.Util;

public class InputFixInit {

    public static IGuiScreenFix impl;

    public static void init() {
        Util.EnumOS os = Util.getOSType();
        switch (os) {
            case WINDOWS:
                impl = new GuiScreenFixWindows();
                break;
            case LINUX:
            case OSX:
                try {
                    impl = new GuiScreenFixOthers();
                } catch (Throwable t) {
                    impl = new GuiScreenFixWindows();
                }
                break;
            default:
                break;
        }

        if (impl != null) {
            System.out.println("[InputFix] Initialized for " + os);
        }
    }
}
