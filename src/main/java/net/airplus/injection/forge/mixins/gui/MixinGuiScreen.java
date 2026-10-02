/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.injection.forge.mixins.gui;

import net.airplus.AirPlus;
import net.airplus.features.command.CommandManager;
import net.airplus.features.module.modules.misc.ComponentOnHover;
import net.airplus.features.module.modules.render.HUD;
import net.airplus.file.configs.models.ClientConfiguration;
import net.airplus.injection.implementations.IMinecraft;
import net.airplus.utils.inputfix.GuiScreenFix;
import net.airplus.utils.inputfix.InputFixInit;
import net.airplus.utils.render.shader.Background;
import net.airplus.utils.render.ParticleUtils;
import net.airplus.utils.render.MenuBackground;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;

import static net.minecraft.client.renderer.GlStateManager.disableFog;
import static net.minecraft.client.renderer.GlStateManager.disableLighting;

@Mixin(GuiScreen.class)
@SideOnly(Side.CLIENT)
public abstract class MixinGuiScreen {
    @Shadow
    public Minecraft mc;

    @Shadow
    protected List<GuiButton> buttonList;

    @Shadow
    public int width;

    @Shadow
    public int height;

    @Shadow
    protected FontRenderer fontRendererObj;

    @Shadow
    public void updateScreen() {
    }

    @Shadow
    protected abstract void keyTyped(char typedChar, int keyCode);

    @Shadow
    public abstract void handleComponentHover(IChatComponent component, int x, int y);

    @Shadow
    protected abstract void drawHoveringText(List<String> textLines, int x, int y);

    @Inject(method = "drawWorldBackground", at = @At("HEAD"))
    private void drawWorldBackground(final CallbackInfo callbackInfo) {
        final HUD hud = HUD.INSTANCE;

        if (hud.getInventoryParticle() && mc.thePlayer != null) {
            final ScaledResolution scaledResolution = new ScaledResolution(mc);
            final int width = scaledResolution.getScaledWidth();
            final int height = scaledResolution.getScaledHeight();
            ParticleUtils.INSTANCE.drawParticles(Mouse.getX() * width / mc.displayWidth, height - Mouse.getY() * height / mc.displayHeight - 1);
        }
    }

    /**
     * @author CCBlueX
     */
    @Inject(method = "drawBackground", at = @At("HEAD"), cancellable = true)
    private void drawClientBackground(final CallbackInfo callbackInfo) {
        disableLighting();
        disableFog();

        if (ClientConfiguration.INSTANCE.getCustomBackground()) {
            final Background background = AirPlus.INSTANCE.getBackground();

            if (background == null) {
                // Use built-in menu background image
                MenuBackground.INSTANCE.drawBackground(width, height);
            } else {
                // Use custom background
                background.drawBackground(width, height);
            }

            if (ClientConfiguration.INSTANCE.getParticles()) {
                ParticleUtils.INSTANCE.drawParticles(Mouse.getX() * width / mc.displayWidth, height - Mouse.getY() * height / mc.displayHeight - 1);
            }

            callbackInfo.cancel();
        }
    }

    @Inject(method = "drawBackground", at = @At("RETURN"))
    private void drawParticles(final CallbackInfo callbackInfo) {
        if (ClientConfiguration.INSTANCE.getParticles())
            ParticleUtils.INSTANCE.drawParticles(Mouse.getX() * width / mc.displayWidth, height - Mouse.getY() * height / mc.displayHeight - 1);
    }

    @Inject(method = "sendChatMessage(Ljava/lang/String;Z)V", at = @At("HEAD"), cancellable = true)
    private void messageSend(String msg, boolean addToChat, final CallbackInfo callbackInfo) {
        if (msg.startsWith(String.valueOf(CommandManager.INSTANCE.getPrefix())) && addToChat) {
            mc.ingameGUI.getChatGUI().addToSentMessages(msg);

            CommandManager.INSTANCE.executeCommands(msg);
            callbackInfo.cancel();
        }
    }

    @Inject(method = "handleComponentHover", at = @At("HEAD"))
    private void handleHoverOverComponent(IChatComponent component, int x, int y, final CallbackInfo callbackInfo) {
        if (component == null || component.getChatStyle().getChatClickEvent() == null || !ComponentOnHover.INSTANCE.handleEvents())
            return;

        final ChatStyle chatStyle = component.getChatStyle();

        final ClickEvent clickEvent = chatStyle.getChatClickEvent();
        final HoverEvent hoverEvent = chatStyle.getChatHoverEvent();

        drawHoveringText(Collections.singletonList("§c§l" + clickEvent.getAction().getCanonicalName().toUpperCase() + ": §a" + clickEvent.getValue()), x, y - (hoverEvent != null ? 17 : 0));
    }

    /**
     * @author CCBlueX (superblaubeere27)
     * @reason Making it possible for other mixins to receive actions
     */
    @Overwrite
    protected void actionPerformed(GuiButton button) {
        injectedActionPerformed(button);
    }

    /**
     * @author AirClient
     * @reason Chinese input fix (IME support): route keyboard input through the
     * platform specific input fix implementation, which also forwards the LWJGL
     * events carrying actual text (key code 0 with a defined character).
     */
    @Overwrite
    public void handleKeyboardInput() {
        if (InputFixInit.impl != null) {
            GuiScreenFix.handleKeyboardInput((GuiScreen) (Object) this);
        } else {
            char c = Keyboard.getEventCharacter();
            int k = Keyboard.getEventKey();
            if (Keyboard.getEventKeyState() || (k == 0 && Character.isDefined(c))) {
                this.keyTyped(c, k);
            }
        }

        ((IMinecraft) this.mc).airplus$dispatchKeypresses();
    }

    protected void injectedActionPerformed(GuiButton button) {

    }
}