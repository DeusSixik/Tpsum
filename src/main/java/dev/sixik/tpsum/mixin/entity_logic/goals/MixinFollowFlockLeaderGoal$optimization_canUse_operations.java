package dev.sixik.tpsum.mixin.entity_logic.goals;

import dev.sixik.tpsum.entity.FlockSearch;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.FollowFlockLeaderGoal;
import net.minecraft.world.entity.animal.AbstractSchoolingFish;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FollowFlockLeaderGoal.class)
public abstract class MixinFollowFlockLeaderGoal$optimization_canUse_operations {

    @Shadow
    @Final
    private AbstractSchoolingFish mob;

    @Shadow
    private int nextStartTick;

    /**
     * @author Sixik
     * @reason We use the search container to reduce allocations and provide quick access to data.
     */
    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    public void canUse(CallbackInfoReturnable<Boolean> cir) {
        if (this.mob.hasFollowers()) {
            cir.setReturnValue(false);
            return;
        }
        if (this.mob.isFollower()) {
            cir.setReturnValue(true);
            return;
        }

        if (this.nextStartTick > 0) {
            --this.nextStartTick;
            cir.setReturnValue(false);
            return;
        }

        this.nextStartTick = this.nextStartTick(this.mob);

        /*
            Using search container
         */
        final FlockSearch search = FlockSearch.POOL.get().init(this.mob);
        final AABB box = this.mob.getBoundingBox().inflate(8.0, 8.0, 8.0);

        final EntityTypeTest<Entity, AbstractSchoolingFish> typeTest = EntityTypeTest.forClass(AbstractSchoolingFish.class);
        this.mob.level().getEntities().get(
                typeTest,
                box,
                search
        );

        if (search.leader != null)
             search.leader.addFollowers(search.followers.stream());
        else this.mob.addFollowers(search.followers.stream());

        cir.setReturnValue(this.mob.isFollower());
    }

    /**
     * @author Sixik
     * @reason Instead of 200..219 we make 100..400 ticks.
     * And at the same time we smear them into several ticks.
     */
    @Overwrite
    protected int nextStartTick(AbstractSchoolingFish fish) {
        return 20 + fish.getRandom().nextInt(200);
    }
}
