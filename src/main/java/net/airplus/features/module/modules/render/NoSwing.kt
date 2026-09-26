/*
 * Air Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 */
package net.airplus.features.module.modules.render

import net.airplus.features.module.Category
import net.airplus.features.module.Module
import net.airplus.utils.attack.CooldownHelper
import net.airplus.utils.client.PacketUtils.sendPacket
import net.minecraft.network.play.client.C0APacketAnimation

object NoSwing : Module("NoSwing", Category.RENDER) {

    /**
     * NoSwing 的核心思路：取消客户端挥手动画，但正常发送挥手包。
     * Scaffold 等模块的「无动画挥手」复用此逻辑，保证两处行为一致。
     */
    fun swingWithoutAnimation() {
        sendPacket(C0APacketAnimation())
        CooldownHelper.resetLastAttackedTicks()
    }
}
