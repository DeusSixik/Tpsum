package dev.sixik.tpsum.entity;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.entity.animal.AbstractSchoolingFish;
import org.jetbrains.annotations.NotNull;

public class FlockSearch implements AbortableIterationConsumer<AbstractSchoolingFish> {
    public static ThreadLocal<FlockSearch> POOL = ThreadLocal.withInitial(() -> new FlockSearch(null));

    public AbstractSchoolingFish mob;
    public AbstractSchoolingFish leader = null;
    public final ObjectArrayList<AbstractSchoolingFish> followers = new ObjectArrayList<>();

    public FlockSearch(AbstractSchoolingFish mob) {
        this.mob = mob;
    }

    public FlockSearch init(AbstractSchoolingFish mob) {
        this.mob = mob;
        this.leader = null;
        this.followers.clear();
        return this;
    }

    @Override
    public @NotNull Continuation accept(@NotNull AbstractSchoolingFish fish) {
        if (fish == mob) return Continuation.CONTINUE;

        /*
            If you have found a potential leader
         */
        if (leader == null && fish.canBeFollowed()) {
            leader = fish;
        }

        /*
            If a fish can become a follower (has no leader)
         */
        if (!fish.isFollower()) {
            followers.add(fish);
        }

        return Continuation.CONTINUE;
    }
}
