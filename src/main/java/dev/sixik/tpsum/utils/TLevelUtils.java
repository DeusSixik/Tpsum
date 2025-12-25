package dev.sixik.tpsum.utils;

import com.google.common.collect.Lists;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class TLevelUtils {

    public static <T extends Entity> List<T> getAllEntitiesOfClass(
            final Level level,
            final Class<T> entityClass,
            final AABB zone
    ) {
        return getAllEntities(level, EntityTypeTest.forClass(entityClass), zone);
    }

    public static <T extends Entity> List<T> getAllEntities(
            final Level level,
            final EntityTypeTest<Entity, T> typeTest,
            final AABB zone
    ) {
        final List<T> list = Lists.newArrayList();
        getAllEntities(level, typeTest, zone, list);
        return list;
    }

    public static <T extends Entity> void getAllEntities(
            final Level level,
            final EntityTypeTest<Entity, T> typeTest,
            final AABB zone,
            final List<? super T> outList
    ) {
        getAllEntities(level, typeTest, zone, outList, Integer.MAX_VALUE);
    }

    public static <T extends Entity> void getAllEntities(
            final Level level,
            final EntityTypeTest<Entity, T> typeTest,
            final AABB zone,
            final List<? super T> outList,
            final int entityCount
    ) {
        level.getProfiler().incrementCounter("getEntities");
        level.getEntities().get(typeTest, zone, (entity) -> {
            outList.add(entity);
            if (outList.size() >= entityCount) {
                return AbortableIterationConsumer.Continuation.ABORT;
            }

            return AbortableIterationConsumer.Continuation.CONTINUE;
        });
        for (final net.minecraftforge.entity.PartEntity<?> p : level.getPartEntities()) {
            final T t = typeTest.tryCast(p);
            if (t != null && t.getBoundingBox().intersects(zone)) {
                outList.add(t);
                if (outList.size() >= entityCount) {
                    break;
                }
            }
        }
    }

    public static <T extends LivingEntity> T findNearestEntityOptimized(
            final Level level,
            final Class<T> entityClass,
            final TargetingConditions targetConditions,
            final LivingEntity getter,
            final AABB zone
    ) {
        final NearestResult<T> result = new NearestResult<>();
        final double centerX = getter.getX();
        final double centerY = getter.getY();
        final double centerZ = getter.getZ();

        /*
          Iterating over entities directly through the internal mechanism of the level
         */
        level.getEntities().get(EntityTypeTest.forClass(entityClass), zone, (entity) -> {

            /*
                First, a cheap distance check, then a heavy predicate
             */
            final double distSqr = entity.distanceToSqr(centerX, centerY, centerZ);

            if (distSqr < result.bestDistSqr) {

                /*
                    If the distance is suitable, we check the conditions (visibility, team, etc.)
                 */
                if (targetConditions.test(getter, entity)) {
                    result.bestDistSqr = distSqr;
                    result.entity = entity;
                }
            }
            return AbortableIterationConsumer.Continuation.CONTINUE;
        });

        return result.entity;
    }

    private static class NearestResult<T> {
        T entity = null;
        double bestDistSqr = Double.MAX_VALUE;
    }
}
