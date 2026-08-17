package soloMapling.ArtificialPlayer.GCMoveSystem;

import org.gms.client.Character;
import org.gms.net.server.Server;
import org.gms.net.server.world.World;
import soloMapling.ArtificialPlayer.BotHelpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/*
 * M0 measurement tooling for the GCMoveSystem LOD work. Reports the current dynamic-movement load
 * (enabled bots, their job state, the maps they occupy) and offers a load/unload helper so the CPU
 * cost at N dynamic bots can be profiled before the LOD scheduler is built (measure before
 * optimizing). Reporting only; no movement behavior is changed here.
 */
final class LodMetrics {
    private LodMetrics() {
    }

    private static final int DEFAULT_LOAD = 50;
    private static final int TOP_MAPS = 6;

    // Bots enabled by `!gcmove lod load` so `unload` releases exactly those (never a user-driven bot).
    private static final Set<Integer> LOADED = ConcurrentHashMap.newKeySet();

    /* Human-readable snapshot of the current dynamic-movement load. */
    static List<String> stats() {
        Collection<BotMovementState> states = GCMovement.enabledStates();
        int total = states.size();
        int moving = 0, traveling = 0, following = 0, idle = 0;
        int tierFull = 0, tierHalo = 0, tierDwell = 0, tierCoarse = 0;
        Map<Integer, Integer> botsByMap = new HashMap<>();
        for (BotMovementState st : states) {
            Character bot = st.bot;
            if (bot == null) {
                continue;
            }
            int mapId = bot.getMapId();
            botsByMap.merge(mapId, 1, Integer::sum);
            boolean t = GCMovement.isTraveling(bot);
            boolean f = GCMovement.isFollowing(bot);
            boolean m = GCMovement.isMoving(bot);
            if (t) {
                traveling++;
            } else if (m) {
                moving++;
            } else if (f) {
                following++;
            } else {
                idle++;
            }
            if (ObserverTracker.isFull(mapId)) {
                tierFull++;
            } else if (ObserverTracker.isHalo(mapId)) {
                tierHalo++;
            } else if (ObserverTracker.isActiveMap(mapId)) {
                tierDwell++; // recently observed — held at full physics through the hysteresis dwell
            } else {
                tierCoarse++;
            }
        }

        List<String> out = new ArrayList<>();
        out.add("=== GCMove LOD load ===");
        out.add(String.format("dynamic bots: %d  (moving=%d traveling=%d following=%d idle=%d)",
                total, moving, traveling, following, idle));
        out.add(String.format("tiers: FULL=%d HALO=%d dwell=%d coarse=%d   "
                + "(maps: full=%d halo=%d active=%d)",
                tierFull, tierHalo, tierDwell, tierCoarse,
                ObserverTracker.fullCount(), ObserverTracker.haloCount(), ObserverTracker.activeCount()));
        out.add(String.format("occupied maps: %d   load-test bots: %d", botsByMap.size(), LOADED.size()));
        botsByMap.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .limit(TOP_MAPS)
                .forEach(e -> out.add(String.format("  map %d : %d bot(s)", e.getKey(), e.getValue())));
        out.add("(watch the JVM/host CPU at this bot count — that is the M0 measurement)");
        return out;
    }

    /*
     * Enable dynamic movement (+ idle fidget, so they actually walk/jump) on up to n bots not
     * already under dynamic control, to generate measurable physics+broadcast load. Returns how many
     * were enabled. Idempotent: already-dynamic bots are left untouched.
     */
    static int load(int n) {
        if (n <= 0) {
            n = DEFAULT_LOAD;
        }
        int enabled = 0;
        for (World world : Server.getInstance().getWorlds()) {
            for (Character chr : world.getPlayerStorage().getAllCharacters()) {
                if (enabled >= n) {
                    return enabled;
                }
                if (chr == null || !BotHelpers.isBot(chr) || GCMovement.isEnabled(chr)) {
                    continue;
                }
                GCMovement.enable(chr);
                GCMovement.setFidget(chr, true);
                LOADED.add(chr.getId());
                enabled++;
            }
        }
        return enabled;
    }

    /* Release every bot enabled by .load(int) (disable fidget + dynamic control). */
    static int unload() {
        int released = 0;
        for (int id : LOADED) {
            Character bot = BotHelpers.getCharFromChannelStorage(id);
            if (bot != null) {
                GCMovement.setFidget(bot, false);
                GCMovement.disable(bot);
            }
            released++;
        }
        LOADED.clear();
        return released;
    }
}
