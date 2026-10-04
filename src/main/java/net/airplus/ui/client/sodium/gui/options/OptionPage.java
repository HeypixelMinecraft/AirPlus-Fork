package net.airplus.ui.client.sodium.gui.options;

import com.google.common.collect.ImmutableList;

/**
 * Port of me.jellysquid.mods.sodium.client.gui.options.OptionPage (Sodium 0.4.1).
 * Text replaced with String.
 */
public class OptionPage {
    private final String name;
    private final ImmutableList<OptionGroup> groups;
    private final ImmutableList<Option<?>> options;

    public OptionPage(String name, ImmutableList<OptionGroup> groups) {
        this.name = name;
        this.groups = groups;

        ImmutableList.Builder<Option<?>> builder = ImmutableList.builder();

        for (OptionGroup group : groups) {
            builder.addAll(group.getOptions());
        }

        this.options = builder.build();
    }

    public ImmutableList<OptionGroup> getGroups() {
        return this.groups;
    }

    public ImmutableList<Option<?>> getOptions() {
        return this.options;
    }

    public String getName() {
        return this.name;
    }
}
