package soloMapling.ArtificialPlayer.BotTypes.OPQ;

import org.gms.client.Character;
import org.gms.server.maps.MapleMap;
import org.gms.server.maps.Reactor;
import soloMapling.ArtificialPlayer.BotTypes.OPQ.OPQSharedContext.OPQPhase;
import soloMapling.server.ExecutorServiceManager;

import java.awt.Point;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static soloMapling.ArtificialPlayer.BotGeneration.warpBotToLocation;
import static soloMapling.ArtificialPlayer.BotLogic.isNpcPresent;
import static soloMapling.ArtificialPlayer.BotTypes.OPQ.OPQConstants.CHAMBERLAIN_EAK;
import static soloMapling.ArtificialPlayer.BotTypes.OPQ.OPQConstants.STAGE_1_COMPLETE_TP;
import static soloMapling.BotLogger.log;

/**
 * Coordinator for a single OPQ run.
 * <p>
 * Responsibilities:
 * - Register / unregister OPQ bots
 * - Track the party leader and warp bots into Stage 1 when the leader enters
 * (bots have no real client, so the OPQ NPC script does not warp them — the
 * orchestrator must do it server-side)
 * - Hand out unique cloud reactor assignments (closest unclaimed live reactor)
 * - Poll stage completion and flip the shared flags
 * - Reset per-run state between runs
 * <p>
 * Non-responsibilities: ANY fine-grained bot behavior. The orchestrator never
 * tells a bot what state to be in — bots decide their own state from map + the
 * shared context. This is a pure blackboard pattern.
 */
public class OPQOrchestrator {

    private static final long TICK_PERIOD_MS = 1000; // 1 Hz is plenty

    private static final OPQOrchestrator INSTANCE = new OPQOrchestrator();

    public static OPQOrchestrator getInstance() {
        return INSTANCE;
    }

    private final OPQSharedContext context = new OPQSharedContext();
    private final List<OPQBot> registeredBots = new CopyOnWriteArrayList<>();

    // Leader tracking: cached on first observation by any bot, refreshed each tick.
    private volatile int leaderId = -1;

    // Stage 2: sorted box reactor index (oid -> sorted position 0-6, right-to-left).
    // Built lazily on first Stage 2 assignment. Cleared on reset.
    private final Map<Integer, Integer> boxSortedIndex = new HashMap<>();

    private ScheduledFuture<?> tickHandle;

    private OPQOrchestrator() {
    }

    private static void opqLog(String msg) {
        log("[OPQOrchestrator] " + msg);
    }

    // =========================================================================
    // Public API
    // =========================================================================

    public OPQSharedContext getSharedContext() {
        return context;
    }

    public synchronized void registerBot(OPQBot bot) {
        if (bot == null) return;
        if (!registeredBots.contains(bot)) {
            registeredBots.add(bot);
            opqLog("Registered bot: " + bot.getChr().getName());
        }
        ensureTickRunning();
    }

    public synchronized void unregisterBot(OPQBot bot) {
        if (bot == null) return;
        registeredBots.remove(bot);
        int id = bot.getChr().getId();
        context.putCloudAssignment(id, null);
        context.putBoxAssignment(id, null);
        context.putPlatformAssignment(id, null);
        context.clearTaskComplete(id);
        opqLog("Unregistered bot: " + bot.getChr().getName());
        if (registeredBots.isEmpty()) {
            stopTick();
        }
    }

    /**
     * Bots call this whenever they observe themselves in a party so the
     * orchestrator can lock onto the leader id. Cheap; safe to call every tick.
     */
    public void noteLeaderFromBot(OPQBot bot) {
        if (bot.getChr().getParty() == null) return;
        int observedLeader = bot.getChr().getParty().getLeaderId();
        if (observedLeader > 0 && observedLeader != leaderId) {
            leaderId = observedLeader;
            opqLog("Leader id locked: " + leaderId);
        }
        // Auto-activate the run once any bot is partied. Without this the
        // tick would no-op (pqActive=false) and followLeaderWarp would never
        // fire. Calling !opq start manually still works and is idempotent.
        if (!context.isPqActive()) {
            context.setPqActive(true);
            opqLog("Auto-activated run on first leader observation.");
        }
    }

    /**
     * Reserve a unique cloud reactor for this bot and return its oid.
     * Picks the closest live reactor not already assigned to another bot.
     * Returns null if there are no free live reactors.
     */
    public synchronized Integer assignCloudReactor(OPQBot bot) {
        int id = bot.getChr().getId();
        MapleMap map = bot.getChr().getMap();
        if (map == null) return null;

        // Re-validate any existing assignment first — if dead, drop it and re-pick.
        Integer existing = context.getMyCloudAssignment(id);
        if (existing != null) {
            Reactor r = map.getReactorByOid(existing);
            if (r != null && r.isAlive() && r.getState() < 4) {
                return existing;
            }
            opqLog("Dropping stale cloud assignment oid=" + existing + " for " + bot.getChr().getName());
            context.putCloudAssignment(id, null);
        }

        Collection<Integer> taken = context.getCloudAssignmentsView().values();
        Reactor best = null;
        double bestDist = Double.MAX_VALUE;
        Point botPos = bot.getChr().getPosition();
        for (Reactor r : map.getAllReactors()) {
            if (!isCloudReactor(r)) continue;
            if (!r.isAlive() || r.getState() >= 4) continue;
            if (taken.contains(r.getObjectId())) continue;
            double d = botPos.distance(r.getPosition());
            if (d < bestDist) {
                bestDist = d;
                best = r;
            }
        }
        if (best == null) {
            opqLog("No free live cloud reactor for " + bot.getChr().getName()
                    + " (alive on map=" + map.getAllReactors().size() + ", taken=" + taken.size() + ")");
            return null;
        }
        int pickedOid = best.getObjectId();
        context.putCloudAssignment(id, pickedOid);
        opqLog("Assigned cloud reactor oid=" + pickedOid + " (id=" + best.getId()
                + " pos=" + best.getPosition() + ") -> " + bot.getChr().getName());
        return pickedOid;
    }

    /**
     * Whether the given map still has at least one cloud reactor (data id in
     * the OPQ stage-1 cloud range) that is alive and not already claimed by
     * another bot. Used by bots to decide whether to loop back for another
     * cloud or transition out of stage-1 work.
     */
    public boolean hasUnclaimedLiveCloudReactor(MapleMap map, int askingBotId) {
        if (map == null) return false;
        Collection<Integer> taken = context.getCloudAssignmentsView().values();
        Integer mine = context.getMyCloudAssignment(askingBotId);
        for (Reactor r : map.getAllReactors()) {
            if (!isCloudReactor(r)) continue;
            if (!r.isAlive() || r.getState() >= 4) continue;
            int oid = r.getObjectId();
            // Don't count reactors taken by *other* bots; the asker's own
            // (already-broken) assignment will get cleared before re-querying.
            if (taken.contains(oid) && (mine == null || mine != oid)) continue;
            return true;
        }
        return false;
    }

    /**
     * Reserve a unique box reactor for this bot and return its oid.
     * Picks the closest live box reactor (state == 0, not claimed by another bot).
     * Skips reactors with state > 0 (may be hit by a real player).
     * Returns null if no free untouched box reactors remain.
     */
    public synchronized Integer assignBoxReactor(OPQBot bot) {
        int id = bot.getChr().getId();
        MapleMap map = bot.getChr().getMap();
        if (map == null) return null;

        buildBoxSortedIndex(map);

        Integer existing = context.getMyBoxAssignment(id);
        if (existing != null) {
            Reactor r = map.getReactorByOid(existing);
            if (r != null && r.isAlive() && r.getState() < 4) {
                return existing;
            }
            opqLog("Dropping stale box assignment oid=" + existing + " for " + bot.getChr().getName());
            context.putBoxAssignment(id, null);
        }

        Collection<Integer> taken = context.getBoxAssignmentsView().values();
        Reactor best = null;
        double bestDist = Double.MAX_VALUE;
        Point botPos = bot.getChr().getPosition();
        for (Reactor r : map.getAllReactors()) {
            if (!isBoxReactor(r)) continue;
            if (!r.isAlive() || r.getState() >= 4) continue;
            // Skip reactors partially hit by someone else (real player)
            if (r.getState() > 0 && !taken.contains(r.getObjectId())) continue;
            if (taken.contains(r.getObjectId())) continue;
            double d = botPos.distance(r.getPosition());
            if (d < bestDist) {
                bestDist = d;
                best = r;
            }
        }
        if (best == null) {
            opqLog("No free box reactor for " + bot.getChr().getName()
                    + " (alive on map=" + map.getAllReactors().size() + ", taken=" + taken.size() + ")");
            return null;
        }
        int pickedOid = best.getObjectId();
        context.putBoxAssignment(id, pickedOid);
        String ordinal = getBoxOrdinal(best.getId());
//        opqLog("Assigned box reactor oid=" + pickedOid + " (" + ordinal
//                + " box, id=" + best.getId() + " pos=" + best.getPosition()
//                + ") -> " + bot.getChr().getName());
        return pickedOid;
    }

    /**
     * Whether the given map still has at least one box reactor that is alive,
     * untouched (state == 0), and not claimed by another bot.
     */
    public boolean hasUnclaimedLiveBoxReactor(MapleMap map, int askingBotId) {
        if (map == null) return false;
        Collection<Integer> taken = context.getBoxAssignmentsView().values();
        for (Reactor r : map.getAllReactors()) {
            if (!isBoxReactor(r)) continue;
            if (!r.isAlive() || r.getState() >= 4) continue;
            if (r.getState() > 0 && !taken.contains(r.getObjectId())) continue;
            int oid = r.getObjectId();
            if (taken.contains(oid)) continue;
            return true;
        }
        return false;
    }

    /**
     * Reserve a unique stage-2 platform for this bot and return it.
     */
    public synchronized String assignPlatformTarget(OPQBot bot) {
        int id = bot.getChr().getId();
        String existing = context.getMyPlatformAssignment(id);
        if (existing != null) return existing;

        String pick = pickUnusedString(OPQConstants.STAGE_2_BOX_PLATFORMS,
                context.getPlatformAssignmentsView().values());
        if (pick != null) {
            context.putPlatformAssignment(id, pick);
            opqLog("Assigned box " + pick + " -> " + bot.getChr().getName());
        } else {
            opqLog("No free box platform for " + bot.getChr().getName()
                    + " (pool size=" + OPQConstants.STAGE_2_BOX_PLATFORMS.size()
                    + ", taken=" + context.getPlatformAssignmentsView().size() + ")");
        }
        return pick;
    }

    /**
     * Called externally (e.g. from a debug command) when the leader starts a new run.
     */
    public synchronized void resetForNewRun() {
        opqLog("Resetting for a new run.");
        context.reset();
        context.setPqActive(true);
        context.setCurrentPhase(OPQPhase.RECRUITMENT);
        leaderId = -1;
        boxSortedIndex.clear();
    }

    public void mirrorPhase(OPQPhase phase) {
        OPQPhase prev = context.getCurrentPhase();
        if (prev != phase) {
            context.setCurrentPhase(phase);
            opqLog("Phase mirrored: " + prev + " -> " + phase);
        }
    }

    /**
     * Hard stop — orchestrator idles, bots fall back to INACTIVE.
     */
    public synchronized void shutdownRun() {
        opqLog("Shutting down current run.");
        context.reset();
        leaderId = -1;
        boxSortedIndex.clear();
    }

    // =========================================================================
    // Internal tick
    // =========================================================================

    private void ensureTickRunning() {
        if (tickHandle != null && !tickHandle.isCancelled() && !tickHandle.isDone()) return;
        tickHandle = ExecutorServiceManager.getScheduledExecutorService()
                .scheduleWithFixedDelay(this::safeTick,
                        TICK_PERIOD_MS, TICK_PERIOD_MS, TimeUnit.MILLISECONDS);
    }

    private void stopTick() {
        if (tickHandle != null) {
            tickHandle.cancel(false);
            tickHandle = null;
        }
    }

    private void safeTick() {
        try {
            tick();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Tick responsibilities:
     * 1. Drag bots into Stage 1 when the leader's mapId reads OPQ_STAGE_1.
     * 2. Flip stage-complete flags based on the leader's observed state.
     * <p>
     * Stage 1 completion: bots are not the trigger — they only know they've
     * dropped their clouds. The real "done" signal is the NPC handin, which
     * teleports the leader up to the top platform. We poll the leader's
     * position and flip stage1Complete once they land in the tolerance box.
     */
    private void tick() {
        if (!context.isPqActive()) return;

        if (allBotsLostParty()) {
            opqLog("All registered bots lost their party — resetting run.");
            resetForNewRun();
            return;
        }

        OPQPhase phase = context.getCurrentPhase();
        switch (phase) {
            case STAGE_1 -> {
                if (!context.isStage1Complete() && isChamberlainSpawned()) {
                    opqLog("Stage 1 complete — Chamberlain Spawned");
                    context.setStage1Complete(true);
                    clearCompletionFlagsForRegistered();
                }
            }
            case STAGE_2 -> {
                if (!context.isStage2Complete() && allRegisteredBotsComplete()) {
                    opqLog("Stage 2 complete — all registered bots reported done.");
                    context.setStage2Complete(true);
                    clearCompletionFlagsForRegistered();
                }
            }
            default -> { /* no-op */ }
        }
    }

    /**
     * Resolve the live leader Character from any registered bot's channel
     * server. Returns null if there's no leader yet, no bots registered, or
     * the leader has logged out / changed channels.
     */
    private Character resolveLeader() {
        if (leaderId <= 0 || registeredBots.isEmpty()) return null;
        OPQBot anchor = registeredBots.get(0);
        return anchor.getChr().getClient().getChannelServer()
                .getPlayerStorage().getCharacterById(leaderId);
    }

    public boolean isChamberlainSpawned() {
        return isNpcPresent(resolveLeader().getMap(), CHAMBERLAIN_EAK);
    }


    /**
     * If the leader has entered Stage 1, drag every registered bot in with
     * 1. Leader entered via the OPQ NPC
     * 2. Leader completes stage 1 by talking to NPC, who teleports him to the tower
     */
    public void followLeaderWarp(Character fakechar, Point pt) {
        Character leader = resolveLeader();
        if (leader == null) return;

        try {
            warpBotToLocation(fakechar, pt, leader.getMap());
        } catch (Exception e) {
            opqLog("Warp failed for " + fakechar.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private boolean allBotsLostParty() {
        if (registeredBots.isEmpty()) return false;
        for (OPQBot bot : registeredBots) {
            if (bot.getChr().getParty() != null) return false;
        }
        return true;
    }

    private enum StagePhase {STAGE_1, STAGE_2}

    private boolean allAssignedBotsComplete(StagePhase stage) {
        int assigned = 0;
        int done = 0;
        for (OPQBot bot : registeredBots) {
            int id = bot.getChr().getId();
            Object assignment = (stage == StagePhase.STAGE_1)
                    ? context.getMyCloudAssignment(id)
                    : context.getMyPlatformAssignment(id);
            if (assignment == null) continue;
            assigned++;
            if (context.isMyTaskComplete(id)) done++;
        }
        return assigned > 0 && assigned == done;
    }

    private boolean allRegisteredBotsComplete() {
        if (registeredBots.isEmpty()) return false;
        for (OPQBot bot : registeredBots) {
            if (!context.isMyTaskComplete(bot.getChr().getId())) return false;
        }
        return true;
    }

    private void clearCompletionFlagsForRegistered() {
        for (OPQBot bot : registeredBots) {
            context.clearTaskComplete(bot.getChr().getId());
        }
    }

    private static boolean isCloudReactor(Reactor r) {
        return r.getId() == OPQConstants.STAGE_1_CLOUD_REACTOR_ID;
    }

    private static boolean isBoxReactor(Reactor r) {
        int id = r.getId();
        return id >= OPQConstants.STAGE_2_BOX_REACTOR_FIRST
                && id <= OPQConstants.STAGE_2_BOX_REACTOR_LAST;
    }

    /**
     * Build the sorted index for box reactors on the given map.
     * Sorts by x-position descending (rightmost = index 0 = "1st").
     * Called lazily on first Stage 2 assignment.
     */
    private void buildBoxSortedIndex(MapleMap map) {
        if (!boxSortedIndex.isEmpty()) return;
        List<Reactor> boxes = new ArrayList<>();
        for (Reactor r : map.getAllReactors()) {
            if (isBoxReactor(r)) boxes.add(r);
        }
        boxes.sort(Comparator.comparingInt((Reactor r) -> r.getPosition().x).reversed());
        for (int i = 0; i < boxes.size(); i++) {
            boxSortedIndex.put(boxes.get(i).getObjectId(), i);
            opqLog("Box sorted index: pos=" + i + " (" + OPQConstants.STAGE_2_BOX_ORDINALS[i]
                    + ") oid=" + boxes.get(i).getObjectId()
                    + " dataId=" + boxes.get(i).getId()
                    + " x=" + boxes.get(i).getPosition().x);
        }
    }

    /**
     * Get the ordinal label for a box reactor based on its sorted position (right-to-left).
     */
    public String getBoxOrdinal(int reactorOid) {
        Integer idx = boxSortedIndex.get(reactorOid);
        if (idx != null && idx >= 0 && idx < OPQConstants.STAGE_2_BOX_ORDINALS.length) {
            return OPQConstants.STAGE_2_BOX_ORDINALS[idx];
        }
        return "?";
    }

    /**
     * Get the deterministic item ID that a box reactor should drop, based on sorted position.
     * Box at sorted index 0 (rightmost/"1st") drops STAGE_2_ITEMS[0] = 4001056, etc.
     */
    public int getBoxItemId(int reactorOid) {
        Integer idx = boxSortedIndex.get(reactorOid);
        if (idx != null && idx >= 0 && idx < OPQConstants.STAGE_2_ITEMS.size()) {
            return OPQConstants.STAGE_2_ITEMS.get(idx);
        }
        return OPQConstants.STAGE_2_ITEMS.get(0);
    }

    /**
     * Find the music box reactor (central drop zone) on the given map and return its position.
     */
    public static Point getMusicBoxPosition(MapleMap map) {
        if (map == null) return null;
        for (Reactor r : map.getAllReactors()) {
            if (r.getId() == OPQConstants.STAGE_2_MUSIC_BOX_REACTOR_ID) {
                return r.getPosition();
            }
        }
        return null;
    }

    private static String pickUnusedString(List<String> pool, Iterable<String> used) {
        Deque<String> taken = new ArrayDeque<>();
        used.forEach(taken::add);
        for (String candidate : pool) {
            if (!taken.contains(candidate)) return candidate;
        }
        return null;
    }
}
