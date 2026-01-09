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
     * @reason Replaces iterator.remove() overhead with bitmask operations. Zero allocations in loop.
     */
    @Overwrite
    public static void primeHeightmaps(ChunkAccess chunk, Set<Heightmap.Types> types) {
        final int numTypes = types.size();
        final Heightmap[] heightmaps = new Heightmap[numTypes];
        int idx = 0;
        for (Heightmap.Types type : types) {
            heightmaps[idx++] = chunk.getOrCreateHeightmapUnprimed(type);
        }

        final int highestSection = chunk.getHighestSectionPosition() + 16;
        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        /*
            Bit mask: 1 means that the elevation map is not yet filled
         */
        final int initialMask = (1 << numTypes) - 1;

        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                int activeMask = initialMask;

                for (int y = highestSection - 1; y >= chunk.getMinBuildHeight(); --y) {
                    pos.set(x, y, z);
                    BlockState state = chunk.getBlockState(pos);

                    if (state.isAir()) continue;

                    for (int i = 0; i < numTypes; ++i) {

                        /*
                            Checking if the i bit is active
                         */
                        if ((activeMask & (1 << i)) != 0) {
                            if (heightmaps[i].isOpaque.test(state)) {
                                heightmaps[i].setHeight(x, z, y + 1);

                                /*
                                    Turn off bit i (set it to 0)
                                 */
                                activeMask &= ~(1 << i);
                            }
                        }
                    }

                    /*
                        If all the bits are zero, stop the loop at Y
                     */
                    if (activeMask == 0) break;
                }
            }
        }
    }
}

