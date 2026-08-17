package soloMapling.ArtificialPlayer.BotTownSystem;

import org.gms.client.Character;
import org.gms.server.maps.MapleMap;
import soloMapling.ArtificialPlayer.BotSpotClaims;
import soloMapling.ArtificialPlayer.BotGrindSystem.BotSpotPicker;
import soloMapling.ArtificialPlayer.GCMoveSystem.GCMovement;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Disperse a bot from its arrival portal to a claimed, scattered ground spot in town and micro-fidget
// there, so returning training bots (and, later, stationed social cohorts) stop piling on the spawn
// portal as a clump of statues. Map-agnostic: takes a Character + an anchor, never a bot type, so more
// than one consumer can share it (Phase 4).
//
// Composition only, over primitives that already exist map-agnostically:
//   - BotSpotPicker picks the organic ground spot (walkable ledges, reachability-filtered from the
//     anchor, so a pick is never stranded on an island the bot can't path to);
//   - BotSpotClaims caps how many bots share one ledge, so a crowd returning to the same town within a
//     minute spreads across ledges instead of stacking (the MAX_BOTS_PER_SPOT lesson from grinding);
//   - GCMovement.move walks there when observed, or coarse-relocates with no packets when the map is
//     unobserved (the driver's analytic ETA path) - so an unobserved return is just "placed" for free;
//   - GCFidget adds turn/duck/hop/small-wander life once the bot settles.
// Our own creation (not a GreenCat extraction).
public final class TownLoiter {

    private TownLoiter() {
    }

    // How many bots may claim one ledge before we prefer another - keeps a returning crowd from clumping.
    private static final int CAPACITY_PER_LEDGE = 3;

    // Re-pick attempts when the first ledge we land on is already at capacity.
    private static final int PICK_ATTEMPTS = 4;

    // botId -> the ledge claim it holds, so stop() can release exactly what settle() took.
    private record Claim(int mapId, int ledgeId) {
    }

    private static final Map<Integer, Claim> ACTIVE = new ConcurrentHashMap<>();

    // Guards the release-prior + claim + record sequence so it is atomic against a concurrent stop().
    // settle() runs on the bot's macro tick; stop() also runs from teardown (stopScheduledTask) on another
    // thread - without this, a stop() landing mid-settle would find no claim to release and then settle
    // would record one, permanently leaking that ledge's capacity.
    private static final Object CLAIM_LOCK = new Object();

    // Scatter the bot across its whole current town from where it stands (its arrival portal), with idle
    // fidget once it settles. The common case for a training bot popping back to town between grinds.
    public static void settle(Character bot) {
        if (bot == null || bot.getMap() == null) {
            return;
        }
        Point p = bot.getPosition();
        settle(bot, p.x, p.y, 0, true);
    }

    // Settle near (anchorX,anchorY) within `radius` px (radius <= 0 = anywhere in town), optionally
    // fidgeting once arrived. Idempotent per bot: any prior claim/fidget is released first.
    public static void settle(Character bot, int anchorX, int anchorY, int radius, boolean fidget) {
        if (bot == null || bot.getMap() == null) {
            return;
        }
        MapleMap map = bot.getMap();
        int mapId = bot.getMapId();
        int botId = bot.getId();

        // Gather candidate (spot, ledgeId) pairs first, outside the lock - these are pure terrain reads, so
        // parallel settles don't serialize on them.
        List<int[]> candidateLedges = new ArrayList<>(); // ledgeId per candidate (-1 = unclaimable)
        List<Point> candidateSpots = new ArrayList<>();
        for (int i = 0; i < PICK_ATTEMPTS; i++) {
            Point spot = radius > 0
                    ? BotSpotPicker.pickGroundSpot(map, anchorX, anchorY, anchorX - radius, anchorX + radius)
                    : BotSpotPicker.pickGroundSpot(map, anchorX, anchorY);
            if (spot == null) {
                break; // nav graph not baked / no eligible ledge
            }
            candidateSpots.add(spot);
            candidateLedges.add(new int[]{GCMovement.regionIdAt(map, spot.x, spot.y)});
        }

        // Atomically drop any prior claim and take a new one, so a stop() on another thread can't slip
        // between the two and orphan the claim.
        Point chosen = null;
        synchronized (CLAIM_LOCK) {
            releaseLocked(botId); // idempotent re-settle: free the previous ledge before taking another
            for (int i = 0; i < candidateSpots.size(); i++) {
                Point spot = candidateSpots.get(i);
                int ledgeId = candidateLedges.get(i)[0];
                if (ledgeId < 0) {
                    chosen = spot; // no ledge to claim - accept uncontended
                    break;
                }
                if (BotSpotClaims.claim(mapId, ledgeId, CAPACITY_PER_LEDGE, botId) >= 0) {
                    ACTIVE.put(botId, new Claim(mapId, ledgeId));
                    chosen = spot;
                    break;
                }
                // ledge already full - try the next candidate
            }
        }
        if (chosen == null) {
            return; // no reachable spot right now; standing on the portal beats a crash
        }

        GCMovement.move(bot, chosen.x, chosen.y); // walk (observed) or coarse-relocate (unobserved)
        GCMovement.setFidget(bot, fidget); // true: stands down while walking then fidgets; false: cancels any prior
    }

    // End loitering: stop fidget and release the ledge claim. Idempotent - safe when not loitering. Call
    // on phase exit, retype, or teardown. Cancelling fidget unconditionally is safe: no other production
    // system drives GCFidget, so TownLoiter owns the toggle.
    public static void stop(Character bot) {
        if (bot == null) {
            return;
        }
        GCMovement.setFidget(bot, false);
        synchronized (CLAIM_LOCK) {
            releaseLocked(bot.getId());
        }
    }

    // Release the bot's ledge claim. Caller must hold CLAIM_LOCK.
    private static void releaseLocked(int botId) {
        Claim c = ACTIVE.remove(botId);
        if (c != null) {
            BotSpotClaims.release(c.mapId, c.ledgeId, botId);
        }
    }

    public static boolean isLoitering(Character bot) {
        return bot != null && ACTIVE.containsKey(bot.getId());
    }
}
