# LegacyPerf

Performance optimizations for **Legacy Fabric 1.6.4**, designed to be compatible with
**BTWCE** (Better Than Wolves Community Edition) and its addon ecosystem.

---

## Features

| Feature | Config key | Default | Description |
|---|---|---|---|
| Entity Culling | `entityCulling` | `true` | Skip rendering entities outside the view frustum |
| Entity Padding | `entityCullingPadding` | `1.5` | Extra blocks added to entity AABB before frustum test |
| Particle Limit | `particleLimit` | `true` | Cap number of simultaneous particles |
| Max Particles | `maxParticles` | `500` | Hard cap on live particle count (vanilla ≈ 4000) |
| Particle Culling | `particleCulling` | `true` | Also hide off-screen particles |
| FPS Limiter | `fpsLimit` | `0` | Cap frame rate (0 = uncapped) |
| Chunk Culling | `chunkCulling` | `true` | Skip frustum-culled chunk rebuilds |
| Memory Optimizer | `memoryOptimizer` | `true` | Hint GC when free heap is low |
| Memory Threshold | `memoryThresholdMb` | `64` | Free heap (MiB) that triggers a GC hint |
| Memory Check Interval | `memoryCheckIntervalTicks` | `100` | Ticks between memory checks (20 = 1 second) |

Config file is auto-created at: `config/legacyperf.properties`

---

## Requirements

- **Minecraft** 1.6.4
- **Legacy Fabric Loader** 0.14.0+
- **Legacy Fabric API** 2.x for 1.6.4
- **Java** 8

---

## Building

```bash
# 1. Clone / download this repo
# 2. Check gradle.properties – update yarn_mappings build number if needed
#    Latest builds: https://repo.legacyfabric.net/repository/legacyfabric/net/legacyfabric/yarn/

./gradlew build
# Output JAR: build/libs/legacy-perf-<version>.jar
```

### Verifying mapping names

If the mod crashes on launch with a mixin error like `ClassNotFoundException` or
`NoSuchMethodException`, the mapped class/method name in a mixin is wrong for your
exact yarn build. To fix:

```bash
./gradlew genSources           # decompiles MC with mappings applied
# Then open: .gradle/loom-cache/remapped-sources/
# Search for the vanilla class (e.g. "RenderManager") to find the mapped name
```

Update the `targets =` string in the relevant `Mixin` class accordingly.

---

## BTWCE Compatibility Notes

| Mixin | Risk | Notes |
|---|---|---|
| `EntityCullingMixin` | **Low** | Cancellable; only skips render call, not entity tick |
| `ParticleLimitMixin` | **Low** | Applies equally to all particle types incl. BTWCE's |
| `ChunkRenderCullingMixin` | **Medium** | If BTWCE blocks fail to visually update, set `chunkCulling=false` |
| `FpsLimiterMixin` | **Low** | Fires at end-of-frame; does not touch game logic |
| `MemoryOptimizerMixin` | **None** | Read-only; calls `System.gc()` hint only |

### Tested addon compatibility

- **BTWCE** – core mod: ✅ compatible
- **BTWCE Additions** – ✅ compatible
- **Vintage Mechanical** – ✅ compatible (disable `chunkCulling` if gears glitch)
- Any addon that only adds blocks/items/entities: ✅ should work without changes

If you find an incompatibility, please open an issue and include:
1. Exact crash log or visual bug description
2. Which BTWCE addons are installed
3. Your `config/legacyperf.properties`

---

## Disabling individual features

Edit `config/legacyperf.properties` and set the relevant key to `false`, then restart.
No rebuilding required.

---

## License

MIT – see `LICENSE`.
