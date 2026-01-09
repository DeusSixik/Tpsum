package dev.sixik.tpsum.mixin.compat.spawn_range_adjuster;

import dev.xkmc.spawn_range_adjuster.init.SRAConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NaturalSpawner.class)
public class SRA$NaturalSpawnerMixin {

    @Inject(
            method = {"spawnForChunk"},
            at = {@At("HEAD")},
            cancellable = true
    )
    private static void spawnRangeAdjuster$spawnEarlyTermination(ServerLevel sl, LevelChunk c, NaturalSpawner.SpawnState state, boolean friendly, boolean hostile, boolean persistent, CallbackInfo ci) {
        if (!persistent && (double)sl.random.nextFloat() < SRAConfig.COMMON.skipSpawnChance.get()) {
            ci.cancel();
        } else {
            final int dist = SRAConfig.COMMON.maxSpawnRange.get();
            if (dist <= 100) {
                final BlockPos middle = c.getPos().getMiddleBlockPosition(64);
                for(ServerPlayer e : sl.getPlayers(EntitySelector.NO_SPECTATORS)) {
                    final double horDist = e.position().subtract(middle.getCenter()).horizontalDistance();
                    if (horDist < (double)(dist + 8)) {
                        return;
                    }
                }

                ci.cancel();
            }
        }
    }
}
