package soloMapling.ArtificialPlayer.GCMoveSystem;

import org.gms.client.Character;
import org.gms.server.maps.MapleMap;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

// Adapted from GreenCatMS - GCMoveSystem tick driver, now also ticks contact damage. Credit: NutNNut.
/*
 * The 50 ms (20 Hz) tick driver for GCMoveSystem dynamic movement. Replaces GreenCat's
 * per-bot TimerManager task + BotManager.stepMovementCore funnel with a
 * self-contained scheduled driver.
 *
 * Threading: a shared scheduled pool; each bot is driven by one repeating fixed-rate task.
 * java.util.concurrent.ScheduledThreadPoolExecutor never runs the SAME periodic task
 * concurrently with itself, so per-bot single-thread (which GreenCat's non-volatile physics
 * fields assume) is preserved. A scale-out LOD scheduler can replace this later.
 */
final class GCMovementDriver {
    private GCMovementDriver() {
    }

    private static final int AI_TICK_MS = 100;            // heavy decisions every other tick
    // LOD scheduling cadence for an unobserved bot (no real player on or adjacent to its map): the
    // driver self-reschedules its tick at this interval instead of TICK_MS, so it stops consuming
    // 20 Hz wakeups. The analytic CoarseExecutor is pure wall-clock, so the slower cadence yields
    // identical positions.
    private static final int UNOBSERVED_TICK_MS = 250;
    // Ours (Fable Phase 3): an unobserved bot with no movement job at all doesn't need
    // the 250ms coarse cadence either. A job started while idling begins up to ~1s
    // late, which is invisible on a map nobody can see; on promotion the next tick
    // returns to TICK_MS. At ~1200 mostly-idle background bots this cuts the constant
    // movement wakeups roughly 4x.
    private static final int UNOBSERVED_IDLE_TICK_MS = 1000;
    // Coarse "arrived" box: within this of the goal, hold (and fire arrival) instead of replanning.
    private static final int COARSE_ARRIVE_PX = 12;
    private static final boolean ENABLE_UNSTUCK = true;
    private static final int AIR_STUCK_RECOVER_TICKS = 30;
    // Ours: live fall-off-map catch. The airborne integrator has no VR-bottom clamp, so a bot that slips
    // through a foothold gap (or off the side) free-falls forever — the frozen-air watchdog only fires once
    // the position STOPS changing, which a live plummet never does. Trigger a snap-back once the bot is this
    // far outside the map's VR bounds (footholds live inside the VR, so a legit deep drop never reaches here).
    private static final int FALL_RECOVER_SLACK_PX = 400;
    // Organic map-entry: appear standing at the spawn portal, wait a "client load" beat, then drop.
    // Mirrors WarpCommands.botEnterPortalDropDown (the recorded engine's ~1.5s lag before the drop).
    private static final long PORTAL_DROP_DELAY_MS = 1500;
    private static final int PORTAL_DROP_DELAY_JITTER_MS = 600; // + 0..600ms so arrivals aren't uniform
    // Abandon a move target the bot can't get closer to for this long (unreachable / blocked / bug),
    // so a bot never tries to reach a point forever. The clock resets on any real progress.
    private static final long MOVE_NO_PROGRESS_MS = 8_000;
    private static final int MOVE_PROGRESS_EPS_PX = 16;
    // Recompute a bot's movement profile on this cadence so runtime changes (chiefly party Haste —
    // joining/leaving a party with a high-level thief) take effect without needing a map change.
    // refreshMovementProfile no-ops when the bucket is unchanged, so this is ~free for non-party bots.
    private static final long PROFILE_REFRESH_INTERVAL_MS = 20_000;

    private static final AtomicInteger THREAD_SEQ = new AtomicInteger();
    private static final ScheduledExecutorService POOL = Executors.newScheduledThreadPool(
            Math.max(2, Runtime.getRuntime().availableProcessors() / 2),
            r -> {
                Thread t = new Thread(r, "gcmove-tick-" + THREAD_SEQ.getAndIncrement());
                t.setDaemon(true);
                return t;
            });

    static void start(BotMovementState entry) {
        stop(entry);
        entry.tickStopped = false;
        scheduleNext(entry, 0);
    }

    static void stop(BotMovementState entry) {
        entry.tickStopped = true; // gate the self-reschedule; an in-flight tick may finish once more
        if (entry.task != null) {
            entry.task.cancel(false);
            entry.task = null;
        }
    }

    /*
     * Self-rescheduling tick: instead of a fixed 20 Hz task per bot, each tick schedules the next at a
     * cadence that matches the bot's tier — TICK_MS (50 ms) when its map is observed,
     * UNOBSERVED_TICK_MS (250 ms) when not. So an unobserved bot stops consuming 20 Hz wakeups
     * (the analytic CoarseExecutor is pure wall-clock, so a slower cadence gives identical positions).
     * Successive ticks never overlap (the next is scheduled only after this one returns) and the
     * executor establishes happens-before between them, so per-bot single-thread semantics hold even
     * though the physical pool thread may differ.
     */
    private static void scheduleNext(BotMovementState entry, long delayMs) {
        if (entry.tickStopped) {
            return;
        }
        entry.task = POOL.schedule(() -> {
            safeTick(entry);
            scheduleNext(entry, nextDelayMs(entry));
        }, delayMs, TimeUnit.MILLISECONDS);
    }

    private static long nextDelayMs(BotMovementState entry) {
        Character bot = entry.bot;
        boolean active = bot != null && bot.getMap() != null
                && ObserverTracker.isActiveMap(bot.getMapId());
        if (active) {
            return BotPhysicsEngine.cfg.TICK_MS;
        }
        if (bot != null
                && !GCMovement.isMoving(bot) && !GCMovement.isTraveling(bot) && !GCMovement.isFollowing(bot)) {
            return UNOBSERVED_IDLE_TICK_MS; // jobless + unseen: idle heartbeat only
        }
        return UNOBSERVED_TICK_MS;
    }

    // Throttled profile recompute. Self-thief speed/jump is static (job/level), but party Haste is dynamic
    // (roster changes at runtime), and the profile is otherwise only built once at enable(). Every
    // PROFILE_REFRESH_INTERVAL_MS we recompute; refreshMovementProfile returns fast and re-warms only when
    // the speed/jump bucket actually changed, so a non-party bot pays just one fromCharacter() per interval.
    private static void maybeRefreshProfile(BotMovementState entry) {
        long now = System.currentTimeMillis();
        if (now - entry.lastProfileRefreshMs < PROFILE_REFRESH_INTERVAL_MS) {
            return;
        }
        entry.lastProfileRefreshMs = now;
        BotMovementManager.refreshMovementProfile(entry);
    }

    private static void safeTick(BotMovementState entry) {
        try {
            soloMapling.server.BotPerfStats.MOVEMENT_TICKS.increment();
            tick(entry);
        } catch (Throwable t) {
            // A thrown exception must not break the self-reschedule chain — swallow so the bot keeps ticking.
        }
    }

    private static void tick(BotMovementState entry) {
        Character bot = entry.bot;
        if (bot == null || bot.getMap() == null) {
            stop(entry);
            return;
        }
        // Pick up runtime profile changes (party Haste) before the map-change branch, so onMapChange warms
        // the new map's graph with the up-to-date profile. Throttled + no-op when unchanged (see helper).
        maybeRefreshProfile(entry);
        if (entry.lastMapId != bot.getMapId()) {
            onMapChange(entry, bot);
            return;
        }

        // Sitting in a chair (e.g. a grinding TrainingBot on a rest break): hold the sit and skip the
        // tick. botSitChair set the SIT stance and broadcast showChair; if the driver kept idling it
        // would reset the stance to standing (idleOnGround -> syncCharacterState) and broadcast that
        // every tick, snapping the bot upright the instant it sits. A sitting bot isn't moving, so
        // nothing is lost by holding; botCancelChair clears the chair and the driver resumes normally.
        // Mirrors the old engine's getChair() > 0 guards in MovementCommands.
        if (bot.getChair() > 0) {
            return;
        }

        // Rope rest hold (grind break): the bot is deliberately hanging idle on a rope. Freeze the hang and
        // broadcast — never run nav / steering / player-reaction / contact-knockback below, any of which
        // could dislodge it. Set on arrival by GrindBreakRoutine, cleared on break end (and leaveGrind).
        if (entry.resting) {
            if (entry.climbing) {
                BotPhysicsEngine.holdClimb(entry, bot);
            } else {
                BotPhysicsEngine.idleOnGround(entry, bot); // shouldn't happen (rest is a rope hang), but hold anyway
            }
            broadcastIfObserved(entry);
            return;
        }

        // Contact/fall damage: render the bot taking hits from mobs. Runs before the movement branches
        // so even an idle/standing bot recoils when a mob touches it. Self-gates on LOD (does ~nothing
        // when no real player shares the bot's map) and on the i-frame window.
        BotContactDamage.tickMobDamage(entry, bot);

        boolean active = ObserverTracker.isActiveMap(bot.getMapId());

        // LOD promotion (M2): the bot was moving analytically (coarse) and a player just arrived —
        // rebuild the physics shadow at its current interpolated position so physics resumes cleanly
        // instead of snapping from stale shadow coords. Minimal reconstruction; full hysteresis is M3.
        if (active && entry.coarseActive) {
            reconstructPhysicsFromCoarse(entry, bot);
        }

        boolean runAiTick = consumeAiTick(entry);

        // Player reaction (ported pathAware): on an observed map, occasionally emote/chat at or stop-and-
        // turn toward a nearby real player while roaming. Grounded bots only; no-op when unobserved.
        if (active && runAiTick && !entry.inAir && !entry.climbing) {
            BotPlayerReaction.maybeReact(entry, bot);
        }
        // While a stop-reaction is in progress, hold position (don't walk off mid-greeting) and keep the
        // stall timer fresh so the pause isn't mistaken for being stuck. Resumes automatically after.
        if (entry.reactingUntilMs > System.currentTimeMillis()) {
            entry.moveProgressAtMs = System.currentTimeMillis();
            BotPhysicsEngine.idleOnGround(entry, bot);
            broadcastIfObserved(entry);
            return;
        }

        // Pending organic portal/teleport drop: hold standing at the spawn portal (the bot appears
        // up at the portal, above the floor), then release the natural fall once the load beat passes.
        if (entry.portalDropAtMs > 0L) {
            if (System.currentTimeMillis() < entry.portalDropAtMs) {
                BotPhysicsEngine.idleOnGround(entry, bot); // float at the spawn point
                entry.inAir = true; // show the JUMP stance while floating (the release fall already does)
                broadcastIfObserved(entry);
                return;
            }
            entry.portalDropAtMs = 0L;
            BotPhysicsEngine.beginPortalDrop(entry, bot, bot.getPosition()); // release the fall
            broadcastIfObserved(entry);
            return;
        }

        // Abandon a move target the bot can't make progress toward, so it never tries forever.
        if (giveUpStalledMove(entry)) {
            BotPhysicsEngine.idleOnGround(entry, bot);
            broadcastIfObserved(entry);
            return;
        }

        Point target = resolveTarget(entry, bot);
        boolean hasGoal = target != null || entry.inAir || entry.climbing || entry.navEdge != null;
        if (!hasGoal) {
            if (entry.duckUntilMs > System.currentTimeMillis()) {
                BotPhysicsEngine.proneOnGround(entry, bot); // idle fidget: hold a crouch/duck pose
            } else {
                BotPhysicsEngine.idleOnGround(entry, bot);
            }
            broadcastIfObserved(entry); // dedup suppresses idle spam; skipped entirely when unobserved
            return;
        }

        // LOD coarse (M2): an unobserved bot with a plannable in-map point goal moves by analytic ETA
        // over the baked edge times — no physics, no broadcast — so it costs ~nothing. Falls through to
        // throttled physics when mid-air/climbing or the map has no baked graph (never bake just to move
        // an unwatched bot). `active` is false here only when nobody can see the bot.
        if (!active && tryCoarseAdvance(entry, bot, target)) {
            return;
        }
        stepMovementCore(entry, target != null ? target : bot.getPosition(), runAiTick);
    }

    /*
     * Move an unobserved bot toward target analytically. Builds a
     * MovementPlan from the cached graph once, then advances it by wall-clock time and writes
     * the interpolated position. Returns false (caller falls back to throttled physics) when
     * the bot is airborne/climbing or the map has no cached graph.
     */
    private static boolean tryCoarseAdvance(BotMovementState entry, Character bot, Point target) {
        if (target == null || entry.inAir || entry.climbing) {
            return false;
        }
        // Already at the goal: arrive (fires the callback, holds an anchor) without replanning each tick.
        if (entry.coarsePlan == null
                && Math.abs(bot.getPosition().x - target.x) <= COARSE_ARRIVE_PX
                && Math.abs(bot.getPosition().y - target.y) <= COARSE_ARRIVE_PX) {
            arriveCoarse(entry, bot, target);
            return true;
        }
        BotNavigationGraph graph = BotNavigationGraphProvider.peekBestGraph(bot.getMap(), entry.movementProfile);
        if (graph == null) {
            return false; // no cached graph -> let the M1 throttle cover it (don't trigger a bake)
        }
        long now = System.currentTimeMillis();
        boolean needPlan = entry.coarsePlan == null
                || entry.coarsePlanMapId != bot.getMapId()
                || !target.equals(entry.coarsePlanTarget);
        if (needPlan) {
            MovementPlan plan = MovementPlan.inMap(graph, bot.getMap(), bot.getPosition(), target);
            if (plan == null) {
                arriveCoarse(entry, bot, target); // already in the target region / unplannable
                return true;
            }
            entry.coarsePlan = plan;
            entry.coarsePlanStartMs = now;
            entry.coarsePlanTarget = new Point(target);
            entry.coarsePlanMapId = bot.getMapId();
        }
        entry.coarseActive = true;
        CoarseExecutor.Step step = CoarseExecutor.advance(entry.coarsePlan, entry.coarsePlanStartMs, now);
        if (step.position() != null) {
            bot.setPosition(step.position());
        }
        if (step.complete()) {
            arriveCoarse(entry, bot, entry.coarsePlanTarget);
        }
        return true;
    }

    /* Finish a coarse trip: snap to the goal, clear the plan + move target, fire the arrival callback. */
    private static void arriveCoarse(BotMovementState entry, Character bot, Point target) {
        if (target != null) {
            bot.setPosition(new Point(target));
        }
        entry.coarsePlan = null;
        entry.coarsePlanTarget = null;
        entry.moveTarget = null;
        entry.moveTargetPrecise = false;
        entry.moveBestDist = Integer.MAX_VALUE;
        BotMovementManager.clearNavigationState(entry);
        GCMovement.fireArrival(entry);
    }

    /*
     * Promotion (COARSE -> FULL/HALO): rebuild the physics shadow at the bot's current interpolated
     * position so the resumed physics tick doesn't snap from stale shadow coordinates. Minimal — full
     * hysteresis/safe-node reconstruction is M3.
     */
    private static void reconstructPhysicsFromCoarse(BotMovementState entry, Character bot) {
        entry.coarseActive = false;
        entry.coarsePlan = null;
        entry.coarsePlanTarget = null;
        Point pos = bot.getPosition();
        Point ground = BotPhysicsEngine.findGroundPoint(bot.getMap(), new Point(pos.x, pos.y - 1));
        BotPhysicsEngine.teleportTo(entry, bot, ground != null ? ground : pos);
        BotMovementManager.resetEntryStateAfterTeleport(entry);
    }

    /*
     * No-progress give-up: if the bot can't get meaningfully closer to its moveTarget for
     * MOVE_NO_PROGRESS_MS, abandon the move (clear target + nav, drop the callback) so an
     * unreachable / blocked / buggy goal can't loop forever. Any real progress resets the clock, so
     * long legitimate walks are never cut off. Follow/farm without a moveTarget are unaffected.
     */
    private static boolean giveUpStalledMove(BotMovementState entry) {
        if (entry.moveTarget == null || entry.inAir || entry.climbing) {
            return false;
        }
        Point bp = entry.bot.getPosition();
        int dist = Math.abs(bp.x - entry.moveTarget.x) + Math.abs(bp.y - entry.moveTarget.y);
        long now = System.currentTimeMillis();
        if (entry.moveProgressAtMs == 0L) {
            entry.moveProgressAtMs = now;
        }
        if (dist < entry.moveBestDist - MOVE_PROGRESS_EPS_PX) {
            entry.moveBestDist = dist;
            entry.moveProgressAtMs = now;
            return false;
        }
        if (now - entry.moveProgressAtMs <= MOVE_NO_PROGRESS_MS) {
            return false;
        }
        entry.moveTarget = null;
        entry.moveTargetPrecise = false;
        entry.moveBestDist = Integer.MAX_VALUE;
        BotMovementManager.clearNavigationState(entry);
        GCMovement.abandonMove(entry);
        return true;
    }

    private static Point resolveTarget(BotMovementState entry, Character bot) {
        if (entry.moveTarget != null) {
            return entry.moveTarget;
        }
        if (entry.following && entry.owner != null && entry.owner.getMap() == bot.getMap()) {
            return entry.owner.getPosition();
        }
        if (entry.farmAnchor != null) {
            return entry.farmAnchor;
        }
        return null;
    }

    /* Faithful port of BotManager.stepMovementCore (minus fidget). */
    private static void stepMovementCore(BotMovementState entry, Point target, boolean runAiTick) {
        BotNavigationManager.NavigationDirective nav =
                BotNavigationManager.resolveTarget(entry, target, runAiTick);
        if (nav.consumedTick) {
            return;
        }
        Point steering = nav.targetPos;
        if (entry.moveTargetPrecise && entry.navEdge == null) {
            entry.navPreciseTarget = true;
        }
        tickMovementPhase(entry, steering, runAiTick);
        if (runAiTick && !entry.inAir && !entry.climbing) {
            BotNavigationManager.tryExecuteCommittedEdgeAfterGroundMovement(entry, target);
        }
        tickStuckDetection(entry);
        clearReachedMoveTarget(entry);
    }

    private static void tickMovementPhase(BotMovementState entry, Point target, boolean runAiTick) {
        if (entry.climbing) {
            BotMovementManager.tickClimbing(entry, target, runAiTick);
        } else if (isSwimMap(entry) && entry.inAir) {
            BotMovementManager.tickSwimming(entry, target);
        } else if (entry.inAir) {
            BotMovementManager.tickAirborne(entry, target);
        } else {
            BotMovementManager.tickGrounded(entry, target);
        }
    }

    private static boolean isSwimMap(BotMovementState entry) {
        return entry.bot != null && entry.bot.getMap() != null && entry.bot.getMap().isSwim();
    }

    private static void clearReachedMoveTarget(BotMovementState entry) {
        if (entry.moveTarget == null) {
            return;
        }
        Point botPos = entry.bot.getPosition();
        int arrivalDist = entry.moveTargetPrecise ? 8 : BotMovementManager.cfg.STOP_DIST;
        if (Math.abs(botPos.x - entry.moveTarget.x) <= arrivalDist
                && Math.abs(botPos.y - entry.moveTarget.y) <= arrivalDist) {
            entry.moveTarget = null;
            entry.moveTargetPrecise = false;
            GCMovement.fireArrival(entry);
        }
    }

    private static boolean consumeAiTick(BotMovementState entry) {
        entry.aiTickAccumulatorMs += BotPhysicsEngine.cfg.TICK_MS;
        if (entry.aiTickAccumulatorMs < AI_TICK_MS) {
            return false;
        }
        entry.aiTickAccumulatorMs -= AI_TICK_MS;
        return true;
    }

    // Organic portal entry: the bot appears at the portal lifted at least this far above the floor
    // (a natural portal sits higher → keep its height), floats the "client load" beat, then drops.
    // Guarantees a visible spawn→float→drop even when the arrival portal sits on the ground. Mirrors
    // the recorded engine's portalenterdrop feel; tune to taste.
    private static final int PORTAL_FLOAT_HEIGHT_PX = 60;

    static void onMapChange(BotMovementState entry, Character bot) {
        entry.lastMapId = bot.getMapId();
        // New map: drop any in-progress reaction pause. The per-player react cooldown is intentionally
        // NOT reset here - it's tied to the player so map-hopping can't re-trigger greetings at them.
        entry.reactingUntilMs = 0L;
        // Drop any coarse plan from the previous map; onMapChange re-seeds the physics shadow below.
        entry.coarsePlan = null;
        entry.coarsePlanTarget = null;
        entry.coarseActive = false;
        MapleMap map = bot.getMap();
        // If a real player is already on this map, mark it observed NOW so the visible spawn (float →
        // drop, jump stance) and FULL combat start this tick instead of waiting up to one observer poll.
        if (ObserverTracker.hasRealPlayerNow(map)) {
            ObserverTracker.markObservedNow(bot.getMapId());
            // Ours: the movement above is already instant - also wake the arriving bot's macro brain so
            // it acts promptly instead of on its slow 2-6s/10s wheel. See BotMapEntryResponder (B).
            soloMapling.ArtificialPlayer.BotMapEntryResponder.onBotArrivedObserved(bot);
        }
        entry.fhIndex = BotMovementManager.buildFhIndex(map);
        Point spawn = bot.getPosition();
        Point ground = BotPhysicsEngine.findGroundPoint(map, new Point(spawn.x, spawn.y - 1));
        // Clear stale nav from the old map first (does not touch the physics state set below).
        BotMovementManager.resetEntryStateAfterTeleport(entry);
        // Only play the visible spawn→float→drop where a player can actually see it; on unobserved
        // maps just land on the floor (nobody's watching, and it keeps coarse travel from stalling
        // ~1.5s per hop). The hold + release runs in tick(); beginPortalDrop hands off to the
        // airborne integrator.
        if (ground != null && ObserverTracker.isActiveMap(bot.getMapId())) {
            // Lift to at least PORTAL_FLOAT_HEIGHT_PX above the floor (keep a higher natural portal),
            // so the bot floats at/above the portal a beat, then drops — every time, not just for
            // portals that happen to sit high.
            int floatY = Math.min(spawn.y, ground.y - PORTAL_FLOAT_HEIGHT_PX);
            BotPhysicsEngine.teleportTo(entry, bot, new Point(spawn.x, floatY));
            entry.portalDropAtMs = System.currentTimeMillis() + PORTAL_DROP_DELAY_MS
                    + ThreadLocalRandom.current().nextInt(PORTAL_DROP_DELAY_JITTER_MS + 1);
        } else {
            // Unobserved (or no floor below): just stand where we landed — no float.
            BotPhysicsEngine.teleportTo(entry, bot, ground != null ? ground : spawn);
            entry.portalDropAtMs = 0L;
        }
        BotNavigationGraphProvider.warmGraphAsync(map, entry.movementProfile);
        broadcastIfObserved(entry);
    }

    /*
     * Emit a movement packet only when the bot's map is observed (a real player is on it or an
     * adjacent map). For an unobserved bot nobody receives the packet, so this skips the snapshot +
     * map-iterate entirely. Gates only the driver-owned broadcasts (idle / drop / map-change /
     * recovery); the moving-bot broadcasts live inside the untouchable core and are reduced instead by
     * the unobserved tick throttle.
     */
    private static void broadcastIfObserved(BotMovementState entry) {
        if (entry.bot != null && ObserverTracker.isActiveMap(entry.bot.getMapId())) {
            BotMovementManager.broadcastMovement(entry);
        }
    }

    // ── Stuck detection (faithful port; recovery teleport inlined) ──
    private static void tickStuckDetection(BotMovementState entry) {
        entry.unstuckCooldownMs = BotMovementManager.tickDown(entry.unstuckCooldownMs);
        tickFrozenAirborneWatchdog(entry);
        tickFallOffMapRecovery(entry); // ours: catch a live plummet the frozen-air watchdog above misses
        if (entry.inAir || entry.climbing || entry.graphWarmupFallback
                || (entry.navEdge == null && entry.moveTarget == null)) {
            entry.stuckMs = 0;
            entry.stuckCheckX = Integer.MIN_VALUE;
            return;
        }
        Point botPos = entry.bot.getPosition();
        if (entry.stuckCheckX == Integer.MIN_VALUE) {
            entry.stuckCheckX = botPos.x;
            entry.stuckCheckY = botPos.y;
            return;
        }
        boolean moved = Math.abs(botPos.x - entry.stuckCheckX) > 8
                || Math.abs(botPos.y - entry.stuckCheckY) > 8;
        if (moved) {
            entry.stuckMs = 0;
            entry.stuckCheckX = botPos.x;
            entry.stuckCheckY = botPos.y;
        } else {
            entry.stuckMs += BotPhysicsEngine.cfg.TICK_MS;
        }
        if (ENABLE_UNSTUCK && entry.stuckMs >= 500 && entry.unstuckCooldownMs == 0) {
            entry.stuckMs = 0;
            entry.stuckCheckX = Integer.MIN_VALUE;
            BotMovementManager.tickUnstuck(entry);
        }
    }

    private static void tickFrozenAirborneWatchdog(BotMovementState entry) {
        if (!entry.inAir || entry.climbing) {
            entry.airStuckTicks = 0;
            entry.airStuckX = Integer.MIN_VALUE;
            return;
        }
        Point pos = entry.bot.getPosition();
        if (pos.x != entry.airStuckX || pos.y != entry.airStuckY) {
            entry.airStuckTicks = 0;
            entry.airStuckX = pos.x;
            entry.airStuckY = pos.y;
            return;
        }
        if (++entry.airStuckTicks < AIR_STUCK_RECOVER_TICKS) {
            return;
        }
        entry.airStuckTicks = 0;
        entry.airStuckX = Integer.MIN_VALUE;
        // Recovery: snap to ground beneath the goal (or current position).
        MapleMap map = entry.bot.getMap();
        Point goal = entry.moveTarget != null ? entry.moveTarget : entry.navTargetPos;
        Point base = goal != null ? goal : pos;
        Point ground = BotPhysicsEngine.findGroundPoint(map, new Point(base.x, base.y - 1));
        BotPhysicsEngine.teleportTo(entry, entry.bot, ground != null ? ground : pos);
        BotMovementManager.resetEntryStateAfterTeleport(entry);
        broadcastIfObserved(entry);
    }

    // Live fall-off-map recovery: if the bot has left the map's VR bounds by more than a slack margin (fell
    // below the floor or off the side), snap it back to solid ground under its current goal. Mirrors the
    // frozen-air watchdog's recovery, but keys off "outside the map" instead of "frozen in the air", so it
    // catches an active free-fall. Ours (Fable fluid-combat pass).
    private static void tickFallOffMapRecovery(BotMovementState entry) {
        Character bot = entry.bot;
        MapleMap map = (bot != null) ? bot.getMap() : null;
        if (map == null) {
            return;
        }
        Rectangle vr = map.getMapArea();
        if (vr == null || vr.width <= 0 || vr.height <= 0) {
            return; // no usable VR bounds — can't tell inside from outside
        }
        Point pos = bot.getPosition();
        if (pos == null) {
            return;
        }
        boolean belowFloor = pos.y > vr.y + vr.height + FALL_RECOVER_SLACK_PX;
        boolean offSides = pos.x < vr.x - FALL_RECOVER_SLACK_PX
                || pos.x > vr.x + vr.width + FALL_RECOVER_SLACK_PX;
        if (!belowFloor && !offSides) {
            return;
        }
        // Snap to ground under the current goal (grind spot / nav target); if there's no goal, drop onto the
        // first foothold below the VR top at the bot's clamped X.
        Point goal = entry.moveTarget != null ? entry.moveTarget : entry.navTargetPos;
        Point base = (goal != null)
                ? new Point(goal.x, goal.y)
                : new Point(Math.max(vr.x, Math.min(vr.x + vr.width, pos.x)), vr.y);
        Point ground = BotPhysicsEngine.findGroundPoint(map, new Point(base.x, base.y - 1));
        BotPhysicsEngine.teleportTo(entry, bot, ground != null ? ground : base);
        BotMovementManager.resetEntryStateAfterTeleport(entry);
        // De-thrash: resetEntryStateAfterTeleport only clears NAV state, leaving moveTarget — so the bot
        // would re-aim at the same too-far-below goal and re-fire the same doomed descent (teleport loop =
        // the "glitchy" fall on tall maps). Drop the goal too so the brain re-decides a safe target next tick.
        entry.moveTarget = null;
        broadcastIfObserved(entry);
    }
}
