package dev.sixik.tpsum.mixin.entity_logic.goal_basic;

import dev.sixik.tpsum.entity.GoalBitsMaskSupport;
import dev.sixik.tpsum.entity.GoalFlagsBitsMaskGetter;
import dev.sixik.tpsum.entity.GoalSelectorExtension;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.EnumSet;

@Mixin(GoalSelector.class)
public class GoalSelectorMixin$optimize_tick_operations implements GoalSelectorExtension {

    @Unique
    private int bts$disabledFlags = 0;

    /**
     * @author Sixik
     * @reason Replacing the call to the original goalContainsAnyFlags with our version with bits operations
     */
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/goal/GoalSelector;goalContainsAnyFlags(Lnet/minecraft/world/entity/ai/goal/WrappedGoal;Ljava/util/EnumSet;)Z"))
    public boolean bts$tick(WrappedGoal goal, EnumSet<Goal.Flag> unused) {
        return bts$goalContainsAnyFlags(goal, bts$disabledFlags);
    }

    /**
     * @author Sixik
     * @reason Redirect to fast bits operations
     */
    @Overwrite
    public void disableControlFlag(Goal.Flag flag) {
        bts$disabledFlags |= ((GoalFlagsBitsMaskGetter)(Object)flag).bts$getBits();
    }

    /**
     * @author Sixik
     * @reason Redirect to fast bits operations
     */
    @Overwrite
    public void enableControlFlag(Goal.Flag flag) {
        bts$disabledFlags &= ~((GoalFlagsBitsMaskGetter)(Object)flag).bts$getBits();
    }

    @Override
    public boolean bts$goalContainsAnyFlags(final WrappedGoal goal, final int disabledFlagsBits) {
        return (((GoalBitsMaskSupport)goal).bts$getBits() & disabledFlagsBits) != 0;
    }
}
