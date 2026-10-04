package net.airplus.ui.client.sodium.gui.options.storage;

/**
 * Port of me.jellysquid.mods.sodium.client.gui.options.storage.OptionStorage (Sodium 0.4.1).
 */
public interface OptionStorage<T> {
    T getData();

    void save();
}
