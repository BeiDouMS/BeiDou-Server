package soloMapling.ArtificialPlayer.BotGrindSystem;

import org.gms.client.Character;
import org.gms.server.life.Monster;
import org.gms.server.maps.MapObject;
import soloMapling.ArtificialPlayer.BotAttackSystem.BotAttackDriver;
import soloMapling.ArtificialPlayer.GCMoveSystem.GCMovement;

import java.awt.Point;
import java.util.Random;
import java.util.function.Consumer;

// The per-bot grind engine: a thin facade + shared combat context over one active GrindStrategy
// (CAMP / ROAM today; PATROL / STACK planned). TrainingBot's macro brain talks only to this class —
// the public surface is identical to the pre-split GrindBrain, so the macro FSM and watchdog didn't
// change. The strategy owns WHERE the bot fights (spots, claims, rings, tethers); this class owns
// what every style shares: the sticky target, engage flags, blink/turn/kite cadences (EngageBeat),
// organic loot (GrindLoot), rope recovery (ClimbRecovery), approach-progress tracking, the watchdog
// heartbeat, and the claim lock. Ours (SoloMapling); reads terrain only through GCMovement.
public final class GrindBrain {

    // ── Shared approach/engage tunables (read by the strategies + beats) ──
    static final int APPROACH_X = 70;                // melee floor: within this dx -> stop and swing
    static final int APPROACH_Y = 90;                // melee floor: within this dy -> in range
    private static final double APPROACH_REACH_FRAC = 0.80;  // stop at this fraction of attack reach
    static final int ROAM_RETARGET_EPS = 16;         // skip re-issuing move for tiny shifts
    private static final long TARGET_RETARGET_TIMEOUT = 4_000; // give up walking to an unreachable target/spot after this
    private static final int APPROACH_PROGRESS_EPS = 20;     // bot must move at least this far to count as progress
    static final int APPROACH_Y_TOLERANCE = 120;     // within() vertical slack (covers a sloped/stacked anchor)
    static final int ACQUIRE_MARGIN_PX = 130;        // reach a bit past the leash to grab a near-miss same-ledge mob

    // Claim-hygiene lock: claim-mutating transitions (start / release / SELECT_SPOT / relocate) span
    // TWO thread families — the shared combat ticker runs the states, the macro tick (virtual thread)
    // runs start/release — and an unsynchronized release() racing an in-flight select could land its
    // release BEFORE the select's claim, orphaning that claim forever (BotSpotClaims has no TTL; a
    // ghost holder then repels real bots from the spot for the rest of the uptime). All claim mutations
    // hold claimLock, and selects re-check claimActive under it so a released brain never claims.
    // Movement calls stay OUTSIDE the lock.
    final Object claimLock = new Object();
    boolean claimActive = false;     // guarded by claimLock: true between start() and release()

    private final Consumer<String> debug;  // debug narration sink (TrainingBot::debugChat)
    final Random rng = new Random();

    // ── Components (shared beats) + strategies (one instance each; active one switches per episode) ──
    final EngageBeat engage = new EngageBeat(this);
    final GrindLoot loot = new GrindLoot(this);
    private final ClimbRecovery climb = new ClimbRecovery(this);
    private final CampStrategy camp = new CampStrategy(this);
    private final RoamStrategy roam = new RoamStrategy(this);
    private final StackStrategy stack = new StackStrategy(this);
    private final PatrolStrategy patrol = new PatrolStrategy(this);
    private volatile GrindStrategy strategy;      // active strategy (null until start())
    private volatile int episodeMapId = -1;       // map the episode started on (mid-episode warp detect)
    private volatile GrindStyle forcedStyle;      // GM override (!bot grindstyle); null = auto
    private volatile boolean styleSwapRequested = false; // consumed at the next combat tick (live swap)

    // ── Shared combat state (package-visible: strategies + beats read/write directly) ──
    int targetOid = -1;
    long retargetDeadline = 0L;
    private int noProgressAnchorX = Integer.MIN_VALUE;
    private int noProgressAnchorY = 0;
    boolean engaged = false;
    int lastMoveTargetX = Integer.MIN_VALUE;
    private int approachX = -1;              // lazy attack-reach cache
    private int approachY = -1;
    MovementStyle style = MovementStyle.PLANTED;
    float fjCap = 1f;                        // level-tier flash-jump dash cap (FlashJumpTiers)
    long attackWalkLockUntil = 0L;           // hold walking until this time after a swing (bypassed by blink/dash)
    long lastKillMs = 0L;

    // ── Combat heartbeat (read by TrainingBot's macro watchdog -> volatile) ──
    private volatile long lastCombatProgressMs = 0L;
    private boolean wasObserved = false;

    // ── Debug narration (edge-triggered: speak only when the message changes) ──
    static final boolean NARRATE = false; // master toggle: false silences all grind debug chatter
    private static final long GIVE_UP_NARRATE_GAP_MS = 8_000;
    private String lastNarrate = "";
    private long lastGiveUpNarrateMs = 0L;

    public GrindBrain(Consumer<String> debug) {
        this.debug = debug != null ? debug : msg -> { };
    }

    // ── Public surface (unchanged from the pre-split GrindBrain) ──

    // Grind-episode entry: fresh heartbeat, resolve the map's strategy, and let it pre-position so the
    // bot disperses into the field even before a player arrives.
    public void start(Character chr) {
        synchronized (claimLock) {
            claimActive = true;
            lastCombatProgressMs = now();
            wasObserved = false;
            style = MovementStylePolicy.forBot(chr);
            fjCap = FlashJumpTiers.capFor(chr);
            engage.reset();
            loot.reset();
            climb.reset();
            engaged = false;
            targetOid = -1;
            lastMoveTargetX = Integer.MIN_VALUE;
            lastKillMs = now();
            lastNarrate = "";
            lastGiveUpNarrateMs = 0L;
            camp.resetEpisodeUnderLock();
            roam.resetEpisodeUnderLock();
            stack.resetEpisodeUnderLock();
            patrol.resetEpisodeUnderLock();
        }
        // Strategy resolution + pre-positioning stay outside the lock: profile() may bake the nav
        // graph on first touch, and movement takes its own locks.
        styleSwapRequested = false; // a fresh episode resolves the (possibly forced) style anyway
        episodeMapId = (chr != null) ? chr.getMapId() : -1;
        strategy = resolveStrategy(chr);
        GrindStrategy s = strategy;
        if (s != null) {
            s.start(chr);
        }
    }

    // One combat tick (already gated by the caller on running && phase == GRIND). Observed maps run the
    // active strategy; unobserved maps no-op here (the macro tick accrues abstract EXP instead).
    public void tick(Character chr) {
        if (chr == null) {
            return;
        }
        // Mid-episode map change (GM warp etc.) or a GM style override: swap strategies safely. The
        // release path uses the RECORDED claim map, never the new one, so no ghost claim survives;
        // then re-anchor here under the (possibly forced) style.
        boolean mapChanged = episodeMapId >= 0 && chr.getMapId() != episodeMapId;
        if (mapChanged || styleSwapRequested) {
            styleSwapRequested = false;
            synchronized (claimLock) {
                if (!claimActive) {
                    return; // bot is being released — don't restart an episode under it
                }
                GrindStrategy old = strategy;
                if (old != null) {
                    old.releaseUnderLock(chr);
                }
            }
            episodeMapId = chr.getMapId();
            strategy = resolveStrategy(chr);
            GrindStrategy fresh = strategy;
            if (fresh != null) {
                fresh.start(chr);
            }
            return;
        }
        if (!GCMovement.isMapObserved(chr.getMapId())) {
            wasObserved = false; // map went quiet; the next observed tick starts a fresh stuck window
            return;
        }
        if (!wasObserved) {
            // A player just arrived: the bot was abstract-leveling, not swinging, so reset the heartbeat so
            // the watchdog gives a grace window instead of instantly judging it stuck.
            wasObserved = true;
            lastCombatProgressMs = now();
        }
        if (GCMovement.isClimbing(chr)) {
            climb.handleClimb(chr); // RECOVER inline
            return;
        }
        climb.onGrounded();
        GrindStrategy s = strategy;
        if (s != null) {
            s.tick(chr);
        }
    }

    // Drop the spot claim + reset combat state when the bot leaves the map / stops.
    public void release(Character chr) {
        synchronized (claimLock) {
            claimActive = false; // an in-flight select on the ticker aborts instead of claiming into a leak
            GrindStrategy s = strategy;
            if (s != null) {
                s.releaseUnderLock(chr);
            }
            targetOid = -1;
            engaged = false;
            lastMoveTargetX = Integer.MIN_VALUE;
        }
    }

    // Watchdog teleport-to-portal escalation: drop the target and re-anchor from the new position.
    // Deliberately does NOT reset the heartbeat, so the bail timer keeps running if the teleport
    // didn't help. The strategy's re-select releases its old claim first, so no claim leaks here.
    public void resetupAfterTeleport(Character chr) {
        targetOid = -1;
        resetApproachProgress(chr);
        GrindStrategy s = strategy;
        if (s != null) {
            s.resetAfterTeleport(chr);
        }
    }

    public long msSinceProgress() {
        return now() - lastCombatProgressMs;
    }

    // True when this map is full for the active style (camp: every reachable spot claimed; roam: seek
    // range persistently dry). TrainingBot's macro crowd-bail reads this to leave for a deeper map.
    public boolean mapSaturated() {
        GrindStrategy s = strategy;
        return s != null && s.saturated();
    }

    // True while the bot is mid-rope or just dismounted — the watchdog defers escalation during recovery.
    public boolean isRecovering(Character chr) {
        return climb.isRecovering(chr);
    }

    public String spotLabel() {
        GrindStrategy s = strategy;
        return s != null ? s.label() : "[no-spot]";
    }

    // ── Strategy resolution + GM override ──

    // GM tooling (!bot grindstyle): force this bot's archetype (null = back to auto). A live grind
    // episode swaps its strategy on the next combat tick (claims released against their recorded
    // map); an idle bot just resolves the forced style when it next starts grinding.
    public void forceStyle(GrindStyle wanted) {
        forcedStyle = wanted;
        styleSwapRequested = true;
    }

    public GrindStyle forcedStyle() {
        return forcedStyle;
    }

    // The style the active strategy is running (null = no episode live).
    public GrindStyle activeStyle() {
        GrindStrategy s = strategy;
        return (s != null) ? s.style() : null;
    }

    private GrindStrategy resolveStrategy(Character chr) {
        MapGrindProfile p = (chr != null && chr.getMap() != null) ? SpotFinder.profile(chr.getMap()) : null;
        GrindStyle forced = forcedStyle;
        return strategyFor(forced != null ? forced : GrindStylePolicy.natural(p, style));
    }

    private GrindStrategy strategyFor(GrindStyle wanted) {
        return switch (wanted) {
            case CAMP -> camp;
            case PATROL -> patrol;
            case ROAM -> roam;
            case STACK -> stack;
        };
    }

    // ── Shared services (package-visible for the strategies + beats) ──

    void markProgress() {
        lastCombatProgressMs = now();
    }

    // The active strategy's movement clamp for the shared engage beats.
    int[] effectiveLeash(Character chr) {
        GrindStrategy s = strategy;
        if (s != null) {
            return s.leash(chr);
        }
        Point p = (chr != null) ? chr.getPosition() : null;
        int cx = (p != null) ? p.x : 0;
        return new int[]{cx - APPROACH_X, cx + APPROACH_X};
    }

    // Strategy-appropriate ground under x (camp: the spot's ledge; roam: whatever is below).
    Point strategyGroundAt(Character chr, int x) {
        GrindStrategy s = strategy;
        return (s != null) ? s.groundAt(chr, x) : null;
    }

    // The shared sticky target resolved to a live hostile Monster, with no leash validation — for
    // direction-only consumers (rope dismount). Strategies apply their own validity gates on top.
    Monster currentTargetMonster(Character chr) {
        if (targetOid < 0 || chr == null || chr.getMap() == null) {
            return null;
        }
        MapObject mo = chr.getMap().getMapObject(targetOid);
        return (mo instanceof Monster m && SpotFinder.isHostile(m)) ? m : null;
    }

    // Lazily derive (and cache) the stop-and-swing distance from this bot's attack reach so ranged/magic
    // bots fire from afar. Floored at the melee values for short attacks.
    boolean inAttackRange(Character chr, Monster mob) {
        if (approachX < 0) {
            approachX = Math.max(APPROACH_X, (int) (BotAttackDriver.attackReachX(chr) * APPROACH_REACH_FRAC));
            approachY = Math.max(APPROACH_Y, (int) (BotAttackDriver.attackReachY(chr) * APPROACH_REACH_FRAC));
        }
        Point pos = chr.getPosition();
        Point mp = mob.getPosition();
        return pos != null && mp != null
                && Math.abs(mp.x - pos.x) <= approachX && Math.abs(mp.y - pos.y) <= approachY;
    }

    // ── Approach-progress tracking (no-progress give-up on an unreachable target/spot) ──

    void resetApproachProgress(Character chr) {
        retargetDeadline = now() + TARGET_RETARGET_TIMEOUT;
        Point p = (chr != null) ? chr.getPosition() : null;
        noProgressAnchorX = (p != null) ? p.x : Integer.MIN_VALUE;
        noProgressAnchorY = (p != null) ? p.y : 0;
    }

    boolean madeApproachProgress(Character chr) {
        Point p = chr.getPosition();
        if (p == null) {
            return false;
        }
        if (noProgressAnchorX == Integer.MIN_VALUE
                || Math.abs(p.x - noProgressAnchorX) + Math.abs(p.y - noProgressAnchorY) > APPROACH_PROGRESS_EPS) {
            noProgressAnchorX = p.x;
            noProgressAnchorY = p.y;
            retargetDeadline = now() + TARGET_RETARGET_TIMEOUT; // moving -> push the give-up deadline out
            return true;
        }
        return false;
    }

    // ── Narration ──

    void narrate(String msg) {
        if (!NARRATE) {
            return;
        }
        if (!msg.equals(lastNarrate)) {
            lastNarrate = msg;
            debug.accept(msg);
        }
    }

    void narrateGiveUp() {
        if (!NARRATE) {
            return;
        }
        if (now() - lastGiveUpNarrateMs >= GIVE_UP_NARRATE_GAP_MS) {
            lastGiveUpNarrateMs = now();
            debug.accept("can't reach target mob -> retargeting (traversal?)");
        }
    }

    // Raw narration line (GrindLoot applies its own throttle before calling).
    void debugLine(String msg) {
        debug.accept(msg);
    }

    static int clamp(int v, int lo, int hi) {
        return v < lo ? lo : (v > hi ? hi : v);
    }

    private static long now() {
        return System.currentTimeMillis();
    }
}
