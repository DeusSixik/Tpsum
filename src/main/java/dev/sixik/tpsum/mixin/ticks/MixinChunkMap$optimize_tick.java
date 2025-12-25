package dev.sixik.tpsum.mixin.ticks;

import com.mojang.datafixers.DataFixer;
import dev.sixik.tpsum.math.Vec3iSetter;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Mixin(ChunkMap.class)
public abstract class MixinChunkMap$optimize_tick extends ChunkStorage implements ChunkHolder.PlayerProvider {

    @Shadow
    @Final
    ServerLevel level;
    @Shadow
    @Final
    private Int2ObjectMap<ChunkMap.TrackedEntity> entityMap;
    @Shadow
    @Final
    private ChunkMap.DistanceManager distanceManager;
    @Unique
    private final List<ServerPlayer> bts$movingPlayers = new ObjectArrayList<>();

    private MixinChunkMap$optimize_tick(Path pRegionFolder, DataFixer pFixerUpper, boolean pSync) {
        super(pRegionFolder, pFixerUpper, pSync);
    }

    @Inject(method = "tick()V", at = @At("HEAD"), cancellable = true)
    private void bts$optimizedTick(CallbackInfo ci) {
        ci.cancel();

        bts$movingPlayers.clear();
        final List<ServerPlayer> allPlayers = this.level.players();

        final ObjectIterator<ChunkMap.TrackedEntity> iterator = this.entityMap.values().iterator();

        while (iterator.hasNext()) {
            final ChunkMap.TrackedEntity trackedEntity = iterator.next();
            final Entity entity = trackedEntity.entity;

            /*
                Instead of SectionPos.of(), we use primitive coordinate shifting (saving allocation).
             */
            final int currentX = entity.getBlockX() >> 4;
            final int currentY = entity.getBlockY() >> 4;
            final int currentZ = entity.getBlockZ() >> 4;

            final SectionPos lastPos = trackedEntity.lastSectionPos;
            final boolean hasMoved = lastPos.x() != currentX || lastPos.y() != currentY || lastPos.z() != currentZ;

            if (hasMoved) {
                trackedEntity.updatePlayers(allPlayers);
                if (entity instanceof ServerPlayer) {
                    bts$movingPlayers.add((ServerPlayer) entity);
                }

                /*
                    Updating a position without creating a new object
                 */
                ((Vec3iSetter)trackedEntity.lastSectionPos).bts$set(currentX, currentY, currentZ);
            }

            /*
                 Quick check of the ticking range
             */
            if (hasMoved || this.distanceManager.inEntityTickingRange(SectionPos.asLong(currentX, currentY, currentZ))) {
                trackedEntity.serverEntity.sendChanges();
            }
        }

        /*
            We only update those who moved using the reusable list
         */
        if (!bts$movingPlayers.isEmpty()) {
            final ObjectIterator<ChunkMap.TrackedEntity> it2 = this.entityMap.values().iterator();
            while (it2.hasNext())
                it2.next().updatePlayers(bts$movingPlayers);
        }
    }
}
