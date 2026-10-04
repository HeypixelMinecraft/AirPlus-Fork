package net.airplus.ui.client.sodium.gui.options;

/**
 * Port of me.jellysquid.mods.sodium.client.gui.options.OptionImpact (Sodium 0.4.1).
 * TranslatableText replaced with hardcoded color-coded strings.
 */
public enum OptionImpact implements TextProvider {
    LOW("\u00a7aLow"),
    MEDIUM("\u00a7eMedium"),
    HIGH("\u00a76High"),
    VARIES("\u00a7fVaries");

    private final String text;

    OptionImpact(String text) {
        this.text = text;
    }

    @Override
    public String getLocalizedName() {
        return this.text;
    }
}
