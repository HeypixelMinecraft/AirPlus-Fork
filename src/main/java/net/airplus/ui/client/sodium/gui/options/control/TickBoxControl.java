package net.airplus.ui.client.sodium.gui.options.control;

import net.airplus.ui.client.sodium.gui.options.Option;
import net.airplus.ui.client.sodium.util.Dim2i;

/**
 * Port of me.jellysquid.mods.sodium.client.gui.options.control.TickBoxControl (Sodium 0.4.1).
 * Rect2i replaced with Dim2i; drawRectOutline implemented via four thin quads.
 */
public class TickBoxControl implements Control<Boolean> {
    private final Option<Boolean> option;

    public TickBoxControl(Option<Boolean> option) {
        this.option = option;
    }

    @Override
    public ControlElement<Boolean> createElement(Dim2i dim) {
        return new TickBoxControlElement(this.option, dim);
    }

    @Override
    public int getMaxWidth() {
        return 30;
    }

    @Override
    public Option<Boolean> getOption() {
        return this.option;
    }

    private static class TickBoxControlElement extends ControlElement<Boolean> {
        private final Dim2i button;

        public TickBoxControlElement(Option<Boolean> option, Dim2i dim) {
            super(option, dim);

            this.button = new Dim2i(dim.getLimitX() - 16, dim.getCenterY() - 5, 10, 10);
        }

        @Override
        public void render(int mouseX, int mouseY) {
            super.render(mouseX, mouseY);

            final int x = this.button.x();
            final int y = this.button.y();
            final int w = x + this.button.width();
            final int h = y + this.button.height();

            final boolean enabled = this.option.isAvailable();
            final boolean ticked = enabled && this.option.getValue();

            final int color;

            if (enabled) {
                color = ticked ? 0xFF94E4D3 : 0xFFFFFFFF;
            } else {
                color = 0xFFAAAAAA;
            }

            if (ticked) {
                this.drawRect(x + 2, y + 2, w - 2, h - 2, color);
            }

            this.drawRectOutline(x, y, w, h, color);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (this.option.isAvailable() && button == 0 && this.dim.containsCursor(mouseX, mouseY)) {
                this.option.setValue(!this.option.getValue());
                this.playClickSound();

                return true;
            }

            return false;
        }

        protected void drawRectOutline(int x, int y, int w, int h, int color) {
            this.drawRect(x, y, w, y + 1, color);
            this.drawRect(x, h - 1, w, h, color);
            this.drawRect(x, y, x + 1, h, color);
            this.drawRect(w - 1, y, w, h, color);
        }
    }

}
