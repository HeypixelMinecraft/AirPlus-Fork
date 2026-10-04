package net.airplus.ui.client.sodium.util;

/**
 * Port of me.jellysquid.mods.sodium.client.util.Dim2i (Sodium 0.4.1, tag mc1.18.2-0.4.1).
 * Converted from a Java record to a final class for the 1.8.9 Java 8 toolchain.
 */
public final class Dim2i {
    private final int x;
    private final int y;
    private final int width;
    private final int height;

    public Dim2i(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int x() {
        return this.x;
    }

    public int y() {
        return this.y;
    }

    public int width() {
        return this.width;
    }

    public int height() {
        return this.height;
    }

    public int getLimitX() {
        return this.x + this.width;
    }

    public int getLimitY() {
        return this.y + this.height;
    }

    public boolean containsCursor(double x, double y) {
        return x >= this.x && x < this.getLimitX() && y >= this.y && y < this.getLimitY();
    }

    public int getCenterX() {
        return this.x + (this.width / 2);
    }

    public int getCenterY() {
        return this.y + (this.height / 2);
    }
}
