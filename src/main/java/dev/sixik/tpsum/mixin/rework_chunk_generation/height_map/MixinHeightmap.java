package dev.sixik.tpsum.mixin.rework_chunk_generation.height_map;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Arrays;
import java.util.Set;

@Mixin(Heightmap.class)
public class MixinHeightmap {

    /**
     * @author Sixik
     * @reason Using primitive array and flag. If block is Air skip iteration element
     */
    @Overwrite
    public static void primeHeightmaps(ChunkAccess chunk, Set<Heightmap.Types> types) {
        int numTypes = types.size();
        Heightmap[] heightmaps = new Heightmap[numTypes];
        int index = 0;
        for (Heightmap.Types type : types) {
            heightmaps[index++] = chunk.getOrCreateHeightmapUnprimed(type);
        }

        int highestSection = chunk.getHighestSectionPosition() + 16;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                boolean[] active = new boolean[numTypes];
                Arrays.fill(active, true);

                int activeCount = numTypes;
                for (int y = highestSection - 1; y >= chunk.getMinBuildHeight(); --y) {
                    pos.set(x, y, z);
                    BlockState state = chunk.getBlockState(pos);
                    if (state.isAir()) continue;

                    for (int i = 0; i < numTypes; ++i) {
                        if (active[i] && heightmaps[i].isOpaque.test(state)) {
                            heightmaps[i].setHeight(x, z, y + 1);
                            active[i] = false;
                            if (--activeCount == 0) break;
                        }
                    }
                    if (activeCount == 0) break;
                }
            }
        }
    }
}

