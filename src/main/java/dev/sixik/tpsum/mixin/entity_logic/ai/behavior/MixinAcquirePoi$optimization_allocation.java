package dev.sixik.tpsum.mixin.entity_logic.ai.behavior;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.AcquirePoi;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.pathfinder.Path;
import org.apache.commons.lang3.mutable.MutableLong;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Optional;
import java.util.function.Predicate;

@Mixin(AcquirePoi.class)
public abstract class MixinAcquirePoi$optimization_allocation {

    /**
     * @author Sixik
     * @reason
     */
    @Overwrite
    public static BehaviorControl<PathfinderMob> create(
            Predicate<Holder<PoiType>> pAcquirablePois,
            MemoryModuleType<GlobalPos> pExistingAbsentMemory,
            MemoryModuleType<GlobalPos> pAcquiringMemory,
            boolean pOnlyIfAdult,
            Optional<Byte> pEntityEventId
    ) {
        final MutableLong nextTryAt = new MutableLong(0L);

        /*
            fastutil map: no Long boxing for keys
         */
        final Long2ObjectMap<AcquirePoi.JitteredLinearRetry> retryCache = new Long2ObjectOpenHashMap<>();

        /*
            Reuse per-behavior (like vanilla keeps shared state in the closure)
         */
        final ObjectArrayList<Pair<Holder<PoiType>, BlockPos>> foundPois = new ObjectArrayList<>(5);
        final ObjectOpenHashSet<BlockPos> targetPositions = new ObjectOpenHashSet<>(8);

        OneShot<PathfinderMob> oneshot = BehaviorBuilder.create(instance ->
                instance.group(instance.absent(pAcquiringMemory)).apply(instance, acquiringMemAcc ->
                        (level, mob, gameTime) -> {

                            if (pOnlyIfAdult && mob.isBaby()) return false;

                            /*
                                initial jitter
                             */
                            if (nextTryAt.getValue() == 0L) {
                                nextTryAt.setValue(gameTime + (long) level.random.nextInt(20));
                                return false;
                            }

                            if (gameTime < nextTryAt.getValue()) return false;

                            /*
                                vanilla: set next cooldown immediately after passing the gate
                             */
                            nextTryAt.setValue(gameTime + 20L + (long) level.getRandom().nextInt(20));

                            final PoiManager poi = level.getPoiManager();

                            /*
                                cleanup expired retries
                             */
                            retryCache.long2ObjectEntrySet().removeIf(e -> !e.getValue().isStillValid(gameTime));

                            /*
                                retry gate predicate
                             */
                            final Predicate<BlockPos> posPredicate = pos -> {
                                AcquirePoi.JitteredLinearRetry retry = retryCache.get(pos.asLong());
                                if (retry == null) return true;
                                if (!retry.shouldRetry(gameTime)) return false;
                                retry.markAttempt(gameTime);
                                return true;
                            };

                            /*
                                reuse collections
                             */
                            foundPois.clear();
                            targetPositions.clear();

                            /*
                                capture without boxing
                             */
                            final int[] maxRange = {1};
                            poi.findAllClosestFirstWithType(
                                            pAcquirablePois,
                                            posPredicate,
                                            mob.blockPosition(),
                                            48,
                                            PoiManager.Occupancy.HAS_SPACE
                                    )
                                    .limit(5L)
                                    .forEach(pair -> {
                                        foundPois.add(pair);
                                        BlockPos p = pair.getSecond();
                                        targetPositions.add(p);
                                        int vr = pair.getFirst().value().validRange();
                                        if (vr > maxRange[0]) maxRange[0] = vr;
                                    });

                            if (foundPois.isEmpty()) {
                                return true;
                            }

                            final Path path = mob.getNavigation().createPath(targetPositions, maxRange[0]);

                            if (path != null && path.canReach()) {
                                final BlockPos targetPos = path.getTarget();
                                poi.getType(targetPos).ifPresent(type -> {
                                    poi.take(pAcquirablePois, (t, p) -> p.equals(targetPos), targetPos, 1);
                                    acquiringMemAcc.set(GlobalPos.of(level.dimension(), targetPos));
                                    pEntityEventId.ifPresent(id -> level.broadcastEntityEvent(mob, id));
                                    retryCache.clear();
                                    DebugPackets.sendPoiTicketCountPacket(level, targetPos);
                                });
                            } else {
                                /*
                                    record failed attempts (primitive-long computeIfAbsent)
                                 */
                                for (int i = 0; i < foundPois.size(); i++) {
                                    final BlockPos p = foundPois.get(i).getSecond();
                                    final long key = p.asLong();
                                    retryCache.computeIfAbsent(key,
                                            k -> new AcquirePoi.JitteredLinearRetry(level.random, gameTime));
                                }
                            }

                            return true;
                        }
                )
        );

        return pAcquiringMemory == pExistingAbsentMemory
                ? oneshot
                : BehaviorBuilder.create(instance ->
                instance.group(instance.absent(pExistingAbsentMemory)).apply(instance, acc -> oneshot));
    }
}
