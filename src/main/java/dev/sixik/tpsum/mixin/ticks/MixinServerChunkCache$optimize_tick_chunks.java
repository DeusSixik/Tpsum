package dev.sixik.tpsum.mixin.ticks;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.*;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Arrays;

@Mixin(ServerChunkCache.class)
public abstract class MixinServerChunkCache$optimize_tick_chunks extends ChunkSource {

    @Shadow
    @Final
    public ChunkMap chunkMap;
    @Shadow
    @Final
    private DistanceManager distanceManager;

    @Shadow
    @Final
    public ServerLevel level;
    @Shadow
    private boolean spawnEnemies;
    @Shadow
    private boolean spawnFriendlies;
    @Unique
    private LevelChunk[] bts$reusableChunks = new LevelChunk[256];
    @Unique
    private ChunkHolder[] bts$reusableHolders = new ChunkHolder[256];


    @Inject(method = "tickChunks", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Lists;newArrayListWithCapacity(I)Ljava/util/ArrayList;"), cancellable = true)
    private void bts$optimizedTickChunks(CallbackInfo ci, @Local(ordinal = 0) ProfilerFiller profilerfiller, @Local(ordinal = 0) long inhabitedDelta, @Local(ordinal = 0) int randomTickSpeed, @Local(ordinal = 1) boolean is400thTick, @Local(ordinal = 0) NaturalSpawner.SpawnState spawnState) {
        ci.cancel();

        int count = 0;
        final Iterable<ChunkHolder> holders = this.chunkMap.getChunks();

        /*
            Secure data collection with capacity verification
         */
        for (final ChunkHolder holder : holders) {
            final LevelChunk chunk = holder.getTickingChunk();
            if (chunk != null) {

                /*
                    Checking the capacity before recording
                 */
                this.bts$ensureCapacity(count + 1);
                bts$reusableChunks[count] = chunk;
                bts$reusableHolders[count] = holder;
                count++;
            }
        }

        profilerfiller.popPush("spawnAndTick");
        final boolean canSpawn = this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING);
        final boolean canSpawnTypes = this.spawnEnemies || this.spawnFriendlies;

        if (count != 0) {

            /*
                Fisher-Yates Shuffle
             */
            final RandomSource random = this.level.random;
            for (int i = count - 1; i > 0; i--) {
                final int j = random.nextInt(i + 1);

                LevelChunk tempChunk = bts$reusableChunks[i];
                bts$reusableChunks[i] = bts$reusableChunks[j];
                bts$reusableChunks[j] = tempChunk;

                ChunkHolder tempHolder = bts$reusableHolders[i];
                bts$reusableHolders[i] = bts$reusableHolders[j];
                bts$reusableHolders[j] = tempHolder;
            }

            for (int i = 0; i < count; i++) {
                final LevelChunk chunk = bts$reusableChunks[i];
                final ChunkPos pos = chunk.getPos();
                final long posLong = pos.toLong();

                if ((this.level.isNaturalSpawningAllowed(pos) && this.chunkMap.anyPlayerCloseEnoughForSpawning(pos))
                        || this.distanceManager.shouldForceTicks(posLong)) {

                    chunk.incrementInhabitedTime(inhabitedDelta);

                    if (canSpawn && canSpawnTypes && this.level.getWorldBorder().isWithinBounds(pos)) {
                        NaturalSpawner.spawnForChunk(
                                this.level,
                                chunk,
                                spawnState,
                                this.spawnFriendlies,
                                this.spawnEnemies,
                                is400thTick
                        );
                    }

                    if (this.level.shouldTickBlocksAt(posLong)) {
                        this.level.tickChunk(chunk, randomTickSpeed);
                    }
                }
            }
        }

        profilerfiller.popPush("customSpawners");
        if (canSpawn) this.level.tickCustomSpawners(this.spawnEnemies, this.spawnFriendlies);

        /*
            Clear temp data
         */
        profilerfiller.popPush("broadcast");
        for (int i = 0; i < count; i++) {
            bts$reusableHolders[i].broadcastChanges(bts$reusableChunks[i]);
            bts$reusableChunks[i] = null;
            bts$reusableHolders[i] = null;
        }

        profilerfiller.pop();
        profilerfiller.pop();
        this.chunkMap.tick();
    }

    @Unique
    private void bts$ensureCapacity(int minCapacity) {
        if(minCapacity <= bts$reusableChunks.length) return;

        int newSize = bts$reusableChunks.length << 1;
        if (newSize < minCapacity) newSize = minCapacity;

        bts$reusableChunks = Arrays.copyOf(bts$reusableChunks, newSize);
        bts$reusableHolders = Arrays.copyOf(bts$reusableHolders, newSize);
    }
}
