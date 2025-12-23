package dev.sixik.tpsum.mixin.blocks;

import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockBehaviour.BlockStateBase.Cache.class)
public interface BlockStateCacheAccessor {

    @Accessor("isCollisionShapeFullBlock")
    boolean tpsum$isCollisionShapeFullBlock();
}