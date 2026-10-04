package net.airplus.ui.client.sodium.gui.options.control;

import net.airplus.ui.client.sodium.gui.options.Option;
import net.airplus.ui.client.sodium.gui.widgets.AbstractWidget;
import net.airplus.ui.client.sodium.util.Dim2i;
import net.minecraft.util.EnumChatFormatting;

/**
 * Port of me.jellysquid.mods.sodium.client.gui.options.control.ControlElement (Sodium 0.4.1).
 * MatrixStack dropped; Formatting replaced with EnumChatFormatting codes.
 */
public class ControlElement<T> extends AbstractWidget {
    protected final Option<T> option;

    protected final Dim2i dim;

    protected boolean hovered;

    public ControlElement(Option<T> option, Dim2i dim) {
        this.option = option;
        this.dim = dim;
    }

    public boolean isHovered() {
        return this.hovered;
    }

    public void render(int mouseX, int mouseY) {
        String name = this.option.getName();
        String label;

        if (this.hovered && this.font.getStringWidth(name) > (this.dim.width() - this.option.getControl().getMaxWidth())) {
            name = name.substring(0, Math.min(name.length(), 10)) + "...";
        }

        if (this.option.isAvailable()) {
            if (this.option.hasChanged()) {
                label = EnumChatFormatting.ITALIC.toString() + name + " *";
            } else {
                label = EnumChatFormatting.WHITE.toString() + name;
            }
        } else {
            label = EnumChatFormatting.GRAY.toString() + EnumChatFormatting.STRIKETHROUGH + name;
        }

        this.hovered = this.dim.containsCursor(mouseX, mouseY);

        this.drawRect(this.dim.x(), this.dim.y(), this.dim.getLimitX(), this.dim.getLimitY(), this.hovered ? 0xE0000000 : 0x90000000);
        this.drawString(label, this.dim.x() + 6, this.dim.getCenterY() - 4, 0xFFFFFFFF);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    public void mouseDragged(double mouseX, double mouseY, int button) {
    }

    public void mouseReleased() {
    }

    public Option<T> getOption() {
        return this.option;
    }

    public Dim2i getDimensions() {
        return this.dim;
    }
}
