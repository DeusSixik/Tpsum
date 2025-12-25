package dev.sixik.tpsum.mixin.math;

import dev.sixik.tpsum.math.Vec3iSetter;
import net.minecraft.core.Vec3i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Vec3i.class)
public class MixinVec3i$access implements Vec3iSetter {
    @Shadow
    private int x;

    @Shadow
    private int y;

    @Shadow
    private int z;

    @Override
    public void bts$setX(int x) {
        this.x = x;
    }

    @Override
    public void bts$setY(int y) {
        this.y = y;
    }

    @Override
    public void bts$setZ(int z) {
        this.z = z;
    }

    @Override
    public void bts$set(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
