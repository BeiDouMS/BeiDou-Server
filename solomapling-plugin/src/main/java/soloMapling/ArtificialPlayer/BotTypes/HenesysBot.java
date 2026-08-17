package soloMapling.ArtificialPlayer.BotTypes;

import org.gms.client.Character;
import org.gms.server.maps.MapleMap;
import soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands;
import soloMapling.ArtificialPlayer.BotMessagingSystem.ChatMessage;
import soloMapling.ArtificialPlayer.BotMessagingSystem.MessageQueue;
import soloMapling.ArtificialPlayer.BotMovementSystem.NavigationSystem.PortalConnection;
import soloMapling.ArtificialPlayer.BotSM;
import soloMapling.ArtificialPlayer.BotTypeManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static soloMapling.ArtificialPlayer.BotClientHandler.getBotClient;
import static soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands.BotEmote;
import static soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands.BotSpeak;
import static soloMapling.ArtificialPlayer.BotCommandsPack.WarpCommands.botWarpMapOnPortal;
import static soloMapling.ArtificialPlayer.BotDialogueHandler.getRandomResolvedLine;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands.nudgeAwayFromOverlap;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands.pathFinderAware;
import static soloMapling.BotLogger.log;
import static soloMapling.Environment.PlatformPlacement.botMoveToPlatformAnyUnoccupiedSpotAware;
import static soloMapling.Environment.PlatformPlacement.getAllCharsOnMap;
import static soloMapling.Environment.PlatformPlacement.getCurrentPlatform;
import static soloMapling.Environment.PlatformPlacement.getMainPlatformIds;
import static soloMapling.server.SoloMaplingUtilities.getRandomElement;
import static soloMapling.server.SoloMaplingUtilities.rollChanceInverse;

public class HenesysBot extends BotSM {
    private HenesysBotState henesysBotState = HenesysBotState.RESET;
    private List<String> hint = Collections.singletonList(getChr().getName());

    private long startTime;
    private long endTime;
    private long lastMapChangeTime;

    // Henesys Map IDs -
    private static final int HENESYS_MAIN = 100000000;
    private static final int HENESYS_MARKET = 100000100;
    private static final int HENESYS_PARK = 100000200;
    private static final int PET_PARK = 100000202;
    private static final List<Integer> HENESYS_MAPS = List.of(HENESYS_MAIN, HENESYS_MARKET, HENESYS_PARK, PET_PARK);

    // Portal IDs for connecting maps -
    private static final int PORTAL_MAIN_TO_MARKET = 23;
    private static final int PORTAL_MAIN_TO_PARK = 24;
    private static final int PORTAL_MARKET_TO_MAIN = 14;
    private static final int PORTAL_PARK_TO_MAIN = 18;
    private static final int PORTAL_PARK_TO_MARKET = 19;
    private static final int PORTAL_MARKET_TO_PARK = 15;
    private static final int PORTAL_PARK_TO_PETPARK = 13;
    private static final int PORTAL_PETPARK_TO_PARK = 5;

    private static final long JQ_CONVERSION_COOLDOWN_MS = 10 * 60 * 1000;
    private long lastJQConversionTime = 0;

    private final PortalConnection portalConnection = new PortalConnection();

    // Cooldown between map changes (3 minutes)
    private static final long MAP_CHANGE_COOLDOWN_MS = 3 * 60 * 1000;

    public HenesysBot(Character character) {
        super(character);
        dialoguePath = "HenesysBotDialogue.yaml";
        botType = "HenesysBot";
        lastMapChangeTime = 0;
    }

    private void setHenesysBotState(HenesysBotState state) {
        this.henesysBotState = state;
    }

    private enum HenesysBotState {
        RESET,
        IDLE,
        WANDER,
        EMOTE,
        CHANGE_MAP
    }

    private void resetHenesysBotState() {
        setHenesysBotState(HenesysBotState.RESET);
        startTime = System.currentTimeMillis();
        endTime = 0;
        initPortalConnections();
    }

    private void initPortalConnections() {
        // Henesys connections - registerBidirectional handles both directions
        portalConnection.registerBidirectional(100000000, 23, 100000100, 14);  // Main <-> Market
        portalConnection.registerBidirectional(100000000, 24, 100000200, 18);  // Main <-> Park
        portalConnection.registerBidirectional(100000200, 19, 100000100, 15);  // Park <-> Market
        portalConnection.registerBidirectional(100000200, 13, 100000202, 5);   // Park <-> Pet Park
    }

    @Override
    public void updateState() {
        super.updateState();
        if (checkIfNotRunningOrPaused()) {
            return;
        }
        getDebugger().debugLoggingFull(String.format("%s HenesysBotState: %s", this.getChr().getName(), henesysBotState), String.format("%s", henesysBotState));

        switch (henesysBotState) {
            case RESET:
                resetHenesysBotState();
                setHenesysBotState(HenesysBotState.IDLE);
                break;
            case IDLE:
                decideNextAction();
                break;
            case WANDER:
                wanderPlatforms();
                doRandomEmote();
                doRandomChat("WanderChat");
                setHenesysBotState(HenesysBotState.IDLE);
                break;
            case EMOTE:
                doRandomEmote();
                doRandomChat("EmoteReaction");
                setHenesysBotState(HenesysBotState.IDLE);
                break;
            case CHANGE_MAP:
                doRandomChat("MapTransition");
                changeMap();
                lastMapChangeTime = System.currentTimeMillis();
                doRandomEmote();
                wanderToMainPlatform();
                setHenesysBotState(HenesysBotState.IDLE);
                break;
            default:
                log("Unexpected state: " + henesysBotState);
                state = BotState.FINISHED;
                resetHenesysBotState();
                throw new IllegalStateException("Unexpected state: " + state);
        }
    }

    /**
     * Decides what action the bot takes this tick.
     * Weighted rolls determine whether it wanders, changes maps, or just idles.
     */
    private void decideNextAction() {
        if (getChr().getMapId() == PET_PARK
                && (System.currentTimeMillis() - lastJQConversionTime) > JQ_CONVERSION_COOLDOWN_MS
                && rollChanceInverse(4)) {
            convertToJQBot();
            return;
        }

        boolean mapChangeCooledDown = (System.currentTimeMillis() - lastMapChangeTime) > MAP_CHANGE_COOLDOWN_MS;

        if (mapChangeCooledDown && rollChanceInverse(10)) {
            setHenesysBotState(HenesysBotState.CHANGE_MAP);
            return;
        }

        // ~33% chance to wander on any given tick
        if (rollChanceInverse(3)) {
            setHenesysBotState(HenesysBotState.WANDER);
            return;
        }

        // 10% chance to do an emote even while staying put
        if (rollChanceInverse(5)) {
            setHenesysBotState(HenesysBotState.EMOTE);
            return;
        }

        // Otherwise stay idle - do nothing this tick
    }

    /**
     * Moves the bot to another platform on the current map.
     * Uses weighted rolls for variety - sometimes stays on current platform,
     * sometimes moves to a nearby one, sometimes picks from the full map.
     */
    private void wanderPlatforms() {
        List<String> platforms = getWanderablePlatforms(getChr().getMapId());
        if (platforms.isEmpty()) return;

        boolean moved = false;
        if (rollChanceInverse(5)) {
            String current = getCurrentPlatform(getChr());
            if (platforms.contains(current)) {
                botMoveToPlatformAnyUnoccupiedSpotAware(getChr(), current);
                moved = true;
            }
        } else if (rollChanceInverse(10)) {
            botMoveToPlatformAnyUnoccupiedSpotAware(getChr(), getRandomElement(platforms.size() > 1 ? platforms.subList(0, 2) : platforms));
            moved = true;
        } else if (rollChanceInverse(35)) {
            botMoveToPlatformAnyUnoccupiedSpotAware(getChr(), getRandomElement(platforms));
            moved = true;
        }

        if (moved && rollChanceInverse(2)) {
            nudgeAwayFromOverlap(getChr());
        }
    }


    private void doRandomEmote() {
        if (rollChanceInverse(10)) {
            int emoteId = rollChanceInverse(50) ? 2 : 3;
            BotEmote(getChr(), emoteId);
        }
    }

    private void doRandomChat(String dialogueNode) {
        if (rollChanceInverse(20)) {
            try {
                String line = getRandomResolvedLine(this, dialogueNode);
                if (line != null) BotSpeak(getChr(), line);
            } catch (Exception e) {
                // dialogue YAML node missing, skip
            }
        }
    }

    private void changeMap() {
        Set<Integer> connected = portalConnection.getConnectedMaps(getChr().getMapId());
        if (connected.isEmpty()) return;

        List<Integer> options = new ArrayList<>(connected);
        int chosen = pickLeastCrowdedMap(options);

        PortalConnection.PortalRoute route = portalConnection.getRoute(getChr().getMapId(), chosen);
        if (route == null) return;
        warpToMap(route.destMapId(), route.sourcePortalId());
    }

    private int pickLeastCrowdedMap(List<Integer> mapIds) {
        Random rng = new Random();
        double[] weights = new double[mapIds.size()];
        double total = 0;

        for (int i = 0; i < mapIds.size(); i++) {
            int population = getAllCharsOnMap(mapIds.get(i)).size();
            weights[i] = 1.0 / (1 + population);
            total += weights[i];
        }

        double roll = rng.nextDouble() * total;
        double cumulative = 0;
        for (int i = 0; i < weights.length; i++) {
            cumulative += weights[i];
            if (roll < cumulative) return mapIds.get(i);
        }
        return mapIds.get(mapIds.size() - 1);
    }

    /**
     * Moves to the portal and enters the target map.
     * Placeholder - adjust to match your portal/warp system.
     */
    private void warpToMap(int targetMapId, int portalId) {
//        debugprint("Warping to Map henesys bot method: ", targetMapId, portalId);
        // Look up the route
        PortalConnection.PortalRoute route = portalConnection.getRoute(getChr().getMapId(), targetMapId);
        if (route == null) {
            log("No direct portal connection from " + getChr().getMapId() + " to " + targetMapId);
            return;
        }

        try {
            // Move to the portal position, then warp
            pathFinderAware(getChr(), getChr().getMap().getPortal(portalId).getPosition());
            MapleMap destMap = getBotClient().getChannelServer().getMapFactory().getMap(route.destMapId());
            botWarpMapOnPortal(getChr(), destMap, route.destPortalId());
            checkPrioritySpeed();
            log(getChr().getName() + " changing map to " + targetMapId + " via portal " + portalId);
        } catch (Exception e) {
            log("HenesysBot map change failed: " + e.getMessage());
        }
    }

    /**
     * Returns the portal ID to use when traveling from one Henesys map to another.
     * Returns -1 if no direct route exists.
     *
     * Placeholder values - update with actual portal IDs.
     */
    private int getPortalForRoute(int fromMap, int toMap) {
        if (fromMap == HENESYS_MAIN && toMap == HENESYS_MARKET) return PORTAL_MAIN_TO_MARKET;
        if (fromMap == HENESYS_MAIN && toMap == HENESYS_PARK) return PORTAL_MAIN_TO_PARK;
        if (fromMap == HENESYS_MARKET && toMap == HENESYS_MAIN) return PORTAL_MARKET_TO_MAIN;
        if (fromMap == HENESYS_PARK && toMap == HENESYS_MAIN) return PORTAL_PARK_TO_MAIN;
        return -1; // no direct route (e.g. market <-> park requires going through main)
    }

    private void wanderToMainPlatform() {
        int mapId = getChr().getMapId();
        List<String> platforms = getWanderablePlatforms(mapId);
        if (!platforms.isEmpty()) {
            botMoveToPlatformAnyUnoccupiedSpotAware(getChr(), getRandomElement(platforms));
        }
    }

    private List<String> getWanderablePlatforms(int mapId) {
        if (mapId == PET_PARK) {
            return List.of("m1");
        }
        return getMainPlatformIds(mapId);
    }

    private void convertToJQBot() {
        doRandomChat("MapTransition");
        log("[HenesysBot] " + getChr().getName() + " converting to JQ bot on Pet Park.");
        BotTypeManager.convertBotType(getChr(), BotTypeManager.BotType.HENESYS_JQ_BOT);
    }

    public void setLastJQConversionTime(long time) {
        this.lastJQConversionTime = time;
    }

    @Override
    public void displayCommands(Character chr) {
        SocialCommands.displayPlayerChatCommands(chr, hint);
    }

    @Override
    public void processMessages() {
        try {
            ChatMessage message = MessageQueue.getInstance().getMessageWithTimeout("secondary", 1, TimeUnit.SECONDS);
            if (message == null) {
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
