package dev.sixik.tpsum.mixin.entity_spawn;

import dev.sixik.tpsum.level.CustomNaturalSpawner;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.LocalMobCapCalculator;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(NaturalSpawner.class)
public class MixinNaturalSpawner$redirect_to_optimized_methods {

    /**
     * @author Sixik
     * @reason Change to my spawner
     */
    @Overwrite
    public static NaturalSpawner.SpawnState createState(int p_186525_, Iterable<Entity> p_186526_, NaturalSpawner.ChunkGetter p_186527_, LocalMobCapCalculator p_186528_) {
        return CustomNaturalSpawner.createState(p_186525_, p_186526_, p_186527_, p_186528_);
    }

    /**
     * @author Sixik
     * @reason Change to my spawner
     */
    @Overwrite
    public static void spawnCategoryForPosition(MobCategory p_47039_, ServerLevel p_47040_, ChunkAccess p_47041_, BlockPos p_47042_, NaturalSpawner.SpawnPredicate p_47043_, NaturalSpawner.AfterSpawnCallback p_47044_) {
        CustomNaturalSpawner.spawnCategoryForPosition(p_47039_, p_47040_, p_47041_, p_47042_, p_47043_, p_47044_);
    }

    /**
     * @author Sixik
     * @reason Change to my spawner
     */
    @Overwrite
    public static boolean isValidSpawnPostitionForType(ServerLevel p_220422_, MobCategory p_220423_, StructureManager p_220424_, ChunkGenerator p_220425_, MobSpawnSettings.SpawnerData p_220426_, BlockPos.MutableBlockPos p_220427_, double p_220428_) {
        return CustomNaturalSpawner.fastIsValidSpawnPostitionForType(p_220422_, p_220423_, p_220424_, p_220425_, p_220426_, p_220427_, p_220428_);
    }
}
