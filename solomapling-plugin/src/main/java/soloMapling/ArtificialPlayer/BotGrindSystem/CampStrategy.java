package soloMapling.ArtificialPlayer.BotGrindSystem;

import org.gms.client.Character;
import org.gms.server.life.Monster;
import org.gms.server.maps.MapObject;
import soloMapling.ArtificialPlayer.BotSpotClaims;
import soloMapling.ArtificialPlayer.GCMoveSystem.GCMovement;

import java.awt.Point;
import java.util.List;
import java.util.Set;

// CAMP: the localized spot grind. Plant on a tight spawn-point-dense spot and wait respawn lulls out
// instead of chasing map-global respawns across the screen:
//
//   SELECT_SPOT -> TRAVEL_TO_SPOT -> FIGHT <-> WAIT -> RELOCATE -> SELECT_SPOT
//
// The bot spends almost all its grind life oscillating FIGHT <-> WAIT in ONE place; that oscillation is
// the natural look. Targeting, approach, and loot are all leashed to the spot radius (not a wide band).
// The expensive map-wide work (cluster spawn points + score candidates) runs only at SELECT_SPOT; the
// hot FIGHT/WAIT ticks do cheap local radius scans. Moved verbatim from the pre-split GrindBrain; the
// shared engage/loot beats live in EngageBeat/GrindLoot on the brain. Ours (SoloMapling).
//
// Extension point: PatrolStrategy subclasses this and overrides pickSpot() only — the whole
// SELECT/TRAVEL/FIGHT/WAIT machinery, claims, bands, and patience are inherited unchanged, so a
// patrol is literally "camp whose next spot comes from a ring instead of a re-score".
class CampStrategy implements GrindStrategy {

    // ── Regime-adjusted patience: COMPACT maps camp through the respawn lull (dense, fast refill);
    //    SPREAD/SPARSE maps leave a dry spot sooner. Knob DEFAULTS only — the FSM is identical. ──
    private static final long WAIT_PATIENCE_COMPACT_MS = 8_000;  // lull tolerance before CONSIDERING a relocate
    private static final long WAIT_PATIENCE_SPREAD_MS = 4_000;
    private static final long WAIT_PATIENCE_SPARSE_MS = 2_500;
    private static final long UNPRODUCTIVE_COMPACT_MS = 35_000;  // cumulative no-kill window that (with patience) relocates
    private static final long UNPRODUCTIVE_SPREAD_MS = 18_000;
    private static final long UNPRODUCTIVE_SPARSE_MS = 10_000;
    private static final long RELOCATE_EXCLUDE_MS = 30_000;  // down-weight a just-left spot so it isn't re-picked at once
    private static final int SELECT_CLAIM_ATTEMPTS = 3;      // re-pick this many times if a spot fills under us (cohort race)

    // ── Anchor-proximity watchdog (flash-jump classes fling themselves onto a stacked mini-platform and
    //    loop trying to dash back; the progress heartbeat doesn't catch it because they may still be
    //    landing hits down there). Escalation ladder: soft walk-home -> hard teleport-home -> relocate. ──
    private static final long OFF_ANCHOR_SOFT_MS = 4_000;     // off the anchor ledge this long -> escalate past the soft walk-home
    private static final long OFF_ANCHOR_TELEPORT_MS = 2_500; // the pathfind-home stalled this long -> hard teleport onto the anchor
    private static final long REANCHOR_WINDOW_MS = 15_000;    // a fresh off-anchor within this of a hard return -> spot is a trap, relocate

    // ── Intra-spot spacing (sharers hold personal bands of a wide spot) ──
    private static final long BAND_FALLBACK_MS = 2_500;      // personal band empty this long -> hunt spot-wide until it feeds again
    private static final int BAND_RECENTER_EPS = 100;        // WAIT walks back to the band center beyond this offset

    // ── Targeting ──
    private static final int CLUSTER_RADIUS_PX = 160;        // neighbour radius for scoring which mob sits in the densest pack

    enum State { SELECT_SPOT, TRAVEL_TO_SPOT, FIGHT, WAIT, RELOCATE }

    final GrindBrain b;

    // spot/spotIndex/claimedSlot/state are volatile because the ticker reads them without the claim
    // lock; each state method snapshots `spot` into a local so a macro release() nulling it mid-tick
    // can't NPE the tick (Audit III 2.4).
    private volatile State state = State.SELECT_SPOT;
    private volatile Spot spot;              // current claimed spot (anchor, regionId, radius, spawnCount)
    private volatile int spotIndex = -1;     // index into profile.spots() == BotSpotClaims spotId
    private volatile int claimedSlot = -1;   // this bot's stable claim slot on the spot (-1 = none) -> its section band
    // The claim is released with the RECORDED map + bot id, not chr.getMapId() at release time, so a
    // bot moved off-map mid-claim (GM warp) can never orphan a ghost claim (Audit III 1.2).
    private int claimMapId = -1;
    private int claimBotId = -1;
    private long bandEmptySinceMs = 0L;      // when the personal band last read empty of mobs (0 = feeding)
    private boolean bandFallback = false;    // hunting spot-wide because the band dried; cleared on a band acquisition
    int excludedSpotIndex = -1;              // just-left spot, down-weighted for RELOCATE_EXCLUDE_MS (pickSpot reads it)
    long excludedUntilMs = 0L;
    private long waitStartedMs = 0L;

    // Anchor-proximity watchdog state (ticker-thread only, mirroring waitStartedMs).
    private long offAnchorSinceMs = 0L;      // when the bot first read off the anchor ledge (0 = on it)
    private long hardReturnAt = 0L;          // when the current pathfind-home was issued (0 = none pending)
    private long lastReturnedMs = 0L;        // when we last hard-teleported home (arms the re-anchor window)

    // True when the last spot selection found every reachable spot already claimed (the bot is sharing).
    private volatile boolean mapSaturated = false;

    CampStrategy(GrindBrain brain) {
        this.b = brain;
    }

    @Override
    public GrindStyle style() {
        return GrindStyle.CAMP;
    }

    // Pre-select + move so a cohort spreads to spots on arrival, even unobserved (the driver's
    // analytic coarse executor walks it there).
    @Override
    public void start(Character chr) {
        doSelectSpot(chr);
    }

    @Override
    public void tick(Character chr) {
        switch (state) {
            case SELECT_SPOT -> doSelectSpot(chr);
            case TRAVEL_TO_SPOT -> doTravel(chr);
            case FIGHT -> doFight(chr);
            case WAIT -> doWait(chr);
            case RELOCATE -> state = State.SELECT_SPOT; // claim already released on the way into RELOCATE
        }
    }

    @Override
    public void releaseUnderLock(Character chr) {
        releaseClaimLocked();
        spot = null;
        claimedSlot = -1;
        bandEmptySinceMs = 0L;
        bandFallback = false;
        mapSaturated = false;
        resetAnchorWatchdog();
        state = State.SELECT_SPOT;
    }

    @Override
    public void resetEpisodeUnderLock() {
        excludedSpotIndex = -1;
        excludedUntilMs = 0L;
        spotIndex = -1; // a leftover claim was released by release(); a raced one is prevented by claimActive
        claimMapId = -1;
        claimBotId = -1;
        spot = null;
        claimedSlot = -1;
        bandEmptySinceMs = 0L;
        bandFallback = false;
        mapSaturated = false;
        waitStartedMs = 0L;
        resetAnchorWatchdog();
        state = State.SELECT_SPOT;
    }

    // Watchdog teleport-to-portal escalation: re-select a spot from the new position. doSelectSpot
    // releases the old claim before claiming the new one, so no claim leaks here.
    @Override
    public void resetAfterTeleport(Character chr) {
        resetAnchorWatchdog();
        state = State.SELECT_SPOT;
    }

    private void resetAnchorWatchdog() {
        offAnchorSinceMs = 0L;
        hardReturnAt = 0L;
        lastReturnedMs = 0L;
    }

    @Override
    public boolean saturated() {
        return mapSaturated;
    }

    @Override
    public String label() {
        Spot s = spot;
        return s == null ? "[no-spot]"
                : "[spot x" + s.anchor().x + " r" + s.radius()
                + " n" + s.sameLedgeSpawnCount() + "/" + s.spawnCount() + "]";
    }

    // The X range movement may target this tick: the personal band, or the whole spot when not
    // banding / while band-fallback hunting.
    @Override
    public int[] leash(Character chr) {
        Spot s = spot;
        if (s == null) {
            Point p = (chr != null) ? chr.getPosition() : null;
            int cx = (p != null) ? p.x : 0;
            return new int[]{cx - GrindBrain.APPROACH_X, cx + GrindBrain.APPROACH_X}; // spot-less blip: hold near the bot
        }
        int[] band = bandFallback ? null : personalBand(chr, s);
        return (band != null) ? band
                : new int[]{s.anchor().x - s.radius() - GrindBrain.ACQUIRE_MARGIN_PX,
                            s.anchor().x + s.radius() + GrindBrain.ACQUIRE_MARGIN_PX};
    }

    // Camp hops land on the spot's own ledge — the same authority the acquisition gate uses.
    @Override
    public Point groundAt(Character chr, int x) {
        Spot s = spot;
        if (s == null || chr == null || chr.getMap() == null) {
            return null;
        }
        return GCMovement.groundPointInRegion(chr.getMap(), s.regionId(), x);
    }

    // ── States ──

    private void doSelectSpot(Character chr) {
        if (chr == null || chr.getMap() == null) {
            return;
        }
        MapGrindProfile p = SpotFinder.profile(chr.getMap()); // cached; builds clusters on first touch (kept outside the lock)
        Spot s = selectAndClaim(chr, p);
        if (s == null) {
            return; // no reachable spot (rare — DECIDE guarantees mobs), or released mid-flight
        }
        // Movement + transition stay outside the lock (the movement driver takes its own locks).
        b.resetApproachProgress(chr);
        b.lastKillMs = now();
        if (within(chr, s.anchor(), s.radius())) {
            b.narrate("FIGHT " + label());
            state = State.FIGHT;
        } else {
            GCMovement.move(chr, s.anchor().x, s.anchor().y);
            b.lastMoveTargetX = s.anchor().x;
            b.narrate("TRAVEL -> " + label());
            state = State.TRAVEL_TO_SPOT;
        }
    }

    // The claim-mutating half of SELECT_SPOT, serialized under the brain's claimLock so a macro-thread
    // release() can never interleave with this ticker-thread claim (the ghost-claim race). Returns the
    // claimed (or shared) spot, or null when nothing is reachable / the brain was released mid-flight.
    private Spot selectAndClaim(Character chr, MapGrindProfile p) {
        synchronized (b.claimLock) {
            if (!b.claimActive) {
                return null; // release() landed while this tick was in flight (bot leaving) — claiming now would leak
            }
            // Release any spot we were holding (teleport re-select path) before picking a new one — keeps claims
            // from leaking and makes SELECT_SPOT idempotent w.r.t. the claim registry.
            releaseClaimLocked();
            // Pick a spot AND secure its claim against a cohort that selected at the same instant. claim() is
            // capacity-enforcing, so a -1 means the spot filled between scoring and claiming (two bots that
            // arrived together both saw it empty). Re-pick: pickBest now sees the winner's holder count, and the
            // over-cap penalty steers us to a free spot.
            Spot s = null;
            int idx = -1;
            boolean claimed = false;
            int slot = -1;
            for (int attempt = 0; attempt < SELECT_CLAIM_ATTEMPTS; attempt++) {
                s = pickSpot(chr, p);
                if (s == null) {
                    spot = null;
                    claimedSlot = -1;
                    mapSaturated = false; // no reachable spot is a different problem (watchdog), not crowding
                    b.narrate("no reachable spot here -> waiting"); // edge-triggered; macro watchdog bails if it persists
                    return null; // rare (DECIDE guarantees mobs)
                }
                idx = indexOfSame(p.spots(), s);
                slot = BotSpotClaims.claim(chr.getMapId(), idx, s.shareCap(), chr.getId());
                if (slot >= 0) {
                    claimed = true;
                    break;
                }
            }
            // Never got an open slot → the map is saturated (more bots than spots). Grind the best candidate
            // shared rather than idling; s/idx hold the last (best) spot scored — which, under the
            // overflow-scaled penalty, is the LEAST-CROWDED claimed spot, not the raw argmax.
            mapSaturated = !claimed; // every reachable spot already claimed -> sharing -> signal the macro crowd-bail
            if (!claimed) {
                // Register the share anyway (uncapped claim) so later overflow bots see this sharer in
                // holders() and the scaled penalty spreads the surplus evenly. A squatter slot lands past
                // shareCap, so personalBand treats it as unbanded (whole-spot leash) — correct here.
                slot = BotSpotClaims.claim(chr.getMapId(), idx, Integer.MAX_VALUE, chr.getId());
                b.narrate("all spots taken -> sharing the least-crowded one");
            }
            spotIndex = idx;
            claimMapId = chr.getMapId();
            claimBotId = chr.getId();
            spot = s;
            claimedSlot = slot;
            bandEmptySinceMs = 0L;
            bandFallback = false;
            return s;
        }
    }

    // The next spot candidate for SELECT (one attempt of the claim-retry loop above). CAMP re-scores
    // the whole map each attempt; PATROL overrides this to rotate its ring instead.
    Spot pickSpot(Character chr, MapGrindProfile p) {
        return SpotFinder.pickBest(chr, p, excludedSpotIndex, excludedUntilMs);
    }

    // Release the held claim against the map + bot it was RECORDED on. Must hold b.claimLock.
    private void releaseClaimLocked() {
        if (spotIndex >= 0 && claimMapId >= 0) {
            BotSpotClaims.release(claimMapId, spotIndex, claimBotId);
        }
        spotIndex = -1;
        claimMapId = -1;
    }

    private void doTravel(Character chr) {
        Spot s = spot;
        if (s == null) {
            state = State.SELECT_SPOT;
            return;
        }
        if (within(chr, s.anchor(), s.radius())) {
            GCMovement.stop(chr);
            b.resetApproachProgress(chr);
            b.narrate("FIGHT " + label());
            state = State.FIGHT;
            return;
        }
        if (b.madeApproachProgress(chr)) {
            b.markProgress(); // moving toward the spot is not stuck
        } else if (now() > b.retargetDeadline) {
            toRelocate(); // can't reach this spot -> exclude it and re-pick
        }
    }

    private void doFight(Character chr) {
        Spot s = spot;
        if (s == null) {
            state = State.SELECT_SPOT;
            return;
        }
        if (enforceAnchorProximity(chr, s)) {
            return; // off the anchor ledge too long -> Layer 3 took this tick to escalate the return
        }
        Monster t = stickyTarget(chr, s);
        if (t == null) {
            t = acquireTarget(chr, s);
            b.targetOid = (t != null) ? t.getObjectId() : -1;
            b.resetApproachProgress(chr);
        }
        if (t == null) {
            enterWait(chr, s);
            return;
        }
        // Passive at-feet loot: never interrupts the swing or diverts to distant loot.
        b.loot.grabLootAtFeet(chr);
        if (b.inAttackRange(chr, t)) {
            if (b.engage.preSwingAdjust(chr, t)) {
                return; // turned / kited / re-centred this beat — swing next beat
            }
            b.engage.engageAndSwing(chr, t);
            return;
        }
        b.engaged = false;
        if (offAnchorLedge(chr, s)) {
            // Layer 2 soft-correct: we're off the anchor ledge (a mistimed flash jump / knockback) with the
            // mob out of range — don't chase it further off, pathfind back to the anchor (never a flash jump,
            // so it climbs the rope). An in-range mob still gets swung by the branch above, so the return
            // reads as a fighting climb. Layer 3 escalates if this soft walk-home doesn't land in time.
            GCMovement.move(chr, s.anchor().x, s.anchor().y);
            b.lastMoveTargetX = s.anchor().x;
            b.markProgress(); // walking home is productive, not wedged
            return;
        }
        approachLeashed(chr, s, t);
        if (b.madeApproachProgress(chr)) {
            b.markProgress(); // closing on the mob -> not stuck
        } else if (now() > b.retargetDeadline) {
            b.targetOid = -1; // unreachable mob in radius -> re-pick; heartbeat NOT refreshed, so a real wedge shows
            b.narrateGiveUp();
        }
    }

    private void enterWait(Character chr, Spot s) {
        b.engaged = false;
        waitStartedMs = now();
        // On a shared spot, spend the lull standing at the personal band's center: sharers on a long
        // platform then hold visibly spaced positions instead of bunching wherever the last kill landed.
        int[] band = personalBand(chr, s);
        Point pos = chr.getPosition();
        int cx = (band != null) ? (band[0] + band[1]) / 2 : 0;
        if (band != null && pos != null && Math.abs(pos.x - cx) > BAND_RECENTER_EPS) {
            Point gp = GCMovement.groundPointInRegion(chr.getMap(), s.regionId(), cx);
            GCMovement.move(chr, cx, (gp != null) ? gp.y : s.anchor().y);
            b.lastMoveTargetX = cx;
        } else {
            GCMovement.stop(chr);
        }
        b.narrate("WAIT (respawn lull) " + label());
        state = State.WAIT;
    }

    private void doWait(Character chr) {
        Spot s = spot;
        if (s == null) {
            state = State.SELECT_SPOT;
            return;
        }
        if (enforceAnchorProximity(chr, s)) {
            return; // corral back to the anchor before waiting out a lull off-ledge
        }
        Monster t = acquireTarget(chr, s);
        if (t != null) {
            b.targetOid = t.getObjectId();
            b.resetApproachProgress(chr);
            b.narrate("FIGHT " + label());
            state = State.FIGHT;
            return;
        }
        if (spotStillValid(chr, s)) {
            b.markProgress(); // healthy patience at a live spot is not stuck
        }
        // Idle behaviour = collect the drop pile. A respawn lull is exactly when a real player walks around
        // tidying up their loot; the sweep covers the full radius from wherever the bot stopped, so a
        // stationed ranged bot finally clears its pile here instead of fidgeting.
        int[] leash = leash(chr);
        if (b.loot.tryWalkAndLoot(chr, leash[0], leash[1], 2 * s.radius())) {
            return;
        }
        // Pile cleared and no mob — hold position and wait out the respawn lull (regime-scaled: a sparse
        // or spread map gives up on a dry spot much sooner than a dense compact one).
        if (now() - waitStartedMs >= waitPatienceMs(chr) && now() - b.lastKillMs >= unproductiveMs(chr)) {
            toRelocate();
        }
    }

    private void toRelocate() {
        synchronized (b.claimLock) {
            excludedSpotIndex = spotIndex;
            excludedUntilMs = now() + RELOCATE_EXCLUDE_MS;
            releaseClaimLocked();
            spot = null;
            claimedSlot = -1;
            bandEmptySinceMs = 0L;
            bandFallback = false;
            b.targetOid = -1;
        }
        b.narrate("RELOCATE (spot dry)");
        state = State.RELOCATE;
    }

    // ── Regime knobs ──

    // The current map's grind regime (COMPACT/SPREAD/SPARSE), read from the profile SELECT_SPOT already
    // built and cached; defaults to COMPACT (camp-through-lulls) if somehow not yet built.
    private MapGrindProfile.Regime regime(Character chr) {
        MapGrindProfile p = SpotFinder.profileIfBuilt(chr.getMapId());
        return (p != null) ? p.regime() : MapGrindProfile.Regime.COMPACT;
    }

    private long waitPatienceMs(Character chr) {
        return switch (regime(chr)) {
            case COMPACT -> WAIT_PATIENCE_COMPACT_MS;
            case SPREAD -> WAIT_PATIENCE_SPREAD_MS;
            case SPARSE -> WAIT_PATIENCE_SPARSE_MS;
        };
    }

    private long unproductiveMs(Character chr) {
        return switch (regime(chr)) {
            case COMPACT -> UNPRODUCTIVE_COMPACT_MS;
            case SPREAD -> UNPRODUCTIVE_SPREAD_MS;
            case SPARSE -> UNPRODUCTIVE_SPARSE_MS;
        };
    }

    // ── Bands + targeting ──

    // The bot's personal X band on a shared spot: claim slot s of shareCap partitions anchor±radius via
    // BotSpotClaims.section (stable per slot for the holder's whole tenure). Null = no banding applies —
    // single-capacity spot, alone on the spot, or holding a squatter slot past the cap — and the caller
    // uses the whole spot span.
    private int[] personalBand(Character chr, Spot s) {
        if (s == null || claimedSlot < 0 || claimedSlot >= s.shareCap() || s.shareCap() <= 1) {
            return null;
        }
        int mapForClaim = claimMapId >= 0 ? claimMapId : chr.getMapId();
        if (BotSpotClaims.holders(mapForClaim, spotIndex) <= 1) {
            return null; // alone -> use the whole spot (banding kicks in when a second bot claims)
        }
        return BotSpotClaims.section(s.anchor().x - s.radius(), s.anchor().x + s.radius(),
                claimedSlot, s.shareCap());
    }

    // Acquire the next target, band-first and measured from the BOT (anchor-relative pick made every
    // sharer converge on the identical mob). A band that stays empty past a short lull widens the hunt to
    // the whole spot — kills are allowed anywhere in the spot, starvation isn't — until the band feeds
    // again; approach stays leashed via leash() either way.
    private Monster acquireTarget(Character chr, Spot s) {
        Point pos = chr.getPosition();
        Point from = (pos != null) ? pos : s.anchor();
        // Whole-spot scan reaches a margin past the leash so a near-miss mob just outside the anchor
        // radius (same ledge) still gets seen and grabbed — a real player would obviously hit it.
        int spotX0 = s.anchor().x - s.radius() - GrindBrain.ACQUIRE_MARGIN_PX;
        int spotX1 = s.anchor().x + s.radius() + GrindBrain.ACQUIRE_MARGIN_PX;
        int[] band = personalBand(chr, s);
        if (band == null) {
            return SpotFinder.bestClusterHostileInBand(chr.getMap(), s.anchor(), s.radius(), spotX0, spotX1, from, CLUSTER_RADIUS_PX);
        }
        Monster t = SpotFinder.bestClusterHostileInBand(chr.getMap(), s.anchor(), s.radius(), band[0], band[1], from, CLUSTER_RADIUS_PX);
        if (t != null) {
            bandFallback = false;
            bandEmptySinceMs = 0L;
            return t;
        }
        if (bandEmptySinceMs == 0L) {
            bandEmptySinceMs = now();
        }
        if (bandFallback || now() - bandEmptySinceMs >= BAND_FALLBACK_MS) {
            bandFallback = true;
            return SpotFinder.bestClusterHostileInBand(chr.getMap(), s.anchor(), s.radius(), spotX0, spotX1, from, CLUSTER_RADIUS_PX);
        }
        return null; // brief band lull — hold the segment rather than instantly poach the neighbour's
    }

    // The current sticky target, still valid: alive, hostile, within the spot radius of the ANCHOR (not the
    // bot), and on the anchor's OWN ledge. Anchoring stickiness to the spot is what stops a fleeing/kited mob
    // dragging the bot off; the same-ledge check stops a mob that jumps/wanders onto a stacked platform from
    // keeping the bot committed to roping after it (it gets re-acquired on our own ledge next tick).
    private Monster stickyTarget(Character chr, Spot s) {
        if (b.targetOid < 0 || s == null) {
            return null;
        }
        MapObject mo = chr.getMap().getMapObject(b.targetOid);
        if (!(mo instanceof Monster m) || !SpotFinder.isHostile(m)) {
            return null;
        }
        Point mp = m.getPosition();
        if (mp == null || Math.abs(mp.x - s.anchor().x) > s.radius() + GrindBrain.ACQUIRE_MARGIN_PX) {
            return null; // left the spot radius (+ near-miss margin)
        }
        if (GCMovement.onDifferentLedge(chr.getMap(), s.anchor().x, s.anchor().y, mp.x, mp.y)) {
            return null; // moved onto a separate platform — drop it and re-acquire on our own ledge
        }
        return m;
    }

    // Out of range: walk toward the mob's foothold, leashed to the effective X range (personal band, or
    // the whole spot when unbanded / band-fallback hunting) so the bot never leaves its segment to chase.
    // Aims at the foothold UNDER the mob (a jumping/airborne or sloped mob otherwise reads as a point in
    // empty space and detours the pathfinder).
    private void approachLeashed(Character chr, Spot s, Monster mob) {
        Point mp = mob.getPosition();
        if (mp == null || s == null) {
            return;
        }
        int[] leash = leash(chr);
        int tx = GrindBrain.clamp(mp.x, leash[0], leash[1]);
        Point gp = GCMovement.groundPointBelow(chr.getMap(), mp.x, mp.y);
        int ty = (gp != null) ? gp.y : mp.y;
        if (b.engage.skillMoveToward(chr, tx, ty)) {
            return; // mage blinked / hermit dashed toward the mob instead of walking
        }
        if (now() < b.attackWalkLockUntil) {
            return; // just swung — a walking class can't move mid-attack; hold, then approach the next mob
        }
        if (Math.abs(tx - b.lastMoveTargetX) < GrindBrain.ROAM_RETARGET_EPS) {
            return; // already heading there
        }
        GCMovement.move(chr, tx, ty);
        b.lastMoveTargetX = tx;
    }

    // The BOT itself (not a mob) has ended up on a different ledge than its anchor — a mistimed flash
    // jump, a knockback, or a fall onto a stacked mini-platform. Peek-only and "can't tell -> false", so
    // an unbaked graph reads as on-anchor (the guard simply doesn't fire until SELECT_SPOT bakes it).
    private boolean offAnchorLedge(Character chr, Spot s) {
        Point p = (chr != null) ? chr.getPosition() : null;
        return p != null && s != null && chr.getMap() != null
                && GCMovement.onDifferentLedge(chr.getMap(), s.anchor().x, s.anchor().y, p.x, p.y);
    }

    // Layer 3 positional watchdog. Being off the anchor ledge isn't seen by the combat progress heartbeat
    // (a flash-jump class may keep landing hits on whatever it fell onto), so it's tracked here on its own
    // timer and escalated on a ladder: give the soft walk-home (Layer 3's grace window) first crack, then
    // hard-teleport onto the anchor if the pathfind stalls, then abandon the spot entirely if it ejects the
    // bot AGAIN soon after — that spot is a flash-jump trap. Returns true when it drove movement this tick.
    private boolean enforceAnchorProximity(Character chr, Spot s) {
        if (!offAnchorLedge(chr, s)) {
            offAnchorSinceMs = 0L;
            hardReturnAt = 0L;
            return false;
        }
        long t = now();
        if (offAnchorSinceMs == 0L) {
            offAnchorSinceMs = t;
        }
        if (t - offAnchorSinceMs < OFF_ANCHOR_SOFT_MS) {
            return false; // soft window: doFight/doWait's own walk-home (Layer 2) handles it, still swinging in-range mobs
        }
        // Re-anchor rung: off the anchor AGAIN so soon after a hard return -> this spot keeps ejecting the
        // bot. Abandon it for a fresh one (toRelocate excludes it for RELOCATE_EXCLUDE_MS).
        if (lastReturnedMs != 0L && t - lastReturnedMs <= REANCHOR_WINDOW_MS) {
            resetAnchorWatchdog();
            b.narrate("anchor keeps ejecting -> relocate");
            toRelocate();
            return true;
        }
        // Hard-return rung: pathfind home (climbs the rope, never a flash jump); if that stalls, teleport
        // straight onto the anchor ground. markProgress so the macro heartbeat watchdog doesn't ALSO fire
        // mid-return — this positional ladder owns the off-anchor failure mode.
        b.markProgress();
        if (hardReturnAt == 0L) {
            hardReturnAt = t;
        }
        if (t - hardReturnAt >= OFF_ANCHOR_TELEPORT_MS) {
            GCMovement.teleportTo(chr, s.anchor().x, s.anchor().y); // snaps to the anchor's ground point
            lastReturnedMs = t;     // arm the re-anchor window
            offAnchorSinceMs = 0L;  // let the teleport land before re-judging
            hardReturnAt = 0L;
            b.targetOid = -1;
            b.narrate("off anchor -> hard teleport home");
            return true;
        }
        GCMovement.move(chr, s.anchor().x, s.anchor().y);
        b.lastMoveTargetX = s.anchor().x;
        b.narrate("off anchor -> returning to anchor");
        return true;
    }

    // Healthy WAIT test for the heartbeat contract: a spot that's still reachable and still has spawn points
    // is feeding, so waiting it out is working, not wedged. Graph unbaked / on no ledge -> don't penalize.
    private boolean spotStillValid(Character chr, Spot s) {
        if (s == null || s.spawnCount() <= 0) {
            return false;
        }
        Point pos = chr.getPosition();
        if (pos == null) {
            return false;
        }
        Set<Integer> reach = GCMovement.reachableRegions(chr.getMap(), pos.x, pos.y);
        return reach.isEmpty() || s.regionId() < 0 || reach.contains(s.regionId());
    }

    // ── Helpers ──

    // Horizontal is the real leash; a generous Y tolerance covers a sloped / vertically-stacked anchor.
    private static boolean within(Character chr, Point anchor, int radius) {
        Point p = (chr != null) ? chr.getPosition() : null;
        return p != null && Math.abs(p.x - anchor.x) <= radius
                && Math.abs(p.y - anchor.y) <= GrindBrain.APPROACH_Y_TOLERANCE;
    }

    // Index by identity — pickBest returns an element OF the cached list, so this matches the spotId
    // BotSpotClaims/pickBest use, and is robust to two value-equal Spot records.
    private static int indexOfSame(List<Spot> spots, Spot s) {
        for (int i = 0; i < spots.size(); i++) {
            if (spots.get(i) == s) {
                return i;
            }
        }
        return spots.indexOf(s);
    }

    private static long now() {
        return System.currentTimeMillis();
    }
}
