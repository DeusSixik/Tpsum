package dev.sixik.tpsum.mixin;

import dev.sixik.tpsum.level.CachedNoiseBiomeSource;
import net.minecraft.world.level.biome.BiomeManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BiomeManager.class, priority = Integer.MAX_VALUE / 2)
public class MixinBiomeManager$access_optimize {

    @Mutable
    @Shadow
    @Final
    private BiomeManager.NoiseBiomeSource noiseBiomeSource;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void bts$init(BiomeManager.NoiseBiomeSource p_186677_, long p_186678_, CallbackInfo ci) {
        noiseBiomeSource = new CachedNoiseBiomeSource(p_186677_, 512);
    }
}
