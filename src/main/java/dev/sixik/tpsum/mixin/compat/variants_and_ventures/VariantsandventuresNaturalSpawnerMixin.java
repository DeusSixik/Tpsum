package dev.sixik.tpsum.mixin.compat.variants_and_ventures;

import com.faboslav.variantsandventures.common.events.entity.EntitySpawnEvent;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(NaturalSpawner.class)
public class VariantsandventuresNaturalSpawnerMixin {

    @WrapOperation(
            method = {"spawnMobsForChunkGeneration"},
            at = {@At(
                    value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/event/EventHooks;checkSpawnPosition(Lnet/minecraft/world/entity/Mob;Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/entity/MobSpawnType;)Z"
            )}
    )
    private static boolean variantsandventures$onCheckEntitySpawn(Mob instance, ServerLevelAccessor worldAccess, MobSpawnType spawnReason, Operation<Boolean> operation) {
        return !EntitySpawnEvent.EVENT.invoke(new EntitySpawnEvent(instance, worldAccess, instance.isBaby(), spawnReason)) && operation.call(instance, worldAccess, spawnReason);
    }
}
