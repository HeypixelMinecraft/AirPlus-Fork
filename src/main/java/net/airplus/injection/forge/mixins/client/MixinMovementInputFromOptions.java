/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.injection.forge.mixins.client;

import net.airplus.event.EventManager;
import net.airplus.event.MovementInputEvent;
import net.airplus.features.module.modules.combat.SuperKnockback;
import net.airplus.features.module.modules.world.scaffolds.Scaffold;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MovementInputFromOptions.class)
public class MixinMovementInputFromOptions extends MixinMovementInput {

    @Inject(method = "updatePlayerMoveState", at = @At(value = "FIELD", target = "Lnet/minecraft/util/MovementInputFromOptions;jump:Z"))
    private void hookSuperKnockbackInputBlock(CallbackInfo ci) {
        SuperKnockback module = SuperKnockback.INSTANCE;

        if (module.shouldBlockInput()) {
            if (module.getOnlyMove()) {
                this.moveForward = 0f;

                if (!module.getOnlyMoveForward()) {
                    this.moveStrafe = 0f;
                }
            }
        }

        Scaffold.INSTANCE.handleMovementOptions(((MovementInput) (Object) this));
    }

    @Inject(method = "updatePlayerMoveState", at = @At(value = "FIELD", target = "Lnet/minecraft/util/MovementInputFromOptions;sneak:Z", ordinal = 1))
    private void injectMovementInputEvent(CallbackInfo ci) {
        EventManager.INSTANCE.call(new MovementInputEvent((MovementInput) (Object) this));
    }
}
