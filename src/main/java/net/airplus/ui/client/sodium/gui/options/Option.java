package net.airplus.ui.client.sodium.gui.options;

import net.airplus.ui.client.sodium.gui.options.control.Control;
import net.airplus.ui.client.sodium.gui.options.storage.OptionStorage;

import java.util.Collection;

/**
 * Port of me.jellysquid.mods.sodium.client.gui.options.Option (Sodium 0.4.1).
 * net.minecraft.text.Text replaced with plain Strings for the 1.8.9 port.
 */
public interface Option<T> {
    String getName();

    String getTooltip();

    OptionImpact getImpact();

    Control<T> getControl();

    T getValue();

    void setValue(T value);

    void reset();

    OptionStorage<?> getStorage();

    boolean isAvailable();

    boolean hasChanged();

    void applyChanges();

    Collection<OptionFlag> getFlags();
}
