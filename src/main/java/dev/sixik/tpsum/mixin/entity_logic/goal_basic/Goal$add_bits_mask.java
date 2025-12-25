package dev.sixik.tpsum.mixin.entity_logic.goal_basic;

import dev.sixik.tpsum.entity.GoalBitsMaskSupport;
import dev.sixik.tpsum.entity.GoalFlagsUtils;
import net.minecraft.world.entity.ai.goal.Goal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.EnumSet;

@Mixin(Goal.class)
public class Goal$add_bits_mask implements GoalBitsMaskSupport {

    @Unique
    private int bts$bits = 0;

    /**
     * @author Sixik
     * @reason We update our bitmask when changing
     */
    @Inject(method = "setFlags", at = @At("RETURN"))
    public void bts$setFlags(EnumSet<Goal.Flag> flags, CallbackInfo ci) {
        bts$setBits(GoalFlagsUtils.bitsFromEnum(flags));
    }

    @Override
    public void bts$setBits(int bits) {
        this.bts$bits = bits;
    }

    @Override
    public int bts$getBits() {
        return bts$bits;
    }
}
