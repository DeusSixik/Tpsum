# Tpsum

**Tpsum** is a small performance optimization mod for Minecraft, focused on improving hotspots that are called extremely often during gameplay.
It also ports part of the chunk/worldgen optimizations from **BTS Concurrent** and improves performance in the following areas:

* **Biome lookup**: faster `getBiome` calls
* **Mob spawning**: a more efficient `spawnCategoryForPosition` implementation
* **Task loop**: optimizations for the `BlockableEventLoop` task processing

## Download
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/tpsum)
- [Github](https://github.com/DeusSixik/Tpsum/releases/)