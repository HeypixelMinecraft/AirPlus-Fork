/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.utils.io

import java.nio.Buffer
import java.nio.ByteBuffer

/**
 * Prevents crashes when flip() is called from higher Java versions.
 *
 * JDK 9 才给 ByteBuffer 增加了协变重写的 flip()，旧版 Java/Android 上只有 Buffer.flip()。
 * 因此这里强制以 Buffer 类型调用，避免编译出 ByteBuffer.flip() 而触发 NoSuchMethodError。
 */
fun ByteBuffer.flipSafely() {
    try {
        (this as Buffer).flip()
    } catch (t: Throwable) {
        // NoSuchMethodError / NoClassDefFoundError 属于 Error，必须用 Throwable 捕获
        t.printStackTrace()
    }
}