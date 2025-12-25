### 3) Worldgen / chunk init / allocation reductions

#### 3.1 `ConfiguredFeature.place` - pooled feature context (alloc reduction)
Tpsum uses a pool-backed `PooledFeatureContext` for `ConfiguredFeature.place(...)` (inspired by work from the C2ME project).

**Benefit:** reduced allocations and GC during feature placement.

#### 3.2 `LevelChunkSection.fillBiomesFromNoise` - leaner inner loop
Tpsum overwrites biome filling to use a tighter loop and `getAndSetUnchecked(...)`.

**Benefit:** minor CPU improvement; typically small but consistent.

#### 3.3 `Heightmap.primeHeightmaps` - early exit & air skip
Tpsum rewrites heightmap priming to:
- skip iterations for air blocks
- stop scanning downward as soon as all requested heightmap types are filled for a column

**Benefit:** can reduce work significantly in many columns.

> [!NOTE]
> **Implementation note:**  
> the current rewrite creates a `boolean[]` per (x,z) column. This is a tradeoff; it can be further optimized (e.g., bitset) if needed.
