package dev.sixik.tpsum.mixin.entity_spawn;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.LocalMobCapCalculator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalMobCapCalculator.MobCounts.class)
public class MixinLocalMobCapCalculator$MobCount$optimize_allocation {

    @Unique
    private static final MobCategory[] BTS$MOB_CATEGORIES = MobCategory.values();

    @Unique
    private final int[] bts$counts = new int[BTS$MOB_CATEGORIES.length];

    @Unique
    private static final Object2IntOpenHashMap<MobCategory> BTS$NULL = new Object2IntOpenHashMap<>(0);

    /**
     * @author Sixik
     * @reason Since we are using primitives, we do not need to create an array from FastUtils and, consequently,
     * we can return NULL so as not to allocate memory for it.
     */
    @Redirect(
            method = "<init>",
            at = @At(
                    value = "NEW",
                    target = "(I)Lit/unimi/dsi/fastutil/objects/Object2IntOpenHashMap;"
            )
    )
    private <T> Object2IntOpenHashMap<T> avoidAllocation(final int unused) {
        return (Object2IntOpenHashMap<T>) BTS$NULL;
    }

    /**
     * @author Sixik
     * @reason When creating, we do not create a copy, but use our static array.
     */
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/MobCategory;values()[Lnet/minecraft/world/entity/MobCategory;"))
    public MobCategory[] bts$init() {
        return BTS$MOB_CATEGORIES;
    }

    /**
     * @author Sixik
     * @reason We use a simple array instead of calculating with {@code counts.computeInt}
     */
    @Inject(method = "add(Lnet/minecraft/world/entity/MobCategory;)V", at = @At("HEAD"), cancellable = true)
    public void bts$add(MobCategory pCategory, CallbackInfo ci) {
        ci.cancel();
        bts$counts[pCategory.ordinal()]++;
    }

    /**
     * @author Sixik
     * @reason We use a simple array instead of {@code counts.getOrDefault}
     */
    @Inject(method = "canSpawn", at = @At("HEAD"), cancellable = true)
    public void canSpawn(MobCategory pCategory, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(bts$counts[pCategory.ordinal()] < pCategory.getMaxInstancesPerChunk());
    }
}
