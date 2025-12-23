# Tpsum

**Tpsum** is a small, performance-focused optimization mod for Minecraft.  
It targets **hot paths** that are called extremely often during normal gameplay (server tick, mob spawning, biome lookup, and worldgen), with the goal of reducing CPU time and avoidable allocations/GC pressure.

> Tpsum does **not** add content. It focuses on efficiency and smoother TPS under load.

---

## Highlights

- Faster **NaturalSpawner** (mob spawning)
- Faster **biome lookup** (`BiomeManager` / noise biomes)
- Reduced **worldgen allocations** (feature placement context pooling)
- Optimized **task loop** (`BlockableEventLoop`) for high task throughput

---

## Download
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/tpsum)
- [Github](https://github.com/DeusSixik/Tpsum/releases/)

---

## Optimizations

### 1) Mob spawning (NaturalSpawner)

#### 1.1 `NaturalSpawner.createState` - chunk-grouped processing
Vanilla iterates entities and often calls `chunkGetter.query(...)` per-entity.  
Tpsum groups entities by chunk first (`Long2ObjectOpenHashMap<long -> ObjectArrayList<Entity>>`) and performs **one `query` per chunk**, then processes all entities in that chunk batch.

**Benefit:** fewer `query` calls, fewer callbacks, less overhead when many entities are present.

#### 1.2 `SpawnState.canSpawnForCategory` - cached caps
Vanilla recalculates the per-category cap:

`cap = maxInstancesPerChunk * spawnableChunkCount / MAGIC_NUMBER`

Tpsum precomputes these values once in the `SpawnState` constructor and stores them in an `int[]` indexed by `MobCategory.ordinal()`.

**Benefit:** cheaper hot-path checks during spawning.

#### 1.3 `spawnCategoryForPosition` - reduced player lookup frequency
Vanilla may call player-nearest queries repeatedly inside inner spawn loops.  
Tpsum retrieves the nearest player **once per spawn attempt group** and uses only `distanceToSqr(...)` afterward.

**Benefit:** less overhead from nearest-player searching, which can be expensive on busy servers.

> **Note (behavioral nuance):**  
> Tpsum chooses the nearest player relative to the *base* coordinates used for the group, rather than re-selecting per candidate position. In edge cases with multiple players and borderline distances, this may slightly change which player is considered “nearest” for some attempts.

#### 1.4 `isRightDistanceToPlayerAndSpawnPoint` - fewer allocations & faster comparisons
Tpsum removes temporary allocations (e.g., `Vec3`, `ChunkPos` comparisons via object creation) and replaces them with:
- direct distance checks using `distToCenterSqr(...)`
- “same chunk” checks using `(x >> 4)` / `(z >> 4)` comparisons

**Benefit:** faster checks in very frequently called code.

#### 1.5 Fast `getBlockState` path for in-chunk access
Inside `spawnCategoryForPosition`, calls to `ChunkAccess.getBlockState(pos)` are redirected:
- if the `ChunkAccess` is a `LevelChunk` and the position belongs to the same chunk,
  Tpsum reads directly from the appropriate `LevelChunkSection` via `sec.getBlockState(x&15, y&15, z&15)`.

**Benefit:** fewer layers of indirection in blockstate reads during spawn attempts.

#### 1.6 `isRedstoneConductor` replaced with `isCollisionShapeFullBlock`
In vanilla spawning code, an early bailout uses `BlockStateBase.isRedstoneConductor(...)`, which calls a `StatePredicate` stored in `BlockBehaviour.Properties`.  
On heavily modded servers this predicate can become a hot path (often involving shape/solid checks), and in profiling it may show up as a large self-time cost.

Tpsum replaces this early check with `isCollisionShapeFullBlock(...)`, which can use the internal `BlockStateBase.Cache` fast-path boolean instead of executing a heavier predicate.

**Benefit:** significantly less CPU spent in the spawn hot path, fewer expensive predicate/shape checks per spawn attempt.



---

### 2) Biome lookup

#### 2.1 Cached noise-biome source (`BiomeManager.NoiseBiomeSource`)
Tpsum wraps the `NoiseBiomeSource` with a `ThreadLocal` cache (`CachedNoiseBiomeSource`, size 512, power-of-two indexed).

**Benefit:** faster repeated `getNoiseBiome(x,y,z)` calls in worldgen and biome-related lookups.

#### 2.2 `BiomeManager.getFiddle` math micro-optimization
Replaces `floorMod(seed >> 24, 1024)` with `((seed >> 24) & 1023)` and simplifies the arithmetic.

**Benefit:** tiny but extremely hot function, helps in aggregate.

---

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

> **Implementation note:**  
> the current rewrite creates a `boolean[]` per (x,z) column. This is a tradeoff; it can be further optimized (e.g., bitset) if needed.

---

### 4) Task loop / scheduler (BlockableEventLoop)

Tpsum replaces parts of `BlockableEventLoop` task bookkeeping with:
- `MpscUnboundedArrayQueue` as the pending task queue
- `LongAdder` as the pending count
- streamlined `tell()` / `pollTask()` / `dropAllTasks()` / `waitForTasks()` logic
- `submitAsync()` uses `CompletableFuture.runAsync(..., this)`

**Benefit:** improved throughput when many tasks are posted concurrently.

**Compatibility warning:** this is one of the most mixin-heavy and mod-touched areas; conflicts with other task-loop / tick-loop / scheduler mods are more likely here.

---

## Compatibility

Tpsum may conflict with mods that also modify:
- `NaturalSpawner` / mob caps / spawn rules
- `BiomeManager` / noise biome sources
- worldgen feature placement internals (`ConfiguredFeature`)
- `BlockableEventLoop` and other task/tick scheduling optimizers

If you use multiple optimization mods, test combinations carefully.

---

## Safety / Behavior Notes

- Tpsum aims to preserve vanilla behavior whenever possible.
- A few changes (notably the reduced nearest-player lookup frequency in spawning) may cause **small behavioral differences in edge cases**.
- The goal is practical performance improvement with minimal gameplay impact.

---

## Credits

- Feature context pooling approach based on ideas used by the **C2ME** project (see their repository for details).