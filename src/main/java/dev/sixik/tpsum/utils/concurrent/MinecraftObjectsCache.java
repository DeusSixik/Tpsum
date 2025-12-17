package dev.sixik.tpsum.utils.concurrent;

import net.minecraft.core.Holder;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class MinecraftObjectsCache {

    public static final MobSpawn MOB_SPAWN = new MobSpawn();

    public static class MobSpawn {
        protected final Map<MobCacheKey, WeightedRandomList<MobSpawnSettings.SpawnerData>> mobCache =
                new ConcurrentHashMap<>();

        protected MobSpawn() {

        }

        public WeightedRandomList<MobSpawnSettings.SpawnerData> getOrCreate(Holder<Biome> biome, MobCategory category, @Nullable Structure structure,
                                                                            Function<MobCacheKey, WeightedRandomList<MobSpawnSettings.SpawnerData>> function) {
            return mobCache.computeIfAbsent(new MinecraftObjectsCache.MobCacheKey(biome, category, structure), function);
        }
    }

    public record MobCacheKey(Holder<Biome> biome, MobCategory category, @Nullable Structure structure) {}


}
