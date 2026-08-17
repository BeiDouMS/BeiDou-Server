package soloMapling.ArtificialPlayer.BotGrindSystem;

import org.gms.client.Character;
import org.gms.server.life.Monster;
import org.gms.server.maps.MapObject;
import soloMapling.ArtificialPlayer.BotSpotClaims;
import soloMapling.ArtificialPlayer.GCMoveSystem.GCMovement;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

// STACK: the vertical "leash tether" grind for maps whose feed lives in vertically layered ledges
// (slime tree, golem temple, Kerning subway car roofs). The whole stack is claimed as ONE composite
// spot on its primary member; claim slots map to member LEDGES (not X bands), so bots layer visibly
// across the levels — each works its own floor of the tree and hunts neighbouring floors only when
// its own runs dry. Acquisition relaxes the same-ledge gate INSIDE the tether box only (target must
// stand on a member ledge); approach goes through the shared skill-move (mage up/down blinks via the
// axis-aware gate, thieves hop/rope via nav). Admission is gated by GrindStylePolicy to classes that
// can actually traverse the stack. Ours (SoloMapling); map-archetype grinding, STACK phase.
final class StackStrategy implements GrindStrategy {

    private static final long WAIT_PATIENCE_MS = 5_000;      // stack lull tolerance before considering a relocate
    private static final long UNPRODUCTIVE_MS = 20_000;      // cumulative no-kill window that (with patience) relocates
    private static final long LEDGE_FALLBACK_MS = 2_500;     // assigned ledge dry this long -> hunt the whole stack
    private static final long RELOCATE_EXCLUDE_MS = 30_000;  // down-weight a just-left stack so it isn't re-picked at once
    private static final int SELECT_CLAIM_ATTEMPTS = 3;      // re-pick if a stack fills under us (cohort race)
    private static final int TETHER_Y_PAD = 60;              // tether box vertical slack past the top/bottom ledges
    private static final int CLUSTER_RADIUS_PX = 160;        // densest-pack targeting radius (same as camp)
    private static final int RECENTER_EPS = 100;             // WAIT walks back to the assigned anchor beyond this offset
    private static final double CROWDING_W = 30.0;           // per claimant on a candidate stack (spread the cohort)
    private static final double DISTANCE_W = 0.015;          // per px to the stack (prefer near)
    private static final double FEED_W = 10.0;               // per harvestable spawn in the stack
    private static final double SELECT_JITTER = 12.0;        // decorrelate identical cohorts

    enum State { SELECT_STACK, TRAVEL, FIGHT, WAIT, RELOCATE }

    private final GrindBrain b;

    private volatile State state = State.SELECT_STACK;
    private volatile SpotStack stack;            // the claimed stack
    private volatile List<Spot> members;         // resolved member spots (top-down, same order as spotIndices)
    private volatile Spot assigned;              // this bot's member ledge (null = squatter/whole-stack)
    private volatile int primaryIdx = -1;        // claim key: the stack's primary spot index
    private int claimMapId = -1;                 // claim released against the RECORDED map + bot (never the current map)
    private int claimBotId = -1;
    private volatile int claimedSlot = -1;
    private long ledgeEmptySinceMs = 0L;         // assigned ledge last read empty (0 = feeding)
    private boolean ledgeFallback = false;       // hunting the whole stack because the assigned ledge dried
    private int excludedPrimaryIdx = -1;         // just-left stack, down-weighted for RELOCATE_EXCLUDE_MS
    private long excludedUntilMs = 0L;
    private long waitStartedMs = 0L;
    private volatile boolean mapSaturated = false;

    StackStrategy(GrindBrain brain) {
        this.b = brain;
    }

    @Override
    public GrindStyle style() {
        return GrindStyle.STACK;
    }

    @Override
    public void start(Character chr) {
        doSelectStack(chr);
    }

    @Override
    public void tick(Character chr) {
        switch (state) {
            case SELECT_STACK -> doSelectStack(chr);
            case TRAVEL -> doTravel(chr);
            case FIGHT -> doFight(chr);
            case WAIT -> doWait(chr);
            case RELOCATE -> state = State.SELECT_STACK; // claim already released on the way into RELOCATE
        }
    }

    @Override
    public void releaseUnderLock(Character chr) {
        releaseClaimLocked();
        stack = null;
        members = null;
        assigned = null;
        claimedSlot = -1;
        ledgeEmptySinceMs = 0L;
        ledgeFallback = false;
        mapSaturated = false;
        state = State.SELECT_STACK;
    }

    @Override
    public void resetEpisodeUnderLock() {
        releaseClaimLocked();
        stack = null;
        members = null;
        assigned = null;
        claimedSlot = -1;
        ledgeEmptySinceMs = 0L;
        ledgeFallback = false;
        excludedPrimaryIdx = -1;
        excludedUntilMs = 0L;
        waitStartedMs = 0L;
        mapSaturated = false;
        state = State.SELECT_STACK;
    }

    @Override
    public void resetAfterTeleport(Character chr) {
        state = State.SELECT_STACK;
    }

    @Override
    public boolean saturated() {
        return mapSaturated;
    }

    @Override
    public String label() {
        SpotStack st = stack;
        Spot a = assigned;
        return st == null ? "[no-stack]"
                : "[stack x" + st.x0() + ".." + st.x1() + " ledges " + st.spotIndices().size()
                + " feed " + st.totalFeed() + (a != null ? " @y" + a.anchor().y : " (whole)") + "]";
    }

    // The tether's X range (all beats — turn, kite, hop, loot — stay inside the stack).
    @Override
    public int[] leash(Character chr) {
        SpotStack st = stack;
        if (st == null) {
            Point p = (chr != null) ? chr.getPosition() : null;
            int cx = (p != null) ? p.x : 0;
            return new int[]{cx - GrindBrain.APPROACH_X, cx + GrindBrain.APPROACH_X};
        }
        return new int[]{st.x0() - GrindBrain.ACQUIRE_MARGIN_PX, st.x1() + GrindBrain.ACQUIRE_MARGIN_PX};
    }

    // Hops may land on any tether ground (a lower member ledge is fine); the awayHopLands Y check
    // still keeps single hops near the bot's own level.
    @Override
    public Point groundAt(Character chr, int x) {
        Point p = (chr != null) ? chr.getPosition() : null;
        if (p == null || chr.getMap() == null) {
            return null;
        }
        return GCMovement.groundPointBelow(chr.getMap(), x, p.y);
    }

    // ── States ──

    private void doSelectStack(Character chr) {
        if (chr == null || chr.getMap() == null) {
            return;
        }
        MapGrindProfile p = SpotFinder.profile(chr.getMap());
        if (p == null || p.stacks().isEmpty()) {
            // No stack on this map (forced style, or profile changed under us): behave like a dry
            // select — the macro watchdog bails the map if this persists.
            b.narrate("no stack on this map -> waiting");
            return;
        }
        if (!selectAndClaim(chr, p)) {
            return;
        }
        b.resetApproachProgress(chr);
        b.lastKillMs = now();
        Spot dest = (assigned != null) ? assigned : members.get(members.size() - 1);
        Point pos = chr.getPosition();
        if (pos != null && Math.abs(pos.x - dest.anchor().x) <= dest.radius()
                && Math.abs(pos.y - dest.anchor().y) <= GrindBrain.APPROACH_Y_TOLERANCE) {
            b.narrate("FIGHT " + label());
            state = State.FIGHT;
        } else {
            GCMovement.move(chr, dest.anchor().x, dest.anchor().y);
            b.lastMoveTargetX = dest.anchor().x;
            b.narrate("TRAVEL -> " + label());
            state = State.TRAVEL;
        }
    }

    // Score + claim a stack under the brain's claim lock (same ghost-claim protections as camp).
    // Claim key = the stack's primary member spot index; capacity = one bot per member ledge (capped).
    // Slot i maps to member ledge i counted bottom-up, so early claimers take the lower (arrival-side)
    // floors and later ones layer upward.
    private boolean selectAndClaim(Character chr, MapGrindProfile p) {
        synchronized (b.claimLock) {
            if (!b.claimActive) {
                return false; // released mid-flight — claiming now would leak
            }
            releaseClaimLocked();
            Point pos = chr.getPosition();
            SpotStack picked = null;
            int pickedPrimary = -1;
            boolean claimed = false;
            int slot = -1;
            for (int attempt = 0; attempt < SELECT_CLAIM_ATTEMPTS; attempt++) {
                picked = pickBestStack(chr, p, pos);
                if (picked == null) {
                    stack = null;
                    mapSaturated = false;
                    b.narrate("no traversable stack -> waiting");
                    return false;
                }
                pickedPrimary = primaryIndexOf(p, picked);
                int cap = Math.min(picked.spotIndices().size(), SpotFinder.SHARE_CAP_MAX);
                slot = BotSpotClaims.claim(chr.getMapId(), pickedPrimary, cap, chr.getId());
                if (slot >= 0) {
                    claimed = true;
                    break;
                }
            }
            mapSaturated = !claimed; // every traversable stack full -> sharing -> macro crowd-bail signal
            if (!claimed) {
                slot = BotSpotClaims.claim(chr.getMapId(), pickedPrimary, Integer.MAX_VALUE, chr.getId());
                b.narrate("all stacks taken -> sharing the least-crowded one");
            }
            stack = picked;
            primaryIdx = pickedPrimary;
            claimMapId = chr.getMapId();
            claimBotId = chr.getId();
            claimedSlot = slot;
            List<Spot> mem = new ArrayList<>(picked.spotIndices().size());
            for (int idx : picked.spotIndices()) {
                mem.add(p.spots().get(idx));
            }
            members = List.copyOf(mem);
            int memberCount = members.size();
            // Bottom-up ledge assignment; a squatter slot past the member count hunts the whole stack.
            assigned = (slot < memberCount) ? members.get(memberCount - 1 - slot) : null;
            ledgeEmptySinceMs = 0L;
            ledgeFallback = false;
            return true;
        }
    }

    // Best traversable stack for this bot: feed-heavy, uncrowded, near — the camp pickBest shape
    // applied to stacks. Skips the just-left stack while its cooldown holds.
    private SpotStack pickBestStack(Character chr, MapGrindProfile p, Point pos) {
        SpotStack best = null;
        double bestScore = -Double.MAX_VALUE;
        for (SpotStack st : p.stacks()) {
            if (!GrindStylePolicy.canTraverse(b.style, st)) {
                continue;
            }
            int primary = primaryIndexOf(p, st);
            if (primary == excludedPrimaryIdx && now() < excludedUntilMs) {
                continue;
            }
            int holders = BotSpotClaims.holders(chr.getMapId(), primary);
            double dist = (pos != null)
                    ? pos.distance(new Point((st.x0() + st.x1()) / 2, (st.topY() + st.bottomY()) / 2))
                    : 0;
            double score = FEED_W * st.totalFeed()
                    - CROWDING_W * holders
                    - DISTANCE_W * dist
                    + ThreadLocalRandom.current().nextDouble() * SELECT_JITTER;
            if (score > bestScore) {
                bestScore = score;
                best = st;
            }
        }
        return best;
    }

    // The claim key for a stack: its feed-heaviest member's spot index (stable — the profile's spot
    // list is cached per map), so camp claimants on the same platform show up in holders() too.
    private static int primaryIndexOf(MapGrindProfile p, SpotStack st) {
        int bestIdx = st.spotIndices().get(0);
        int bestFeed = -1;
        for (int idx : st.spotIndices()) {
            int feed = p.spots().get(idx).sameLedgeSpawnCount();
            if (feed > bestFeed) {
                bestFeed = feed;
                bestIdx = idx;
            }
        }
        return bestIdx;
    }

    private void doTravel(Character chr) {
        SpotStack st = stack;
        Spot dest = (assigned != null) ? assigned : (members != null && !members.isEmpty() ? members.get(members.size() - 1) : null);
        if (st == null || dest == null) {
            state = State.SELECT_STACK;
            return;
        }
        Point pos = chr.getPosition();
        if (pos != null && Math.abs(pos.x - dest.anchor().x) <= dest.radius()
                && Math.abs(pos.y - dest.anchor().y) <= GrindBrain.APPROACH_Y_TOLERANCE) {
            GCMovement.stop(chr);
            b.resetApproachProgress(chr);
            b.narrate("FIGHT " + label());
            state = State.FIGHT;
            return;
        }
        if (b.madeApproachProgress(chr)) {
            b.markProgress();
        } else if (now() > b.retargetDeadline) {
            toRelocate(); // can't reach the assigned floor -> exclude this stack and re-pick
        }
    }

    private void doFight(Character chr) {
        SpotStack st = stack;
        if (st == null || members == null) {
            state = State.SELECT_STACK;
            return;
        }
        Monster t = stickyTarget(chr, st);
        if (t == null) {
            t = acquireTarget(chr, st);
            b.targetOid = (t != null) ? t.getObjectId() : -1;
            b.resetApproachProgress(chr);
        }
        if (t == null) {
            enterWait(chr);
            return;
        }
        b.loot.grabLootAtFeet(chr);
        if (b.inAttackRange(chr, t)) {
            if (b.engage.preSwingAdjust(chr, t)) {
                return;
            }
            b.engage.engageAndSwing(chr, t);
            return;
        }
        b.engaged = false;
        approachTether(chr, st, t);
        if (b.madeApproachProgress(chr)) {
            b.markProgress();
        } else if (now() > b.retargetDeadline) {
            b.targetOid = -1;
            b.narrateGiveUp();
        }
    }

    private void enterWait(Character chr) {
        b.engaged = false;
        waitStartedMs = now();
        // Spend the lull back on the assigned floor's anchor so layered bots hold visible levels.
        Spot a = assigned;
        Point pos = chr.getPosition();
        if (a != null && pos != null
                && (Math.abs(pos.x - a.anchor().x) > RECENTER_EPS
                    || Math.abs(pos.y - a.anchor().y) > GrindBrain.APPROACH_Y)) {
            GCMovement.move(chr, a.anchor().x, a.anchor().y);
            b.lastMoveTargetX = a.anchor().x;
        } else {
            GCMovement.stop(chr);
        }
        b.narrate("WAIT (stack lull) " + label());
        state = State.WAIT;
    }

    private void doWait(Character chr) {
        SpotStack st = stack;
        if (st == null) {
            state = State.SELECT_STACK;
            return;
        }
        Monster t = acquireTarget(chr, st);
        if (t != null) {
            b.targetOid = t.getObjectId();
            b.resetApproachProgress(chr);
            b.narrate("FIGHT " + label());
            state = State.FIGHT;
            return;
        }
        b.markProgress(); // patience at a claimed stack is healthy waiting, not a wedge
        int[] leash = leash(chr);
        if (b.loot.tryWalkAndLoot(chr, leash[0], leash[1], Math.max(300, (st.x1() - st.x0()) / 2))) {
            return;
        }
        if (now() - waitStartedMs >= WAIT_PATIENCE_MS && now() - b.lastKillMs >= UNPRODUCTIVE_MS) {
            toRelocate();
        }
    }

    private void toRelocate() {
        synchronized (b.claimLock) {
            excludedPrimaryIdx = primaryIdx;
            excludedUntilMs = now() + RELOCATE_EXCLUDE_MS;
            releaseClaimLocked();
            stack = null;
            members = null;
            assigned = null;
            claimedSlot = -1;
            ledgeEmptySinceMs = 0L;
            ledgeFallback = false;
            b.targetOid = -1;
        }
        b.narrate("RELOCATE (stack dry)");
        state = State.RELOCATE;
    }

    // Must hold b.claimLock.
    private void releaseClaimLocked() {
        if (primaryIdx >= 0 && claimMapId >= 0) {
            BotSpotClaims.release(claimMapId, primaryIdx, claimBotId);
        }
        primaryIdx = -1;
        claimMapId = -1;
    }

    // ── Targeting ──

    // Assigned-floor-first acquisition inside the tether box; a floor that stays dry past a short
    // lull widens the hunt to the whole stack (kills anywhere in the stack are fine, starvation
    // isn't) until the assigned floor feeds again. Mirrors camp's personal-band fallback, with the
    // band axis rotated from X sections to ledges.
    private Monster acquireTarget(Character chr, SpotStack st) {
        Point pos = chr.getPosition();
        Point from = (pos != null) ? pos : new Point((st.x0() + st.x1()) / 2, st.bottomY());
        int x0 = st.x0() - GrindBrain.ACQUIRE_MARGIN_PX;
        int x1 = st.x1() + GrindBrain.ACQUIRE_MARGIN_PX;
        int yTop = st.topY() - TETHER_Y_PAD;
        int yBottom = st.bottomY() + TETHER_Y_PAD;
        Spot a = assigned;
        if (a == null) {
            return SpotFinder.bestStackHostile(chr.getMap(), members, null, x0, x1, yTop, yBottom, from, CLUSTER_RADIUS_PX);
        }
        Monster t = SpotFinder.bestStackHostile(chr.getMap(), members, a, x0, x1, yTop, yBottom, from, CLUSTER_RADIUS_PX);
        if (t != null) {
            ledgeFallback = false;
            ledgeEmptySinceMs = 0L;
            return t;
        }
        if (ledgeEmptySinceMs == 0L) {
            ledgeEmptySinceMs = now();
        }
        if (ledgeFallback || now() - ledgeEmptySinceMs >= LEDGE_FALLBACK_MS) {
            ledgeFallback = true;
            return SpotFinder.bestStackHostile(chr.getMap(), members, null, x0, x1, yTop, yBottom, from, CLUSTER_RADIUS_PX);
        }
        return null; // brief floor lull — hold the level rather than instantly poach a neighbour's
    }

    // Sticky target: alive, hostile, still inside the tether box. Cross-floor stickiness is allowed
    // (traversing to it is the point of the archetype); leaving the tether drops it.
    private Monster stickyTarget(Character chr, SpotStack st) {
        if (b.targetOid < 0) {
            return null;
        }
        MapObject mo = chr.getMap().getMapObject(b.targetOid);
        if (!(mo instanceof Monster m) || !SpotFinder.isHostile(m)) {
            return null;
        }
        Point mp = m.getPosition();
        if (mp == null
                || mp.x < st.x0() - GrindBrain.ACQUIRE_MARGIN_PX || mp.x > st.x1() + GrindBrain.ACQUIRE_MARGIN_PX
                || mp.y < st.topY() - TETHER_Y_PAD || mp.y > st.bottomY() + TETHER_Y_PAD) {
            return null; // left the tether
        }
        return m;
    }

    // Approach inside the tether: mages up/down-blink via the axis-aware skill move, others walk the
    // nav (ropes / jump-ups) toward the foothold under the mob; X stays clamped to the tether.
    private void approachTether(Character chr, SpotStack st, Monster mob) {
        Point mp = mob.getPosition();
        if (mp == null) {
            return;
        }
        int[] leash = leash(chr);
        int tx = GrindBrain.clamp(mp.x, leash[0], leash[1]);
        Point gp = GCMovement.groundPointBelow(chr.getMap(), mp.x, mp.y);
        int ty = (gp != null) ? gp.y : mp.y;
        if (b.engage.skillMoveToward(chr, tx, ty)) {
            return; // blinked/dashed toward it (vertical blinks included)
        }
        if (now() < b.attackWalkLockUntil) {
            return;
        }
        if (Math.abs(tx - b.lastMoveTargetX) < GrindBrain.ROAM_RETARGET_EPS) {
            return;
        }
        GCMovement.move(chr, tx, ty);
        b.lastMoveTargetX = tx;
    }

    private static long now() {
        return System.currentTimeMillis();
    }
}
