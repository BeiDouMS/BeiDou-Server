# SoloMapling population configuration

One file drives world population:

**`EnvironmentPopulation.yaml`**

| Section | Role |
|---------|------|
| `scale` | Multiplies Henesys / FM / merchant / training counts |
| `waves.*` | Wave toggles and batch sizes |
| `waves.training.cohorts` | TrainingBot spawn hubs + level bands |
| `waves.town_presence.towns` | Ambient SocialBot / wanderer counts (was `TownPresence.yaml`) |

`TownPresence.yaml` is deprecated and no longer read (stub only).

## Paths (BeiDou)

| Path | Role |
|------|------|
| `solomapling-plugin/.../Environment/EnvironmentPopulation.yaml` | Source of truth (packed in plugin jar) |
| `gms-server/.../Environment/EnvironmentPopulation.yaml` | Runtime FS copy (cwd = `gms-server`) |

## Live commands (GM ≥ 4)

```
!env population show|reload   # whole file (waves + towns)
!env townpresence reload      # same file; refreshes town plan + social map scope
```

## Verify

```bash
cd /path/to/BeiDou-Server
mvn -pl solomapling-plugin -am test -Dtest=EnvironmentPopulationConfigTest -Dsurefire.failIfNoSpecifiedTests=false
```
