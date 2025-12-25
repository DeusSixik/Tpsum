package dev.sixik.tpsum.mixin.compat.variants_and_ventures;

import com.faboslav.variantsandventures.common.events.entity.EntitySpawnEvent;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.sixik.tpsum.level.CustomNaturalSpawner;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CustomNaturalSpawner.class)
public class VariantsandventuresCustomNaturalSpawnerMixin {

    @WrapOperation(
            method = {"spawnCategoryForPosition"},
            at = {@At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/NaturalSpawner;isValidPositionForMob(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Mob;D)Z"
            )}
    )
    private static boolean variantsandventures$onEntitySpawn(ServerLevel serverWorld, Mob mob, double d, Operation<Boolean> operation) {
        return !EntitySpawnEvent.EVENT.invoke(new EntitySpawnEvent(mob, serverWorld, mob.isBaby(), MobSpawnType.NATURAL)) && operation.call(serverWorld, mob, d);
    }
}
