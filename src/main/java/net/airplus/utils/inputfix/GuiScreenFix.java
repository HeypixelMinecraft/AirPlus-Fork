/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 *
 * Ported from AirClient's Chinese input fix (net.ccbluex.liquidbounce.utils.inputfix).
 * Entry point used by MixinGuiScreen#handleKeyboardInput.
 */
package net.airplus.utils.inputfix;

import net.airplus.injection.implementations.IMinecraft;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

import java.lang.reflect.Method;

public class GuiScreenFix {

    private static class Proxy implements IGuiScreen {
        private GuiScreen gui;

        @Override
        public void keyTyped(char c, int k) {
            try {
                if (gui != null)
                    keyTyped.invoke(gui, c, k);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }

        public Proxy setGui(GuiScreen gui) {
            this.gui = gui;
            return this;
        }
    }

    private static final ThreadLocal<Proxy> proxies = new ThreadLocal<Proxy>() {
        @Override
        protected Proxy initialValue() {
            return new Proxy();
        }
    };

    private static final Method keyTyped = findKeyTypedMethod();

    private static Method findKeyTypedMethod() {
        for (String name : new String[]{"func_73869_a", "keyTyped"}) {
            try {
                Method m = GuiScreen.class.getDeclaredMethod(name, char.class, int.class);
                m.setAccessible(true);
                return m;
            } catch (NoSuchMethodException ignored) {
            }
        }
        throw new RuntimeException("[InputFix] Failed to find GuiScreen.keyTyped");
    }

    public static void handleKeyboardInput(GuiScreen gui) {
        Proxy p = proxies.get().setGui(gui);
        if (InputFixInit.impl != null)
            InputFixInit.impl.handleKeyboardInput(p);
        else if (Keyboard.getEventKeyState())
            p.keyTyped(Keyboard.getEventCharacter(), Keyboard.getEventKey());

        ((IMinecraft) gui.mc).airplus$dispatchKeypresses();
    }
}
