package soloMapling.ArtificialPlayer;

import org.gms.client.Character;
import com.esotericsoftware.yamlbeans.YamlReader;
import org.gms.net.server.Server;
import org.gms.server.maps.MapleMap;
import soloMapling.ArtificialPlayer.BotMessagingSystem.CharacterStorage;
import soloMapling.ArtificialPlayer.BotSM;
import soloMapling.ArtificialPlayer.BotTownSystem.TownPresenceConfig;
import soloMapling.ArtificialPlayer.BotTypes.SocialBot;
import soloMapling.server.ExecutorServiceManager;

import java.awt.*;
import java.io.FileReader;
import java.util.*;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands.BotEmote;
import static soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands.BotSpeak;
import static soloMapling.ArtificialPlayer.BotHelpers.isBot;
import static soloMapling.ArtificialPlayer.BotHelpers.blockingSleep;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands.botFaceTowardsPoint;
import static soloMapling.BotLogger.log;
import static soloMapling.Environment.PlatformPlacement.getAllCharsOnMap;

public class ConversationManager {

    private static ConversationManager instance;
    private ScheduledFuture<?> scheduledTask;
    private final Random random = new Random();
    private boolean running = false;

    private static final int MIN_INTERVAL_MS = 20_000;
    private static final int MAX_INTERVAL_MS = 40_000;

    private static final int SMALL_MAP_MIN_INTERVAL_MS = 70_000;
    private static final int SMALL_MAP_MAX_INTERVAL_MS = 100_000;
    private static final int SMALL_MAP_THRESHOLD = 20;

    private final Map<Integer, Long> lastConversationPerMap = new HashMap<>();

    private static final double CLUSTER_RANGE_X = 250;
    private static final int CLUSTER_RANGE_Y = 30;
    private static final int MAX_CLUSTER_SIZE = 4;

    private static final int[] HENESYS_MAP_IDS = {
            100000000,  // Henesys
            100000100,  // Henesys Market
            100000200,  // Henesys Park
            100000102   // Henesys Potion Shop
    };

    // The maps this manager animates: the four Henesys maps plus every town map in TownPresence.yaml.
    // Built once (start / refreshMapScope) rather than per tick; rebuilt live on !env townpresence reload.
    // The existing player-presence gate means unpopulated town maps no-op, so widening is free where
    // nobody's watching.
    private volatile Set<Integer> ambientMapIds = buildAmbientMapIds();

    private static final String DIALOGUE_YAML = "src/main/java/soloMapling/ArtificialPlayer/BotDialoguePack/ConversationDialogue.yaml";

    private final Set<Integer> botsInConversation = Collections.synchronizedSet(new HashSet<>());
    private final LinkedList<String> recentScriptIds = new LinkedList<>();
    private static final int RECENT_HISTORY_SIZE = 20;

    private List<ConversationScript> allScripts;

    private ConversationManager() {}

    public static synchronized ConversationManager getInstance() {
        if (instance == null) {
            instance = new ConversationManager();
        }
        return instance;
    }

    public void start() {
        if (running) return;
        running = true;
        loadScripts();
        refreshMapScope();
        scheduleNextTick();
        log("[ConversationManager] Started with " + (allScripts != null ? allScripts.size() : 0) + " scripts.");
    }

    // Rebuild the animated-map scope from config. Called on start and by !env townpresence reload so town
    // curation applies without a restart.
    public void refreshMapScope() {
        ambientMapIds = buildAmbientMapIds();
        log("[ConversationManager] Map scope: " + ambientMapIds.size() + " maps.");
    }

    private static Set<Integer> buildAmbientMapIds() {
        Set<Integer> ids = new LinkedHashSet<>();
        for (int id : HENESYS_MAP_IDS) {
            ids.add(id);
        }
        ids.addAll(TownPresenceConfig.allTownMapIds());
        return ids;
    }

    public void stop() {
        running = false;
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
        }
        botsInConversation.clear();
        log("[ConversationManager] Stopped.");
    }

    public boolean isInConversation(int characterId) {
        return botsInConversation.contains(characterId);
    }

    private void scheduleNextTick() {
        if (!running) return;
        int delay = MIN_INTERVAL_MS + random.nextInt(MAX_INTERVAL_MS - MIN_INTERVAL_MS);
        // Fable Phase 4: run the tick body on a virtual thread - conversation playback
        // sleeps between lines, and that used to pin one of the shared scheduled pool's
        // threads for the whole script. The next tick is still scheduled only after the
        // current one finishes, so conversations never overlap.
        scheduledTask = ExecutorServiceManager.getScheduledExecutorService().schedule(
                () -> ExecutorServiceManager.runAsync(() -> {
                    try {
                        tick();
                    } catch (Exception e) {
                        log("[ConversationManager] Error during tick: " + e.getMessage());
                        e.printStackTrace();
                    }
                    scheduleNextTick();
                }), delay, TimeUnit.MILLISECONDS);
    }

    private void tick() {
        if (allScripts == null || allScripts.isEmpty()) return;

        Set<Integer> mapsWithRealPlayers = getMapsWithRealPlayers();
        if (mapsWithRealPlayers.isEmpty()) return;

        long now = System.currentTimeMillis();

        for (int mapId : ambientMapIds) {
            if (!mapsWithRealPlayers.contains(mapId)) continue;
            if (!isMapCooldownReady(mapId, now)) continue;

            List<Character> cluster = findClusterOnMap(mapId);
            if (cluster != null && cluster.size() >= 2) {
                lastConversationPerMap.put(mapId, now);
                startConversation(cluster, null);
                return;
            }
        }
    }

    private boolean isMapCooldownReady(int mapId, long now) {
        Long lastTime = lastConversationPerMap.get(mapId);
        if (lastTime == null) return true;

        int botCount = getAllCharsOnMap(mapId).size();
        long cooldown;
        if (botCount <= SMALL_MAP_THRESHOLD) {
            cooldown = SMALL_MAP_MIN_INTERVAL_MS + random.nextInt(SMALL_MAP_MAX_INTERVAL_MS - SMALL_MAP_MIN_INTERVAL_MS);
        } else {
            cooldown = MIN_INTERVAL_MS + random.nextInt(MAX_INTERVAL_MS - MIN_INTERVAL_MS);
        }

        return (now - lastTime) >= cooldown;
    }

    private Set<Integer> getMapsWithRealPlayers() {
        Set<Integer> maps = new HashSet<>();
        try {
            for (Character chr : Server.getInstance().getChannel(0, 1).getPlayerStorage().getAllCharacters()) {
                if (!isBot(chr)) {
                    maps.add(chr.getMapId());
                }
            }
        } catch (Exception e) {
            // Channel not ready
        }
        return maps;
    }

    public void triggerOnMap(Character player) {
        if (allScripts == null || allScripts.isEmpty()) {
            loadScripts();
        }
        int mapId = player.getMapId();
        List<Character> cluster = findClusterOnMap(mapId);
        if (cluster == null || cluster.size() < 2) {
            player.yellowMessage("[ConversationManager] No cluster of 2+ filler bots found on this map.");
            return;
        }
        startConversation(cluster, player);
    }

    private List<Character> findClusterOnMap(int mapId) {
        try {
            MapleMap map = Server.getInstance().getChannel(0, 1).getMapFactory().getMap(mapId);
            if (map == null) return null;

            List<Character> fillerBots = new ArrayList<>();
            for (Character chr : map.getAllPlayers()) {
                if (!isBot(chr)) continue;
                BotSM bot = CharacterStorage.getBotById(chr.getId());
                if (bot == null || !bot.isAvailableForAmbientActions()) continue;
                // Cluster convos face participants with the OLD movement engine (botFaceTowardsPoint), so
                // the pool must be stationed old-engine SocialBots only. In the towns this excludes
                // GC-engine TownWandererBots - facing one would fire a mismatched packet and freeze a
                // roamer mid-stroll. Henesys is already all SocialBots, so this is a no-op there.
                if (!(bot instanceof SocialBot)) continue;
                if (botsInConversation.contains(chr.getId())) continue;
                fillerBots.add(chr);
            }

            if (fillerBots.size() < 2) return null;

            Collections.shuffle(fillerBots, random);

            for (Character anchor : fillerBots) {
                List<Character> cluster = new ArrayList<>();
                cluster.add(anchor);
                Point anchorPos = anchor.getPosition();

                for (Character other : fillerBots) {
                    if (other.getId() == anchor.getId()) continue;
                    if (botsInConversation.contains(other.getId())) continue;
                    Point otherPos = other.getPosition();
                    if (Math.abs(anchorPos.x - otherPos.x) <= CLUSTER_RANGE_X
                            && Math.abs(anchorPos.y - otherPos.y) <= CLUSTER_RANGE_Y) {
                        cluster.add(other);
                        if (cluster.size() >= MAX_CLUSTER_SIZE) break;
                    }
                }

                if (cluster.size() >= 2) {
                    return cluster;
                }
            }
        } catch (Exception e) {
            // Map not loaded
        }
        return null;
    }

    private void startConversation(List<Character> cluster, Character debugPlayer) {
        int clusterSize = cluster.size();
        ConversationScript script = pickScript(clusterSize);
        if (script == null) {
            if (debugPlayer != null) debugPlayer.yellowMessage("[ConversationManager] No matching script found for cluster of " + clusterSize);
            return;
        }

        List<Character> participants = new ArrayList<>(cluster.subList(0, Math.min(clusterSize, script.getParticipantCount())));

        for (Character chr : participants) {
            botsInConversation.add(chr.getId());
        }

        recentScriptIds.add(script.getId());
        if (recentScriptIds.size() > RECENT_HISTORY_SIZE) {
            recentScriptIds.removeFirst();
        }

        log("[ConversationManager] Starting '" + script.getId() + "' with " + participants.size() + " bots.");

        if (debugPlayer != null) {
            debugPlayer.yellowMessage("[ConversationManager] Script: '" + script.getId() + "' | Participants: " + participants.size());
            String[] roles = {"A", "B", "C", "D"};
            for (int i = 0; i < participants.size(); i++) {
                Character p = participants.get(i);
                debugPlayer.yellowMessage("  Role " + roles[i] + ": " + p.getName()
                        + " | Pos: (" + p.getPosition().x + ", " + p.getPosition().y + ")"
                        + " | Map: " + p.getMapId());
            }
        }

        ExecutorServiceManager.runAsync(() -> {
            try {
                playConversation(participants, script);
            } catch (Exception e) {
                log("[ConversationManager] Conversation playback error: " + e.getMessage());
                e.printStackTrace();
            } finally {
                for (Character chr : participants) {
                    botsInConversation.remove(chr.getId());
                }
            }
        });
    }

    private ConversationScript pickScript(int maxParticipants) {
        List<ConversationScript> eligible = new ArrayList<>();
        for (ConversationScript script : allScripts) {
            if (script.getParticipantCount() <= maxParticipants
                    && !recentScriptIds.contains(script.getId())) {
                eligible.add(script);
            }
        }

        if (eligible.isEmpty()) {
            for (ConversationScript script : allScripts) {
                if (script.getParticipantCount() <= maxParticipants) {
                    eligible.add(script);
                }
            }
        }

        if (eligible.isEmpty()) return null;
        return eligible.get(random.nextInt(eligible.size()));
    }

    // Deliberate synchronous choreography: runs on its own virtual thread (runAsync
    // above), line delays are YAML data-driven, and the `running` check between
    // lines is the kill switch. Blocking here is the point - do not de-sleep.
    private void playConversation(List<Character> participants, ConversationScript script) {
        Map<String, Character> roleMap = new HashMap<>();
        String[] roles = {"A", "B", "C", "D"};
        for (int i = 0; i < participants.size(); i++) {
            roleMap.put(roles[i], participants.get(i));
        }

        Point center = calculateCenter(participants);
        for (Character chr : participants) {
            botFaceTowardsPoint(chr, center);
        }
        blockingSleep(800);

        for (ConversationScript.ConversationLine line : script.getLines()) {
            if (!running) return;

            Character speaker = roleMap.get(line.getSpeaker());
            if (speaker == null) continue;

            if (line.getDelayMs() > 0) {
                blockingSleep((int) line.getDelayMs());
            }

            BotSpeak(speaker, line.getText());
            if (line.hasEmote()) {
                BotEmote(speaker, line.getEmote());
            }
        }

        blockingSleep(2000);
    }

    private Point calculateCenter(List<Character> participants) {
        int sumX = 0, sumY = 0;
        for (Character chr : participants) {
            sumX += chr.getPosition().x;
            sumY += chr.getPosition().y;
        }
        return new Point(sumX / participants.size(), sumY / participants.size());
    }

    // --- YAML Loading ---

    @SuppressWarnings("unchecked")
    private void loadScripts() {
        allScripts = new ArrayList<>();
        try {
            YamlReader reader = new YamlReader(new FileReader(DIALOGUE_YAML));
            Map<String, Object> root = (Map<String, Object>) reader.read();
            Map<String, Object> conversations = (Map<String, Object>) root.get("conversations");
            if (conversations == null) return;

            for (Map.Entry<String, Object> entry : conversations.entrySet()) {
                String id = entry.getKey();
                Map<String, Object> convoMap = (Map<String, Object>) entry.getValue();

                int participants = toInt(convoMap.get("participants"));
                List<Object> linesList = (List<Object>) convoMap.get("lines");
                if (linesList == null) continue;

                List<ConversationScript.ConversationLine> parsedLines = new ArrayList<>();
                for (Object lineObj : linesList) {
                    Map<String, Object> lineMap = (Map<String, Object>) lineObj;
                    String speaker = (String) lineMap.get("speaker");
                    String text = (String) lineMap.get("text");
                    int emote = lineMap.containsKey("emote") ? toInt(lineMap.get("emote")) : -1;
                    long delay = 6000 + random.nextInt(3001);
                    parsedLines.add(new ConversationScript.ConversationLine(speaker, text, emote, delay));
                }

                allScripts.add(new ConversationScript(id, participants, parsedLines));
            }

            log("[ConversationManager] Loaded " + allScripts.size() + " conversation scripts.");
        } catch (Exception e) {
            log("[ConversationManager] Failed to load conversation scripts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static int toInt(Object obj) {
        if (obj instanceof Integer) return (Integer) obj;
        if (obj instanceof Long) return ((Long) obj).intValue();
        if (obj instanceof String) return Integer.parseInt((String) obj);
        return 0;
    }

    private static long toLong(Object obj) {
        if (obj instanceof Long) return (Long) obj;
        if (obj instanceof Integer) return ((Integer) obj).longValue();
        if (obj instanceof String) return Long.parseLong((String) obj);
        return 0;
    }
}
