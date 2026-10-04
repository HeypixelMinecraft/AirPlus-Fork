package net.airplus.ui.client.sodium.gui.options.control;

import net.airplus.ui.client.sodium.gui.options.Option;
import net.airplus.ui.client.sodium.util.Dim2i;

/**
 * Port of me.jellysquid.mods.sodium.client.gui.options.control.Control (Sodium 0.4.1).
 */
public interface Control<T> {
    Option<T> getOption();

    ControlElement<T> createElement(Dim2i dim);

    int getMaxWidth();
}
