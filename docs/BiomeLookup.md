### 2) Biome lookup

#### 2.1 Cached noise-biome source (`BiomeManager.NoiseBiomeSource`)
Tpsum wraps the `NoiseBiomeSource` with a `ThreadLocal` cache (`CachedNoiseBiomeSource`, size 512, power-of-two indexed).

**Benefit:** faster repeated `getNoiseBiome(x,y,z)` calls in worldgen and biome-related lookups.

#### 2.2 `BiomeManager.getFiddle` math micro-optimization
Replaces `floorMod(seed >> 24, 1024)` with `((seed >> 24) & 1023)` and simplifies the arithmetic.

**Benefit:** tiny but extremely hot function, helps in aggregate.