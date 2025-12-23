package dev.sixik.tpsum.mixin.blocks;

import dev.sixik.tpsum.level.BlockStateConductorCacheGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockBehaviour.BlockStateBase.Cache.class)
public class MixinBlockState$cache_conductor implements BlockStateConductorCacheGetter {
    @Unique
    private boolean tpsum$isRedstoneConductor;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void tpsum$init(BlockState state, CallbackInfo ci) {
        this.tpsum$isRedstoneConductor = state.isRedstoneConductor(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
    }

    @Override
    public boolean tpsum$isRedstoneConductor() {
        return tpsum$isRedstoneConductor;
    }
}