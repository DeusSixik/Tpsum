package dev.sixik.tpsum.level;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import org.jetbrains.annotations.NotNull;

public final class CachedNoiseBiomeSource implements BiomeManager.NoiseBiomeSource {
    private final BiomeManager.NoiseBiomeSource delegate;

    private final int size;
    private final int mask;

    private final ThreadLocal<Cache> local;

    public CachedNoiseBiomeSource(BiomeManager.NoiseBiomeSource delegate, int sizePow2) {
        if (Integer.bitCount(sizePow2) != 1) throw new IllegalArgumentException("size must be power of two");
        this.delegate = delegate;
        this.size = sizePow2;
        this.mask = sizePow2 - 1;
        local = ThreadLocal.withInitial(() -> new Cache(size));
    }

    @Override
    public @NotNull Holder<Biome> getNoiseBiome(int x, int y, int z) {
        final Cache c = local.get();
        final int idx = hash(x, y, z) & mask;

        if (c.xs[idx] == x && c.ys[idx] == y && c.zs[idx] == z) {
            @SuppressWarnings("unchecked")
            final Holder<Biome> hit = (Holder<Biome>) c.values[idx];
            if (hit != null) return hit;
        }

        final Holder<Biome> v = delegate.getNoiseBiome(x, y, z);
        c.xs[idx] = x;
        c.ys[idx] = y;
        c.zs[idx] = z;
        c.values[idx] = v;
        return v;
    }

    private static int hash(int x, int y, int z) {
        int h = x * 0x9E3779B9;
        h ^= y * 0x85EBCA6B;
        h ^= z * 0xC2B2AE35;
        h ^= (h >>> 16);
        h *= 0x7FEB352D;
        h ^= (h >>> 15);
        h *= 0x846CA68B;
        h ^= (h >>> 16);
        return h;
    }

    private static final class Cache {
        final int[] xs, ys, zs;
        final Object[] values;

        public Cache(int size) {
            xs = new int[size];
            ys = new int[size];
            zs = new int[size];
            values = new Object[size];
        }
    }
}
