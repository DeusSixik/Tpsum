package dev.sixik.tpsum.mixin.entity_logic.goals;

import dev.sixik.tpsum.utils.TLevelUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(AvoidEntityGoal.class)
public class MixinAvoidEntityGoal$optimization_canUse_operations<T extends LivingEntity> {

    @Shadow
    @Nullable
    protected T toAvoid;

    @Shadow
    @Final
    protected PathfinderMob mob;

    @Shadow
    @Final
    protected float maxDist;

    @Shadow
    @Nullable
    protected Path path;

    @Shadow
    @Final
    protected PathNavigation pathNav;

    @Shadow
    @Final
    protected Class<T> avoidClass;

    @Shadow
    @Final
    private TargetingConditions avoidEntityTargeting;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    public void bts$canUse(CallbackInfoReturnable<Boolean> cir) {
        final AABB searchBox = this.mob.getBoundingBox().inflate(this.maxDist, 3.0D, this.maxDist);
        this.toAvoid = bts$findNearestEntityOptimized(
                this.mob.level(),
                this.avoidClass,
                this.avoidEntityTargeting,
                this.mob,
                searchBox
        );

        if(this.toAvoid == null) {
            cir.setReturnValue(false);
            return;
        }

        final Vec3 posAway = DefaultRandomPos.getPosAway(this.mob, 16, 7, this.toAvoid.position());
        if (posAway == null || this.toAvoid.distanceToSqr(posAway.x, posAway.y, posAway.z) < this.toAvoid.distanceToSqr(this.mob)) {
            cir.setReturnValue(false);
            return;
        }

        this.path = this.pathNav.createPath(posAway.x, posAway.y, posAway.z, 0);
        cir.setReturnValue(this.path != null);
    }

    @Unique
    private T bts$findNearestEntityOptimized(
            final Level level,
            final Class<T> entityClass,
            final TargetingConditions targetConditions,
            final LivingEntity getter,
            final AABB zone
    ) {
        /*
            If the old target is still alive and close enough, we continue to be afraid of it without scanning the list.
         */
        if (this.toAvoid != null && this.toAvoid.isAlive() && this.mob.distanceToSqr(this.toAvoid) < (maxDist * maxDist)) {
            return this.toAvoid;
        }

        /*
            Otherwise, we perform the search using our optimized method.
         */
        return TLevelUtils.findNearestEntityOptimized(level, entityClass, targetConditions, getter, zone);
    }
}
