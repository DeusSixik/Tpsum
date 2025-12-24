package dev.sixik.tpsum.mixin.compat.botania;

import dev.sixik.tpsum.level.CustomNaturalSpawner;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import vazkii.botania.common.block.flower.generating.NarslimmusBlockEntity;

@Mixin(CustomNaturalSpawner.class)
public class BotaniaCustomNaturalSpawnerMixin {

    @ModifyArg(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)V"
            ),
            method = {"spawnCategoryForPosition(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;)V"}
    )
    private static Entity onSpawned(Entity entity) {
        NarslimmusBlockEntity.onSpawn(entity);
        return entity;
    }


}
