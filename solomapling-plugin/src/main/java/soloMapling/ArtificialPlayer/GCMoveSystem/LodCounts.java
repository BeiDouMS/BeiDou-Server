package soloMapling.ArtificialPlayer.GCMoveSystem;

// Ours (not part of the GreenCatMS port). Public read-only bridge so code outside
// this package (!env perf) can read observation-tier counts without widening
// ObserverTracker's package-private surface. Lives here only because it must
// touch package internals.
public final class LodCounts {

    private LodCounts() {
    }

    public static int fullMaps() {
        return ObserverTracker.fullCount();
    }

    public static int haloMaps() {
        return ObserverTracker.haloCount();
    }

    public static int activeMaps() {
        return ObserverTracker.activeCount();
    }

    public static int dynamicBots() {
        return GCMovement.enabledStates().size();
    }

    // True once the 1s observer poll is running (started by the first GCMovement.enable).
    // Callers that gate behavior on tier queries should fall back to their old path when
    // this is false, so a world with zero GC-movement bots still behaves.
    public static boolean trackerRunning() {
        return ObserverTracker.started();
    }

    // A real player is on this map right now (FULL tier).
    public static boolean isMapFull(int mapId) {
        return ObserverTracker.isFull(mapId);
    }

    // FULL or HALO or inside the post-observation dwell - "someone can (nearly) see this map".
    public static boolean isMapActive(int mapId) {
        return ObserverTracker.isActiveMap(mapId);
    }
}
