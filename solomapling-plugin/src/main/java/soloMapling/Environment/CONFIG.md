# SoloMapling population configuration

Wave counts, FM/merchant batches, and **TrainingBot spawn hubs** are driven by
`EnvironmentPopulation.yaml` (loaded by `EnvironmentPopulationConfig`).

Town ambient SocialBot / TownWanderer counts remain in
`ArtificialPlayer/BotTownSystem/TownPresence.yaml`.

## Files

| Path | Role |
|------|------|
| `solomapling-plugin/.../Environment/EnvironmentPopulation.yaml` | Source of truth (also packed into the plugin jar) |
| `gms-server/src/main/java/soloMapling/Environment/EnvironmentPopulation.yaml` | Runtime FS copy (cwd = `gms-server`) |
| `TownPresence.yaml` | Wave 9 town ambient headcounts |

## Resolution order

1. `solomapling.population-config` in `application.yml` (optional absolute/relative path)
2. `src/main/java/soloMapling/Environment/EnvironmentPopulation.yaml` under the process cwd
3. Classpath `soloMapling/Environment/EnvironmentPopulation.yaml` inside the plugin jar
4. Built-in defaults (same numbers as the BeiDou-tuned hardcoded Wave 8)

## Key knobs

```yaml
version: 1
scale: 1.0          # multiplies henesys / FM / merchant / training counts

waves:
  training:
    enabled: true
    warm_nav: {map: 100000000, hops: 1}
    cohorts:
      - {map: 100000000, count: 25, level_lo: 10, level_hi: 95}
  town_presence:
    enabled: true   # still reads TownPresence.yaml for per-town counts
```

- **`scale: 0.25`** — quick pilot (~¼ TrainingBot / FM counts)
- **`training.cohorts[].map`** — spawn **hub** (town or deep junction). Field grind maps are discovered at runtime (`TrainingMapFinder`), not listed here.
- Set `waves.*.enabled: false` to skip an entire startup wave.

## Live commands (GM ≥ 4)

```
!env population show     # dump source, scale, training total
!env population reload   # re-read YAML (applies to next EnvironmentManager load)
!env townpresence reload # TownPresence.yaml only
```

Applying a new plan to an already-populated world requires a restart (or carefully using `!env` spawn helpers). `reload` alone does not despawn existing bots.

## Verify without a full server boot

```bash
cd /path/to/BeiDou-Server
mvn -pl solomapling-plugin -am test -Dtest=EnvironmentPopulationConfigTest
```
