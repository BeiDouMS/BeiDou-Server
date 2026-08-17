package soloMapling.ArtificialPlayer.BotTypes;

import org.gms.client.Character;
import soloMapling.ArtificialPlayer.BotChatterSystem.BotChatter;
import soloMapling.ArtificialPlayer.BotFlavorSystem.BotFlavor;
import soloMapling.ArtificialPlayer.BotFlavorSystem.LevelUpCongrats;
import soloMapling.ArtificialPlayer.BotGrindSystem.MapMobIndex;
import soloMapling.ArtificialPlayer.BotSM;
import soloMapling.ArtificialPlayer.BotWanderSystem.BotWanderSystem;
import soloMapling.ArtificialPlayer.GCMoveSystem.GCMovement;
import soloMapling.server.EventMessageSystem.EventBus;
import soloMapling.server.EventMessageSystem.EventType;
import soloMapling.server.EventMessageSystem.GameEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands.BotEmote;
import static soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands.BotSpeak;
import static soloMapling.ArtificialPlayer.BotDialogueHandler.getRandomResolvedLine;
import static soloMapling.BotLogger.log;
import static soloMapling.server.SoloMaplingUtilities.rollChanceInverse;

// Generic roaming town bot: strolls a town's ledges, emotes/chats, and occasionally drifts to an
// adjacent town sub-map. The map-agnostic counterpart to HenesysBot - HenesysBot stays the hand-polished
// Henesys set piece (untouched); this brings the same "player wandering the city" feel to every other
// town, driven entirely by data (WZ terrain + a discovered map family) rather than hardcoded map IDs,
// portal-pair tables, or platform-CSV recordings.
//
// Movement is pure GC-engine composition: BotWanderSystem does the self-driving ledge roam (edge-trimmed,
// no recordings), GCMovement.travel crosses sub-map boundaries over the portal graph (no portal table).
// Our own creation (not a GreenCat extraction).
public class TownWandererBot extends BotSM {

    private enum WanderState { RESET, IDLE, WANDER, EMOTE, CHANGE_MAP }

    // Written on the tick and read in the travel callback thread - keep volatile.
    private volatile WanderState wanderState = WanderState.RESET;
    private volatile boolean traveling = false;

    private int homeMapId = -1;
    private List<Integer> townFamily = List.of(); // home + adjacent mob-free (town/interior) maps
    private long lastMapChangeMs = 0;

    // Don't hop maps more than this often, so a wanderer settles into a town instead of teleport-spamming.
    private static final long MAP_CHANGE_COOLDOWN_MS = 90_000;

    private final Random rng = new Random();

    public TownWandererBot(Character character) {
        super(character);
        dialoguePath = "TownWandererDialogue.yaml";
        botType = "TownWandererBot";
        EventBus.getInstance().subscribe(EventType.LEVEL_UP, this); // congratulate nearby levelers
    }

    // A wanderer is always free to emote/congratulate except while a map-hop owns its movement or it's
    // mid ambient chatter with another bot.
    @Override
    public boolean isAvailableForAmbientActions() {
        return !traveling && !BotChatter.isEngaged(getChr());
    }

    @Override
    public void updateState() {
        super.updateState();
        if (checkIfNotRunningOrPaused()) {
            return;
        }
        Character chr = getChr();
        if (chr == null || chr.getMap() == null) {
            return;
        }
        getDebugger().debugLoggingFull(
                String.format("%s TownWandererBot: %s", chr.getName(), wanderState), String.format("%s", wanderState));

        processQueuedEvents(); // drain any LEVEL_UP congrats events buffered since the last tick

        switch (wanderState) {
            case RESET -> doReset();
            case IDLE -> decideNext();
            case WANDER -> {
                ensureRoaming();
                doRandomEmote();
                doRandomChat("WanderChat");
                wanderState = WanderState.IDLE;
            }
            case EMOTE -> {
                doRandomEmote();
                doRandomChat("EmoteReaction");
                wanderState = WanderState.IDLE;
            }
            case CHANGE_MAP -> doChangeMap();
        }
    }

    private void doReset() {
        if (homeMapId < 0) {
            homeMapId = getChr().getMapId();
            townFamily = discoverTownFamily(homeMapId);
        }
        ensureRoaming();
        wanderState = WanderState.IDLE;
    }

    // Home map + adjacent mob-free maps (town interiors / connectors) within one hop, so a wanderer can
    // drift between a town's sub-rooms with no hardcoded map/portal table. mapsWithinHops already excludes
    // taxi/ferry hops (town-local); we drop any map that has mobs (a field, not a town room). Home-only fallback.
    private List<Integer> discoverTownFamily(int home) {
        List<Integer> fam = new ArrayList<>();
        fam.add(home);
        try {
            for (int m : GCMovement.mapsWithinHops(home, 1)) {
                if (m != home && MapMobIndex.level(m) < 0) {
                    fam.add(m);
                }
            }
        } catch (Exception ignored) {
            // world graph not ready / unknown map - home-only is fine
        }
        return fam;
    }

    private void decideNext() {
        BotFlavor.maybeExpress(this); // occasional idle emote / buff-flex / skill-swing (self-gated on observed + cooldown)
        BotChatter.maybeStartChatter(this); // occasional short back-and-forth with a nearby town bot (self-gated)
        ensureRoaming(); // keep the bot drifting whenever it's idle (also restarts the roam after a map hop)
        boolean cooled = (System.currentTimeMillis() - lastMapChangeMs) > MAP_CHANGE_COOLDOWN_MS;
        if (cooled && townFamily.size() > 1 && rollChanceInverse(12)) {
            wanderState = WanderState.CHANGE_MAP;
            return;
        }
        if (rollChanceInverse(3)) {
            wanderState = WanderState.WANDER;
            return;
        }
        if (rollChanceInverse(5)) {
            wanderState = WanderState.EMOTE;
            return;
        }
        // else stay idle - BotWanderSystem keeps the bot drifting between ticks
    }

    // Make sure the self-driving ledge roam is running (unless a map-hop is mid-flight, which owns movement).
    private void ensureRoaming() {
        Character chr = getChr();
        if (chr != null && !traveling && !BotWanderSystem.isWandering(chr)) {
            BotWanderSystem.start(chr);
        }
    }

    private void doChangeMap() {
        Character chr = getChr();
        int dest = pickNextMap(townFamily, chr.getMapId());
        if (dest < 0) {
            wanderState = WanderState.IDLE;
            return;
        }
        doRandomChat("MapTransition");
        BotWanderSystem.stop(chr); // hand movement to travel
        traveling = true;
        lastMapChangeMs = System.currentTimeMillis();
        // The callback fires on the GCTravel poller thread, which can race a stop/convert. Do NOT restart
        // movement here (that would resurrect a stopped bot) - just clear the flag; the next macro tick's
        // ensureRoaming() restarts the roam, gated by the FSM's own alive-check.
        GCMovement.travel(chr, dest, ok -> traveling = false);
        wanderState = WanderState.IDLE;
    }

    // Pick a family map other than the current one. Random (not crowd-weighted): weighting by live
    // population would call getAllCharsOnMap on every candidate each hop, loading otherwise-dormant
    // interior sub-maps just to count occupants; a wanderer drifting anywhere reachable reads fine.
    private int pickNextMap(List<Integer> family, int currentMapId) {
        List<Integer> options = new ArrayList<>();
        for (int m : family) {
            if (m != currentMapId) {
                options.add(m);
            }
        }
        return options.isEmpty() ? -1 : options.get(rng.nextInt(options.size()));
    }

    private void doRandomEmote() {
        if (rollChanceInverse(10)) {
            BotEmote(getChr(), rollChanceInverse(2) ? 2 : 3);
        }
    }

    private void doRandomChat(String dialogueNode) {
        if (rollChanceInverse(20)) {
            try {
                String line = getRandomResolvedLine(this, dialogueNode);
                if (line != null) {
                    BotSpeak(getChr(), line);
                }
            } catch (Exception e) {
                // dialogue node missing - skip quietly
            }
        }
    }

    @Override
    public void handleEvent(GameEvent event) {
        // A nearby character (player or bot) levelled up - maybe congratulate them.
        LevelUpCongrats.react(this, event);
    }

    // Release the roam + movement lock when the bot stops (FINISHED / converted / manually stopped).
    @Override
    public synchronized void stopScheduledTask() {
        Character chr = getChr();
        if (chr != null) {
            BotChatter.forget(chr);
            BotWanderSystem.stop(chr);
            GCMovement.disable(chr);
        }
        super.stopScheduledTask();
        log("[TownWandererBot] stopped: " + (chr != null ? chr.getName() : "?"));
    }
}
