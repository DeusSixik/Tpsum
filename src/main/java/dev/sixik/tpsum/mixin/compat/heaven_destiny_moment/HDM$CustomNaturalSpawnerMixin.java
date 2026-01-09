package dev.sixik.tpsum.mixin.compat.heaven_destiny_moment;

import com.xiaohunao.heaven_destiny_moment.HeavenDestinyMoment;
import com.xiaohunao.heaven_destiny_moment.common.mixed.SpawnerDataMomentMixed;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstanceManager;
import dev.sixik.tpsum.level.CustomNaturalSpawner;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(CustomNaturalSpawner.class)
public class HDM$CustomNaturalSpawnerMixin {

    @Inject(
            method = {"spawnCategoryForPosition(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;)V"},
            at = {@At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)V"
            )},
            locals = LocalCapture.CAPTURE_FAILSOFT,
            cancellable = true
    )
    private static void spawnCategoryForPosition(
            final MobCategory mobCategory,
            final ServerLevel serverLevel,
            final ChunkAccess chunkAccess,
            final BlockPos pos,
            final NaturalSpawner.SpawnPredicate spawnPredicate,
            final NaturalSpawner.AfterSpawnCallback afterSpawnCallback,
            final CallbackInfo ci,
            final StructureManager structureManager,
            final ChunkGenerator chunkGenerator,
            final int yPos,
            final BlockState state,
            final BlockPos.MutableBlockPos mutableBlockPos,
            final int totalSpawned,
            final RandomSource random,
            final int k, final int x, final int z,
            final MobSpawnSettings.SpawnerData spawnerData,
            final SpawnGroupData spawnGroupData,
            final int clusterSize, final int spawnedInGroup,
            final Player nearestPlayer,
            final int q, final double d, final double e, final double distSqr,
            final Mob mob
    ) {
        if (spawnerData instanceof SpawnerDataMomentMixed ownSpawnerData) {
            if (ownSpawnerData.heaven_destiny_moment$getMoment() != HeavenDestinyMoment.EMITY_MOMENT) {
                final MomentInstanceManager momentInstanceManager = MomentInstanceManager.of(serverLevel.getLevel());

                for(MomentInstance instance : momentInstanceManager.getMomentInstances(ownSpawnerData.heaven_destiny_moment$getMoment())) {
                    if (instance.canSpawnEntity(serverLevel, mob, pos)) {
                        instance.addEnemy(mob);
                    } else {
                        ci.cancel();
                    }
                }
            }
        }
    }


}
