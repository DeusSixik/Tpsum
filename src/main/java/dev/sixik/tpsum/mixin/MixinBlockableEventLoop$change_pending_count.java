package dev.sixik.tpsum.mixin;

import net.minecraft.util.profiling.metrics.ProfilerMeasured;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.util.thread.ProcessorHandle;
import org.jctools.queues.MpscUnboundedArrayQueue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.LockSupport;

@Mixin(value = BlockableEventLoop.class, priority = Integer.MAX_VALUE / 2)
public abstract class MixinBlockableEventLoop$change_pending_count<R extends Runnable> implements ProfilerMeasured, ProcessorHandle<R>, Executor {

    @Shadow
    private int blockingCount;

    @Shadow
    protected abstract boolean shouldRun(R p_18703_);

    @Shadow
    protected abstract void doRunTask(R p_18700_);

    @Shadow
    protected abstract Thread getRunningThread();

    @Unique
    private final LongAdder bts$pendigCount = new LongAdder();

    @Unique
    private final MpscUnboundedArrayQueue<R> bts$custom_pendingRunnables = new MpscUnboundedArrayQueue<>(256);

    @Inject(method = "getPendingTasksCount", at = @At("HEAD"), cancellable = true)
    public void bts$getPendingTasksCount(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(bts$pendigCount.intValue());
    }

    @Inject(method = "tell(Ljava/lang/Runnable;)V", at = @At(value = "HEAD"), cancellable = true)
    public void bts$tell(R task, CallbackInfo ci) {
        ci.cancel();
        bts$custom_pendingRunnables.offer(task);
        bts$pendigCount.increment();
        LockSupport.unpark(getRunningThread());
    }

    @Inject(method = "pollTask", at = @At("HEAD"), cancellable = true)
    public void bts$pollTask(CallbackInfoReturnable<Boolean> cir) {
        R r = bts$custom_pendingRunnables.peek();
        if (r == null) {
            cir.setReturnValue(false);
            return;
        }
        if (blockingCount == 0 && !shouldRun(r)) {
            cir.setReturnValue(false);
            return;
        }

        R taken = bts$custom_pendingRunnables.poll();
        if (taken == null) {
            cir.setReturnValue(false);
            return;
        }

        bts$pendigCount.decrement();
        doRunTask(taken);
        cir.setReturnValue(true);
    }

    @Inject(method = "dropAllTasks", at = @At("HEAD"), cancellable = true)
    public void bts$dropAllTasks(CallbackInfo ci) {
        ci.cancel();
        bts$custom_pendingRunnables.clear();
        bts$pendigCount.reset();
    }

    @Inject(method = "waitForTasks", at = @At("HEAD"), cancellable = true)
    public void bts$waitForTasks(CallbackInfo ci) {
        ci.cancel();
        LockSupport.parkNanos("waiting for tasks", 100000L);
    }

    @Inject(method = "submitAsync", at = @At("HEAD"), cancellable = true)
    public void bts$submitAsync(Runnable p_18690_, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        cir.setReturnValue(CompletableFuture.runAsync(p_18690_, this));
    }
}
