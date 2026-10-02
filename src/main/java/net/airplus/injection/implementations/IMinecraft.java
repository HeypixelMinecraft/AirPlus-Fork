/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 *
 * Gives other classes access to Minecraft#dispatchKeypresses, which vanilla
 * GuiScreen#handleKeyboardInput normally calls. Implemented by MixinMinecraft.
 */
package net.airplus.injection.implementations;

public interface IMinecraft {

    void airplus$dispatchKeypresses();
}
