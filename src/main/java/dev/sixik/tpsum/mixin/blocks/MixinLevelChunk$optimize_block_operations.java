package dev.sixik.tpsum.mixin.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraftforge.common.capabilities.ICapabilityProviderImpl;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelChunk.class)
public abstract class MixinLevelChunk$optimize_block_operations extends ChunkAccess implements ICapabilityProviderImpl<LevelChunk> {


    @Shadow
    @Final
    Level level;

    @Unique
    private static final BlockState BTS$AIR = Blocks.AIR.defaultBlockState();
    @Unique
    private static final FluidState BTS$EMPTY_FLUID = Fluids.EMPTY.defaultFluidState();
    @Unique
    private static final BlockState BTS$VOID_AIR = Blocks.VOID_AIR.defaultBlockState();

    @Unique
    private int bts$minSection;

    @Unique
    private boolean bts$isDebug;

    @Unique
    private BlockState bts$defaultBlockState;

    private MixinLevelChunk$optimize_block_operations(ChunkPos pChunkPos, UpgradeData pUpgradeData, LevelHeightAccessor pLevelHeightAccessor, Registry<Biome> pBiomeRegistry, long pInhabitedTime, @Nullable LevelChunkSection[] pSections, @Nullable BlendingData pBlendingData) {
        super(pChunkPos, pUpgradeData, pLevelHeightAccessor, pBiomeRegistry, pInhabitedTime, pSections, pBlendingData);
    }

    @Inject(
            method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/chunk/UpgradeData;Lnet/minecraft/world/ticks/LevelChunkTicks;Lnet/minecraft/world/ticks/LevelChunkTicks;J[Lnet/minecraft/world/level/chunk/LevelChunkSection;Lnet/minecraft/world/level/chunk/LevelChunk$PostLoadProcessor;Lnet/minecraft/world/level/levelgen/blending/BlendingData;)V",
            at = @At("RETURN")
    )
    public void onConstruct(Level pLevel, ChunkPos pPos, UpgradeData pData, LevelChunkTicks pBlockTicks, LevelChunkTicks pFluidTicks, long pInhabitedTime, LevelChunkSection[] pSections, LevelChunk.PostLoadProcessor pPostLoad, BlendingData pBlendingData, CallbackInfo ci) {
        this.bts$minSection = pLevel.getMinSection();

        final boolean emptyChunk = ((Object) this instanceof EmptyLevelChunk);
        this.bts$isDebug = !emptyChunk && this.level.isDebug();
        this.bts$defaultBlockState = emptyChunk ? BTS$VOID_AIR : BTS$AIR;
    }

    @Inject(method = "getBlockState", at = @At("HEAD"), cancellable = true)
    public void bts$getBlockState(BlockPos pPos, CallbackInfoReturnable<BlockState> cir) {
        cir.setReturnValue(bts$fastGetBlockState(pPos.getX(), pPos.getY(), pPos.getZ()));
    }

    @Inject(method = "getFluidState(III)Lnet/minecraft/world/level/material/FluidState;", at = @At("HEAD"), cancellable = true)
    public void bts$fastGetFluidState(int pX, int pY, int pZ, CallbackInfoReturnable<FluidState> cir) {
        cir.setReturnValue(bts$fastGetFluidState(pX, pY, pZ));
    }

    @Unique
    private BlockState bts$getDebugBlockState(int x, int y, int z) {
        if (y == 60) return Blocks.BARRIER.defaultBlockState();
        if (y == 70) return DebugLevelSource.getBlockStateFor(x, z);
        return BTS$AIR;
    }

    @Unique
    public BlockState bts$fastGetBlockState(int x, int y, int z) {
        if (bts$isDebug) return this.bts$getDebugBlockState(x, y, z);

        /*
            Inline operation
         */
        final int index = (y >> 4) - this.bts$minSection;

        if (index >= 0 && index < this.sections.length) {
            LevelChunkSection section = this.sections[index];

            /*
                Thread safe check (section != null) maybe she didn't generate yet
             */
            if (section != null && !section.hasOnlyAir()) {
                return section.getBlockState(x & 15, y & 15, z & 15);
            }
        }

        return bts$defaultBlockState;
    }

    @Unique
    public FluidState bts$fastGetFluidState(int x, int y, int z) {
        /*
            Inline operation
         */
        final int index = (y >> 4) - this.bts$minSection;

        if (index >= 0 && index < this.sections.length) {
            final LevelChunkSection section = this.sections[index];

            if (section != null && !section.hasOnlyAir()) {
                return section.getFluidState(x & 15, y & 15, z & 15);
            }
        }

        return BTS$EMPTY_FLUID;
    }
}
