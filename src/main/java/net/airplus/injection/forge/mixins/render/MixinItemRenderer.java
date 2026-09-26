/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.injection.forge.mixins.render;

import net.airplus.features.module.modules.render.FreeCam;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.ItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemRenderer.class)
public class MixinItemRenderer {

    @Redirect(method = "renderOverlays", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;isEntityInsideOpaqueBlock()Z"))
    private boolean injectFreeCam(EntityPlayerSP instance) {
        return !FreeCam.INSTANCE.handleEvents() && instance.isEntityInsideOpaqueBlock();
    }
}
