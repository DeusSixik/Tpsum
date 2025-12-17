package dev.sixik.tpsum.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(NaturalSpawner.class)
public class MixinNaturalSpawner$fast_access {

    @Redirect(method = "spawnCategoryForPosition(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/ChunkAccess;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private static BlockState bts$spawnCategoryForPosition$fastBlockState(ChunkAccess instance, BlockPos pos) {
        return bts$fastGetBlockState(instance, pos);
    }

    @Unique
    private static BlockState bts$fastGetBlockState(ChunkAccess ca, BlockPos pos) {
        if (!(ca instanceof LevelChunk lc)) return ca.getBlockState(pos);

        int x = pos.getX();
        int z = pos.getZ();

        ChunkPos cp = lc.getPos();
        if ((x >> 4) != cp.x || (z >> 4) != cp.z) {
            return lc.getBlockState(pos);
        }

        int y = pos.getY();
        int secIdx = lc.getSectionIndex(y);
        LevelChunkSection sec = lc.getSections()[secIdx];
        return sec.getBlockState(x & 15, y & 15, z & 15);
    }
}
