# Tpsum

**Tpsum** is a small, performance-focused optimization mod for Minecraft.  
It targets **hot paths** that are called extremely often during normal gameplay (server tick, mob spawning, biome lookup, and worldgen), with the goal of reducing CPU time and avoidable allocations/GC pressure.

> [!IMPORTANT]
> Tpsum does **not** add content. It focuses on efficiency and smoother TPS under load.

## Highlights

- Faster **NaturalSpawner** (mob spawning)
- Faster **biome lookup** (`BiomeManager` / noise biomes)
- Reduced **worldgen allocations** (feature placement context pooling)
- Optimized **task loop** (`BlockableEventLoop`) for high task throughput

## Download
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/tpsum)
- [Github](https://github.com/DeusSixik/Tpsum/releases/)

## Optimizations

1. [Mob spawning (NaturalSpawner)](docs/NaturalSpawner.md)
2. [Biome lookup](docs/BiomeLookup.md)
3. [Worldgen / chunk init / allocation reductions](docs/WorldGen.md)
4. [Task loop / scheduler (BlockableEventLoop)](docs/BlockableEventLoop.md)
5. [Goal flags & selector hot-path (Goal / Goal.Flag / GoalSelector)](docs/Goals.md)
6. [Entity target search & AI goal rewrites](docs/AIGoals.md)

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