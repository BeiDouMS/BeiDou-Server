# BeiDou Extension Runtime + SoloMapling Plugin

Feature branch reference: `feat/beidou-solomapling-plugin`

## Modules

| Module / package | Role |
|------------------|------|
| `extension-api` | Shared SPI (`ServerExtension`, `HostRuntime`, `HostConfig`, `HostEventBus`, `HostCommandRegistry`) |
| `org.gms.extension.runtime` | Host-side runtime (`ExtensionLoader`, `BeiDouHostRuntime`, …) |
| `solomapling-plugin` | Full SoloMapling framework as a loadable jar |
| `gms-server/plugins/*.jar` | Drop zone for external plugins (gitignored jars; keep `.gitkeep`) |

## Load order

1. Spring Boot starts (`ServerApplication`)
2. `ServerManager` builds `BeiDouHostRuntime` and `ExtensionLoader.load(plugins/)` → each extension `onLoad`
3. `Server.init()` (login + channels)
4. `ExtensionLoader.notifyServerReady()` → each extension `onServerReady`
5. SoloMapling (if `spawn-bots-on-startup: true`) schedules `EnvironmentManager.environmentLoadStartup()` ~1s later

## Config (`gms-server/src/main/resources/application.yml`)

```yaml
solomapling:
  plugins-enabled: true
  plugins-dir: plugins
  spawn-bots-on-startup: true
```

## Build & install SoloMapling plugin

```bash
# from BeiDou-Server root
mvn -pl extension-api,gms-server,solomapling-plugin -am package -DskipTests
cp solomapling-plugin/target/solomapling-plugin-*-SNAPSHOT.jar gms-server/plugins/

cd gms-server
java -Xmx4g -Dspring.config.location=src/main/resources/application.yml \
  -jar target/BeiDou-boot.jar
```

Runnable artifact is **`BeiDou-boot.jar`** (classifier `boot`). Thin `BeiDou.jar` is published for plugin compile classpath.

See the repository root `README.md` section **扩展运行时 / SoloMapling** for the full operator guide.

Population / TrainingBot / town ambient counts: `EnvironmentPopulation.yaml`
(`waves.*` and `waves.town_presence.towns`) — see
`solomapling-plugin/src/main/java/soloMapling/Environment/CONFIG.md`.

Runtime load order (not from `BeiDou-boot.jar`): optional
`solomapling.population-config` → cwd
`src/main/java/soloMapling/Environment/EnvironmentPopulation.yaml` → plugin-jar classpath.

