package dev.sixik.tpsum.mixin.compat.spawn_range_adjuster;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.sixik.tpsum.level.CustomNaturalSpawner;
import dev.xkmc.spawn_range_adjuster.init.SRAConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = CustomNaturalSpawner.class)
public class SRA$CustomNaturalSpawnerMixin {

    @WrapOperation(
            method = {"spawnCategoryForPosition(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;)V"},
            at = {@At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;getNearestPlayer(DDDDZ)Lnet/minecraft/world/entity/player/Player;"
            )}
    )
    private static Player spawnRangeAdjuster$getNearestPlayer(ServerLevel sl, double x, double y, double z, double r, boolean flag, Operation<Player> original) {
        final Player ans = original.call(sl, x, y, z, r, flag);
        if (ans == null) return null;

        final int ydiff = SRAConfig.COMMON.maxSpawnYDiff.get();
        return Math.abs(ans.position().y() - y) > ydiff ? null : ans;
    }
}
