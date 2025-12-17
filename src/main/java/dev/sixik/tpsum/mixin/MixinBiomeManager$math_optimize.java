package dev.sixik.tpsum.mixin;

import net.minecraft.world.level.biome.BiomeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BiomeManager.class, priority = Integer.MAX_VALUE / 2)
public class MixinBiomeManager$math_optimize {


    @Inject(method = "getFiddle", at = @At("HEAD"), cancellable = true)
    private static void bts$getFiddle(long seed, CallbackInfoReturnable<Double> cir) {
        // floorMod(seed >> 24, 1024) == ((seed >> 24) & 1023)
        int v = (int) ((seed >> 24) & 1023L);  // 0..1023
        // (v/1024 - 0.5) * 0.9 == (v - 512) * (0.9/1024)
        cir.setReturnValue((v - 512) * 0.00087890625D);
    }

}
