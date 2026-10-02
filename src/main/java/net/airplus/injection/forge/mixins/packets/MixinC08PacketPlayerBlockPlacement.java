/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.injection.forge.mixins.packets;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.airplus.viaversion.vialoadingbase.ViaLoadingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(C08PacketPlayerBlockPlacement.class)
@SideOnly(Side.CLIENT)
public abstract class MixinC08PacketPlayerBlockPlacement {

    @Shadow
    private BlockPos position;

    @Shadow
    private int placedBlockDirection;

    @Shadow
    private ItemStack stack;

    @Shadow
    private float facingX;

    @Shadow
    private float facingY;

    @Shadow
    private float facingZ;

    /**
     * PacketFix：跨版本（目标协议 > 1.8，协议号 > 47）时方块放置偏移量不再乘以 16，
     * 与 1.9+ 服务器的序列化格式保持一致。
     */
    @Inject(method = "writePacketData", at = @At("HEAD"), cancellable = true)
    private void packetFix$writePacketData(PacketBuffer buf, CallbackInfo ci) throws IOException {
        buf.writeBlockPos(this.position);
        buf.writeByte(this.placedBlockDirection);
        buf.writeItemStackToBuffer(this.stack);

        if (ViaLoadingBase.getInstance().getTargetVersion().getVersion() > ProtocolVersion.v1_8.getVersion()) {
            buf.writeByte((int) this.facingX);
            buf.writeByte((int) this.facingY);
            buf.writeByte((int) this.facingZ);
        } else {
            buf.writeByte((int) (this.facingX * 16.0F));
            buf.writeByte((int) (this.facingY * 16.0F));
            buf.writeByte((int) (this.facingZ * 16.0F));
        }

        ci.cancel();
    }
}
