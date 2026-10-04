package net.airplus.ui.client.sodium.gui.options.control;

import net.airplus.ui.client.sodium.gui.options.Option;
import net.airplus.ui.client.sodium.util.Dim2i;
import net.minecraft.util.MathHelper;
import org.apache.commons.lang3.Validate;

/**
 * Port of me.jellysquid.mods.sodium.client.gui.options.control.SliderControl (Sodium 0.4.1).
 * Rect2i replaced with Dim2i; mouseDragged is driven by the screen's mouseClickMove/mouseReleased
 * (1.8.9 GuiScreen has no per-element drag callbacks).
 */
public class SliderControl implements Control<Integer> {
    private final Option<Integer> option;

    private final int min, max, interval;

    private final ControlValueFormatter mode;

    public SliderControl(Option<Integer> option, int min, int max, int interval, ControlValueFormatter mode) {
        Validate.isTrue(max > min, "The maximum value must be greater than the minimum value");
        Validate.isTrue(interval > 0, "The slider interval must be greater than zero");
        Validate.isTrue(((max - min) % interval) == 0, "The maximum value must be divisable by the interval");
        Validate.notNull(mode, "The slider mode must not be null");

        this.option = option;
        this.min = min;
        this.max = max;
        this.interval = interval;
        this.mode = mode;
    }

    @Override
    public ControlElement<Integer> createElement(Dim2i dim) {
        return new Button(this.option, dim, this.min, this.max, this.interval, this.mode);
    }

    @Override
    public Option<Integer> getOption() {
        return this.option;
    }

    @Override
    public int getMaxWidth() {
        return 130;
    }

    private static class Button extends ControlElement<Integer> {
        private static final int THUMB_WIDTH = 2, TRACK_HEIGHT = 1;

        private final Dim2i sliderBounds;
        private final ControlValueFormatter formatter;

        private final int min;
        private final int range;
        private final int interval;

        private double thumbPosition;
        private boolean dragging;

        public Button(Option<Integer> option, Dim2i dim, int min, int max, int interval, ControlValueFormatter formatter) {
            super(option, dim);

            this.min = min;
            this.range = max - min;
            this.interval = interval;
            this.thumbPosition = this.getThumbPositionForValue(option.getValue());
            this.formatter = formatter;

            this.sliderBounds = new Dim2i(dim.getLimitX() - 96, dim.getCenterY() - 5, 90, 10);
        }

        @Override
        public void render(int mouseX, int mouseY) {
            super.render(mouseX, mouseY);

            if (this.option.isAvailable() && this.hovered) {
                this.renderSlider();
            } else {
                this.renderStandaloneValue();
            }
        }

        private void renderStandaloneValue() {
            int sliderX = this.sliderBounds.x();
            int sliderY = this.sliderBounds.y();
            int sliderWidth = this.sliderBounds.width();
            int sliderHeight = this.sliderBounds.height();

            String label = this.formatter.format(this.option.getValue());
            int labelWidth = this.font.getStringWidth(label);

            this.drawString(label, sliderX + sliderWidth - labelWidth, sliderY + (sliderHeight / 2) - 4, 0xFFFFFFFF);
        }

        private void renderSlider() {
            int sliderX = this.sliderBounds.x();
            int sliderY = this.sliderBounds.y();
            int sliderWidth = this.sliderBounds.width();
            int sliderHeight = this.sliderBounds.height();

            this.thumbPosition = this.getThumbPositionForValue(option.getValue());

            double thumbOffset = MathHelper.clamp_double((double) (this.getIntValue() - this.min) / this.range * sliderWidth, 0, sliderWidth);

            double thumbX = sliderX + thumbOffset - THUMB_WIDTH;
            double trackY = sliderY + (sliderHeight / 2) - ((double) TRACK_HEIGHT / 2);

            this.drawRect(thumbX, sliderY, thumbX + (THUMB_WIDTH * 2), sliderY + sliderHeight, 0xFFFFFFFF);
            this.drawRect(sliderX, trackY, sliderX + sliderWidth, trackY + TRACK_HEIGHT, 0xFFFFFFFF);

            String label = String.valueOf(this.getIntValue());

            int labelWidth = this.font.getStringWidth(label);

            this.drawString(label, sliderX - labelWidth - 6, sliderY + (sliderHeight / 2) - 4, 0xFFFFFFFF);
        }

        public int getIntValue() {
            return this.min + (this.interval * (int) Math.round(this.getSnappedThumbPosition() / this.interval));
        }

        public double getSnappedThumbPosition() {
            return this.thumbPosition / (1.0D / this.range);
        }

        public double getThumbPositionForValue(int value) {
            return (value - this.min) * (1.0D / this.range);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (this.option.isAvailable() && button == 0 && this.sliderBounds.containsCursor(mouseX, mouseY)) {
                this.dragging = true;
                this.setValueFromMouse(mouseX);

                return true;
            }

            return false;
        }

        @Override
        public void mouseDragged(double mouseX, double mouseY, int button) {
            if (this.dragging && this.option.isAvailable() && button == 0) {
                this.setValueFromMouse(mouseX);
            }
        }

        @Override
        public void mouseReleased() {
            this.dragging = false;
        }

        private void setValueFromMouse(double d) {
            this.setValue((d - (double) this.sliderBounds.x()) / (double) this.sliderBounds.width());
        }

        private void setValue(double d) {
            this.thumbPosition = MathHelper.clamp_double(d, 0.0D, 1.0D);

            int value = this.getIntValue();

            if (this.option.getValue() != value) {
                this.option.setValue(value);
            }
        }
    }

}
