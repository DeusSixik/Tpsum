package dev.sixik.tpsum.mixin.rework_chunk_generation.biome;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkAccess.class)
public abstract class MixinChunkAccess$fast_getNoiseBiome implements BlockGetter, BiomeManager.NoiseBiomeSource, LightChunk, StructureAccess {

    @Unique
    private int bts$minSection;

    @Shadow
    @Final
    protected LevelChunkSection[] sections;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void bts$init(ChunkPos pChunkPos, UpgradeData pUpgradeData, LevelHeightAccessor pLevelHeightAccessor, Registry pBiomeRegistry, long pInhabitedTime, LevelChunkSection[] pSections, BlendingData pBlendingData, CallbackInfo ci) {
        bts$minSection = pLevelHeightAccessor.getMinSection();
    }

    @Inject(method = "getNoiseBiome", at = @At("HEAD"), cancellable = true)
    public void bts$getNoiseBiome(int pX, int pY, int pZ, CallbackInfoReturnable<Holder<Biome>> cir) {
        final LevelChunkSection[] sections = this.sections;

        int sectionY = (pY >> 2) - bts$minSection;
        int _pY = pY & 3;

        if (sectionY < 0) {
            sectionY = 0;
            _pY = 0;
        } else if (sectionY >= sections.length) {
            sectionY = sections.length - 1;
            _pY = 3;
        }

        cir.setReturnValue(sections[sectionY].getNoiseBiome(pX & 3, _pY, pZ & 3));
    }
}
