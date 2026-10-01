package dev.sixik.tpsum.mixin.compat.fowlplay;

import aqario.fowlplay.common.entity.FPMobCategory;
import dev.sixik.tpsum.level.CustomNaturalSpawner;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CustomNaturalSpawner.class)
public class FowlPlayCustomNaturalSpawnerMixin {

    @Redirect(
            method = {"spawnCategoryForPosition(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;)V"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/BlockPos;getY()I"
            )
    )
    private static int fowlplay$midairSpawning(BlockPos pos, MobCategory group, ServerLevel world) {
        return (group == FPMobCategory.birds() || group == FPMobCategory.ambientBirds()) && world.getRandom().nextFloat() < 0.01F && world.getLevelData().getGameTime() % 100L != 0L ? pos.getY() + world.getRandom().nextIntBetweenInclusive(32, 64) : pos.getY();
    }

}
