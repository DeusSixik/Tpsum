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

> [!NOTE]
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

#### 1.7 `fastIsValidSpawnPostitionForType` - reordered checks (cheap → heavy)
Vanilla `isValidSpawnPostitionForType(...)` may perform expensive spawn-list validation (`canSpawnMobAt(...)` → biome/structure spawn tables) before running cheaper position checks.  
On busy servers, many candidate positions fail early due to placement rules or collisions, so doing heavy spawn-list work first wastes CPU.

Tpsum introduces `fastIsValidSpawnPostitionForType(...)` which reorders validation from **light to heavy**:

1. **Cheap:** category / despawn-distance checks
2. **Medium:** `SpawnPlacements` position & rule checks
3. **Medium/Heavy:** collision check (`level.noCollision(...)`)
4. **Heavy:** spawn-list validation (`NaturalSpawner.canSpawnMobAt(...)`)

**Benefit:** fewer expensive biome/structure spawn-list lookups for candidates that would fail earlier checks anyway.

#### 1.8 `fastGetRandomSpawnMobAt` - noise-biome lookup (avoids `Level.getBiome()` / chunk access)
Vanilla `NaturalSpawner.getRandomSpawnMobAt(...)` calls `serverLevel.getBiome(pos)`, which goes through `BiomeManager.getBiome()` and may require chunk access (`Level.getChunk()` / `ServerChunkCache.getChunk(...)`) to resolve the biome.

Tpsum provides `fastGetRandomSpawnMobAt(...)`, which uses the **noise-biome** directly:

- `serverLevel.getNoiseBiome(x >> 2, y >> 2, z >> 2)`

This avoids the more expensive "fiddled biome" selection logic used by `getBiome(...)` and reduces the chance of triggering chunk lookups from the hot spawn path.

**Benefit:** cheaper biome lookup during `getRandomSpawnMobAt()`, less overhead from chunk access in spawn-heavy scenarios.

> [!NOTE]
> **Accuracy trade-off (biome borders):**  
> `getNoiseBiome(...)` returns the underlying noise biome, while `getBiome(...)` applies the biome-zoom "fiddling" step.  
> In most cases this matches the final biome, but near biome borders the result **may differ**, which can slightly affect which spawn list is chosen at the boundary.

> [!NOTE]
> **Behavior preserved:**  
> The water ambient reduction rule is preserved:
> - if `mobCategory == WATER_AMBIENT` and biome matches `BiomeTags.REDUCED_WATER_AMBIENT_SPAWNS`, a random skip (`< 0.98`) still applies.
