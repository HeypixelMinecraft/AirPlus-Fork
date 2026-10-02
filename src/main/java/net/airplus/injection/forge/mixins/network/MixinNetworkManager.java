/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.injection.forge.mixins.network;

import com.viaversion.viaversion.connection.UserConnectionImpl;
import com.viaversion.viaversion.protocol.ProtocolPipelineImpl;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import net.airplus.event.EventManager;
import net.airplus.event.EventState;
import net.airplus.event.PacketEvent;
import net.airplus.viaversion.vialoadingbase.ViaLoadingBase;
import net.airplus.viaversion.vialoadingbase.netty.VLBPipeline;
import net.airplus.viaversion.vialoadingbase.netty.event.CompressionReorderEvent;
import net.airplus.viaversion.viamcp.MCPVLBPipeline;
import net.airplus.viaversion.viamcp.ViaMCP;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.airplus.utils.client.PPSCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetworkManager.class)
public abstract class MixinNetworkManager {

    @Shadow
    private Channel channel;

    @Inject(method = "channelActive", at = @At("TAIL"))
    private void injectViaPipeline(ChannelHandlerContext context, CallbackInfo callback) {
        // Target protocol is the native 1.8 protocol, no translation needed
        if (ViaLoadingBase.getInstance() == null
                || ViaLoadingBase.getInstance().getTargetVersion().getVersion() == ViaMCP.NATIVE_VERSION
                || !(context.channel() instanceof SocketChannel)) {
            return;
        }

        final UserConnectionImpl user = new UserConnectionImpl(context.channel(), true);
        new ProtocolPipelineImpl(user);
        context.pipeline().addLast(new MCPVLBPipeline(user));
    }

    // MCP 1.8.9 的映射名就是拼写错误的 "setCompressionTreshold"（少一个 h）
    @Inject(method = "setCompressionTreshold", at = @At("TAIL"))
    private void injectCompressionReorder(int threshold, CallbackInfo callback) {
        // Reorder Via handlers around the compression handlers once compression is enabled
        if (this.channel != null && this.channel.pipeline().get(VLBPipeline.VIA_DECODER_HANDLER_NAME) != null) {
            this.channel.pipeline().fireUserEventTriggered(new CompressionReorderEvent());
        }
    }

    @Inject(method = "channelRead0", at = @At("HEAD"), cancellable = true)
    private void read(ChannelHandlerContext context, Packet<?> packet, CallbackInfo callback) {
        final PacketEvent event = new PacketEvent(packet, EventState.RECEIVE);
        EventManager.INSTANCE.call(event);

        if (event.isCancelled()) {
            callback.cancel();
            return;
        }

        PPSCounter.INSTANCE.registerType(PPSCounter.PacketType.RECEIVED);
    }

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void send(Packet<?> packet, CallbackInfo callback) {
        final PacketEvent event = new PacketEvent(packet, EventState.SEND);
        EventManager.INSTANCE.call(event);

        if (event.isCancelled()) {
            callback.cancel();
            return;
        }

        PPSCounter.INSTANCE.registerType(PPSCounter.PacketType.SEND);
    }
}