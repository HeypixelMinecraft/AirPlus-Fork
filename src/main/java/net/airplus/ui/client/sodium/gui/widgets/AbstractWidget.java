package net.airplus.ui.client.sodium.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * Port of me.jellysquid.mods.sodium.client.gui.widgets.AbstractWidget (Sodium 0.4.1),
 * adapted to the 1.8.9 immediate-mode render pipeline (Tessellator/WorldRenderer + GlStateManager).
 */
public abstract class AbstractWidget {
    protected final FontRenderer font = Minecraft.getMinecraft().fontRendererObj;

    protected void drawString(String str, int x, int y, int color) {
        this.font.drawString(str, x, y, color);
    }

    protected void drawRect(double x1, double y1, double x2, double y2, int color) {
        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;

        this.drawQuads(vertices -> addQuad(vertices, x1, y1, x2, y2, a, r, g, b));
    }

    protected void drawQuads(Consumer<WorldRenderer> consumer) {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        consumer.accept(worldRenderer);

        tessellator.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    protected static void addQuad(WorldRenderer consumer, double x1, double y1, double x2, double y2, float a, float r, float g, float b) {
        consumer.pos(x2, y1, 0.0D).color(r, g, b, a).endVertex();
        consumer.pos(x1, y1, 0.0D).color(r, g, b, a).endVertex();
        consumer.pos(x1, y2, 0.0D).color(r, g, b, a).endVertex();
        consumer.pos(x2, y2, 0.0D).color(r, g, b, a).endVertex();
    }

    protected void playClickSound() {
        Minecraft.getMinecraft().getSoundHandler()
                .playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
    }

    protected int getStringWidth(String text) {
        return this.font.getStringWidth(text);
    }

    @FunctionalInterface
    protected interface Consumer<T> {
        void accept(T t);
    }
}
