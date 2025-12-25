package dev.sixik.tpsum.entity;

import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.jetbrains.annotations.NotNull;

public class NearestTargetSearch<T extends LivingEntity> implements AbortableIterationConsumer<T> {
    public static final ThreadLocal<NearestTargetSearch<?>> POOL = ThreadLocal.withInitial(NearestTargetSearch::new);

    private Mob getter;
    private TargetingConditions conditions;
    private T bestEntity;
    private double bestDistSqr;

    public NearestTargetSearch<T> init(Mob getter, TargetingConditions conditions) {
        this.getter = getter;
        this.conditions = conditions;
        this.bestEntity = null;
        this.bestDistSqr = Double.MAX_VALUE;
        return this;
    }

    @Override
    public @NotNull Continuation accept(T entity) {
        double distSqr = entity.distanceToSqr(getter.getX(), getter.getEyeY(), getter.getZ());

        /*
            If this entity is ALREADY further away than the one we found earlier, we don't even check it.
         */
        if (distSqr >= bestDistSqr) {
            return Continuation.CONTINUE;
        }

        /*
            We perform heavy checks (including Raycast/visibility) only if the distance is better.
         */
        if (conditions.test(getter, entity)) {
            bestDistSqr = distSqr;
            bestEntity = entity;
        }

        return Continuation.CONTINUE;
    }

    public T getResult() { return bestEntity; }
}
