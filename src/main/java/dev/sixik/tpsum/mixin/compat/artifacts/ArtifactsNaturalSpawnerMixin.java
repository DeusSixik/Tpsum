package dev.sixik.tpsum.mixin.compat.artifacts;

import artifacts.Artifacts;
import artifacts.platform.PlatformServices;
import artifacts.registry.ModLootTables;
import dev.sixik.tpsum.level.CustomNaturalSpawner;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(CustomNaturalSpawner.class)
public class ArtifactsNaturalSpawnerMixin {

    @Inject(
            method = {"spawnCategoryForPosition(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;)V"},
            at = {@At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)V",
                    shift = At.Shift.AFTER
            )}, locals = LocalCapture.CAPTURE_FAILHARD
    )
    private static void spawnCategoryForPosition(
            MobCategory mobCategory,
            ServerLevel serverLevel,
            ChunkAccess chunkAccess,
            BlockPos blockPos,
            NaturalSpawner.SpawnPredicate spawnPredicate,
            NaturalSpawner.AfterSpawnCallback afterSpawnCallback,
            CallbackInfo ci,
            StructureManager structureManager, ChunkGenerator chunkGenerator, int yPos, BlockState state, BlockPos.MutableBlockPos mutableBlockPos, int totalSpawned, RandomSource random, int k, int x, int z, MobSpawnSettings.SpawnerData spawnerData, SpawnGroupData spawnGroupData, int clusterSize, int spawnedInGroup, Player nearestPlayer, int q, double d, double e, double distSqr, Mob mob) {
        if (ModLootTables.ENTITY_EQUIPMENT.containsKey(mob.getType())) {
            if (mob.level() instanceof ServerLevel level) {
                final ResourceLocation id = ModLootTables.ENTITY_EQUIPMENT.get(mob.getType());
                final LootTable loottable = level.getServer().getLootData().getLootTable(id);
                final LootParams.Builder params = new LootParams.Builder(level);
                final LootParams lootparams = params.create(LootContextParamSets.EMPTY);
                loottable.getRandomItems(lootparams, mob.getLootTableSeed(), (stack) -> {
                    if (!PlatformServices.platformHelper.tryEquipInFirstSlot(mob, stack)) {
                        Artifacts.LOGGER.warn("Could not equip item '{}' on spawned entity '{}'", stack, mob);
                    }
                });
            }
        }

    }
}
