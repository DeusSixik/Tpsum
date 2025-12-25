package dev.sixik.tpsum.entity;

import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class GoalFlagsUtils {

    public static EnumSet<Goal.Flag> buildByBits(final int bits) {
        final EnumSet<Goal.Flag> flags = EnumSet.noneOf(Goal.Flag.class);
        if (bits == 0) return flags;

        for (final Goal.Flag value : Goal.Flag.values()) {
            if ((bits & ((GoalFlagsBitsMaskGetter) (Object) value).bts$getBits()) != 0)
                flags.add(value);
        }

        return flags;
    }

    public static int bitsFromEnum(final EnumSet<Goal.Flag> flags) {
        if (flags.isEmpty()) return 0;
        int bits = 0;
        for (final Goal.Flag flag : flags) {
            bits |= ((GoalFlagsBitsMaskGetter) (Object) flag).bts$getBits();
        }
        return bits;
    }
}
