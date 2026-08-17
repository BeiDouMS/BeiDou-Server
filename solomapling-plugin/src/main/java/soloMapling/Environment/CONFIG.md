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

## Where to put the file at runtime (BeiDou)

**Do not put it in `BeiDou-boot.jar`.** The host fat jar does not need this file.
`EnvironmentPopulationConfig` resolves it in this order:

1. Optional override: `application.yml` → `solomapling.population-config: <path>`
2. Working-directory file (start the JVM from **`gms-server/`**):  
   `src/main/java/soloMapling/Environment/EnvironmentPopulation.yaml`
3. Classpath inside **`plugins/solomapling-plugin-*.jar`**:  
   `soloMapling/Environment/EnvironmentPopulation.yaml`

| Location | Role |
|----------|------|
| `solomapling-plugin/.../Environment/EnvironmentPopulation.yaml` | Source of truth; packed into the **plugin** jar at build |
| `gms-server/.../Environment/EnvironmentPopulation.yaml` | Runtime FS copy — preferred for live edits (no plugin rebuild) |
| `BeiDou-boot.jar` | Not used for this config |

If the FS file exists under the process cwd, it wins over the copy inside the plugin jar.

## Live commands (GM ≥ 4)

```
!env population show|reload   # whole file (waves + towns)
!env townpresence reload      # same file; refreshes town plan + social map scope
```

`reload` re-reads YAML but does not despawn bots already online — restart (or a full environment load) to apply new counts.

## Verify

```bash
cd /path/to/BeiDou-Server
mvn -pl solomapling-plugin -am test -Dtest=EnvironmentPopulationConfigTest -Dsurefire.failIfNoSpecifiedTests=false
```
