package dev.sixik.tpsum.mixin.entity_spawn;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LocalMobCapCalculator;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.PotentialCalculator;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NaturalSpawner.SpawnState.class)
public class MixinSpawnState$optimize_operation {

    @Shadow
    @Final
    private int spawnableChunkCount;

    @Shadow
    @Final
    private Object2IntOpenHashMap<MobCategory> mobCategoryCounts;
    @Shadow
    @Final
    private LocalMobCapCalculator localMobCapCalculator;
    @Unique
    private int[] bts$categoryCaps;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void bts$init(
            int i,
            Object2IntOpenHashMap<MobCategory> object2IntOpenHashMap,
            PotentialCalculator potentialCalculator,
            LocalMobCapCalculator localMobCapCalculator,
            CallbackInfo ci
    ) {

        /*
            Precalculate Mob Category caps
         */
        final MobCategory[] values = MobCategory.values();
        this.bts$categoryCaps = new int[values.length];
        for (final MobCategory c : values) {
            this.bts$categoryCaps[c.ordinal()] = (c == MobCategory.MISC)
                    ? 0
                    : (c.getMaxInstancesPerChunk() * spawnableChunkCount) / NaturalSpawner.MAGIC_NUMBER;
        }
    }

    /**
     * @author Sixik
     * @reason Using cached Mob Category caps
     */
    @Overwrite
    public boolean canSpawnForCategory(MobCategory mobCategory, ChunkPos chunkPos) {
        final int cap = this.bts$categoryCaps[mobCategory.ordinal()];
        if (cap <= 0) return false;

        if (this.mobCategoryCounts.getInt(mobCategory) >= cap) {
            return false;
        }
        return this.localMobCapCalculator.canSpawn(mobCategory, chunkPos);
    }
}
