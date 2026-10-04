package net.airplus.ui.client.sodium.gui.options.storage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;

/**
 * Port of me.jellysquid.mods.sodium.client.gui.options.storage.MinecraftOptionsStorage (Sodium 0.4.1),
 * adapted to the 1.8.9 GameSettings API.
 */
public class MinecraftOptionsStorage implements OptionStorage<GameSettings> {
    private final Minecraft client = Minecraft.getMinecraft();

    @Override
    public GameSettings getData() {
        return this.client.gameSettings;
    }

    @Override
    public void save() {
        GameSettings options = this.getData();
        options.saveOptions();

        // Push render-related settings to the server while connected (like the vanilla video settings screen does)
        if (this.client.thePlayer != null) {
            options.sendSettingsToServer();
        }
    }
}
