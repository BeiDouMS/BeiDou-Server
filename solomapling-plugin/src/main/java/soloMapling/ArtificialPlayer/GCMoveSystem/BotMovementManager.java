package soloMapling.ArtificialPlayer.GCMoveSystem;

import org.gms.client.Character;
import io.netty.buffer.Unpooled;
import org.gms.net.packet.ByteBufInPacket;
import org.gms.net.packet.InPacket;
import org.gms.net.packet.Packet;
import org.gms.server.maps.Foothold;
import org.gms.server.maps.MapleMap;
import org.gms.server.maps.Rope;
import org.gms.util.PacketCreator;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

// Ported from GreenCatMS. Credit: NutNNut.
class BotMovementManager {
    enum ActionType {
        IDLE,
        WALK,
        CROUCH,
        JUMP,
        CLIMB_UP,
        CLIMB_DOWN
    }

    record MoveAction(ActionType type, int stepX) {
        private static final MoveAction IDLE = new MoveAction(ActionType.IDLE, 0);
        private static final MoveAction CROUCH = new MoveAction(ActionType.CROUCH, 0);
        private static final MoveAction CLIMB_UP = new MoveAction(ActionType.CLIMB_UP, 0);
        private static final MoveAction CLIMB_DOWN = new MoveAction(ActionType.CLIMB_DOWN, 0);

        static MoveAction idle() {
            return IDLE;
        }

        static MoveAction walk(int stepX) {
            return new MoveAction(ActionType.WALK, stepX);
        }

        static MoveAction crouch() {
            return CROUCH;
        }

        static MoveAction jump(int stepX) {
            return new MoveAction(ActionType.JUMP, stepX);
        }

        static MoveAction climbUp() {
            return CLIMB_UP;
        }

        static MoveAction climbDown() {
            return CLIMB_DOWN;
        }
    }

    static final class JumpLanding {
        private final Point point;
        private final Foothold foothold;

        JumpLanding(Point point, Foothold foothold) {
            this.point = point;
            this.foothold = foothold;
        }

        Point point() {
            return point;
        }

        Foothold foothold() {
            return foothold;
        }
    }

    static class Config extends BotPhysicsEngine.Config {
        public int STOP_DIST = 30;
        public int FOLLOW_DIST = 80;
        public int GRIND_EDGE_MARGIN = 40; // keep bot this many px from foothold edge while grinding
        public int MOB_AVOID_LOOKAHEAD_STEPS = 3;
        // Per-grounded-tick chance to actually commit a legal dodge jump when a mob blocks the walk
        // lane. < 1.0 adds humanlike reaction jitter so dodges aren't frame-perfect; the bot re-rolls
        // each grounded tick the mob stays in the lane, so a high value still dodges promptly.
        public double MOB_AVOID_REACTION_CHANCE = 0.6;

        public int JUMP_Y_THRESH = 30;
        // Within-map "hopelessly far -> teleport to target" fallback. Big maps legitimately exceed
        // smaller values during normal travel, which made bots teleport to the target when they were
        // not actually stuck; 8000 covers the large fields. (Out-of-bounds recovery uses the tighter
        // OOB_TELEPORT_DIST below, gated on the bot being provably outside the map's VR rect.)
        public int TELEPORT_DIST = 8000;
        // Tighter teleport trigger when the bot has slipped outside the map's VR rectangle.
        // Long falls below VRBottom never collide with anything and otherwise wait until the
        // 4000 Manhattan threshold; this lets us recover sooner once we know the bot is OOB.
        public int OOB_TELEPORT_DIST = 600;
        public int FOLLOW_Y_CAP = 200; // max vertical distance for Y-snapped follow target
    }

    static Config cfg = bindConfig(new Config());

    private static Config bindConfig(Config config) {
        BotPhysicsEngine.cfg = config;
        return config;
    }

    static int tickDown(int remainingMs) {
        if (remainingMs <= 0) {
            return 0;
        }
        return Math.max(0, remainingMs - BotPhysicsEngine.cfg.TICK_MS);
    }

    static int delayAfterCurrentTick(int durationMs) {
        if (durationMs <= 0) {
            return 0;
        }
        return Math.max(0, durationMs - BotPhysicsEngine.cfg.TICK_MS);
    }

    static int walkStep(MapleMap map) {
        return BotPhysicsEngine.walkStep(map);
    }

    static int walkStep(MapleMap map, BotMovementProfile profile) {
        return BotPhysicsEngine.walkStep(map, profile);
    }

    static int velocityFromDeltaX(double deltaX) {
        return BotPhysicsEngine.velocityFromDeltaX(deltaX);
    }

    static void stopGroundMotion(BotMovementState entry) {
        BotPhysicsEngine.stopGroundMotion(entry);
    }

    static JumpLanding simulateJumpLanding(MapleMap map, Point from, int stepX) {
        return wrapLanding(BotPhysicsEngine.simulateJumpLanding(map, from, stepX));
    }

    static JumpLanding simulateJumpLanding(MapleMap map, Point from, int stepX, BotMovementProfile profile) {
        return wrapLanding(BotPhysicsEngine.simulateJumpLanding(map, from, stepX, profile));
    }

    static JumpLanding simulateRopeJumpLanding(MapleMap map, Point from, int stepX) {
        return wrapLanding(BotPhysicsEngine.simulateRopeJumpLanding(map, from, stepX));
    }

    static JumpLanding simulateRopeJumpLanding(MapleMap map, Point from, int stepX, BotMovementProfile profile) {
        return wrapLanding(BotPhysicsEngine.simulateRopeJumpLanding(map, from, stepX, profile));
    }

    static boolean canReachRopeFromGround(MapleMap map, Point from, Rope rope) {
        return BotPhysicsEngine.canReachRopeFromGround(map, from, rope);
    }

    static boolean canReachRopeFromGround(MapleMap map, Point from, Rope rope, BotMovementProfile profile) {
        return BotPhysicsEngine.canReachRopeFromGround(map, from, rope, profile);
    }

    static boolean refreshMovementProfile(BotMovementState entry) {
        BotMovementProfile updated = BotMovementProfile.fromCharacter(entry.bot);
        if (updated.equals(entry.movementProfile)) {
            return false;
        }

        MapleMap map = entry.bot != null ? entry.bot.getMap() : null;
        if (map != null
                && map.getFootholds() != null
                && BotNavigationGraphProvider.peekGraph(map, updated) == null) {
            BotNavigationGraphProvider.warmGraphAsync(map, updated);
        }

        entry.movementProfile = updated;
        clearNavigationState(entry);
        return true;
    }

    static void resetEntryState(BotMovementState entry) {
        BotPhysicsEngine.resetMotion(entry, entry.bot.getPosition());
        clearTransientState(entry);
    }

    static void resetEntryStateAfterTeleport(BotMovementState entry) {
        clearTransientState(entry);
    }

    private static void clearTransientState(BotMovementState entry) {
        // GCMoveSystem: grindTarget / fidget are out of scope (pure movement) — dropped.
        entry.attackCooldownMs = 0;
        entry.graphWarmupFallback = false;
        entry.observedOwnerStepX = 0;
        entry.observedOwnerStepY = 0;
        clearNavigationState(entry);
        entry.movementBroadcastValid = false;
    }

    static void clearNavigationState(BotMovementState entry) {
        entry.navTargetPos = null;
        entry.navEdge = null;
        entry.navJumpLaunchEdge = null;
        entry.navJumpLaunchX = Integer.MIN_VALUE;
        entry.navJumpLaunchDelaySteps = Integer.MIN_VALUE;
        entry.navTargetRegionId = -1;
        entry.navPreciseTarget = false;
        entry.navBlockedPosTicks = 0;
    }

    static void tickClimbing(BotMovementState entry, Point targetPos, boolean runAiTick) {
        long startedAt = System.nanoTime();
        try {
            Character bot = entry.bot;
            // Null rope is handled inside advanceClimb/holdClimb — they call beginFall internally.
            BotPhysicsEngine.tickMotionTimers(entry);
            Point botPos = bot.getPosition();
            int dy = targetPos.y - botPos.y;
            int dxOwner = targetPos.x - entry.climbRope.x();

            // If not navigating, allow jumping off when target is far away horizontally
            if (runAiTick && entry.navEdge == null
                    && Math.abs(dxOwner) > cfg.FOLLOW_DIST
                    && entry.climbRope.bottomY() < targetPos.y) {
                jumpOffRope(entry, bot, dxOwner);
                return;
            }

            boolean climbIdle = shouldHoldClimbIdle(entry, dy, dxOwner);
            if (climbIdle) {
                BotPhysicsEngine.holdClimb(entry, bot);
                broadcastMovement(entry);
                return;
            }

            if (shouldSnapToClimbTarget(entry, targetPos, dy)) {
                BotPhysicsEngine.attachToRope(entry, bot, entry.climbRope, targetPos.y);
                broadcastMovement(entry);
                return;
            }

            if (!runAiTick && entry.navEdge == null) {
                // No committed nav edge → no AI-decided climb intent. On non-AI ticks the
                // navDirective falls through to the raw follow target (resolveTarget can't run
                // findNextEdge here), and using its dy to choose a direction can dismount the bot
                // off the rope-top onto the foothold above — pathlog-Preston-2026-05-07 oscillation.
                // Integrate the cached intent instead; the next AI tick will refresh direction.
                if (entry.climbVerticalDir == 0) {
                    BotPhysicsEngine.holdClimb(entry, bot);
                } else {
                    BotPhysicsEngine.advanceClimb(entry, bot);
                }
                broadcastMovement(entry);
                return;
            }

            // Committed climb edges must reach the exact launch anchor so execution can hand off.
            MoveAction action = dy < 0
                    ? MoveAction.climbUp()
                    : dy > 0 ? MoveAction.climbDown() : MoveAction.idle();
            applyClimbAction(entry, bot, action);
        } finally {
            BotPerformanceMonitor.record("move-climb", System.nanoTime() - startedAt);
        }
    }

    static void jumpOffRope(BotMovementState entry, Character bot, int dx) {
        int airVelX = resolveAirVelocityX(entry, bot.getMap(), entry.movementProfile, dx);
        BotPhysicsEngine.beginJumpOffRope(entry, bot, airVelX);
        broadcastMovement(entry);
    }

    static void jumpToRope(BotMovementState entry, Character bot, int dx) {
        Rope sourceRope = entry.climbRope;
        int airVelX = resolveAirVelocityX(entry, bot.getMap(), entry.movementProfile, dx);
        BotPhysicsEngine.beginRopeTransferJump(entry, bot, sourceRope, airVelX);
        broadcastMovement(entry);
    }

    private static void applyClimbAction(BotMovementState entry, Character bot, MoveAction action) {
        entry.climbVerticalDir = switch (action.type()) {
            case CLIMB_UP -> -1;
            case CLIMB_DOWN -> 1;
            default -> 0;
        };

        if (entry.climbVerticalDir == 0) {
            BotPhysicsEngine.holdClimb(entry, bot);
        } else {
            BotPhysicsEngine.advanceClimb(entry, bot);
        }
        broadcastMovement(entry);
    }

    static boolean shouldHoldClimbIdle(BotMovementState entry, int dy, int dxOwner) {
        if (entry.navEdge != null) {
            return false;
        }
        if (entry.resting) {
            return true; // explicit rope rest hold: hang regardless of the grind guard
        }
        return !entry.grinding
                && Math.abs(dy) < cfg.STOP_DIST
                && Math.abs(dxOwner) < cfg.FOLLOW_DIST * 2;
    }

    static boolean shouldSnapToClimbTarget(BotMovementState entry, Point targetPos, int dy) {
        if (entry == null || !entry.climbing || entry.climbRope == null || targetPos == null || dy == 0) {
            return false;
        }
        if (!entry.navPreciseTarget) {
            return false;
        }
        if (targetPos.x != entry.climbRope.x()) {
            return false;
        }
        // Allow target == bottomY: rope-exit launch anchors can be authored at the rope bottom
        // (pathlog-Leroy/John). The exclusive guard rejected those anchors, leaving the bot
        // grinding the climb integrator against a fixed-step overshoot — every step landed
        // past bottomY, beginFall(0,0) detached, repeat. Top step-off keeps its strict guard
        // because dismount there is driven by physics top-boundary detach, not snap.
        if (targetPos.y <= entry.climbRope.topY() || targetPos.y > entry.climbRope.bottomY()) {
            return false;
        }
        return Math.abs(dy) < BotPhysicsEngine.climbStepPerTick();
    }

    static void tickAirborne(BotMovementState entry, Point targetPos) {
        long startedAt = System.nanoTime();
        try {
            entry.swimming = false;
            BotPhysicsEngine.tickMotionTimers(entry);

            Character bot = entry.bot;
            Point botPos = bot.getPosition();

            if (successfullyGrabbedRope(entry, bot, botPos)) {
                return;
            }

            // Set air steering intent. If fidget manager already set moveDir (non-zero),
            // preserve it. Committed nav trajectories (fixedAirArc, JUMP/DROP/CLIMB-launch
            // edges) instead fly with the LAUNCH key held — like the real player performing
            // the hop: held input is a CalcFloat no-op above the 8.93 x fs px/s input band
            // (keeps vx constant, matching the graph's constant-stepX arc sim) and suppresses
            // the no-input air drag that free flight gets.
            if (entry.moveDir == 0) {
                if (shouldApplyAirSteering(entry)) {
                    if (targetPos != null) {
                        int dx = targetPos.x - botPos.x;
                        entry.moveDir = Math.abs(dx) > BotPhysicsEngine.cfg.SWIM_ARRIVAL_RADIUS_PX
                                ? Integer.signum(dx) : 0;
                    }
                } else {
                    entry.moveDir = Integer.signum(entry.airVelX);
                }
            }

            BotPhysicsEngine.AirborneStepResult result = BotPhysicsEngine.stepAirborne(entry, bot);
            if (result == BotPhysicsEngine.AirborneStepResult.WALL) {
                if (successfullyGrabbedRope(entry, bot, bot.getPosition())) {
                    return;
                }
                broadcastMovement(entry);
                return;
            }
            if (result == BotPhysicsEngine.AirborneStepResult.CEILING) {
                broadcastMovement(entry);
                return;
            }
            if (result == BotPhysicsEngine.AirborneStepResult.LANDED) {
                entry.jumpCooldownMs = 0;
                broadcastMovement(entry);
                return;
            }

            // CONTINUE — position advanced, check for rope grab at new position
            if (successfullyGrabbedRope(entry, bot, bot.getPosition())) {
                return;
            }
            if (entry.flashJumpFired) {
                // Apex tick of a flash jump: emit the type-6 "fj" dash frame in place of the normal
                // move so observers see the dash animation. relDx/relDy = this tick's position delta.
                Point now = bot.getPosition();
                broadcastFlashJump(entry, now.x - botPos.x, now.y - botPos.y);
                entry.flashJumpFired = false;
            } else {
                broadcastMovement(entry);
            }
        } finally {
            BotPerformanceMonitor.record("move-air", System.nanoTime() - startedAt);
        }
    }

    private static boolean successfullyGrabbedRope(BotMovementState entry, Character bot, Point botPos) {
        if (!entry.climbUpIntent) {
            return false;
        }

        for (Rope rope : bot.getMap().getRopes()) {
            if (sameRope(entry.blockedRopeGrab, rope)) {
                continue;
            }
            if (Math.abs(rope.x() - botPos.x) > BotPhysicsEngine.cfg.ROPE_GRAB_X) {
                continue;
            }
            if (botPos.y < rope.topY() || botPos.y > rope.bottomY() + 2) {
                continue;
            }

            BotPhysicsEngine.attachToRope(entry, bot, rope, botPos.y);
            broadcastMovement(entry);
            return true;
        }

        return false;
    }

    static boolean sameRope(Rope left, Rope right) {
        return left != null && right != null
                && left.x() == right.x()
                && left.topY() == right.topY()
                && left.bottomY() == right.bottomY()
                && left.isLadder() == right.isLadder();
    }

    private static boolean shouldApplyAirSteering(BotMovementState entry) {
        if (entry.fixedAirArc) {
            return false;
        }
        if (entry.downJumpGracePeriodMS != 0L) {
            return false;
        }
        if (entry.navEdge == null) {
            return true;
        }
        return entry.navEdge.type != BotNavigationGraph.EdgeType.JUMP
                && entry.navEdge.type != BotNavigationGraph.EdgeType.DROP
                && !(entry.navEdge.type == BotNavigationGraph.EdgeType.CLIMB
                && entry.navEdge.launchStepX != 0);
    }

    static void tickSwimming(BotMovementState entry, Point targetPos) {
        long startedAt = System.nanoTime();
        try {
            BotPhysicsEngine.tickMotionTimers(entry);
            computeSwimIntents(entry, targetPos);
            BotPhysicsEngine.applySwimMotion(entry);
            broadcastMovement(entry);
        } finally {
            BotPerformanceMonitor.record("move-swim", System.nanoTime() - startedAt);
        }
    }

    /*
     * Translate a nav target into the discrete swim controls the real client exposes:
     * steer L/R (continuous), JUMP burst (one-shot), UP/DOWN held.
     * No continuous velocity steering — physics integrates the intents.
     */
    private static void computeSwimIntents(BotMovementState entry, Point targetPos) {
        // Capture last vertical hold for hysteresis. Without sticky-middle,
        // a target sinking faster than the bot's UP-terminal sink rate causes
        // dy to oscillate across the LEVEL_BAND boundary every tick — bot
        // alternates UP-hold (slow sink) and free-sink, visibly stuttering.
        int prevVerticalHold = entry.swimVerticalHold;

        // Default to "no input": bot drifts under swim gravity.
        entry.swimMoveDir = 0;
        entry.swimVerticalHold = 0;
        entry.swimJumpRequested = false;

        // Player can't dispatch movement input (strafe/jump/up/down) while
        // CUserLocal::IsAttacking is true. Mirror that here: during animation
        // lock the integrator still ticks (drag + gravity, collision) but no
        // intent is set, so the bot just floats in place.
        if (entry.attackCooldownMs > 0) {
            return;
        }

        if (targetPos == null) {
            // Idle in water — hold UP so the bot doesn't sink endlessly.
            entry.swimVerticalHold = -1;
            return;
        }

        Point pos = entry.bot.getPosition();
        int dx = targetPos.x - pos.x;
        int dy = targetPos.y - pos.y;

        // Horizontal steer.
        int hRadius = BotPhysicsEngine.cfg.SWIM_ARRIVAL_RADIUS_PX;
        if (dx >  hRadius) entry.swimMoveDir =  1;
        else if (dx < -hRadius) entry.swimMoveDir = -1;

        // Arrival band: bot is essentially on top of the target both axes.
        // Hold UP just to maintain altitude, no burst, no horizontal push —
        // prevents the jump/sink oscillation when bot overshoots target by a
        // few px (was: any dy<0 fired a 1000+ px/s burst, then bot fell back
        // through level, repeat).
        int levelBand = BotPhysicsEngine.cfg.SWIM_LEVEL_BAND_PX;
        if (Math.abs(dx) <= hRadius && Math.abs(dy) <= levelBand) {
            entry.swimMoveDir = 0;
            entry.swimVerticalHold = -1;
            return;
        }

        // Vertical intent with hysteresis around band boundaries. The middle
        // band (LEVEL < dy <= DOWN) is "sticky" — we keep whichever hold was
        // active last tick so the bot doesn't flip-flop between UP and free
        // sink as dy crosses LEVEL_BAND each frame while chasing a target
        // that sinks faster than UP-terminal.
        long now = System.currentTimeMillis();
        int jumpTrigger = BotPhysicsEngine.cfg.SWIM_JUMP_TRIGGER_DY_PX;
        int downBand = BotPhysicsEngine.cfg.SWIM_DOWN_BAND_PX;
        if (dy <= -jumpTrigger && now >= entry.swimNextJumpAtMs) {
            entry.swimJumpRequested = true;
            entry.swimNextJumpAtMs = now + BotPhysicsEngine.cfg.SWIM_JUMP_COOLDOWN_MS;
            entry.swimVerticalHold = -1;
        } else if (dy <= levelBand) {
            entry.swimVerticalHold = -1;        // clearly above target → UP
        } else if (dy > downBand) {
            entry.swimVerticalHold = 1;         // clearly far below → DOWN
        } else {
            // Middle band: persist last hold to avoid stutter. If we were
            // sinking (free or DOWN), keep that — UP would just slow our
            // descent and let target pull further away. If we were UP-holding
            // and now drifted past LEVEL, switch to free sink so we catch up.
            entry.swimVerticalHold = prevVerticalHold > 0 ? 1 : 0;
        }
    }

    static void tickGrounded(BotMovementState entry, Point targetPos) {
        long startedAt = System.nanoTime();
        try {
            entry.swimming = false;
            Character bot = entry.bot;

            BotPhysicsEngine.tickMotionTimers(entry);

            Foothold currentFh = BotPhysicsEngine.syncAndDetectGround(entry, bot);
            if (currentFh == null) {
                broadcastMovement(entry);
                return;
            }

            Point botPos = bot.getPosition();
            if (entry.ropeEntryPending) {
                performTopRopeEntry(entry);
                return;
            }
            if (entry.downJumpPending) {
                performDownJump(entry);
                return;
            }

            targetPos = adjustGrindingTargetPosition(entry, currentFh, targetPos);
            if (entry.graphWarmupFallback && targetPos != null) {
                if (BotFallbackMovementManager.tryImmediateAction(entry, botPos, targetPos)) {
                    return;
                }
                targetPos = BotFallbackMovementManager.resolveSteeringTarget(entry, botPos, targetPos);
            }
            MoveAction action = planGroundAction(entry, currentFh, botPos, targetPos);
            applyGroundAction(entry, currentFh, action);
        } finally {
            BotPerformanceMonitor.record("move-ground", System.nanoTime() - startedAt);
        }
    }

    /*
     * Stop-distance used when navPreciseTarget is true.
     * WALK edges use 4px to absorb terrain micro-bumps on sloped footholds.
     * JUMP and straight down-jump DROP edges use 0px because the bot must walk INTO the
     * authored launch window, not stop just outside it. Other precise edge types
     * (CLIMB, PORTAL, non-windowed fallback cases) use 1px to reach the exact anchor.
     */
    static int preciseNavStopDist(BotNavigationGraph.Edge navEdge) {
        if (navEdge != null
                && (navEdge.type == BotNavigationGraph.EdgeType.JUMP
                || (navEdge.type == BotNavigationGraph.EdgeType.DROP && navEdge.launchStepX == 0))) {
            // Bot must walk INTO the launch window, not just near it. The launch window checks
            // are strict, so stopDist=1 can halt the bot exactly 1px before the valid range.
            return 0;
        }
        if (navEdge != null && navEdge.type != BotNavigationGraph.EdgeType.WALK) {
            return 1;
        }
        return 4;
    }

    static Point adjustGrindingTargetPosition(BotMovementState entry, Foothold currentFh, Point targetPos) {
        if (!entry.grinding || entry.navEdge != null || currentFh == null || targetPos == null) {
            return targetPos;
        }

        MapleMap map = entry.bot.getMap();
        BotNavigationGraph graph = BotNavigationGraphProvider.peekGraph(map, entry.movementProfile);
        if (graph == null) {
            BotNavigationGraphProvider.warmGraphAsync(map, entry.movementProfile);
            return targetPos;
        }
        Point botPos = entry.bot.getPosition();
        int currentRegionId = BotNavigationManager.resolveCurrentRegionId(graph, entry, map, botPos);
        int targetRegionId = BotNavigationManager.resolveTargetRegionId(graph, entry, map, targetPos);
        if (currentRegionId < 0 || currentRegionId != targetRegionId) {
            return targetPos;
        }

        BotNavigationGraph.Region currentRegion = graph.getRegion(currentRegionId);
        if (currentRegion == null || currentRegion.isRopeRegion) {
            return targetPos;
        }

        int safeLeft = currentRegion.minX + cfg.GRIND_EDGE_MARGIN;
        int safeRight = currentRegion.maxX - cfg.GRIND_EDGE_MARGIN;
        if (safeLeft >= safeRight) {
            return targetPos;
        }

        int clampedX = Math.max(safeLeft, Math.min(safeRight, targetPos.x));
        return currentRegion.pointAt(clampedX);
    }

    private static MoveAction planGroundAction(BotMovementState entry, Foothold currentFh, Point botPos, Point targetPos) {
        boolean directionalDrop = isDirectionalDropEdge(entry.navEdge);
        int stopDist = directionalDrop ? 0 : entry.navPreciseTarget ? preciseNavStopDist(entry.navEdge) : cfg.STOP_DIST;
        // No hysteresis when navigating to an edge — always move toward the waypoint
        int followDist = directionalDrop ? 0
                : (entry.navEdge != null || entry.navPreciseTarget) ? stopDist : cfg.FOLLOW_DIST;
        int stepX = resolveGroundStepX(entry, botPos, targetPos, stopDist, followDist);
        if (stepX == 0) {
            return MoveAction.idle();
        }
        boolean canWalkStep = BotPhysicsEngine.canWalkGroundStep(entry.bot.getMap(), botPos, stepX);
        if (!canWalkStep) {
            boolean blockedByWall = BotPhysicsEngine.isGroundStepBlockedByWall(entry.bot.getMap(), botPos, stepX);
            if (!blockedByWall
                    && ((directionalDrop && Integer.signum(stepX) == Integer.signum(entry.navEdge.launchStepX))
                    || BotFallbackMovementManager.shouldWalkOffLedge(entry, botPos, targetPos, stepX))) {
                // Walk-off drops should keep walking in the authored direction until physics
                // detects lost ground and transitions into a fall with preserved momentum.
                return MoveAction.walk(stepX);
            }
            // Wall-blocked nav edges are stale or invalid. Clear them so the next AI tick can
            // replan instead of holding a walk stance into the wall.
            if (blockedByWall && entry.navEdge != null) {
                clearNavigationState(entry);
            } else if (entry.navEdge != null && entry.navEdge.type == BotNavigationGraph.EdgeType.WALK) {
                clearNavigationState(entry);
            }
            return MoveAction.idle();
        }
        // GCMoveSystem: mob-dodge is out of scope (pure movement) — always walk.
        return MoveAction.walk(stepX);
    }

    private static boolean isDirectionalDropEdge(BotNavigationGraph.Edge navEdge) {
        return navEdge != null
                && navEdge.type == BotNavigationGraph.EdgeType.DROP
                && navEdge.launchStepX != 0;
    }

    static int resolveGroundStepX(BotMovementState entry, Point botPos, Point targetPos, int stopDist, int followDist) {
        if (entry == null || entry.bot == null || botPos == null || targetPos == null) {
            return 0;
        }
        if (entry.graphWarmupFallback) {
            int localStopDist = Math.min(stopDist, 12);
            return updateStepX(entry, entry.bot.getMap(), botPos.x, targetPos.x, localStopDist, localStopDist);
        }
        return updateStepX(entry, entry.bot.getMap(), botPos.x, targetPos.x, stopDist, followDist);
    }

    private static void applyGroundAction(BotMovementState entry, Foothold currentFh, MoveAction action) {
        Character bot = entry.bot;
        entry.moveDir = switch (action.type()) {
            case WALK, JUMP -> Integer.compare(action.stepX(), 0);
            default -> 0;
        };

        if (action.type() == ActionType.CROUCH) {
            BotPhysicsEngine.queueDownJump(entry, bot);
            broadcastMovement(entry);
            return;
        }
        if (action.type() == ActionType.JUMP) {
            initiateFixedArcJump(entry, bot, action.stepX());
            return;
        }

        BotPhysicsEngine.GroundMotion motion =
                BotPhysicsEngine.applyGroundMotion(entry, bot, currentFh);
        if (motion.lostGround()) {
            broadcastMovement(entry);
            return;
        }

        if (motion.stepX() == 0) {
            applyIdleOrInPlaceMotion(entry, action);
            return;
        }

        broadcastMovement(entry);
    }

    private static void applyIdleOrInPlaceMotion(BotMovementState entry, MoveAction action) {
        // Preserve ground momentum while still trying to walk/jump toward a nav target.
        // Otherwise subpixel uphill/transition movement gets zeroed every tick and the bot
        // can stall forever short of a valid launch window.
        if (entry.movementVelX == 0 && action.type() == ActionType.IDLE) {
            BotPhysicsEngine.idleOnGround(entry, entry.bot);
        }
        broadcastMovement(entry);
    }

    private static void performDownJump(BotMovementState entry) {
        BotPhysicsEngine.beginDownJump(entry, entry.bot);
        broadcastMovement(entry);
    }

    private static void performTopRopeEntry(BotMovementState entry) {
        BotPhysicsEngine.beginTopRopeEntry(entry, entry.bot);
        broadcastMovement(entry);
    }

    static int calcStepX(MapleMap map, int botX, int targetX, boolean wasMovingX) {
        return calcStepX(map, BotMovementProfile.base(), botX, targetX, wasMovingX, cfg.STOP_DIST, cfg.FOLLOW_DIST);
    }

    static int calcStepX(MapleMap map, int botX, int targetX, boolean wasMovingX, int stopDist, int followDist) {
        return calcStepX(map, BotMovementProfile.base(), botX, targetX, wasMovingX, stopDist, followDist);
    }

    static int calcStepX(MapleMap map, BotMovementProfile profile, int botX, int targetX, boolean wasMovingX, int stopDist, int followDist) {
        int dx = targetX - botX;
        int absDx = Math.abs(dx);
        if (absDx <= stopDist) {
            return 0;
        }
        if (!wasMovingX && absDx <= followDist) {
            return 0;
        }
        return Math.min(absDx, BotPhysicsEngine.walkStep(map, profile)) * (dx >= 0 ? 1 : -1);
    }

    static int updateStepX(BotMovementState entry, MapleMap map, int botX, int targetX) {
        return updateStepX(entry, map, botX, targetX, cfg.STOP_DIST, cfg.FOLLOW_DIST);
    }

    static int updateStepX(BotMovementState entry, MapleMap map, int botX, int targetX, int stopDist, int followDist) {
        int stepX = calcStepX(map, entry.movementProfile, botX, targetX, entry.wasMovingX, stopDist, followDist);
        if (stepX == 0) {
            entry.wasMovingX = false;
            return 0;
        }
        entry.wasMovingX = true;
        // Bang-bang approach on slippery ground: only push toward the target while the bot
        // can still brake to a stop inside the remaining distance; otherwise counter-strafe
        // (or coast) so the bot arrives able to stop in the window/radius instead of sliding
        // past it (pathlog-Preston-2026-06-12T083326). Plain passthrough on fs=1 maps.
        // Directional walk-off drops are exempt: they leave the platform with momentum on
        // purpose, so braking short of the ledge would break the edge.
        if (isDirectionalDropEdge(entry.navEdge)) {
            return stepX;
        }
        int approachDir = BotPhysicsEngine.slipperyApproachDir(map, entry.movementProfile, entry.hspeed,
                targetX - botX, launchWindowOvershootSlackPx(entry, botX, targetX));
        return approachDir == Integer.signum(stepX) ? stepX : approachDir;
    }

    /*
     * Extra overshoot allowance (px) past the steering target before the slippery approach
     * controller must brake. Anywhere inside a committed edge's launch window is executable,
     * so a pulse projected to land between the target and the window's far edge is arrival,
     * not overshoot. Without it a tight window (2px on El Nath fs=0.2) can be unreachable
     * from rest: the smallest legal 50ms pulse travels farther than the distance to the
     * target pixel and the controller refuses to accelerate at all
     * (pathlog-Leroy-2026-06-12T140609).
     */
    private static int launchWindowOvershootSlackPx(BotMovementState entry, int botX, int targetX) {
        BotNavigationGraph.Edge edge = entry.navEdge;
        if (edge == null) {
            return 0;
        }
        boolean windowed = edge.type == BotNavigationGraph.EdgeType.JUMP
                || (edge.type == BotNavigationGraph.EdgeType.DROP && edge.launchStepX == 0);
        if (!windowed || !edge.containsLaunchX(targetX)) {
            return 0;
        }
        int dir = Integer.signum(targetX - botX);
        if (dir == 0) {
            return 0;
        }
        int slack = dir > 0 ? edge.launchMaxX - targetX : targetX - edge.launchMinX;
        // JUMP execution additionally requires |x - launchX| <= walkStep around the selected
        // launch point — never allow sliding deeper into a wide window than that gate accepts.
        return Math.clamp(slack, 0, BotPhysicsEngine.walkStep(entry.bot.getMap(), entry.movementProfile));
    }

    static void initiateJump(BotMovementState entry, Character bot, int dx) {
        BotPhysicsEngine.beginGroundJump(entry, bot, resolveAirVelocityX(entry, bot.getMap(), entry.movementProfile, dx));
        broadcastMovement(entry);
    }

    private static void initiateFixedArcJump(BotMovementState entry, Character bot, int dx) {
        initiateJump(entry, bot, dx);
        entry.fixedAirArc = true;
    }

    /*
     * Fires a random recovery action when the bot has been stuck in the same spot.
     * Clears the nav edge so A* replans on the next AI tick.
     */
    static void tickUnstuck(BotMovementState entry) {
        Character bot = entry.bot;
        int walkStep = BotPhysicsEngine.walkStep(bot.getMap(), entry.movementProfile);
        switch (ThreadLocalRandom.current().nextInt(2)) {
            case 0 -> BotPhysicsEngine.beginGroundJump(entry, bot, -walkStep); // jump left
            default -> BotPhysicsEngine.beginGroundJump(entry, bot, walkStep); // jump right
        }
        clearNavigationState(entry);
        entry.unstuckCooldownMs = delayAfterCurrentTick(5000);
        broadcastMovement(entry);
    }

    static void initiateRopeJump(BotMovementState entry, Character bot, int dx) {
        BotPhysicsEngine.beginClimbUpJump(entry, bot, resolveAirVelocityX(entry, bot.getMap(), entry.movementProfile, dx));
        broadcastMovement(entry);
    }

    private static int resolveAirVelocityX(BotMovementState entry, MapleMap map, BotMovementProfile profile, int dx) {
        if (dx == 0) {
            // No direction held at takeoff: the client carries the CURRENT ground hspeed into
            // the air (packet-verified standing jumps 0->0, 3->3, 9->10, 29->29 px/s). Only
            // meaningful on slippery ground where a no-input bot can still be sliding; on
            // fs=1 maps hspeed without input is ~0, so behavior there is exactly as before.
            return entry != null && BotPhysicsEngine.slipperyGround(map) && !entry.climbing
                    ? BotPhysicsEngine.carriedAirVelX(map, entry)
                    : 0;
        }
        // Full walk step always: intent-based, like holding the arrow key through a jump.
        // This is also the packet-true client launch rule: jumping with a direction held
        // snaps vx to +-walkSpeed instantly regardless of current ground speed (even from a
        // slow icy start, -34 -> -124 px/s at takeoff) — see Config.AIR_CONTROL_ACCEL_PXSS.
        // Graph jump edges are calibrated at ±walkStep of their OWN profile, so this matches
        // the simulated arc as long as planning and execution share a graph — which
        // resolveTarget's navGraph identity check now guarantees (a stale cross-profile edge,
        // e.g. stepX=-6 executed at walkStep 9, used to overfly its landing forever).
        int walkStep = BotPhysicsEngine.walkStep(map, profile);
        return dx > 0 ? walkStep : -walkStep;
    }

    static void broadcastMovement(BotMovementState entry) {
        if (!BotPerformanceMonitor.enabled()) {
            doBroadcastMovement(entry);
            return;
        }

        long startedAt = System.nanoTime();
        try {
            doBroadcastMovement(entry);
        } finally {
            BotPerformanceMonitor.record("broadcast-move", System.nanoTime() - startedAt);
        }
    }

    private static void doBroadcastMovement(BotMovementState entry) {
        Character bot = entry.bot;
        // Ours (Fable Phase 1, F4): the physics core still reaches here for unobserved
        // airborne/climbing/no-graph bots - skip the foothold lookup + packet build +
        // broadcast when nobody can see the map, and invalidate the dedup snapshot so
        // the first observed tick always re-broadcasts the current position.
        if (!ObserverTracker.isActiveMap(bot.getMapId())) {
            entry.movementBroadcastValid = false;
            return;
        }
        int x = bot.getPosition().x;
        int y = bot.getPosition().y;
        BotPhysicsEngine.MovementSnapshot snapshot = BotPhysicsEngine.movementSnapshot(entry);
        int fhId = resolveBroadcastFhId(entry, bot);

        if (entry.movementBroadcastValid
                && entry.lastBroadcastX == x
                && entry.lastBroadcastY == y
                && entry.lastBroadcastVelX == snapshot.velX()
                && entry.lastBroadcastVelY == snapshot.velY()
                && entry.lastBroadcastStance == snapshot.stance()
                && entry.lastBroadcastFh == fhId) {
            return;
        }

        entry.movementBroadcastValid = true;
        entry.lastBroadcastX = x;
        entry.lastBroadcastY = y;
        entry.lastBroadcastVelX = snapshot.velX();
        entry.lastBroadcastVelY = snapshot.velY();
        entry.lastBroadcastStance = snapshot.stance();
        entry.lastBroadcastFh = fhId;
        sendMovementPacket(bot, snapshot, fhId);
    }

    // Real clients report the foothold ID they're standing on in every move packet; the
    // client uses it to pick the render z-layer. Without it, bots draw on the top layer
    // (in front of tiles/walls). While airborne, clients keep sending the last-known
    // ground fh, so cache it on the bot entry.
    private static int resolveBroadcastFhId(BotMovementState entry, Character bot) {
        Foothold fh = BotPhysicsEngine.findGroundFoothold(bot.getMap(), bot.getPosition());
        if (fh != null) {
            entry.lastGroundFhId = fh.getId();
        }
        return entry.lastGroundFhId;
    }

    private static void sendMovementPacket(Character bot, BotPhysicsEngine.MovementSnapshot snapshot, int fhId) {
        byte[] data = new byte[15];
        data[0] = 1;
        int x = bot.getPosition().x;
        int y = bot.getPosition().y;
        data[2] = (byte) (x & 0xFF);
        data[3] = (byte) (x >> 8);
        data[4] = (byte) (y & 0xFF);
        data[5] = (byte) (y >> 8);
        data[6] = (byte) (snapshot.velX() & 0xFF);
        data[7] = (byte) (snapshot.velX() >> 8);
        data[8] = (byte) (snapshot.velY() & 0xFF);
        data[9] = (byte) (snapshot.velY() >> 8);
        data[10] = (byte) (fhId & 0xFF);
        data[11] = (byte) (fhId >> 8);
        data[12] = (byte) snapshot.stance();
        data[13] = (byte) (BotPhysicsEngine.cfg.TICK_MS & 0xFF);
        data[14] = (byte) (BotPhysicsEngine.cfg.TICK_MS >> 8);
        InPacket packet = new ByteBufInPacket(Unpooled.wrappedBuffer(data));
        Packet movePacket = PacketCreator.movePlayer(bot.getId(), packet, data.length);
        bot.getMap().broadcastMessage(bot, movePacket, false);
    }

    // Broadcast a hand-built movement fragment path ([numCommands][fragments...]) verbatim to observers. Used
    // by GCMovementSkills for the teleport (cmd 4/3/0) and flash-jump (cmd 0/6) fragments the single-fragment
    // sendMovementPacket above can't express — PacketCreator.movePlayer copies the bytes as-is.
    static void broadcastRawMovement(Character bot, byte[] data) {
        if (bot == null || bot.getMap() == null) {
            return;
        }
        InPacket packet = new ByteBufInPacket(Unpooled.wrappedBuffer(data));
        Packet movePacket = PacketCreator.movePlayer(bot.getId(), packet, data.length);
        bot.getMap().broadcastMessage(bot, movePacket, false);
    }

    // Broadcast a flash jump so observers render the dash animation instead of a plain air-glide. Adapted
    // from GreenCatMS (NutNNut). The client plays the flash-jump action only for movement command type 6
    // ("fj", a RelativeLifeMovement); a normal type-0 tick conveys position but not the FJ action. Fired
    // ONCE at the apex impulse (from tickAirborne); the arc's remaining type-0 ticks carry the rest.
    //
    // The fj fragment MUST be preceded, in the SAME path, by an absolute cmd-0 fragment. Per the v83 client
    // CMovePath::Decode, a lone type-6 fragment reads no x/y — it copies the previous fragment's position
    // (seeded with packet-header garbage) and stores the two shorts as velocity, so the bot flickers
    // off-screen for a frame. Lead with the cmd-0 anchor; fj duration 0 (matches real captures). The two fj
    // shorts are velocity slots, not a position delta — the dash ANIM is triggered by the fragment TYPE, so
    // the magnitude is cosmetic (real position comes from the surrounding cmd-0 ticks).
    static void broadcastFlashJump(BotMovementState entry, int relDx, int relDy) {
        Character bot = entry.bot;
        if (bot == null || bot.getMap() == null) {
            return;
        }
        if (!ObserverTracker.isActiveMap(bot.getMapId())) {
            broadcastMovement(entry); // self-gates: no-op + invalidates the dedup snapshot
            return;
        }
        BotPhysicsEngine.MovementSnapshot snapshot = BotPhysicsEngine.movementSnapshot(entry);
        int stance = snapshot.stance(); // JUMP stance while airborne
        int fhId = resolveBroadcastFhId(entry, bot);
        int x = bot.getPosition().x;
        int y = bot.getPosition().y;
        int dur = BotPhysicsEngine.cfg.TICK_MS;
        byte[] data = new byte[23];
        int i = 0;
        data[i++] = 2;                       // two commands: absolute anchor + fj
        data[i++] = 0;                       // cmd 0 — absolute, anchors the fj fragment's position
        data[i++] = (byte) (x & 0xFF);
        data[i++] = (byte) (x >> 8);
        data[i++] = (byte) (y & 0xFF);
        data[i++] = (byte) (y >> 8);
        data[i++] = (byte) (snapshot.velX() & 0xFF);
        data[i++] = (byte) (snapshot.velX() >> 8);
        data[i++] = (byte) (snapshot.velY() & 0xFF);
        data[i++] = (byte) (snapshot.velY() >> 8);
        data[i++] = (byte) (fhId & 0xFF);
        data[i++] = (byte) (fhId >> 8);
        data[i++] = (byte) stance;
        data[i++] = (byte) (dur & 0xFF);
        data[i++] = (byte) (dur >> 8);
        data[i++] = 6;                       // cmd 6 "fj" — RelativeLifeMovement (plays the dash action)
        data[i++] = (byte) (relDx & 0xFF);
        data[i++] = (byte) (relDx >> 8);
        data[i++] = (byte) (relDy & 0xFF);
        data[i++] = (byte) (relDy >> 8);
        data[i++] = (byte) stance;
        data[i++] = 0;                       // fj duration 0 — matches real captures
        data[i] = 0;
        InPacket packet = new ByteBufInPacket(Unpooled.wrappedBuffer(data));
        Packet movePacket = PacketCreator.movePlayer(bot.getId(), packet, data.length);
        bot.getMap().broadcastMessage(bot, movePacket, false);
        // Pin the dedup cache at the post-impulse state so the next normal tick isn't re-sent redundantly.
        entry.movementBroadcastValid = true;
        entry.lastBroadcastX = x;
        entry.lastBroadcastY = y;
        entry.lastBroadcastVelX = snapshot.velX();
        entry.lastBroadcastVelY = snapshot.velY();
        entry.lastBroadcastStance = stance;
        entry.lastBroadcastFh = fhId;
    }

    static Map<Integer, Foothold> buildFhIndex(MapleMap map) {
        Map<Integer, Foothold> index = new HashMap<>();
        for (Foothold foothold : map.getFootholds().getAllFootholds()) {
            index.put(foothold.getId(), foothold);
        }
        return index;
    }

    private static JumpLanding wrapLanding(BotPhysicsEngine.JumpLanding landing) {
        if (landing == null) {
            return null;
        }
        return new JumpLanding(landing.point(), landing.foothold());
    }
}
