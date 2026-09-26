/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.injection.forge.mixins.tweaks;

import net.airplus.features.module.modules.render.FPSBoost;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.ChunkRenderWorker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;

@Mixin(ChunkRenderDispatcher.class)
public abstract class MixinChunkRenderDispatcher {

    @Shadow
    @Final
    private List<ChunkRenderWorker> listThreadedWorkers;

    @Shadow
    @Final
    @Mutable
    private BlockingQueue<RegionRenderCacheBuilder> queueFreeRenderBuilders;

    @Shadow
    @Final
    private static ThreadFactory threadFactory;

    @Unique
    private boolean airplus$workerPoolExpanded;

    /**
     * Vanilla hardcodes two chunk rebuild workers and a pool of five
     * {@link RegionRenderCacheBuilder}s in {@code <init>}, held in a
     * {@code queueFreeRenderBuilders} of capacity 5. Verified from the bytecode:
     * <ul>
     *     <li>{@code ChunkRenderWorker.getRegionRenderCacheBuilder()} rents a builder with a
     *     blocking {@code take()};</li>
     *     <li>{@code ChunkRenderDispatcher.freeRenderBuilder()} returns it with {@code add()},
     *     which throws {@code IllegalStateException("Queue full")} once the queue is full;</li>
     *     <li>{@code ChunkRenderDispatcher.allocateRenderBuilder()} is a {@code take()} as
     *     well - not a {@code new} - and {@code stopChunkUpdates()} rents five builders with
     *     it in a row, then puts them back with a single {@code addAll()}.</li>
     * </ul>
     * Two invariants follow. The pool has to stay larger than the worker count (and never
     * drop below five, for {@code stopChunkUpdates()}), otherwise a worker blocks in
     * {@code take()} forever; and the queue has to accept every builder that can be in
     * flight at once, otherwise {@code add()} kills the render thread while a world is
     * being loaded.
     * <p>
     * The replacement queue is therefore <i>unbounded</i> instead of merely bigger: sizing a
     * bounded queue correctly means predicting how many builders the workers hold at the
     * moment of the swap, and getting that wrong is what produced the
     * {@code IllegalStateException("Queue full")} seen on world load, when this mixin used
     * to install a queue of {@code workers + 3} slots and refill it to exactly
     * {@code workers} builders with {@code add()}.
     * <p>
     * The pool cannot be resized from the constructor: the dispatcher is created during
     * {@code Minecraft.startGame()} while the client config is still being loaded, so any
     * value read there would always be the default. Instead the pool is expanded lazily on
     * the first call from the render thread, where {@code FPSBoost.chunkWorkers} is available.
     */
    @Inject(method = "runChunkUploads", at = @At("HEAD"))
    private void injectWorkerPoolExpansion(long finishTimeNano, CallbackInfoReturnable<Boolean> cir) {
        if (this.airplus$workerPoolExpanded) {
            return;
        }

        final FPSBoost fpsBoost = FPSBoost.INSTANCE;
        if (!fpsBoost.handleEvents()) {
            return;
        }

        this.airplus$workerPoolExpanded = true;

        final int currentWorkers = this.listThreadedWorkers.size();
        final int targetWorkers = fpsBoost.getChunkWorkers();
        if (targetWorkers <= currentWorkers) {
            return;
        }

        final BlockingQueue<RegionRenderCacheBuilder> enlargedQueue = new LinkedBlockingQueue<>();

        // The new queue is installed before it is filled: freeRenderBuilder() reads the
        // field on every call, so builders returned from now on land in the enlarged queue
        // instead of the one that is about to be dropped.
        final BlockingQueue<RegionRenderCacheBuilder> previousQueue = this.queueFreeRenderBuilders;
        this.queueFreeRenderBuilders = enlargedQueue;
        airplus$drainBuilders(previousQueue, enlargedQueue);

        // Keep vanilla's 3 spare builders on top of the worker count (5 builders for 2
        // workers) so that no worker can end up blocked in take() waiting for one. The
        // amount is fixed up front instead of being re-read from the queue inside a loop,
        // so builders that another thread rents in the meantime cannot inflate the pool.
        final int targetBuilders = targetWorkers + 3;
        for (int i = enlargedQueue.size(); i < targetBuilders; i++) {
            enlargedQueue.add(new RegionRenderCacheBuilder());
        }

        for (int i = currentWorkers; i < targetWorkers; i++) {
            final ChunkRenderWorker worker = new ChunkRenderWorker((ChunkRenderDispatcher) (Object) this);
            threadFactory.newThread(worker).start();
            this.listThreadedWorkers.add(worker);
        }

        // Workers that were finishing a task while the queue was swapped may have returned
        // their builder to the previous queue - move those over as well.
        airplus$drainBuilders(previousQueue, enlargedQueue);
    }

    @Unique
    private static void airplus$drainBuilders(BlockingQueue<RegionRenderCacheBuilder> from,
                                              BlockingQueue<RegionRenderCacheBuilder> to) {
        RegionRenderCacheBuilder builder;
        while ((builder = from.poll()) != null) {
            to.add(builder);
        }
    }
}