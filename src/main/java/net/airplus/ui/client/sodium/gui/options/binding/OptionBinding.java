package net.airplus.ui.client.sodium.gui.options.binding;

/**
 * Port of me.jellysquid.mods.sodium.client.gui.options.binding.OptionBinding (Sodium 0.4.1).
 */
public interface OptionBinding<S, T> {
    void setValue(S storage, T value);

    T getValue(S storage);
}
