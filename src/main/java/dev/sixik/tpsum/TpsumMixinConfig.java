package dev.sixik.tpsum;

import dev.sixik.tpsum.utils.MixinApplier;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TpsumMixinConfig implements IMixinConfigPlugin {

    public static List<MixinApplier> mixinAppliers = new ArrayList<>();

    @Override
    public void onLoad(String mixinPackage) {
        create("com.ishland.c2me.base.common.C2MEConstants",
                new MixinApplier.Param(
                        "",
                        "dev.sixik.tpsum.mixin.rework_chunk_generation.features.MixinConfiguredFeature"
                ),
                new MixinApplier.Param(
                        "",
                        "dev.sixik.tpsum.mixin.rework_chunk_generation.MixinChunkGenerator"
                ),
                new MixinApplier.Param(
                        "",
                        "dev.sixik.tpsum.mixin.rework_chunk_generation.MixinNoiseBasedChunkGenerator"
                )
        );
        create("ca.spottedleaf.moonrise.neoforge.MoonriseNeoForge",
                new MixinApplier.Param(
                        "",
                        "dev.sixik.tpsum.mixin.rework_chunk_generation.MixinChunkGenerator"
                ),
                new MixinApplier.Param(
                        "",
                        "dev.sixik.tpsum.mixin.rework_chunk_generation.MixinNoiseBasedChunkGenerator"
                )
        );
        create("artifacts.Artifacts", new MixinApplier.Param(
                "dev.sixik.tpsum.mixin.compat.artifacts.ArtifactsNaturalSpawnerMixin",
                "artifacts.mixin.item.NaturalSpawnerMixin"
        ));
        create("com.faboslav.variantsandventures.common.VariantsAndVentures",
                new MixinApplier.Param(
                        "dev.sixik.tpsum.mixin.compat.variants_and_ventures.VariantsandventuresNaturalSpawnerMixin",
                        "com.faboslav.variantsandventures.forge.mixin.SpawnHelperMixin"
                ),
                new MixinApplier.Param(
                        "dev.sixik.tpsum.mixin.compat.variants_and_ventures.VariantsandventuresCustomNaturalSpawnerMixin",
                        "com.faboslav.variantsandventures.forge.mixin.SpawnHelperMixin"
                )
        );
        create("com.zv.neoforge.ModNeoForge",
                new MixinApplier.Param(
                        "dev.sixik.tpsum.mixin.compat.zombie_variants.ZombieVariantNaturalSpawnerMixin",
                        "com.zv.mixin.SpawnHelperMix"
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
            if (mixinApplier.hasDisableMixin(mixinClassName) && mixinApplier.isModLoaded())
                return false;

            if (mixinApplier.hasMixin(mixinClassName) && !mixinApplier.isModLoaded())
                return false;
        }
        return true;
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


    public void create(String modClass, MixinApplier.Param... params) {
        mixinAppliers.add(new MixinApplier(modClass, params));
    }
}
