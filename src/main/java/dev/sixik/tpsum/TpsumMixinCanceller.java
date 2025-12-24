package dev.sixik.tpsum;

import com.bawnorton.mixinsquared.api.MixinCanceller;
import dev.sixik.tpsum.utils.MixinApplier;

import java.util.List;

public class TpsumMixinCanceller implements MixinCanceller {

    @Override
    public boolean shouldCancel(List<String> list, String s) {
        for (MixinApplier mixinApplier : TpsumMixinPlugin.mixinAppliers) {
            if(mixinApplier.hasDisableMixin(s) && mixinApplier.isModLoaded()) {
                return true;
            }
        }

        return false;
    }
}
