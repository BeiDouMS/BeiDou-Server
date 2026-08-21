# BeiDou Extension Runtime

## Modules

| Module / package | Role |
|------------------|------|
| `extension-api` | Shared SPI: `ServerExtension`, `HostRuntime`, `ArtificialCharacters`, `TradeParticipantHook` / `TradeParticipants`, lifecycle events |
| `org.gms.extension.runtime` | `ExtensionLoader`, `BeiDouHostRuntime`, `HostHooks` |
| `org.gms.extension.event` | Host gameplay events (`CharacterMapEnteredEvent`, `CharacterChatEvent`, `PartyInviteEvent`, `TradeInviteEvent`, …) |
| `gms-server/plugins/*.jar` | Drop zone for external plugins |
| **solomapling-plugin** (external repo) | Artificial-player framework jar |

SoloMapling sources are **not** a Maven module of this repository.

## Host capabilities (no plugin package imports)

Engine code must not `import soloMapling.*`. Use:

| Capability | API |
|------------|-----|
| Is this character plugin-owned? | `HostHooks.isArtificial(chr)` / `ArtificialCharacters.isArtificial(id)` |
| Trade participant rules | `TradeParticipants` / `HostHooks.trade*` + `TradeInviteEvent` |
| Publish gameplay events | `HostHooks.publish(new CharacterMapEnteredEvent(...))` etc. |
| Bot performance tier on `Character` | `org.gms.client.BotTier` |
| Headless session | `org.gms.client.BotClient` |
| Plugin lifecycle | `ServerExtension` + `ExtensionLoader` |

Plugins register a `CharacterClassifier` and optionally a `TradeParticipantHook` in `onLoad`.

## Load order

1. Spring Boot starts
2. `ServerManager` builds `BeiDouHostRuntime` and `ExtensionLoader.load(plugins/)` → each extension `onLoad` (classifiers + trade hooks + command registration)
3. `Server.init()`
4. `notifyServerReady()` → `onServerReady` → optional SoloMapling world population
5. On shutdown: `onUnload` → `ArtificialCharacters.clear()` / `TradeParticipants.clear()`

## Config

```yaml
solomapling:
  plugins-enabled: true
  plugins-dir: plugins
  spawn-bots-on-startup: true
```

## Build

```bash
mvn -pl extension-api,gms-server -am install -DskipTests
# then build solomapling-plugin and copy jar into gms-server/plugins/
```
