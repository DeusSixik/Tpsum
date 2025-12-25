package dev.sixik.tpsum.entity;

import net.minecraft.world.entity.ai.goal.WrappedGoal;

public interface GoalSelectorExtension {

    boolean bts$goalContainsAnyFlags(final WrappedGoal goal, final int disabledFlagsMask);
}
