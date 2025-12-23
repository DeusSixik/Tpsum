package dev.sixik.tpsum.mixin.blocks;

import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import javax.annotation.Nullable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public interface BlockStateBaseAccessor {

    @Nullable
    @Accessor("cache")
    BlockBehaviour.BlockStateBase.Cache tpsum$getCache();
}
