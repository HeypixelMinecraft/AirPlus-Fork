package net.airplus.utils.kotlin

import kotlinx.coroutines.*
import kotlinx.coroutines.internal.MainDispatcherFactory
import net.airplus.utils.client.ClientUtils.LOGGER
import net.minecraft.client.Minecraft
import net.minecraft.util.IThreadListener
import kotlin.coroutines.CoroutineContext

object SharedScopes {

    // Fallback handler so uncaught exceptions are logged instead of hitting the
    // global Thread.uncaughtExceptionHandler (which relies on ServiceLoader and
    // previously crashed with NoClassDefFoundError in a shaded environment)
    val exceptionHandler = CoroutineExceptionHandler { _, error ->
        LOGGER.error("Uncaught exception in client coroutine", error)
    }

    @JvmField
    val Default = CoroutineScope(Dispatchers.Default + SupervisorJob() + exceptionHandler)

    @JvmField
    val IO = CoroutineScope(Dispatchers.IO + SupervisorJob() + exceptionHandler)

    fun stop() {
        Default.cancel()
        IO.cancel()
    }
}

/**
 * To dispatch tasks on Client thread (Render thread)
 * @author MukjepScarlet
 *
 * Registered as the [Dispatchers.Main] implementation via
 * META-INF/services/kotlinx.coroutines.internal.MainDispatcherFactory
 * (see [RenderMainDispatcherFactory]). This replaces the previous hack of
 * calling Dispatchers.setMain() from kotlinx-coroutines-test, which must
 * never be bundled into the production jar.
 */
internal object RenderDispatcher : MainCoroutineDispatcher() {
    val mc: IThreadListener = Minecraft.getMinecraft()

    override val immediate: MainCoroutineDispatcher
        get() = this

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        try {
            if (mc.isCallingFromMinecraftThread) {
                block.run()
            } else {
                mc.addScheduledTask(block)
            }
        } catch (e: Throwable) {
            context[CoroutineExceptionHandler]?.handleException(context, e) ?: throw e
        }
    }
}

/**
 * ServiceLoader entry point that binds [RenderDispatcher] to [Dispatchers.Main].
 * Replaces kotlinx-coroutines-test's TestMainDispatcherFactory in the shaded jar.
 */
@InternalCoroutinesApi
class RenderMainDispatcherFactory : MainDispatcherFactory {
    override fun createDispatcher(allFactories: List<MainDispatcherFactory>): MainCoroutineDispatcher =
        RenderDispatcher

    override val loadPriority: Int
        get() = Int.MAX_VALUE

    override fun hintOnError(): String? = null
}
