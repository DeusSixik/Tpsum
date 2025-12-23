package dev.sixik.tpsum.mixin.rework_chunk_generation.biome;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LevelChunkSection.class)
public class MixinLevelChunkSection$OptimizeIteration {

    @Shadow
    private PalettedContainerRO<Holder<Biome>> biomes;

    /**
     * @author Sixik
     * @reason Does it even make sense? When I did this, I thought it would give an increase, but it seems to me that the increase is at the margin of error.
     */
    @Overwrite
    public void fillBiomesFromNoise(BiomeResolver biomeResolver, Climate.Sampler climateSampler, int x, int y, int z) {
        final PalettedContainer<Holder<Biome>> palettedcontainer = this.biomes.recreate();
        for (int pX = 0; pX < 4; ++pX) {
            final int preX = x + pX;
            for (int pY = 0; pY < 4; ++pY) {
                final int preY = y + pY;
                for (int pZ = 0; pZ < 4; ++pZ) {
                    palettedcontainer.getAndSetUnchecked(pX, pY, pZ,
                            biomeResolver.getNoiseBiome(preX, preY, z + pZ, climateSampler)
                    );
                }
            }
        }
        this.biomes = palettedcontainer;
    }
}
