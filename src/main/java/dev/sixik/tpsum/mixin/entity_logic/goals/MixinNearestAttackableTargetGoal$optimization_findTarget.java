package dev.sixik.tpsum.mixin.entity_logic.goals;

import dev.sixik.tpsum.entity.NearestTargetSearch;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NearestAttackableTargetGoal.class)
public abstract class MixinNearestAttackableTargetGoal$optimization_findTarget<T extends LivingEntity> extends TargetGoal {

    @Shadow @Final protected Class<T> targetType;
    @Shadow protected TargetingConditions targetConditions;
    @Shadow protected LivingEntity target;

    @Shadow
    protected abstract AABB getTargetSearchArea(double pTargetDistance);

    private MixinNearestAttackableTargetGoal$optimization_findTarget(Mob pMob, boolean pMustSee) {
        super(pMob, pMustSee);
    }

    /**
     * @author Sixik
     * @reason We use the search container to reduce allocations and provide quick access to data.
     */
    @Inject(method = "findTarget", at = @At("HEAD"), cancellable = true)
    protected void findTarget(CallbackInfo ci) {
        ci.cancel();

        /*
            Optimization for players: we use the built-in quick search for players at the level
         */
        if (this.targetType == Player.class || this.targetType == ServerPlayer.class) {
            this.target = this.mob.level().getNearestPlayer(this.targetConditions, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
            return;
        }

        @SuppressWarnings("unchecked")
        final NearestTargetSearch<T> search = ((NearestTargetSearch<T>) NearestTargetSearch.POOL.get()).init(this.mob, this.targetConditions);

        this.mob.level().getEntities().get(
                EntityTypeTest.forClass(this.targetType),
                getTargetSearchArea(getFollowDistance()),
                search
        );

        this.target = search.getResult();
    }
}
