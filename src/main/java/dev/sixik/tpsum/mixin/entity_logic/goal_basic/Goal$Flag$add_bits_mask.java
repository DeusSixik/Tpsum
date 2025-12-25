package dev.sixik.tpsum.mixin.entity_logic.goal_basic;

import dev.sixik.tpsum.entity.GoalFlagsBitsMaskGetter;
import net.minecraft.world.entity.ai.goal.Goal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Goal.Flag.class)
public class Goal$Flag$add_bits_mask implements GoalFlagsBitsMaskGetter {

    @Unique
    private int bts$bits;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void bts$init(String par1, int par2, CallbackInfo ci) {
        this.bts$bits = (1 << par2 + 1);
    }

    @Override
    public int bts$getBits() {
        return bts$bits;
    }
}
