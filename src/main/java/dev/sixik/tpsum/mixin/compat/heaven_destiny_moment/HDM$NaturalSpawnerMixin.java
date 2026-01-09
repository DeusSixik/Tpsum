package dev.sixik.tpsum.mixin.compat.heaven_destiny_moment;

import com.xiaohunao.heaven_destiny_moment.common.context.BiomeEntitySpawnSettings;
import com.xiaohunao.heaven_destiny_moment.common.context.EntitySpawnSettings;
import com.xiaohunao.heaven_destiny_moment.common.context.MobSpawnRule;
import com.xiaohunao.heaven_destiny_moment.common.context.MomentData;
import com.xiaohunao.heaven_destiny_moment.common.moment.Moment;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstanceManager;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(NaturalSpawner.class)
public class HDM$NaturalSpawnerMixin {

    @Inject(
            method = {"mobsAt"},
            at = {@At("RETURN")},
            cancellable = true
    )
    private static void mobsAt(ServerLevel serverLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, MobCategory mobCategory, BlockPos pos, Holder<Biome> biomeHolder, CallbackInfoReturnable<WeightedRandomList<MobSpawnSettings.SpawnerData>> cir) {
        final MomentInstanceManager momentInstanceManager = MomentInstanceManager.of(serverLevel);

        for(MomentInstance instance : momentInstanceManager.getMomentInstances()) {
            Optional.of(instance.getMoment()).filter((moment) -> moment.isInArea(serverLevel, pos)).flatMap(Moment::momentData).flatMap(MomentData::entitySpawnSettings).ifPresent((entitySpawnSettingsContext) -> {
                List<MobSpawnSettings.SpawnerData> unwrap = new ObjectArrayList<>((cir.getReturnValue()).unwrap());
                cir.setReturnValue(entitySpawnSettingsContext.adjustmentBiomeEntitySpawnSettings(instance.getMoment(), mobCategory, unwrap));
            });
        }

    }

    @Inject(
            method = {"getRoughBiome"},
            at = {@At("RETURN")},
            cancellable = true
    )
    private static void getRoughBiome(BlockPos pos, ChunkAccess chunk, CallbackInfoReturnable<Biome> cir) {
        final Level level = ((LevelChunk)chunk).getLevel();
        if (!level.isClientSide) {
            final MomentInstanceManager momentInstanceManager = MomentInstanceManager.of(level);
            final Biome.BiomeBuilder fakeBiome = (new Biome.BiomeBuilder()).hasPrecipitation(false).temperature(0.5F).downfall(0.5F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(1).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build());

            for(MomentInstance instance : momentInstanceManager.getMomentInstances()) {
                Optional.of(instance.getMoment()).filter((moment) -> moment.isInArea((ServerLevel)level, pos)).flatMap(Moment::momentData).flatMap(MomentData::entitySpawnSettings).ifPresent((entitySpawnSettingsContext) -> {
                    MobSpawnSettings mobSettings = cir.getReturnValue().getMobSettings();
                    entitySpawnSettingsContext.biomeEntitySpawnSettings().flatMap(BiomeEntitySpawnSettings::biomeMobSpawnSettings).ifPresent((mobSpawnSettings) -> {
                        final Map<MobCategory, WeightedRandomList<MobSpawnSettings.SpawnerData>> spawners = new Object2ObjectOpenHashMap<>(mobSettings.spawners);
                        final Map<MobCategory, WeightedRandomList<MobSpawnSettings.SpawnerData>> newSpawners = new Object2ObjectOpenHashMap<>();

                        for(Map.Entry<MobCategory, WeightedRandomList<MobSpawnSettings.SpawnerData>> entry : spawners.entrySet()) {
                            final MobCategory mobCategory = entry.getKey();
                            final WeightedRandomList<MobSpawnSettings.SpawnerData> weightedRandomList = entry.getValue();
                            final List<MobSpawnSettings.SpawnerData> unwrap = new ObjectArrayList<>(weightedRandomList.unwrap());
                            newSpawners.put(mobCategory, entitySpawnSettingsContext.adjustmentBiomeEntitySpawnSettings(instance.getMoment(), mobCategory, unwrap));
                        }

                        final float oldCreatureProbability = mobSettings.getCreatureProbability();
                        final float newCreatureProbability = mobSpawnSettings.getCreatureProbability();
                        fakeBiome.mobSpawnSettings(new MobSpawnSettings(Math.max(oldCreatureProbability, newCreatureProbability), newSpawners, new Object2ObjectOpenHashMap<>(mobSettings.mobSpawnCosts)));
                    });
                    if (fakeBiome.mobSpawnSettings == null) {
                        fakeBiome.mobSpawnSettings(mobSettings);
                    }

                    fakeBiome.generationSettings(BiomeGenerationSettings.EMPTY);
                    cir.setReturnValue(fakeBiome.build());
                });
            }

        }
    }

    @Inject(
            method = {"isRightDistanceToPlayerAndSpawnPoint"},
            at = {@At("RETURN")},
            cancellable = true
    )
    private static void isRightDistanceToPlayerAndSpawnPoint(ServerLevel serverLevel, ChunkAccess chunk, BlockPos.MutableBlockPos pos, double distance, CallbackInfoReturnable<Boolean> cir) {
        final MomentInstanceManager momentInstanceManager = MomentInstanceManager.of(serverLevel);

        for(MomentInstance instance : momentInstanceManager.getMomentInstances()) {
            Optional<Boolean> var10000 = Optional.of(instance.getMoment()).filter((moment) -> moment.isInArea(serverLevel, pos)).flatMap(Moment::momentData).flatMap(MomentData::entitySpawnSettings).flatMap(EntitySpawnSettings::rule).flatMap(MobSpawnRule::ignoreDistance);
            var10000.ifPresent(cir::setReturnValue);
        }

    }
}
