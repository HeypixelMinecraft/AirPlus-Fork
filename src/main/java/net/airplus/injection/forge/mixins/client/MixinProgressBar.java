/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.injection.forge.mixins.client;

import net.airplus.injection.forge.SplashRenderer;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 主线程 fallback 渲染路径（安卓兼容）：
 * 当 SharedDrawable 共享上下文不可用（老安卓 EGL 桥）时，MixinSplashProgress 会置位
 * {@link SplashRenderer#mainThreadFallback}。Forge 1.8.9 的 mod 加载全部发生在主线程，
 * 每个 ProgressBar 的 step 都在主线程上——在此重绘一帧 Flux 加载界面即可，
 * 无需后台线程与共享上下文，任何 EGL 实现都能正常显示。
 *
 * Forge 类不混淆，remap = false；ProgressBar 的 step 有两个重载，分别注入。
 */
@Mixin(ProgressManager.ProgressBar.class)
@SideOnly(Side.CLIENT)
public abstract class MixinProgressBar {

    @Inject(method = "step(Ljava/lang/String;)V", at = @At("TAIL"), remap = false)
    private void airplus$onStepString(String message, CallbackInfo ci) {
        SplashRenderer.drawMainThreadSplash((ProgressManager.ProgressBar) (Object) this);
    }

    @Inject(method = "step(Ljava/lang/Class;[Ljava/lang/String;)V", at = @At("TAIL"), remap = false)
    private void airplus$onStepClass(Class<?> type, String[] message, CallbackInfo ci) {
        SplashRenderer.drawMainThreadSplash((ProgressManager.ProgressBar) (Object) this);
    }
}
