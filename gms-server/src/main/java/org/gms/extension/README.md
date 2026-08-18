# BeiDou Extension Runtime

Feature branch reference: `feat/beidou-solomapling-plugin`

## Modules

| Module / package | Role |
|------------------|------|
| `dev.maple.extension:extension-api` | Host-neutral SPI (separate repo: [maple-extension-api](https://github.com/zmzeng/maple-extension-api)) |
| `org.gms.extension.runtime` | Host-side runtime (`ExtensionLoader`, `BeiDouHostRuntime`, …) |
| `gms-server/plugins/*.jar` | Drop zone for external plugins (gitignored jars; keep `.gitkeep`) |

SoloMapling plugin sources live in the [SoloMapling](https://github.com/MadaraGameDev/SoloMapling) repo (the repo **is** the plugin).

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
# Neutral SPI
cd /path/to/maple-extension-api && mvn install

# Host (this repo)
cd /path/to/BeiDou-Server
mvn -pl gms-server -am install -DskipTests
mvn -pl gms-server package -DskipTests

# Plugin (SoloMapling repo)
cd /path/to/SoloMapling
mvn package -DskipTests
cp target/solomapling-plugin-*-SNAPSHOT.jar /path/to/BeiDou-Server/gms-server/plugins/

cd /path/to/BeiDou-Server/gms-server
java -Xmx4g -Dspring.config.location=src/main/resources/application.yml \
  -jar target/BeiDou-boot.jar
```

Runnable artifact is **`BeiDou-boot.jar`** (classifier `boot`). Thin `BeiDou.jar` is published for plugin compile classpath.

See the repository root `README.md` section **扩展运行时 / SoloMapling** for the full operator guide.

Gameplay events (character ids only): `CharacterMapEnteredEvent`, `CharacterChatEvent`, `TradeInviteEvent`.
Artificial characters are identified by plugin-registered `CharacterClassifier`s (`ExtensionLoader.isArtificial`), not a SoloMapling-specific bridge.


Population / TrainingBot / town ambient counts: `EnvironmentPopulation.yaml`
(`waves.*` and `waves.town_presence.towns`) — see SoloMapling
`src/main/java/soloMapling/Environment/CONFIG.md`.

Runtime load order (not from `BeiDou-boot.jar`): optional
`solomapling.population-config` → cwd file override → plugin-jar classpath.
