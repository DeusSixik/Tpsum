package dev.sixik.tpsum;

import dev.sixik.tpsum.utils.MixinApplier;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TpsumMixinPlugin implements IMixinConfigPlugin {

    public static List<MixinApplier> mixinAppliers = new ArrayList<>();

    @Override
    public void onLoad(String s) {
        create("vazkii.botania.forge.ForgeCommonInitializer",
            new MixinApplier.Param(
                "dev.sixik.tpsum.mixin.compat.botania.BotaniaCustomNaturalSpawnerMixin",
                "vazkii.botania.mixin.NaturalSpawnerMixin"
            ),
            new MixinApplier.Param(
                "dev.sixik.tpsum.mixin.compat.botania.BotaniaNaturalSpawnerMixin",
                "vazkii.botania.mixin.NaturalSpawnerMixin"
            )
        );
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        for (MixinApplier mixinApplier : mixinAppliers) {
            if(mixinApplier.hasMixin(mixinClassName) && !mixinApplier.isModLoaded())
                return false;
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> set, Set<String> set1) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {

    }

    @Override
    public void postApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {

    }

    public void create(String modClass, MixinApplier.Param... params) {
        mixinAppliers.add(new MixinApplier(modClass, params));
    }
}
