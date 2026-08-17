package soloMapling.ArtificialPlayer.GCMoveSystem;

import org.gms.client.Character;
import org.gms.server.maps.Foothold;
import org.gms.server.maps.Rope;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;

// Adapted from GreenCatMS - GCMoveSystem movement slice plus the contact-damage fields. Credit: NutNNut.
/*
 * Per-bot movement state for the GCMoveSystem dynamic (calculation-based) movement engine.
 *
 * This is the **movement slice** of GreenCat's BotEntry (a ~250-field mutable bag):
 * only the ~70 physics / nav / broadcast fields the physics+nav core actually reads or writes,
 * with all combat / autopilot / quest / trade / chat / shop state dropped (pure-movement scope).
 * The managers (BotPhysicsEngine, BotMovementManager, BotNavigationManager,
 * BotFallbackMovementManager) are static and mutate these fields in-place across calls —
 * the fields are the contract.
 *
 * Threading: all fields are tick-thread-only and assume a single stable 50 ms tick per bot
 * (driven by GCMovementDriver); never tick one bot from two threads. A few flags are
 * volatile only because a command thread may flip them between ticks.
 */
class BotMovementState {
    final Character bot;                  // the bot avatar (a real org.gms.client.Character)
    volatile Character owner;             // follow anchor (the followed character), nullable
    volatile boolean following = false;
    volatile int followTargetId = 0;      // 0 = owner
    ScheduledFuture<?> task;              // this bot's 50ms tick handle (set by GCMovementDriver)
    BotMovementProfile movementProfile = BotMovementProfile.base();
    long lastProfileRefreshMs = 0L;       // throttles the driver's periodic profile recompute (party Haste)

    // ── Physics integrator state (float "shadow" position the engine advances) ──
    float velY = 0f;
    double hspeed = 0.0;
    double physX = 0.0;
    double physY = 0.0;
    double groundPhysicsCarryMs = 0.0;
    double fallPeakPhysY = Double.POSITIVE_INFINITY;
    boolean inAir = false;
    int jumpCooldownMs = 0;
    int movementVelX = 0;
    int movementVelY = 0;
    int facingDir = 1;
    boolean crouching = false;
    boolean swimming = false;

    // ── Swim intent (set by movement layer, consumed by physics) ──
    int swimMoveDir = 0;                  // -1 left, 0 none, +1 right
    int swimVerticalHold = 0;             // -1 = UP held, 0 = none, +1 = DOWN held
    boolean swimJumpRequested = false;    // one-shot upward burst
    long swimNextJumpAtMs = 0L;

    // ── Move intent ──
    int moveDir = 0;                      // -1 left, 0 none, +1 right
    int groundBrakeDir = 0;               // counter-strafe brake direction on slippery ground

    // ── Rope climbing ──
    boolean climbing = false;
    Rope climbRope = null;
    Rope blockedRopeGrab = null;
    int climbVerticalDir = 0;             // -1 up, 0 idle, +1 down
    boolean wasMovingX = false;

    // ── Airborne ──
    int airVelX = 0;                      // committed horizontal step at launch
    double airSteerVelX = 0.0;            // accumulated air-steering correction
    boolean fixedAirArc = false;          // committed nav arc — disables air steering/drag
    // Flash Jump (Hermit/NL). execFlashJump launches a normal jump then arms pendingFlashJump; the
    // airborne integrator injects the (±550,-350) dash impulse at the apex (velY>=0) and raises
    // flashJumpFired so tickAirborne broadcasts the type-6 "fj" dash frame that one tick. Adapted from
    // GreenCatMS (NutNNut).
    boolean pendingFlashJump = false;
    boolean flashJumpFired = false;
    // Dash size for the armed flash jump, (0..1]: 1 = full maxed dash (fires exactly at the apex),
    // smaller = reduced impulse fired part-way up the ascent — the short flash jump that fits a small
    // platform. Set by execFlashJump alongside pendingFlashJump. Ours (SoloMapling tier/fit layer).
    float flashJumpScale = 1f;
    boolean climbUpIntent = false;
    int ropeGrabCooldownMs = 0;

    // ── Down-jump / rope entry ──
    boolean downJumpPending = false;
    long downJumpGracePeriodMS = 0;
    boolean ropeEntryPending = false;
    Rope ropeEntryRope = null;
    int ropeEntryY = 0;

    // ── Mode flags read by nav/movement (inert under pure-movement scope) ──
    volatile boolean grinding = false;
    // Explicit rest hold: the bot is deliberately hanging idle on a rope for a grind break. Forces the
    // climb-idle hold on (even though grinding is still set) and makes the driver freeze the hang so no
    // nav/reaction/fidget layer dislodges it. Set/cleared via GCMovement.setRestHold.
    volatile boolean resting = false;
    int attackCooldownMs = 0;
    volatile boolean shopVisitPending = false;
    int followTravelTargetMapId = -1;

    // ── Portal gating ──
    long portalUseCooldownUntilMs = 0L;
    int portalEnterReadyTicks = -1;       // -1 = disarmed (airborne / not in range)

    // ── Broadcast-stance timing (ALERT substitution while alertedUntilMs in the future) ──
    long alertedUntilMs = 0L;
    boolean alertResetScheduled = false;  // guards BotContactDamage's one-shot alert-reset task

    // ── Contact-damage state (BotContactDamage; bots take visible hits but never lose HP) ──
    int mobHitCooldownMs = 0;             // i-frame countdown after a contact/fall hit
    Point lastMobTouchCheckPos = null;    // previous-tick foot pos for the swept anti-tunnel AABB
    int lastMobTouchMapId = -1;           // invalidates the sweep across a map change

    // ── Foothold index, rebuilt on map change ──
    int lastMapId = -1;
    Map<Integer, Foothold> fhIndex = new HashMap<>();

    // ── Spacing / stagger + tick gating ──
    int followOffsetX = 0;
    int skipDelayMs = ThreadLocalRandom.current().nextInt(0, 501);
    int spawnWarmupMs = 2_000 + ThreadLocalRandom.current().nextInt(0, 5_001);
    int aiTickAccumulatorMs = 0;
    // LOD scheduling: the driver self-reschedules each bot's tick at a cadence that matches its tier
    // (fast when observed, slow when unobserved), so an unobserved bot stops consuming 20 Hz wakeups.
    // tickStopped guards the self-reschedule against a concurrent stop()/disable().
    volatile boolean tickStopped = false;
    // LOD coarse (M2): the slim analytic-movement record used while the bot is unobserved — it moves
    // along this baked-edge plan by wall-clock time (no physics). coarseActive marks that the last
    // tick advanced analytically, so the driver reconstructs the physics shadow on promotion to FULL.
    MovementPlan coarsePlan = null;
    long coarsePlanStartMs = 0L;
    Point coarsePlanTarget = null;
    int coarsePlanMapId = -1;
    boolean coarseActive = false;
    // Organic map-entry drop: epoch-ms at which the bot, after appearing at the spawn portal,
    // releases its natural fall to the floor. 0 = no pending drop. (Mirrors the recorded engine's
    // teleport-above -> load delay -> drop-down.) Set on map change, consumed by the driver.
    long portalDropAtMs = 0L;
    // Idle fidget: while > now, the driver renders a crouch/duck pose instead of plain standing.
    long duckUntilMs = 0L;

    // ── Player reaction (ported pathAware): pause/chat at a nearby real player while roaming an observed
    // map. reactingUntilMs holds the bot in place during a stop-reaction (the driver idles it);
    // nextPlayerScanMs throttles the scan. The once-per-player anti-spam cooldown lives in
    // BotPlayerReaction (shared across all bots, tied to the player), not here.
    long reactingUntilMs = 0L;
    long nextPlayerScanMs = 0L;

    // ── "Move here" / "farm here" targets (set by the GCMovement façade) ──
    Point moveTarget = null;
    boolean moveTargetPrecise = false;
    String moveTargetSource = null;
    // No-progress give-up: closest manhattan distance reached toward moveTarget so far, and when that
    // best last improved. If the bot can't get closer for a while it abandons the move (unreachable /
    // blocked / bug) instead of trying forever. Reset whenever a new moveTarget is issued.
    int moveBestDist = Integer.MAX_VALUE;
    long moveProgressAtMs = 0L;
    Point farmAnchor = null;
    int farmAnchorMapId = -1;

    // ── Navigation state (written by NM, read by MM) ──
    String lastEdgeBlockReason = null;
    Point navTargetPos = null;
    BotNavigationGraph navGraph = null;
    BotNavigationGraph.Edge navEdge = null;
    BotNavigationGraph.Edge navJumpLaunchEdge = null;
    int navJumpLaunchX = Integer.MIN_VALUE;
    int navJumpLaunchDelaySteps = Integer.MIN_VALUE;
    int navTargetRegionId = -1;
    boolean navPreciseTarget = false;
    int navBlockedPosTicks = 0;
    int navBlockedPosGiveUpTicks = 0;
    int navBlockedPosX = Integer.MIN_VALUE;
    int navBlockedPosY = Integer.MIN_VALUE;
    boolean graphWarmupFallback = false;
    int observedOwnerStepX = 0;
    int observedOwnerStepY = 0;
    String lastNavDecision = "-";

    // ── Stuck detection & unstuck ──
    int stuckMs = 0;
    int unstuckCooldownMs = 0;
    int stuckCheckX = Integer.MIN_VALUE;
    int stuckCheckY = Integer.MIN_VALUE;
    int airStuckTicks = 0;
    int airStuckX = Integer.MIN_VALUE;
    int airStuckY = Integer.MIN_VALUE;

    // ── Movement-broadcast packet cache (dedup of no-op move packets) ──
    boolean movementBroadcastValid = false;
    int lastBroadcastX = 0;
    int lastBroadcastY = 0;
    int lastBroadcastVelX = 0;
    int lastBroadcastVelY = 0;
    int lastBroadcastStance = 0;
    int lastBroadcastFh = 0;
    int lastGroundFhId = 0;

    BotMovementState(Character bot, Character owner) {
        this.bot = bot;
        this.owner = owner;
    }
}
