package net.airplus.injection.forge.mixins.client;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiVideoSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes GuiVideoSettings' private parentScreen so the displayGuiScreen interceptor
 * can carry it over into the Sodium options GUI (for the Done/ESC return path).
 */
@Mixin(GuiVideoSettings.class)
public interface AccessorGuiVideoSettings {
    @Accessor("parentGuiScreen")
    GuiScreen airplus$getParentScreen();
}
