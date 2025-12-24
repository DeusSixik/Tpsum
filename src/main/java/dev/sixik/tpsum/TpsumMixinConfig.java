package dev.sixik.tpsum;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;

import java.util.List;
import java.util.Set;

public class TpsumMixinConfig implements IMixinConfigPlugin {

    private static final String DISABLE_IF_CLASS_PRESENT = "com.ishland.c2me.base.common.C2MEConstants";

    private static final List<String> DISABLE_IF_C2ME = List.of(
            "dev.sixik.tpsum.mixin.rework_chunk_generation.features.MixinConfiguredFeature",
            "dev.sixik.tpsum.mixin.rework_chunk_generation.MixinChunkGenerator",
            "dev.sixik.tpsum.mixin.rework_chunk_generation.MixinNoiseBasedChunkGenerator"
    );

    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if(!DISABLE_IF_C2ME.contains(mixinClassName)) return true;

        return !classExists(DISABLE_IF_CLASS_PRESENT);
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return List.of();
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    private static boolean classExists(String name) {
        try {
            final ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Class.forName(name, false, cl);
            return true;
        } catch (Throwable ignored) {
            try {
                final ClassNode node = MixinService.getService()
                        .getBytecodeProvider()
                        .getClassNode(name, false);
                return node != null;
            } catch (Throwable ignored2) {
                return false;
            }
        }
    }
}
