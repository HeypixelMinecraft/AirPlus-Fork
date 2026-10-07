/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.injection.forge;

import net.airplus.ui.font.AWTFontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.common.ProgressManager;
import org.lwjgl.opengl.Display;

import java.awt.Font;
import java.io.InputStream;
import java.util.Iterator;

import static org.lwjgl.opengl.GL11.*;

/**
 * Flux 风格启动加载界面的公共绘制器，供两条渲染路径共用：
 * <ul>
 *   <li>桌面/新安卓：MixinSplashProgress 后台线程 + SharedDrawable，逐帧动画</li>
 *   <li>老安卓（EGL 桥不支持共享上下文）：MixinProgressBar 在主线程逐 step 重绘同一帧</li>
 * </ul>
 * 全部方法内部吞 Throwable，任何失败只降级显示（纯进度条/黑屏），绝不炸启动。
 */
public final class SplashRenderer {

    /** SharedDrawable 不可用时由 MixinSplashProgress.start() 置位，主线程路径接管 */
    public static volatile boolean mainThreadFallback = false;

    private static volatile long welcomeAt = 0L;
    private static volatile long startedAt = 0L;
    private static volatile boolean textBroken = false;
    private static boolean fontsTried = false;
    private static AWTFontRenderer bigFont;
    private static AWTFontRenderer smallFont;

    private static final String BRAND = "AirPlus 1.8.9";

    private SplashRenderer() {
    }

    public static void markStarted() {
        if (startedAt == 0L) startedAt = System.currentTimeMillis();
    }

    public static void markWelcome() {
        welcomeAt = System.currentTimeMillis();
    }

    /** 懒加载 + 只尝试一次（安卓 headless AWT / 只读 tmpdir 下 Font.createFont 可能失败） */
    public static void initFonts() {
        if (fontsTried) return;
        fontsTried = true;

        try {
            Font big = loadTTF("/assets/minecraft/airplus/fonts/RobotoLight.ttf", 80f);
            Font small = loadTTF("/assets/minecraft/airplus/fonts/Roboto.ttf", 30f);

            if (big != null) bigFont = new AWTFontRenderer(big, 0, 65535, true);
            if (small != null) smallFont = new AWTFontRenderer(small, 0, 65535, true);

            System.out.println("[AirPlus] Splash fonts initialized (big=" + (bigFont != null)
                    + ", small=" + (smallFont != null) + ")");
        } catch (Throwable t) {
            System.out.println("[AirPlus] Splash font init error:");
            t.printStackTrace();
        }
    }

    private static Font loadTTF(String resourcePath, float size) {
        try {
            InputStream stream = SplashRenderer.class.getResourceAsStream(resourcePath);
            if (stream == null) {
                System.out.println("[AirPlus] Splash font resource not found: " + resourcePath);
                return null;
            }
            return Font.createFont(Font.TRUETYPE_FONT, stream).deriveFont(Font.PLAIN, size);
        } catch (Throwable t) {
            System.out.println("[AirPlus] Splash font load failed: " + resourcePath);
            t.printStackTrace();
            return null;
        }
    }

    /** AWTFontRenderer 矩阵组合：平移 (x, y-3+0.5) 后在原点绘制（同 GameFontRenderer.drawString） */
    private static void drawText(AWTFontRenderer renderer, String text, float x, float y, int color) {
        glPushMatrix();
        glTranslated(x - 1.5, y - 2.5, 0.0);
        renderer.drawString(text, 0.0, 0.0, color);
        glPopMatrix();
    }

    /**
     * AWTFontRenderer 管线：getStringWidth 返回 native/2，字形按 native/4 绘制（内部 glScaled(0.25)），
     * 视觉宽度 = 0.5 * W，居中偏移取 W/4。
     */
    private static void drawCenteredText(AWTFontRenderer renderer, String text, int sw, float y, int color) {
        float x = sw / 2f - renderer.getStringWidth(text) / 4f;
        drawText(renderer, text, x, y, color);
    }

    /**
     * 绘制完整一帧：黑底 + 标题 + FML 进度信息/进度条 + brand。
     * 桌面线程每帧调用；主线程 fallback 每个 ProgressBar step 调用。
     */
    public static void renderFrame(ProgressManager.ProgressBar first) {
        try {
            glClear(GL_COLOR_BUFFER_BIT);

            ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
            int sw = sr.getScaledWidth();
            int sh = sr.getScaledHeight();

            glViewport(0, 0, Display.getWidth(), Display.getHeight());
            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            glOrtho(0, sw, sh, 0, -1, 1);
            glMatrixMode(GL_MODELVIEW);
            glLoadIdentity();

            // 纯黑背景（Flux 加载风格）
            glDisable(GL_TEXTURE_2D);
            glColor3f(0f, 0f, 0f);
            glBegin(GL_QUADS);
            glVertex2f(0, 0);
            glVertex2f(0, sh);
            glVertex2f(sw, sh);
            glVertex2f(sw, 0);
            glEnd();

            boolean welcome = welcomeAt > 0L;

            // 文字（Flux 式淡入）
            if (!textBroken && bigFont != null) {
                try {
                    GlStateManager.enableAlpha();
                    GlStateManager.enableBlend();
                    GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
                    GlStateManager.enableTexture2D();

                    String title = welcome ? "Welcome To AirPlus!" : "Loading AirPlus...";
                    long at = welcome ? welcomeAt : startedAt;
                    float alpha = at == 0L ? 1f : Math.min(1f, (System.currentTimeMillis() - at) / 800f);
                    int titleColor = ((int) (alpha * 255f) << 24) | 0xFFFFFF;
                    drawCenteredText(bigFont, title, sw, sh / 2f - 24f, titleColor);

                    if (!welcome && smallFont != null) {
                        if (first != null) {
                            String info = first.getTitle() + " - " + first.getMessage()
                                    + " (" + first.getStep() + "/" + first.getSteps() + ")";
                            drawCenteredText(smallFont, info, sw, sh / 2f + 24f, 0x90FFFFFF);
                        }
                        drawCenteredText(smallFont, BRAND, sw, sh - 24f, 0x60FFFFFF);
                    }

                    GlStateManager.disableTexture2D();
                    GlStateManager.disableBlend();
                    GlStateManager.resetColor();
                } catch (Throwable t) {
                    System.out.println("[AirPlus] Splash text draw error:");
                    t.printStackTrace();
                    textBroken = true;
                }
            }

            // 纯 GL 进度条（字体挂了也能显示）
            if (!welcome && first != null) {
                float barWidth = sw * 0.4f;
                float barX = sw * 0.3f;
                float barY = sh * 0.75f;
                float barHeight = 3f;

                // border
                glColor3f(0.25f, 0.25f, 0.25f);
                glBegin(GL_QUADS);
                glVertex2f(barX - 1f, barY - 1f);
                glVertex2f(barX - 1f, barY + barHeight + 1f);
                glVertex2f(barX + barWidth + 1f, barY + barHeight + 1f);
                glVertex2f(barX + barWidth + 1f, barY - 1f);
                glEnd();

                // fill
                float progress = (first.getStep() + 1f) / (first.getSteps() + 1f);
                glColor3f(1f, 1f, 1f);
                glBegin(GL_QUADS);
                glVertex2f(barX, barY);
                glVertex2f(barX, barY + barHeight);
                glVertex2f(barX + barWidth * progress, barY + barHeight);
                glVertex2f(barX + barWidth * progress, barY);
                glEnd();
            }
        } catch (Throwable t) {
            System.out.println("[AirPlus] Splash frame render error:");
            t.printStackTrace();
        }
    }

    /** 主线程 fallback 绘制入口（MixinProgressBar 调用），step 时刷新一帧 */
    public static void drawMainThreadSplash(ProgressManager.ProgressBar bar) {
        if (!mainThreadFallback) return;

        try {
            initFonts();
            renderFrame(bar);
            Display.update();
        } catch (Throwable t) {
            System.out.println("[AirPlus] Main-thread splash draw error:");
            t.printStackTrace();
        }
    }

    /** 供线程循环取最新的第一个进度条 */
    public static ProgressManager.ProgressBar firstBar() {
        Iterator<ProgressManager.ProgressBar> i = ProgressManager.barIterator();
        return i.hasNext() ? i.next() : null;
    }
}
