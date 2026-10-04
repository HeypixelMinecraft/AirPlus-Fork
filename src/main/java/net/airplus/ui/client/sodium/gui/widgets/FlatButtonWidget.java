package net.airplus.ui.client.sodium.gui.widgets;

import net.airplus.ui.client.sodium.util.Dim2i;

/**
 * Port of me.jellysquid.mods.sodium.client.gui.widgets.FlatButtonWidget (Sodium 0.4.1).
 * Text replaced with String; render() drops the MatrixStack parameter.
 */
public class FlatButtonWidget extends AbstractWidget {
    private final Dim2i dim;
    private final String label;
    private final Runnable action;

    private boolean selected;
    private boolean enabled = true;
    private boolean visible = true;

    public FlatButtonWidget(Dim2i dim, String label, Runnable action) {
        this.dim = dim;
        this.label = label;
        this.action = action;
    }

    public void render(int mouseX, int mouseY) {
        if (!this.visible) {
            return;
        }

        boolean hovered = this.dim.containsCursor(mouseX, mouseY);

        int backgroundColor = this.enabled ? (hovered ? 0xE0000000 : 0x90000000) : 0x60000000;
        int textColor = this.enabled ? 0xFFFFFFFF : 0x90FFFFFF;

        int strWidth = this.font.getStringWidth(this.label);

        this.drawRect(this.dim.x(), this.dim.y(), this.dim.getLimitX(), this.dim.getLimitY(), backgroundColor);
        this.drawString(this.label, this.dim.getCenterX() - (strWidth / 2), this.dim.getCenterY() - 4, textColor);

        if (this.enabled && this.selected) {
            this.drawRect(this.dim.x(), this.dim.getLimitY() - 1, this.dim.getLimitX(), this.dim.getLimitY(), 0xFF94E4D3);
        }
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.enabled || !this.visible) {
            return false;
        }

        if (button == 0 && this.dim.containsCursor(mouseX, mouseY)) {
            this.action.run();
            this.playClickSound();

            return true;
        }

        return false;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
