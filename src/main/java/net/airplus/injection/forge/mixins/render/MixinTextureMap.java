/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.injection.forge.mixins.render;

import net.airplus.features.module.modules.render.FPSBoost;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextureMap.class)
@SideOnly(Side.CLIENT)
public abstract class MixinTextureMap {

    @Unique
    private int airplus$animationTick;

    /**
     * Throttle animated atlas textures (water/lava/fire). Vanilla re-uploads
     * every animated sprite 20 times per second.
     */
    @Inject(method = "updateAnimations", at = @At("HEAD"), cancellable = true)
    private void injectAnimationThrottle(CallbackInfo ci) {
        final FPSBoost fpsBoost = FPSBoost.INSTANCE;
        if (!fpsBoost.handleEvents()) {
            return;
        }

        final String throttle = fpsBoost.getAnimationThrottle();
        if ("Off".equals(throttle)) {
            // Freeze animations on the current frame
            ci.cancel();
        } else if ("Half".equals(throttle) && (++this.airplus$animationTick & 1) == 0) {
            // Update every other tick -> 10Hz instead of 20Hz
            ci.cancel();
        }
    }
}
