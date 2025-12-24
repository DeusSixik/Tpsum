package dev.sixik.tpsum.mixin.compat.botania;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.NaturalSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vazkii.botania.common.brew.effect.BloodthirstMobEffect;
import vazkii.botania.common.brew.effect.EmptinessMobEffect;

@Mixin(NaturalSpawner.class)
public class BotaniaNaturalSpawnerMixin {

    @Inject(at = @At("HEAD"), method = "isValidPositionForMob", cancellable = true)
    private static void emptiness(ServerLevel world, Mob entity, double squaredDistance, CallbackInfoReturnable<Boolean> cir) {
        if (EmptinessMobEffect.shouldCancel(entity)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(at = @At(value = "RETURN", ordinal = 1), cancellable = true, method = "isValidPositionForMob")
    private static void bloodthirstOverride(ServerLevel world, Mob entity, double p_234974_2_, CallbackInfoReturnable<Boolean> cir) {
        if (BloodthirstMobEffect.overrideSpawn(world, entity.blockPosition(), entity.getType().getCategory())) {
            cir.setReturnValue(true);
        }
    }
}
