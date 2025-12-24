package dev.sixik.tpsum.level;

import dev.sixik.tpsum.mixin.blocks.BlockStateBaseAccessor;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.neoforged.neoforge.event.EventHooks;

import java.util.Optional;

public class CustomNaturalSpawner {

    public static NaturalSpawner.SpawnState createState(
            final int i,
            final Iterable<Entity> iterable,
            final NaturalSpawner.ChunkGetter chunkGetter,
            final LocalMobCapCalculator localMobCapCalculator
    ) {
        final Long2ObjectOpenHashMap<ObjectArrayList<Entity>> byChunk_cache = new Long2ObjectOpenHashMap<>();

        for (final Entity entity : iterable) {
            final MobCategory mobCategory = entity.getClassification(true);
            if (entity instanceof Mob mob && (mob.isPersistenceRequired() || mob.requiresCustomPersistence()) || mobCategory == MobCategory.MISC)
                continue;

            /*
            We can use this solution, but fighting for maximum efficiency, so I'm using the code below.

            byChunk_cache.computeIfAbsent(ChunkPos.asLong(entity.blockPosition()),
                    k -> new ObjectArrayList<>()).add(entity);
             */

            final long pos = ChunkPos.asLong(entity.blockPosition());
            ObjectArrayList<Entity> list = byChunk_cache.get(pos);
            if (list == null) {
                list = new ObjectArrayList<>();
                byChunk_cache.put(pos, list);
            }
            list.add(entity);
        }

        final PotentialCalculator potentialCalculator = new PotentialCalculator();
        final Object2IntOpenHashMap<MobCategory> mobCategories = new Object2IntOpenHashMap<MobCategory>();
        byChunk_cache.long2ObjectEntrySet().forEach(entry -> {
            final long chunkKey = entry.getLongKey();
            final ObjectArrayList<Entity> list = entry.getValue();

            chunkGetter.query(chunkKey, levelChunk -> {
                for (final Entity entity : list) {
                    final BlockPos pos = entity.blockPosition();
                    final EntityType<?> type = entity.getType();

                    final MobSpawnSettings.MobSpawnCost cost = getRoughBiome(pos, levelChunk)
                            .getMobSettings().getMobSpawnCost(type);

                    if (cost != null)
                        potentialCalculator.addCharge(pos, cost.charge());

                    if (entity instanceof Mob)
                        localMobCapCalculator.addMob(levelChunk.getPos(), type.getCategory());

                    mobCategories.addTo(type.getCategory(), 1);
                }
            });
        });

        return new NaturalSpawner.SpawnState(i, mobCategories, potentialCalculator, localMobCapCalculator);
    }

    public static Biome getRoughBiome(
            final BlockPos blockPos,
            final ChunkAccess chunkAccess
    ) {
        return chunkAccess.getNoiseBiome(blockPos.getX() >> 2, blockPos.getY() >> 2, blockPos.getZ() >> 2).value();
    }

    public static void spawnCategoryForPosition(
            final MobCategory mobCategory,
            final ServerLevel serverLevel,
            final ChunkAccess chunkAccess,
            final BlockPos blockPos,
            final NaturalSpawner.SpawnPredicate spawnPredicate,
            final NaturalSpawner.AfterSpawnCallback afterSpawnCallback) {

        final StructureManager structureManager = serverLevel.structureManager();
        final ChunkGenerator chunkGenerator = serverLevel.getChunkSource().getGenerator();
        final int yPos = blockPos.getY();
        final BlockState state = fastGetBlockState(chunkAccess, blockPos);

        if (tpsum$isRedstoneConductorFast(state)) return;

        final BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        int totalSpawned = 0;

        /*
           We take out random and take it once so that we can simply access it later
         */
        final RandomSource random = serverLevel.random;

        for (int k = 0; k < 3; ++k) {
            int x = blockPos.getX();
            int z = blockPos.getZ();
            MobSpawnSettings.SpawnerData spawnerData = null;
            SpawnGroupData spawnGroupData = null;
            int clusterSize = Mth.ceil(random.nextFloat() * 4.0F);
            int spawnedInGroup = 0;

            /*
             Removing the player search from the cluster cycle.
             In principle, it is enough for us to get the distance to the base coordinates.

             We could also do a check if the players are too far away, but this is not required yet.
             */
            final Player nearestPlayer = serverLevel.getNearestPlayer(x + 0.5, yPos, z + 0.5, -1.0, false);
            if (nearestPlayer == null) continue;

            for (int q = 0; q < clusterSize; ++q) {

                // Offsets
                x += random.nextInt(6) - random.nextInt(6);
                z += random.nextInt(6) - random.nextInt(6);
                mutableBlockPos.set(x, yPos, z);

                final double d = x + 0.5;
                final double e = z + 0.5;

                /*
                  Counting the distance to the player is much faster than constantly looking for a new player.
                  Of course, there may be deviations from the original logic, but they are not so critical.
                 */
                final double distSqr = nearestPlayer.distanceToSqr(d, yPos, e);

                if (isRightDistanceToPlayerAndSpawnPoint(serverLevel, chunkAccess, mutableBlockPos, distSqr)) {
                    if (spawnerData == null) {
                        Optional<MobSpawnSettings.SpawnerData> optional = fastGetRandomSpawnMobAt(serverLevel, structureManager, chunkGenerator, mobCategory, random, mutableBlockPos);
                        if (optional.isEmpty()) break;

                        spawnerData = optional.get();
                        clusterSize = spawnerData.minCount + random.nextInt(1 + spawnerData.maxCount - spawnerData.minCount);
                    }

                    if (fastIsValidSpawnPostitionForType(serverLevel, mobCategory, structureManager, chunkGenerator, spawnerData, mutableBlockPos, distSqr)
                            && spawnPredicate.test(spawnerData.type, mutableBlockPos, chunkAccess)) {

                        final Mob mob = NaturalSpawner.getMobForSpawn(serverLevel, spawnerData.type);
                        if (mob == null) return;

                        mob.moveTo(d, yPos, e, random.nextFloat() * 360.0F, 0.0F);

                        if (NaturalSpawner.isValidPositionForMob(serverLevel, mob, distSqr)) {
                            spawnGroupData = mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.NATURAL, spawnGroupData);

                            totalSpawned++;
                            spawnedInGroup++;
                            serverLevel.addFreshEntityWithPassengers(mob);
                            afterSpawnCallback.run(mob, chunkAccess);

                            if (totalSpawned >= EventHooks.getMaxSpawnClusterSize(mob)) return;
                            if (mob.isMaxGroupSizeReached(spawnedInGroup)) break;
                        }
                    }
                }
            }
        }
    }

    public static boolean tpsum$isRedstoneConductorFast(
            final BlockState state
    ) {
        final var cache = ((BlockStateBaseAccessor) state).tpsum$getCache();
        if(cache != null) return ((BlockStateConductorCacheGetter)(Object) cache).tpsum$isRedstoneConductor();
        return false;
    }

    public static boolean isRightDistanceToPlayerAndSpawnPoint(
            final ServerLevel serverLevel,
            final ChunkAccess chunkAccess,
            final BlockPos.MutableBlockPos pos,
            final double distance) {
        if (distance <= 576.0F) return false;

        /*
            Micro optimization. We get a position once
         */
        final int localX = pos.getX();
        final int localZ = pos.getZ();

        /*
            Small allocations and temporary Vec3 elements have been removed.
         */
        if (serverLevel.getSharedSpawnPos().distToCenterSqr
                (localX + 0.5, pos.getY(), localZ + 0.5) < 576)
            return false;

        /*
            Fast equals
         */
        final ChunkPos chunkPos = chunkAccess.getPos();
        return (chunkPos.x == localX >> 4 && chunkPos.z == localZ >> 4) || serverLevel.isNaturalSpawningAllowed(pos);
    }

    private static BlockState fastGetBlockState(ChunkAccess ca, BlockPos pos) {
        if (!(ca instanceof LevelChunk lc)) return ca.getBlockState(pos);

        final int x = pos.getX();
        final int z = pos.getZ();

        final ChunkPos cp = lc.getPos();
        if ((x >> 4) != cp.x || (z >> 4) != cp.z) {
            return lc.getBlockState(pos);
        }

        final int y = pos.getY();
        final LevelChunkSection sec = lc.getSections()[lc.getSectionIndex(y)];
        return sec.getBlockState(x & 15, y & 15, z & 15);
    }

    /**
     * A revised version of the method for passing checks from light to heavy
     */
    public static boolean fastIsValidSpawnPostitionForType(
            ServerLevel level,
            MobCategory mobCategory,
            StructureManager structureManager,
            ChunkGenerator chunkGenerator,
            MobSpawnSettings.SpawnerData spawnerData,
            BlockPos.MutableBlockPos pos,
            double distSqr
    ) {
        final EntityType<?> type = spawnerData.type;

        if (type.getCategory() == MobCategory.MISC) return false;

        final int dd = type.getCategory().getDespawnDistance();
        final double ddSqr = (double) dd * (double) dd;
        if (!type.canSpawnFarFromPlayer() && distSqr > ddSqr) return false;

        if (!type.canSummon()) return false;

        if (!NaturalSpawner.canSpawnMobAt(level, structureManager, chunkGenerator, mobCategory, spawnerData, pos))
            return false;

        if (!SpawnPlacements.isSpawnPositionOk(type, level, pos)) return false;
        if (!SpawnPlacements.checkSpawnRules(type, level, MobSpawnType.NATURAL, pos, level.random)) return false;

        return level.noCollision(type.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5));
    }

    /**
     * The fastest way to get a chunk, but with a loss of accuracy at the boundaries of biomes.
     * Is it very critical?
     */
    public static Optional<MobSpawnSettings.SpawnerData> fastGetRandomSpawnMobAt(
            final ServerLevel serverLevel,
            final StructureManager structureManager,
            final ChunkGenerator chunkGenerator,
            final MobCategory mobCategory,
            final RandomSource randomSource,
            final BlockPos blockPos
    ) {
        final Holder<Biome> holder = serverLevel.getNoiseBiome(blockPos.getX() >> 2, blockPos.getY() >> 2, blockPos.getZ() >> 2);
        return mobCategory == MobCategory.WATER_AMBIENT && holder.is(BiomeTags.REDUCED_WATER_AMBIENT_SPAWNS) && randomSource.nextFloat() < 0.98F
                ? Optional.empty()
                : NaturalSpawner.mobsAt(serverLevel, structureManager, chunkGenerator, mobCategory, blockPos, holder).getRandom(randomSource);
    }
}
