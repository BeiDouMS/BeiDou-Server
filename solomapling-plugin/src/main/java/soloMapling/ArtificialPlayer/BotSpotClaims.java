package soloMapling.ArtificialPlayer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

// Generic "spread N bots across spots without overlapping" registry. Not combat-specific: any bot
// system (grinding, town decoration, social grouping, FM spot allocation) can claim a slot on a keyed
// spot (a mapId + a spot id such as a ledge/region id) up to a capacity, get a stable non-overlapping
// X sub-range (section), and release it. Keyed by plain ids + bounds, so it has no dependency on any
// movement / nav internals.
//
// Slots are fixed indices: a holder keeps its slot until it releases, so its section never shifts when
// another holder leaves; a new claimer takes the lowest free slot.
public final class BotSpotClaims {

    private BotSpotClaims() {
    }

    // mapId -> spotId -> (botId -> slotIndex)
    private static final Map<Integer, Map<Integer, Map<Integer, Integer>>> CLAIMS = new ConcurrentHashMap<>();

    // Claim a slot on (mapId, spotId) if it isn't full. Returns the slot index [0, capacity) the bot
    // holds (its existing one if already claimed), or -1 if the spot is full.
    public static synchronized int claim(int mapId, int spotId, int capacity, int botId) {
        Map<Integer, Map<Integer, Integer>> bySpot = CLAIMS.computeIfAbsent(mapId, k -> new HashMap<>());
        Map<Integer, Integer> slots = bySpot.computeIfAbsent(spotId, k -> new HashMap<>());
        Integer existing = slots.get(botId);
        if (existing != null) {
            return existing;
        }
        int cap = Math.max(1, capacity);
        if (slots.size() >= cap) {
            return -1;
        }
        Set<Integer> used = new HashSet<>(slots.values());
        for (int s = 0; s < cap; s++) {
            if (!used.contains(s)) {
                slots.put(botId, s);
                return s;
            }
        }
        return -1;
    }

    public static synchronized void release(int mapId, int spotId, int botId) {
        Map<Integer, Map<Integer, Integer>> bySpot = CLAIMS.get(mapId);
        if (bySpot == null) {
            return;
        }
        Map<Integer, Integer> slots = bySpot.get(spotId);
        if (slots == null) {
            return;
        }
        slots.remove(botId);
        if (slots.isEmpty()) {
            bySpot.remove(spotId);
        }
        if (bySpot.isEmpty()) {
            CLAIMS.remove(mapId);
        }
    }

    public static synchronized int holders(int mapId, int spotId) {
        Map<Integer, Map<Integer, Integer>> bySpot = CLAIMS.get(mapId);
        if (bySpot == null) {
            return 0;
        }
        Map<Integer, Integer> slots = bySpot.get(spotId);
        return slots == null ? 0 : slots.size();
    }

    // The X sub-range [x0, x1] for slot of a capacity-K spot spanning [minX, maxX]. K=1 = whole span.
    public static int[] section(int minX, int maxX, int slot, int capacity) {
        int k = Math.max(1, capacity);
        int s = Math.max(0, Math.min(slot, k - 1));
        int width = Math.max(0, maxX - minX);
        int x0 = minX + (int) ((long) width * s / k);
        int x1 = minX + (int) ((long) width * (s + 1) / k);
        return new int[]{x0, x1};
    }
}
