/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.injection.forge.mixins.render;

import net.airplus.features.module.modules.render.FPSBoost;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.particle.EntityParticleEmitter;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

@Mixin(EffectRenderer.class)
@SideOnly(Side.CLIENT)
public abstract class MixinEffectRenderer {

    @Shadow
    protected abstract void updateEffectLayer(int layer);

    @Shadow
    private List<EntityParticleEmitter> particleEmitters;

    @Shadow
    private List<EntityFX>[][] fxLayers;

    /**
     * FPSBoost: distance and count limits for spawning particles.
     * Vanilla has neither, so block breaking/lava particle storms hit CPU and GPU hard.
     */
    @Inject(method = "addEffect", at = @At("HEAD"), cancellable = true)
    private void injectParticleLimits(EntityFX entityFX, CallbackInfo ci) {
        final FPSBoost fpsBoost = FPSBoost.INSTANCE;
        if (!fpsBoost.handleEvents()) {
            return;
        }

        final int particleDistance = fpsBoost.getParticleDistance();
        if (particleDistance > 0
                && entityFX.getDistanceSq(TileEntityRendererDispatcher.staticPlayerX, TileEntityRendererDispatcher.staticPlayerY, TileEntityRendererDispatcher.staticPlayerZ)
                > (double) particleDistance * particleDistance) {
            ci.cancel();
            return;
        }

        final int limit = fpsBoost.getParticleLimit();
        if (limit > 0) {
            int count = 0;
            for (List<EntityFX>[] layer : this.fxLayers) {
                for (List<EntityFX> list : layer) {
                    count += list.size();
                }
            }

            if (count >= limit) {
                ci.cancel();
            }
        }
    }

    /**
     * @author Mojang
     * @author Marco
     */
    @Overwrite
    public void updateEffects() {
        try {
            for (int i = 0; i < 4; ++i)
                updateEffectLayer(i);

            for (final Iterator<EntityParticleEmitter> it = particleEmitters.iterator(); it.hasNext(); ) {
                final EntityParticleEmitter entityParticleEmitter = it.next();

                entityParticleEmitter.onUpdate();

                if (entityParticleEmitter.isDead)
                    it.remove();
            }
        } catch(final ConcurrentModificationException ignored) {
        }
    }
}