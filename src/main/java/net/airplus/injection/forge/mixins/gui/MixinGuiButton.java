/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.injection.forge.mixins.gui;

import net.airplus.ui.font.AWTFontRenderer;
import net.airplus.ui.font.Fonts;
import net.airplus.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.*;

import java.awt.*;

import static net.minecraft.client.renderer.GlStateManager.resetColor;

@Mixin(GuiButton.class)
@SideOnly(Side.CLIENT)
public abstract class MixinGuiButton extends Gui {

    @Shadow
    public boolean visible;

    @Shadow
    public int xPosition;

    @Shadow
    public int yPosition;

    @Shadow
    public int width;

    @Shadow
    public int height;

    @Shadow
    protected boolean hovered;

    @Shadow
    public boolean enabled;

    @Shadow
    protected abstract void mouseDragged(Minecraft mc, int mouseX, int mouseY);

    @Shadow
    public String displayString;

    @Shadow
    @Final
    protected static ResourceLocation buttonTextures;

    @Shadow
    public int id;

    @Unique
    private long startTime = -1L;

    @Unique
    private boolean lastHover = false;

    /**
     * @author CCBlueX
     */
    @Overwrite
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (visible) {
            hovered = mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height;

            float supposedWidth = width;
            boolean isSlider = false;

            if ((Object) this instanceof GuiOptionSlider) {
                supposedWidth *= ((GuiOptionSlider) (Object) this).sliderValue;
                hovered = true;
                isSlider = true;
            }

            if ((Object) this instanceof GuiScreenOptionsSounds.Button) {
                supposedWidth *= ((GuiScreenOptionsSounds.Button) (Object) this).field_146156_o;
                hovered = true;
                isSlider = true;
            }

            if (hovered != lastHover) {
                startTime = System.currentTimeMillis();
                lastHover = hovered;
            }

            // 悬停进度：进入/离开各 200ms，easeOutQuad 平滑过渡
            float elapsed = MathHelper.clamp_float((System.currentTimeMillis() - startTime) / 200f, 0f, 1f);
            float easeOut = 1f - (1f - elapsed) * (1f - elapsed);
            float anim = hovered ? easeOut : 1f - easeOut;

            float radius = 6F;

            // 深色半透明卡片底，悬停平滑变亮；禁用时整体更暗（与 MenuButton 同一色板）
            Color bg = enabled
                    ? lerpColor(new Color(15, 15, 18, 150), new Color(255, 255, 255, 45), anim)
                    : new Color(15, 15, 18, 80);
            RenderUtils.INSTANCE.drawRoundedRect(xPosition, yPosition, xPosition + width, yPosition + height, bg.getRGB(), radius, RenderUtils.RoundedCorners.ALL);

            // 滑块进度填充
            if (isSlider && enabled && supposedWidth > 0f) {
                float fillRadius = supposedWidth > radius * 2 ? radius : 0F;
                RenderUtils.INSTANCE.drawRoundedRect(xPosition, yPosition, xPosition + supposedWidth, yPosition + height, new Color(255, 255, 255, 55).getRGB(), fillRadius, RenderUtils.RoundedCorners.ALL);
            }

            // 白色细描边随悬停渐显
            int borderAlpha = (int) (40f + 130f * anim);
            if (enabled || borderAlpha > 0) {
                RenderUtils.INSTANCE.drawRoundedBorder(xPosition, yPosition, xPosition + width, yPosition + height, 1F, new Color(255, 255, 255, enabled ? borderAlpha : 30).getRGB(), radius);
            }

            mouseDragged(mc, mouseX, mouseY);

            AWTFontRenderer.Companion.setAssumeNonVolatile(true);

            // 文字由灰变白
            int textColor = enabled
                    ? lerpColor(new Color(190, 190, 195), new Color(255, 255, 255), anim).getRGB()
                    : new Color(105, 108, 115).getRGB();
            final FontRenderer fontRenderer = Fonts.fontSemibold35;
            fontRenderer.drawStringWithShadow(displayString, (float) (xPosition + width / 2 - fontRenderer.getStringWidth(displayString) / 2), yPosition + (height - 5) / 2F, textColor);

            AWTFontRenderer.Companion.setAssumeNonVolatile(false);

            resetColor();
        }
    }

    @Unique
    private static Color lerpColor(Color a, Color b, float t) {
        return new Color(
                (int) (a.getRed() + (b.getRed() - a.getRed()) * t),
                (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t),
                (int) (a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t)
        );
    }
}
