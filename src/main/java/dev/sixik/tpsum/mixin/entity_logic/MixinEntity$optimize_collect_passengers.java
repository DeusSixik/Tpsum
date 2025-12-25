package dev.sixik.tpsum.mixin.entity_logic;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.List;

@Mixin(Entity.class)
public abstract class MixinEntity$optimize_collect_passengers {

    @Shadow
    private ImmutableList<Entity> passengers;

    /**
     * @author Sixik
     * @reason Replaced long, lazy iterations through stream with fast ones "for"
     */
    @Inject(method = "getIndirectPassengers", at = @At("HEAD"), cancellable = true)
    public void bts$getIndirectPassengers(CallbackInfoReturnable<Iterable<Entity>> cir) {
        /*
            Zero allocation if there doesn't have passengers
         */
        if (this.passengers.isEmpty()) {
            cir.setReturnValue(Collections.emptyList());
            return;
        }

        /*
            Create an array with a size of 8 right away so as not to trigger
            Resize when adding new elements.
         */
        final ObjectArrayList<Entity> entities = new ObjectArrayList<>(8);
        bts$fillIndirectPassengers(this.passengers, entities);

        cir.setReturnValue(entities);
    }

    @Unique
    private static void bts$fillIndirectPassengers(final List<Entity> passengers, final List<Entity> out) {
        for (int i = 0; i < passengers.size(); i++) {
            final Entity entity = passengers.get(i);
            out.add(entity);

            final List<Entity> subPassengers = entity.getPassengers();
            if (!subPassengers.isEmpty()) {
                bts$fillIndirectPassengers(subPassengers, out);
            }
        }
    }
}
