package soloMapling.ArtificialPlayer.GCMoveSystem;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/*
 * Hysteresis for the LOD tier decision (M3): a map stays "active" for a dwell window after it stops
 * being observed, so a real player pacing back and forth across a portal can't flap the bots there
 * between full physics and coarse. Promotion is immediate (a map is active
 * the instant it's observed); only demotion waits out the dwell — "err toward FULL, over-render
 * is safe."
 *
 * A pure, clock-injected state machine (no System.currentTimeMillis inside), so it is
 * deterministically unit-testable with a fake clock (see TierDwellTest). Not thread-safe:
 * .observe is driven by the single observer-poll thread; .isActive/.active
 * read the published immutable snapshot and are safe to call from any thread.
 */
final class TierDwell {
    private final long dwellMs;
    private final Map<Integer, Long> stickyUntil = new HashMap<>();
    private volatile Set<Integer> active = Set.of();

    TierDwell(long dwellMs) {
        this.dwellMs = dwellMs;
    }

    /*
     * Record the maps observed at wall-clock nowMs and recompute the active set: every observed
     * map's dwell deadline is refreshed to now + dwellMs, and any map whose deadline has passed
     * is dropped. The surviving keys are the active set.
     */
    void observe(Set<Integer> observed, long nowMs) {
        long deadline = nowMs + dwellMs;
        for (int mapId : observed) {
            stickyUntil.put(mapId, deadline);
        }
        stickyUntil.entrySet().removeIf(e -> e.getValue() <= nowMs);
        active = stickyUntil.isEmpty() ? Set.of() : Set.copyOf(stickyUntil.keySet());
    }

    boolean isActive(int mapId) {
        return active.contains(mapId);
    }

    Set<Integer> active() {
        return active;
    }

    int size() {
        return active.size();
    }
}
