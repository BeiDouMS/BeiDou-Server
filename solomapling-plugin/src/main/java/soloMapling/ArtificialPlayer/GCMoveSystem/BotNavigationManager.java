package soloMapling.ArtificialPlayer.GCMoveSystem;

import org.gms.client.Character;
import org.gms.constants.game.CharacterStance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gms.server.maps.MapleMap;
import org.gms.server.maps.Foothold;
import org.gms.server.maps.Portal;
import org.gms.server.maps.Rope;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

// Ported from GreenCatMS. Credit: NutNNut.
final class BotNavigationManager {
    private static final Logger log = LoggerFactory.getLogger(BotNavigationManager.class);
    private static final int JUMP_READY_X_TOLERANCE = 10;
    private static final int EDGE_READY_X_TOLERANCE = 14;
    private static final int NO_MOVEMENT_WALK_TOLERANCE = 4;
    // Stale-edge give-up: after this many consecutive no-movement ticks blocked on a
    // committed edge's position gate ("*-pos"), drop the edge and replan from the live
    // position (6-10 ticks = 300-500ms, jittered per park spot).
    private static final int BLOCKED_POS_GIVE_UP_MIN_TICKS = 6;
    private static final int BLOCKED_POS_GIVE_UP_JITTER_TICKS = 4;
    // Steering anchor inside a launch window: aim a few px inside the nearest window edge
    // (window center when narrower than two insets) instead of the exact boundary pixel.
    // The executable region is the WHOLE window; steering at the boundary pixel parks bots
    // 1-2px outside it whenever arrival tolerances round against them.
    private static final int LAUNCH_WINDOW_STEER_INSET_PX = 4;
    // After a bot takes a portal, suppress further portal usage for this long. Prevents a bot from
    // immediately re-entering a portal (e.g. bouncing back through the return portal). Gates ONLY
    // portal execution — movement, attacks and every other action continue unaffected.
    private static final long PORTAL_USE_COOLDOWN_MS = 250L;
    // Intra-map portal shortcuts only fire once the bot is LANDED (never midair — e.g. down-jumping
    // from a platform above and clipping the portal below as soon as it's permitted), and then it
    // walks a few extra ticks deeper onto the portal before activating instead of firing the instant
    // it's in range. Positional jitter like a launch window — extra walk ticks, NOT a standing wait.
    private static final int PORTAL_ENTER_EXTRA_TICKS_MAX = 3;
    // Terminal warns are for the absolute worst searches only — the perf monitor already
    // aggregates everything else. Rate-limited so one degenerate map can't flood the console.
    private static final long SLOW_PATHFIND_WARN_NS = 250_000_000L;
    private static final long SLOW_PATHFIND_WARN_COOLDOWN_MS = 10_000L;
    private static final java.util.concurrent.atomic.AtomicLong slowPathfindNextWarnAtMs =
            new java.util.concurrent.atomic.AtomicLong();
    private static final java.util.concurrent.atomic.AtomicInteger slowPathfindSuppressed =
            new java.util.concurrent.atomic.AtomicInteger();

    /* Throttle warmup notifications per (ownerId -> mapId -> lastNotifyMs). */
    private static final Map<Integer, Map<Integer, Long>> WARMUP_NOTIFIED = new ConcurrentHashMap<>();

    static final class NavigationDirective {
        final Point targetPos;
        final boolean consumedTick;

        NavigationDirective(Point targetPos, boolean consumedTick) {
            this.targetPos = targetPos;
            this.consumedTick = consumedTick;
        }
    }

    private static final class SearchNode {
        final SearchState state;
        final int cost;
        final int score;

        SearchNode(SearchState state, int cost, int score) {
            this.state = state;
            this.cost = cost;
            this.score = score;
        }
    }

    // viaPortal: true when this state was reached by a PORTAL edge. It distinguishes "arrived here
    // by teleport" from "arrived by walk/jump/etc." so the search can charge the portal cooldown to
    // a portal that chains straight off another portal (see runSearch). The flag is part of the
    // dedup key, so a region reachable both ways is explored under both costs.
    private record SearchState(int regionId, Point point, boolean viaPortal) {
    }

    private record PathfindProfile(long elapsedNs,
                                   int expandedNodes,
                                   int staleNodes,
                                   int edgeChecks,
                                   int usableEdges,
                                   int relaxations,
                                   int openPeak,
                                   int bestGoalCost,
                                   int resultEdges) {
    }

    static NavigationDirective resolveTarget(BotMovementState entry, Point rawTargetPos, boolean runAiTick) {
        long startedAt = System.nanoTime();
        try {
            Character bot = entry.bot;
            if (bot.getMap().getFootholds() == null) {
                entry.graphWarmupFallback = false;
                clearNavigation(entry);
                return new NavigationDirective(rawTargetPos, false);
            }
            if (bot.getMap().isSwim()) {
                // Swim maps don't use a swim-aware nav graph. Airborne motion is handled
                // by the swim integrator (tickSwimming); on platforms we still need
                // ledge-drops, ropes, and ground jumps. Engage the heuristic fallback —
                // it walks off ledges into water, picks up nearby ropes, and jumps onto
                // higher platforms when useful. tickSwimming consults targetPos directly,
                // so the same rawTargetPos works for both grounded and airborne paths.
                entry.graphWarmupFallback = true;
                clearNavigation(entry);
                return new NavigationDirective(rawTargetPos, false);
            }

            BotNavigationGraph graph = resolveActiveGraph(bot.getMap(), entry.movementProfile);
            if (graph == null) {
                BotNavigationGraphProvider.warmGraphAsync(bot.getMap(), entry.movementProfile);
                entry.graphWarmupFallback = true;
                notifyWarmup(entry, bot);
                entry.lastNavDecision = "graph-warmup";
                clearNavigation(entry);
                Point fallbackTarget = rawTargetPos != null ? new Point(rawTargetPos) : bot.getPosition();
                return new NavigationDirective(fallbackTarget, false);
            }
            if (BotNavigationGraphProvider.peekGraph(bot.getMap(), entry.movementProfile) == null) {
                BotNavigationGraphProvider.warmGraphAsync(bot.getMap(), entry.movementProfile);
                entry.lastNavDecision = "graph-fallback-profile";
            }
            entry.graphWarmupFallback = false;
            if (entry.navGraph != graph) {
                // Served graph swapped (exact-profile build finished, or a different closest
                // fallback won): committed edges were calibrated against the old instance —
                // their windows and launch steps don't transfer. Drop and replan right now.
                if (entry.navGraph != null) {
                    BotMovementManager.clearNavigationState(entry);
                }
                entry.navGraph = graph;
            }
            Point botPos = bot.getPosition();
            int startRegionId = resolveCurrentRegionId(graph, entry, bot.getMap(), botPos);
            int targetRegionId = resolveTargetRegionId(graph, entry, bot.getMap(), rawTargetPos);
            Point pathTargetPos = adjustPathTarget(entry, graph, targetRegionId, rawTargetPos);

            // Stale-edge give-up: a committed edge whose position gate (jump-pos/drop-pos/
            // climb-pos) has rejected the bot for several consecutive ticks WITHOUT the bot
            // moving is parked, not approaching — e.g. a window recorded a few px away from
            // where the bot actually stands (pathlog-Leroy-2026-06-12T141517: stale DROP
            // window [1245,1285], bot at 1287, while a fresh plan's window contained the
            // bot the whole time). Drop the edge and replan from the live position.
            if (runAiTick && entry.navEdge != null
                    && entry.navBlockedPosTicks > 0
                    && entry.navBlockedPosTicks >= entry.navBlockedPosGiveUpTicks) {
                clearNavigation(entry);
            }

            BotNavigationGraph.Edge edge = reuseCommittedEdge(graph, entry, startRegionId, targetRegionId);
            boolean edgeReused = (edge != null);
            if (edgeReused) {
                BotNavigationGraph.Edge refreshedEdge = refreshPendingClimbExitEdge(
                        graph, entry, bot, botPos, startRegionId, targetRegionId, pathTargetPos, edge, runAiTick);
                if (refreshedEdge != edge) {
                    edge = refreshedEdge;
                    edgeReused = edge != null;
                }
                if (edgeReused) {
                    BotNavigationGraph.Edge refreshedGroundEdge = refreshCommittedGroundEdge(
                            graph, entry, bot, startRegionId, targetRegionId, pathTargetPos, edge, runAiTick);
                    if (refreshedGroundEdge != edge) {
                        edge = refreshedGroundEdge;
                        edgeReused = edge != null;
                    }
                }
            }
            if (edge == null && runAiTick && startRegionId >= 0 && targetRegionId >= 0) {
                // Same-region planning is intentionally allowed: intra-region portals appear as
                // self-loop edges (fromRegionId == toRegionId) and A* picks them when the
                // walk-to-entry + walk-from-exit cost beats the direct walk. findPath returns
                // an empty path when direct walk wins, falling through to direct steering.
                edge = findNextEdge(graph, bot, startRegionId, targetRegionId, pathTargetPos);
                if (edge != null) {
                    entry.navEdge = edge;
                    entry.navTargetRegionId = targetRegionId;
                }
            }

            if (edge == null) {
                entry.lastNavDecision = !runAiTick ? "no-ai"
                        : startRegionId < 0 || targetRegionId < 0 ? "no-region"
                        : startRegionId == targetRegionId ? "same-region" : "no-path";
                clearNavigation(entry);
                return new NavigationDirective(rawTargetPos, false);
            }

            NavigationDirective executionDirective = tryExecuteEdge(graph, entry, bot, botPos, rawTargetPos, edge, runAiTick);
            if (executionDirective != null) {
                entry.lastNavDecision = "exec";
                entry.navBlockedPosTicks = 0;
                return executionDirective;
            }

            entry.lastNavDecision = edgeReused ? "reuse" : "new";
            trackBlockedPositionGate(entry, botPos, edgeReused);
            entry.navPreciseTarget = shouldUsePreciseTarget(graph, entry, botPos, edge);
            entry.navTargetPos = selectWaypoint(entry, graph, botPos, edge);
            return new NavigationDirective(new Point(entry.navTargetPos), false);
        } finally {
            BotPerformanceMonitor.record("nav-resolve", System.nanoTime() - startedAt);
        }
    }

    static boolean tryExecuteCommittedEdgeAfterGroundMovement(BotMovementState entry, Point rawTargetPos) {
        if (entry == null || entry.bot == null || entry.navEdge == null || entry.inAir || entry.climbing) {
            return false;
        }

        // Validate the edge is still applicable before attempting execution.
        // tickAirborne may have landed the bot at the destination in this same tick; the navEdge
        // isn't cleared until the next resolveTarget call, so reuseCommittedEdge would correctly
        // discard a DROP/JUMP edge whose toRegionId matches the bot's current region. Without this
        // check, tryExecuteDrop re-fires from the landing platform where there's no lower foothold,
        // sending the bot out of the map.
        BotNavigationGraph graph = resolveActiveGraph(entry.bot.getMap(), entry.movementProfile);
        if (graph == null) {
            BotNavigationGraphProvider.warmGraphAsync(entry.bot.getMap(), entry.movementProfile);
            return false;
        }
        Point botPos = entry.bot.getPosition();
        int startRegionId = resolveCurrentRegionId(graph, entry, entry.bot.getMap(), botPos);
        BotNavigationGraph.Edge edge = reuseCommittedEdge(graph, entry, startRegionId, entry.navTargetRegionId);
        if (edge == null) {
            BotMovementManager.clearNavigationState(entry);
            return false;
        }

        NavigationDirective directive = tryExecuteEdge(graph, entry, entry.bot, botPos, rawTargetPos, edge, true);
        if (directive == null || !directive.consumedTick) {
            return false;
        }

        entry.lastNavDecision = "exec";
        return true;
    }

    private static void clearNavigation(BotMovementState entry) {
        BotMovementManager.clearNavigationState(entry);
    }

    /*
     * Counts consecutive ticks spent parked against a committed edge's position gate
     * (block reason "*-pos") without any actual movement. resolveTarget gives the edge up
     * and replans once the count passes a jittered threshold (~300-500ms). Any position
     * change restarts the count, so a slow legal approach (e.g. slippery-ground pulse
     * creep) is never interrupted while it is making progress.
     */
    private static void trackBlockedPositionGate(BotMovementState entry, Point botPos, boolean edgeReused) {
        boolean blockedPos = edgeReused
                && entry.lastEdgeBlockReason != null
                && entry.lastEdgeBlockReason.endsWith("-pos");
        if (!blockedPos) {
            entry.navBlockedPosTicks = 0;
            return;
        }
        if (entry.navBlockedPosTicks == 0
                || botPos.x != entry.navBlockedPosX
                || botPos.y != entry.navBlockedPosY) {
            entry.navBlockedPosTicks = 0;
            entry.navBlockedPosGiveUpTicks = BLOCKED_POS_GIVE_UP_MIN_TICKS
                    + ThreadLocalRandom.current().nextInt(BLOCKED_POS_GIVE_UP_JITTER_TICKS + 1);
            entry.navBlockedPosX = botPos.x;
            entry.navBlockedPosY = botPos.y;
        }
        entry.navBlockedPosTicks++;
    }

    private static void notifyWarmup(BotMovementState entry, Character bot) {
        Character owner = entry.owner;
        if (owner == null) return;
        int ownerId = owner.getId();
        int mapId = bot.getMap().getId();
        long now = System.currentTimeMillis();
        Map<Integer, Long> byMap = WARMUP_NOTIFIED.get(ownerId);
        if (byMap != null) {
            Long last = byMap.get(mapId);
            if (last != null && (now - last) < 10_000L) return;
        }
        // Only count walkable footholds when we are about to send — lazy, inside throttle gate
        long walkable = bot.getMap().getFootholds().getAllFootholds().stream()
                .filter(fh -> !fh.isWall()).count();
        if (walkable < 100) return;
        WARMUP_NOTIFIED.computeIfAbsent(ownerId, k -> new ConcurrentHashMap<>()).put(mapId, now);
        owner.dropMessage(5, bot.getName() + " is warming map navigation cache, using fallback movement...");
    }

    private static BotNavigationGraph.Edge refreshPendingClimbExitEdge(BotNavigationGraph graph,
                                                                       BotMovementState entry,
                                                                       Character bot,
                                                                       Point botPos,
                                                                       int startRegionId,
                                                                       int targetRegionId,
                                                                       Point targetPos,
                                                                       BotNavigationGraph.Edge edge,
                                                                       boolean runAiTick) {
        if (!runAiTick
                || edge == null
                || !entry.climbing
                || edge.type != BotNavigationGraph.EdgeType.CLIMB
                || edge.launchStepX == 0
                || startRegionId < 0
                || targetRegionId < 0
                || startRegionId == targetRegionId) {
            return edge;
        }

        if (canExecuteClimbExitFromCurrentPosition(graph, bot.getMap(), botPos, edge)) {
            return edge;
        }

        BotNavigationGraph.Edge bestEdge = findNextEdge(graph, bot, startRegionId, targetRegionId, targetPos);
        if (sameEdge(edge, bestEdge) || bestEdge == null) {
            return edge;
        }

        entry.navEdge = bestEdge;
        entry.navTargetRegionId = targetRegionId;
        entry.navTargetPos = null;
        entry.navPreciseTarget = false;
        return bestEdge;
    }

    private static BotNavigationGraph.Edge refreshCommittedGroundEdge(BotNavigationGraph graph,
                                                                      BotMovementState entry,
                                                                      Character bot,
                                                                      int startRegionId,
                                                                      int targetRegionId,
                                                                      Point targetPos,
                                                                      BotNavigationGraph.Edge edge,
                                                                      boolean runAiTick) {
        if (!runAiTick
                || edge == null
                || entry.inAir
                || entry.climbing
                || startRegionId < 0
                || targetRegionId < 0
                || startRegionId == targetRegionId) {
            return edge;
        }

        BotNavigationGraph.Edge bestEdge = findNextEdge(graph, bot, startRegionId, targetRegionId, targetPos);
        if (bestEdge == null || sameEdge(edge, bestEdge)) {
            return edge;
        }
        if (shouldRetainCommittedGroundEdge(edge, bestEdge)) {
            return edge;
        }

        entry.navEdge = bestEdge;
        entry.navTargetRegionId = targetRegionId;
        entry.navTargetPos = null;
        entry.navPreciseTarget = false;
        return bestEdge;
    }

    static BotNavigationGraph.Edge reuseCommittedEdge(BotNavigationGraph graph,
                                                      BotMovementState entry,
                                                      int startRegionId,
                                                      int targetRegionId) {
        BotNavigationGraph.Edge edge = entry.navEdge;
        if (edge == null) {
            return null;
        }
        if (targetRegionId < 0) {
            return null;
        }
        int previousTargetRegionId = entry.navTargetRegionId;
        // Update stored target in-place rather than discarding. The Y-snap offset causes
        // followBase.x to differ between AI and non-AI ticks, making targetRegionId fluctuate
        // even when the owner hasn't meaningfully moved. Relying on structural checks below
        // (start-region match, usability, arrival) is sufficient to detect actual invalidity.
        entry.navTargetRegionId = targetRegionId;
        if (!isEdgeUsable(graph, entry.bot, edge)) {
            return null;
        }
        if (entry.climbing && isRopeEntryEdge(graph, edge)) {
            return null;
        }
        if (startRegionId == edge.toRegionId && !entry.inAir && !entry.climbing
                && edge.fromRegionId != edge.toRegionId) {
            // Self-loop edges (intra-region portals) inherently start and end in the same
            // region. Completion is signalled by execution (tryExecutePortal teleporting the
            // bot), not by a region change — don't retire on region match.
            return null;
        }
        // Once the resolved target is back in the bot's current region, a committed edge that
        // would leave that region is *usually* stale — follow/formation loops where the bot keeps
        // running toward an old jump/drop/portal after the live follow target snapped back onto the
        // current platform. But "same region" does NOT imply "direct walk reaches it": a region can
        // be two platforms split by a gap (e.g. map 1020000 r11), where the only route to a target
        // on the far platform genuinely loops out through a portal and back. A* commits that
        // leave-region edge *for this same-region target* (previousTargetRegionId == targetRegionId);
        // retiring it every tick made non-AI ticks revert to the raw pin (opposite direction) and the
        // bot thrashed in place. Only treat it as stale when the target actually CHANGED region
        // (the snap-back case) — then the edge was planned for a different target and is truly stale.
        if (!entry.inAir && !entry.climbing
                && startRegionId >= 0 && startRegionId == targetRegionId
                && edge.toRegionId != startRegionId
                && previousTargetRegionId != targetRegionId) {
            return null;
        }
        if (startRegionId == edge.fromRegionId) {
            if (!entry.inAir && !entry.climbing
                    && previousTargetRegionId >= 0
                    && previousTargetRegionId != targetRegionId
                    && edge.toRegionId != targetRegionId) {
                return null;
            }
            return edge;
        }
        // While climbing, always keep the edge — findGroundFoothold gives false positives
        // (returns the platform below/behind the rope as the "current" region), which would
        // otherwise drop the exit edge the moment the bot enters the destination region's Y range.
        if (entry.climbing && (startRegionId < 0 || startRegionId != edge.toRegionId)) {
            return edge;
        }
        // DROP/JUMP arcs may enter the destination region before the bot touches down.
        // Keep the edge until landing. Only retain if the bot is in a region consistent with
        // this arc (destination or unmapped) — prevents looping in a wrong region mid-air.
        if (entry.inAir && (startRegionId < 0 || startRegionId == edge.toRegionId)
                && (edge.type == BotNavigationGraph.EdgeType.DROP
                    || edge.type == BotNavigationGraph.EdgeType.JUMP)) {
            return edge;
        }
        if (entry.inAir && edge.type == BotNavigationGraph.EdgeType.CLIMB && edge.launchStepX != 0) {
            // Rope-exit jump arcs use the same sampled ballistic model as JUMP/DROP edges.
            // Keep the committed edge until the bot actually lands or grabs a rope again;
            // otherwise mid-air replans can steer the bot off the authored landing path.
            return edge;
        }
        return null;
    }

    private static NavigationDirective tryExecuteEdge(BotNavigationGraph graph,
                                                      BotMovementState entry,
                                                      Character bot,
                                                      Point botPos,
                                                      Point rawTargetPos,
                                                      BotNavigationGraph.Edge edge,
                                                      boolean runAiTick) {
        if (!runAiTick) {
            return null;
        }

        return switch (edge.type) {
            case JUMP -> tryExecuteJump(graph, entry, bot, rawTargetPos, edge);
            case DROP -> tryExecuteDrop(graph, entry, bot, botPos, rawTargetPos, edge);
            case CLIMB -> tryExecuteClimb(graph, entry, bot, botPos, rawTargetPos, edge);
            case PORTAL -> tryExecutePortalEdge(entry, bot, botPos, rawTargetPos, edge);
            default -> null;
        };
    }

    private static NavigationDirective tryExecuteJump(BotNavigationGraph graph,
                                                      BotMovementState entry,
                                                      Character bot,
                                                      Point rawTargetPos,
                                                      BotNavigationGraph.Edge edge) {
        if (entry.inAir || entry.climbing) {
            return null;
        }
        // F2a (SoloMapling graft): don't COMMIT a jump off a closest-profile fallback graph. While the
        // exact jump/speed-bucket graph bakes off-thread, peekBestGraph (resolveActiveGraph) serves the
        // nearest cached profile; its JUMP edges were validated for a different jump arc, so firing one
        // at the bot's real (post-2026-07-06, taller) jump overshoots tiny platforms. The served graph is
        // the exact one iff it IS the profile's cached instance (peekGraph == graph); otherwise it's a
        // fallback and we withhold only the launch. Route/walk planning keeps using the closest graph
        // (unchanged), and resolveTarget's navGraph-swap replan re-commits a correct jump the instant the
        // exact graph lands. A brief stall at the launch point meanwhile is acceptable and transient.
        if (BotNavigationGraphProvider.peekGraph(bot.getMap(), entry.movementProfile) != graph) {
            entry.lastEdgeBlockReason = "jump-graph-warmup";
            return null;
        }
        Point botPos = bot.getPosition();
        if (!canExecuteSelectedJumpFromCurrentPosition(graph, entry, bot.getMap(), botPos, edge)) {
            // Bot may be standing at the top of a rope region whose bottom is the jump entry.
            // Grab the rope and descend — tickClimbing will naturally drive toward edge.startPoint.
            if (edge.startPoint.y > botPos.y) {
                BotNavigationGraph.Region fromRegion = graph.getRegion(edge.fromRegionId);
                if (fromRegion != null && fromRegion.isRopeRegion) {
                    Rope rope = findRopeForRegion(bot.getMap(), fromRegion);
                    if (rope != null && canGrabRopeAtCurrentPosition(botPos, rope)) {
                        // Attach at bot's current Y — tickClimbing will drive it down to startPoint.
                        // Using edge.startPoint.y would teleport the bot rather than letting it climb.
                        startClimbing(entry, bot, rope, botPos.y);
                        return new NavigationDirective(rawTargetPos, true);
                    }
                }
            }
            entry.lastEdgeBlockReason = "jump-pos";
            return null;
        }

        if (deepenJumpLaunchOneStep(graph, entry, bot.getMap(), edge)) {
            entry.lastEdgeBlockReason = "jump-delay";
            return null; // steering follows the bumped launch X — walk a step deeper first
        }
        // Vertical jumps (launchStepX=0) on slippery ground carry the residual slide into the
        // air (packet-true no-input launch), which would drift the planned straight-up arc.
        // Wait for the stop policy (glide / counter-strafe brake) to shed the slide first.
        // Directional jumps never wait: the launch snaps to ±walkSpeed regardless of slide.
        if (edge.launchStepX == 0
                && BotPhysicsEngine.slipperyGround(bot.getMap())
                && BotPhysicsEngine.carriedAirVelX(bot.getMap(), entry) != 0) {
            entry.lastEdgeBlockReason = "jump-slide";
            return null;
        }
        entry.lastEdgeBlockReason = null;
        setEdgeExecutionTarget(entry, edge);
        BotMovementManager.initiateJump(entry, bot, edge.launchStepX);
        // One-shot launch point: a missed arc re-rolls a fresh spot (and a fresh deepen count)
        // on the next approach instead of repeating the identical failure forever.
        entry.navJumpLaunchEdge = null;
        entry.navJumpLaunchX = Integer.MIN_VALUE;
        entry.navJumpLaunchDelaySteps = Integer.MIN_VALUE;
        return new NavigationDirective(rawTargetPos, true);
    }

    /*
     * Launch variation: instead of always firing the instant the bot reaches its selected
     * launch X, sometimes carry 0-2 more walk steps into the window first (rolled once per
     * approach). A borderline arc — e.g. real speed sitting between graph buckets — that
     * misses from one spot is unlikely to also miss a step or two deeper, and the variation
     * reads more human than frame-perfect launches.
     */
    private static boolean deepenJumpLaunchOneStep(BotNavigationGraph graph,
                                                   BotMovementState entry,
                                                   MapleMap map,
                                                   BotNavigationGraph.Edge edge) {
        if (entry.navJumpLaunchX == Integer.MIN_VALUE) {
            return false; // rope-anchored launches have no window to walk around in
        }
        int dir = Integer.signum(edge.launchStepX);
        if (dir == 0) {
            return false; // vertical jump: no launch direction to deepen along
        }
        if (entry.navJumpLaunchDelaySteps == Integer.MIN_VALUE) {
            entry.navJumpLaunchDelaySteps = ThreadLocalRandom.current().nextInt(3);
        }
        if (entry.navJumpLaunchDelaySteps <= 0) {
            return false;
        }
        int deeperX = entry.navJumpLaunchX + dir * BotPhysicsEngine.walkStep(map, entry.movementProfile);
        BotNavigationGraph.Region fromRegion = graph.getRegion(edge.fromRegionId);
        if (!edge.containsLaunchX(deeperX)
                || fromRegion == null || fromRegion.isRopeRegion
                || deeperX < fromRegion.minX || deeperX > fromRegion.maxX) {
            entry.navJumpLaunchDelaySteps = 0; // no legal room deeper — fire from here
            return false;
        }
        entry.navJumpLaunchDelaySteps--;
        entry.navJumpLaunchX = deeperX;
        return true;
    }

    private static NavigationDirective tryExecuteDrop(BotNavigationGraph graph,
                                                      BotMovementState entry,
                                                      Character bot,
                                                      Point botPos,
                                                      Point rawTargetPos,
                                                      BotNavigationGraph.Edge edge) {
        if (entry.inAir || entry.climbing || entry.downJumpPending) {
            return null;
        }

        if (edge.launchStepX != 0) {
            // Walk-off drops are not an explicit action. Keep steering in the authored direction
            // and let ground physics carry the bot into a fall with preserved momentum.
            return null;
        }

        if (!canExecuteDropFromCurrentPosition(graph, bot.getMap(), botPos, edge)) {
            entry.lastEdgeBlockReason = "drop-pos";
            return null;
        }

        entry.lastEdgeBlockReason = null;
        setEdgeExecutionTarget(entry, edge);
        BotPhysicsEngine.queueDownJump(entry, bot);
        BotMovementManager.broadcastMovement(entry);
        return new NavigationDirective(rawTargetPos, true);
    }

    private static NavigationDirective tryExecuteClimb(BotNavigationGraph graph,
                                                       BotMovementState entry,
                                                       Character bot,
                                                       Point botPos,
                                                       Point rawTargetPos,
                                                       BotNavigationGraph.Edge edge) {
        if (entry.inAir || entry.downJumpPending) {
            return null;
        }

        if (entry.climbing) {
            return tryExecuteClimbExit(graph, entry, bot, botPos, rawTargetPos, edge);
        } else {
            return tryExecuteClimbEntry(graph, entry, bot, botPos, rawTargetPos, edge);
        }
    }

    private static NavigationDirective tryExecuteClimbEntry(BotNavigationGraph graph,
                                                             BotMovementState entry,
                                                             Character bot,
                                                             Point botPos,
                                                             Point rawTargetPos,
                                                             BotNavigationGraph.Edge edge) {
        BotNavigationGraph.Region toRegion = graph.getRegion(edge.toRegionId);
        Rope rope = findRopeForRegion(bot.getMap(), toRegion);
        if (rope == null) {
            return null;
        }
        if (!canExecuteClimbEntryFromCurrentPosition(bot.getMap(), botPos, edge, rope)) {
            entry.lastEdgeBlockReason = "climb-pos";
            return null;
        }

        if (canGrabRopeAtCurrentPosition(botPos, rope)) {
            // Bot is already within the rope's Y range — attach at its current Y, not the edge
            // endPoint. Using endPoint.y (rope top) would teleport a bot at the bottom of the
            // rope all the way to the top instantly.
            entry.lastEdgeBlockReason = null;
            startClimbing(entry, bot, rope, botPos.y);
            return new NavigationDirective(rawTargetPos, true);
        }
        if (canAttachToRopeFromTopPlatform(edge, botPos, rope)) {
            entry.lastEdgeBlockReason = null;
            startClimbing(entry, bot, rope, edge.endPoint.y);
            return new NavigationDirective(rawTargetPos, true);
        }
        if (canGrabRopeFromTopPlatform(edge, botPos, rope)) {
            // Top-of-rope entry is a separate intent from down-jump. Queue it and let grounded
            // physics consume the request on the next tick, just like other input-driven actions.
            entry.lastEdgeBlockReason = null;
            BotPhysicsEngine.queueTopRopeEntry(entry, bot, rope, edge.endPoint.y);
            BotMovementManager.broadcastMovement(entry);
            return new NavigationDirective(rawTargetPos, true);
        }

        if (canExecuteGroundRopeJumpEntryFromCurrentPosition(botPos, edge)) {
            entry.lastEdgeBlockReason = null;
            BotMovementManager.initiateRopeJump(entry, bot, edge.launchStepX);
            return new NavigationDirective(rawTargetPos, true);
        }

        entry.lastEdgeBlockReason = "climb-reach";
        return null;
    }

    private static NavigationDirective tryExecuteClimbExit(BotNavigationGraph graph,
                                                            BotMovementState entry,
                                                            Character bot,
                                                            Point botPos,
                                                            Point rawTargetPos,
                                                            BotNavigationGraph.Edge edge) {
        if (!canExecuteClimbExitFromCurrentPosition(graph, bot.getMap(), botPos, edge)) {
            return null;
        }
        BotNavigationGraph.Region toRegion = graph.getRegion(edge.toRegionId);

        if (toRegion != null && toRegion.isRopeRegion) {
            // Rope-to-rope: jump to the other rope
            Rope targetRope = findRopeForRegion(bot.getMap(), toRegion);
            if (targetRope == null || BotMovementManager.sameRope(entry.climbRope, targetRope)) {
                return null;
            }
            BotMovementManager.jumpToRope(entry, bot, edge.launchStepX);
            return new NavigationDirective(rawTargetPos, true);
        }

        if (edge.launchStepX == 0) {
            // launchStepX==0 means step off the top of the rope onto the foothold above.
            // Physics already handles this: resolveClimbBoundary lands the bot when it reaches
            // topY. Nav just lets the bot climb — the edge completes when the bot transitions
            // to the destination region after physics lands it.
            return null;
        }

        // Jump off rope
        Rope sourceRope = findRopeForRegion(bot.getMap(), graph.getRegion(edge.fromRegionId));
        if (isTopRopeJumpExitReady(sourceRope, botPos, edge) && botPos.y != edge.startPoint.y) {
            startClimbing(entry, bot, sourceRope, edge.startPoint.y);
        }
        BotMovementManager.jumpOffRope(entry, bot, edge.launchStepX);
        return new NavigationDirective(rawTargetPos, true);
    }

    static boolean canExecuteDropFromCurrentPosition(BotNavigationGraph graph,
                                                     MapleMap map,
                                                     Point botPos,
                                                     BotNavigationGraph.Edge edge) {
        if (edge.type != BotNavigationGraph.EdgeType.DROP) {
            return false;
        }
        if (edge.launchStepX != 0) {
            return false;
        }
        if (!isWithinDropLaunchWindow(graph, botPos, edge)) {
            return false;
        }
        return true;
    }

    private static NavigationDirective tryExecutePortalEdge(BotMovementState entry,
                                                            Character bot,
                                                            Point botPos,
                                                            Point rawTargetPos,
                                                            BotNavigationGraph.Edge edge) {
        // Landed gate: never activate a portal while airborne. A down-jump or knockback that clips
        // the portal's trigger box mid-fall must NOT warp — the bot lands and walks in first.
        if (entry.inAir || !isReadyForEdge(botPos, edge)) {
            entry.portalEnterReadyTicks = -1;
            return null;
        }
        // Positional jitter: once eligible, keep walking deeper onto the portal for a few extra ticks
        // (selectWaypoint/precise targeting keep steering to startPoint) instead of firing the instant
        // it's permitted. Tick-counted inward movement, not a standing wait.
        if (entry.portalEnterReadyTicks < 0) {
            entry.portalEnterReadyTicks = ThreadLocalRandom.current().nextInt(PORTAL_ENTER_EXTRA_TICKS_MAX + 1);
        }
        if (entry.portalEnterReadyTicks > 0) {
            entry.portalEnterReadyTicks--;
            return null;
        }
        entry.portalEnterReadyTicks = -1;
        return tryExecutePortal(entry, bot, rawTargetPos, edge);
    }

    private static NavigationDirective tryExecutePortal(BotMovementState entry,
                                                        Character bot,
                                                        Point rawTargetPos,
                                                        BotNavigationGraph.Edge edge) {
        if (System.currentTimeMillis() < entry.portalUseCooldownUntilMs) {
            return null;
        }
        if (!usePortal(bot, edge.portalId)) {
            return null;
        }

        entry.portalUseCooldownUntilMs = System.currentTimeMillis() + PORTAL_USE_COOLDOWN_MS;
        clearNavigation(entry);
        BotMovementManager.resetEntryState(entry);
        return new NavigationDirective(rawTargetPos, true);
    }

    private static boolean shouldUsePreciseTarget(BotNavigationGraph graph,
                                                  BotMovementState entry,
                                                  Point botPos,
                                                  BotNavigationGraph.Edge edge) {
        if (entry.inAir) {
            return false;
        }
        return switch (edge.type) {
            case WALK -> shouldUsePreciseWalkTarget(edge);
            case JUMP -> !canExecuteSelectedJumpFromCurrentPosition(graph, entry, entry.bot.getMap(), botPos, edge);
            case DROP -> edge.launchStepX == 0
                    && !canExecuteDropFromCurrentPosition(graph, entry.bot.getMap(), botPos, edge);
            case CLIMB -> entry.climbing
                    ? edge.launchStepX != 0
                    && !canExecuteClimbExitFromCurrentPosition(graph, entry.bot.getMap(), botPos, edge)
                    : !canExecuteClimbEntryFromCurrentPosition(entry.bot.getMap(), botPos, edge,
                    findRopeForRegion(entry.bot.getMap(), graph.getRegion(edge.toRegionId)));
            case PORTAL -> !isReadyForEdge(botPos, edge) || entry.portalEnterReadyTicks > 0; // precise while walking the extra jitter ticks in
        };
    }

    private static Point selectWaypoint(BotMovementState entry, BotNavigationGraph graph, Point botPos, BotNavigationGraph.Edge edge) {
        return switch (edge.type) {
            case WALK -> new Point(edge.endPoint);
            case CLIMB -> selectClimbWaypoint(graph, entry, botPos, edge);
            case JUMP -> entry.inAir ? new Point(edge.endPoint) : selectJumpWaypoint(graph, entry, botPos, edge);
            case DROP -> selectDropWaypoint(entry, graph, botPos, edge);
            case PORTAL -> new Point(edge.startPoint); // always head to the portal entrance; it only fires once landed there
        };
    }

    static Point selectJumpWaypoint(BotMovementState entry, Point botPos, BotNavigationGraph.Edge edge) {
        BotNavigationGraph graph = BotNavigationGraphProvider.getGraph(entry.bot.getMap(), entry.movementProfile);
        return selectJumpWaypoint(graph, entry, botPos, edge);
    }

    static Point selectJumpWaypoint(BotNavigationGraph graph, Point botPos, BotNavigationGraph.Edge edge) {
        return selectJumpWaypoint(graph, null, botPos, edge);
    }

    private static Point selectJumpWaypoint(BotNavigationGraph graph,
                                            BotMovementState entry,
                                            Point botPos,
                                            BotNavigationGraph.Edge edge) {
        BotNavigationGraph.Region fromRegion = graph.getRegion(edge.fromRegionId);
        if (fromRegion == null || fromRegion.isRopeRegion) {
            return new Point(edge.startPoint);
        }
        int targetX = entry == null
                ? edge.containsLaunchX(botPos.x) ? botPos.x : botPos.x < edge.launchMinX ? edge.launchMinX : edge.launchMaxX
                : selectedJumpLaunchX(entry, graph, edge);
        return fromRegion.pointAt(targetX);
    }

    static Point selectClimbWaypoint(BotMovementState entry, Point botPos, BotNavigationGraph.Edge edge) {
        BotNavigationGraph graph = resolveActiveGraph(entry.bot.getMap(), entry.movementProfile);
        return selectClimbWaypoint(graph, entry, botPos, edge);
    }

    static Point selectClimbWaypoint(BotNavigationGraph graph, BotMovementState entry, Point botPos, BotNavigationGraph.Edge edge) {
        if (entry.inAir) {
            return new Point(edge.endPoint);
        }
        if (entry.climbing && edge.launchStepX != 0) {
            // Jump-off and rope-to-rope exits: only hold position when the exit can execute
            // immediately; otherwise keep steering toward the authored launch anchor.
            // Graphgen and physics both treat edge.startPoint as the required on-rope launch Y;
            // steering toward edge.endPoint here would be a runtime-only model mismatch because
            // a climbing bot cannot physically approach the off-rope landing point.
            if (graph != null && canExecuteClimbExitFromCurrentPosition(graph, entry.bot.getMap(), botPos, edge)) {
                return new Point(botPos);
            }
            return new Point(edge.startPoint);
        }
        if (entry.climbing) {
            // launchStepX==0: keep holding climb direction on the rope and let physics dismount
            // the bot at the boundary. The on-rope steering target should stay on the rope X;
            // trying to snap to an off-rope landing point is a runtime-only constraint and can
            // re-clamp the bot back onto the rope top/bottom.
            int ropeX = entry.climbRope != null ? entry.climbRope.x() : edge.startPoint.x;
            return new Point(ropeX, edge.endPoint.y);
        }
        if (edge.launchStepX != 0 && edge.launchMaxX > edge.launchMinX) {
            // Grounded rope-jump entry: the gate accepts the whole launch window, so steer
            // to the nearest in-window x (inset), not the authored startPoint pixel.
            return new Point(steerXWithinLaunchWindow(edge, botPos.x), edge.startPoint.y);
        }
        return new Point(edge.startPoint);
    }

    /* Nearest in-window steering x, inset from the window boundary (center when narrow). */
    static int steerXWithinLaunchWindow(BotNavigationGraph.Edge edge, int botX) {
        int inset = Math.min((edge.launchMaxX - edge.launchMinX) / 2, LAUNCH_WINDOW_STEER_INSET_PX);
        return Math.clamp(botX, edge.launchMinX + inset, edge.launchMaxX - inset);
    }

    private static BotNavigationGraph resolveActiveGraph(MapleMap map, BotMovementProfile movementProfile) {
        return BotNavigationGraphProvider.peekBestGraph(map, movementProfile);
    }

    static Point selectDropWaypoint(BotMovementState entry,
                                    BotNavigationGraph graph,
                                    Point botPos,
                                    BotNavigationGraph.Edge edge) {
        if (entry.inAir) {
            return new Point(edge.endPoint);
        }
        if (edge.launchStepX == 0) {
            BotNavigationGraph.Region fromRegion = graph != null ? graph.getRegion(edge.fromRegionId) : null;
            if (fromRegion == null || fromRegion.isRopeRegion) {
                return new Point(edge.startPoint);
            }
            int targetX = edge.containsLaunchX(botPos.x)
                    ? botPos.x
                    : steerXWithinLaunchWindow(edge, botPos.x);
            return fromRegion.pointAt(targetX);
        }

        if (hasReachedDirectionalDropRunway(botPos, edge)) {
            return new Point(edge.endPoint);
        }

        BotNavigationGraph.Region fromRegion = graph.getRegion(edge.fromRegionId);
        if (fromRegion == null || fromRegion.isRopeRegion) {
            return new Point(edge.endPoint);
        }

        BotPhysicsEngine.WalkOffLanding liveOutcome = BotPhysicsEngine.simulateWalkOffLanding(
                entry.bot.getMap(), botPos, Integer.signum(edge.launchStepX),
                new BotPhysicsEngine.GroundTravelState(entry.physX, entry.hspeed, entry.groundPhysicsCarryMs),
                entry.movementProfile);
        if (matchesDirectionalDrop(edge, graph, liveOutcome)) {
            // Like rope top step-offs, once the continuous-control exit is naturally executable
            // we stop targeting an intermediate anchor and just keep feeding the authored
            // direction until physics performs the dismount.
            return new Point(edge.endPoint);
        }
        return new Point(edge.startPoint);
    }

    private static boolean hasReachedDirectionalDropRunway(Point botPos, BotNavigationGraph.Edge edge) {
        if (botPos == null || edge == null || edge.launchStepX == 0) {
            return false;
        }

        int direction = Integer.signum(edge.launchStepX);
        return direction > 0
                ? botPos.x >= edge.startPoint.x
                : botPos.x <= edge.startPoint.x;
    }

    private static boolean matchesDirectionalDrop(BotNavigationGraph.Edge edge,
                                                  BotNavigationGraph graph,
                                                  BotPhysicsEngine.WalkOffLanding outcome) {
        if (outcome == null || outcome.landing() == null) {
            return false;
        }
        Foothold landingFoothold = outcome.landing().foothold();
        if (landingFoothold == null) {
            return false;
        }
        if (graph.regionIdByFootholdId.getOrDefault(landingFoothold.getId(), -1) != edge.toRegionId) {
            return false;
        }
        int xTolerance = Math.max(6, Math.abs(edge.launchStepX) + 2);
        int yTolerance = BotMovementManager.cfg.JUMP_Y_THRESH * 2;
        return Math.abs(outcome.landing().point().x - edge.endPoint.x) <= xTolerance
                && Math.abs(outcome.landing().point().y - edge.endPoint.y) <= yTolerance;
    }

    private static BotNavigationGraph.Edge findNextEdge(BotNavigationGraph graph,
                                                        Character bot,
                                                        int startRegionId,
                                                        int targetRegionId,
                                                        Point targetPos) {
        List<BotNavigationGraph.Edge> path = findPath(graph, bot.getMap(), bot.getPosition(), startRegionId, targetRegionId, targetPos, null, routeSeed(bot));
        if (path.isEmpty()) {
            return null;
        }
        return collapseLeadingWalkEdges(path);
    }

    static List<BotNavigationGraph.Edge> findPath(BotNavigationGraph graph,
                                                  Character bot,
                                                  int startRegionId,
                                                  int targetRegionId,
                                                  Point targetPos) {
        return findPath(graph, bot.getMap(), bot.getPosition(), startRegionId, targetRegionId, targetPos, null, routeSeed(bot));
    }

    static List<BotNavigationGraph.Edge> findPath(BotNavigationGraph graph,
                                                  MapleMap map,
                                                  Point startPos,
                                                  int startRegionId,
                                                  int targetRegionId,
                                                  Point targetPos) {
        return findPath(graph, map, startPos, startRegionId, targetRegionId, targetPos, null);
    }

    static List<BotNavigationGraph.Edge> findPathForTargetScore(BotNavigationGraph graph,
                                                                MapleMap map,
                                                                Point startPos,
                                                                int startRegionId,
                                                                int targetRegionId,
                                                                Point targetPos) {
        return findPath(graph, map, startPos, startRegionId, targetRegionId, targetPos, "target-score");
    }

    /*
     * Production pathfinding heuristic toggle. When true (default) the search runs the
     * admissible h=0 (Dijkstra) variant: optimal-cost paths, no portal-skipping. Flip to
     * false to restore the legacy dx/walk-speed heuristic (faster per search, but on
     * Kerning City ~19% of cross-region paths were non-optimal and ~7% walked past a usable
     * portal — see BotNavigationProbe --measure). The legacy .heuristic and the
     * .runSearch zeroHeuristic branch are both retained; this is the single knob.
     */
    static boolean useAdmissibleHeuristic = true;

    private static List<BotNavigationGraph.Edge> findPath(BotNavigationGraph graph,
                                                          MapleMap map,
                                                          Point startPos,
                                                          int startRegionId,
                                                          int targetRegionId,
                                                          Point targetPos,
                                                          String pathfindCaller) {
        return findPath(graph, map, startPos, startRegionId, targetRegionId, targetPos, pathfindCaller, 0L);
    }

    private static List<BotNavigationGraph.Edge> findPath(BotNavigationGraph graph,
                                                          MapleMap map,
                                                          Point startPos,
                                                          int startRegionId,
                                                          int targetRegionId,
                                                          Point targetPos,
                                                          String pathfindCaller,
                                                          long routeSeed) {
        return runSearch(graph, map, startPos, startRegionId, targetRegionId, targetPos,
                pathfindCaller, useAdmissibleHeuristic, true, routeSeed).path();
    }

    /*
     * Core region-graph A* search. With zeroHeuristic=true it runs an admissible h=0
     * search (degenerates to Dijkstra) that always returns the optimal-cost path; the default
     * dx-based heuristic can over-estimate across zero-cost PORTAL edges (and faster-than-walk
     * jumps) and return a longer route. instrument=false skips slow-path logging and the
     * perf record so measurement callers can run the search twice cheaply.
     */
    static SearchOutcome runSearch(BotNavigationGraph graph,
                                   MapleMap map,
                                   Point startPos,
                                   int startRegionId,
                                   int targetRegionId,
                                   Point targetPos,
                                   String pathfindCaller,
                                   boolean zeroHeuristic,
                                   boolean instrument,
                                   long routeSeed) {
        long startedAt = System.nanoTime();
        PathfindProfile profile = null;
        // routeSeed != 0 (per-bot) diversifies routes so 100 bots don't stack on one optimal
        // path, and switches the search from h=0 Dijkstra (full-graph scan) to a per-bot
        // weighted A* that prunes. Seed 0 = exact legacy behavior (probes/calibration/non-bot).
        boolean randomized = routeSeed != 0;
        double epsilon = randomized ? 1.0 + hashFrac(routeSeed, EPSILON_SALT) * EPSILON_SPAN : 0.0;
        try {
            PriorityQueue<SearchNode> open = new PriorityQueue<>(Comparator.comparingInt(node -> node.score));
            Map<SearchState, Integer> gScore = new HashMap<>();
            Map<SearchState, SearchState> cameFrom = new HashMap<>();
            Map<SearchState, BotNavigationGraph.Edge> cameByEdge = new HashMap<>();
            SearchState startState = new SearchState(startRegionId, new Point(startPos), false);
            SearchState bestGoalState = null;
            int bestGoalCost = Integer.MAX_VALUE;
            int expandedNodes = 0;
            int staleNodes = 0;
            int edgeChecks = 0;
            int usableEdges = 0;
            int relaxations = 0;
            int openPeak = 1;

            gScore.put(startState, 0);
            open.add(new SearchNode(startState, 0, hValue(graph, startPos, targetPos, zeroHeuristic, randomized, epsilon)));

            while (!open.isEmpty()) {
                SearchNode current = open.poll();
                if (current.cost != gScore.getOrDefault(current.state, Integer.MAX_VALUE)) {
                    staleNodes++;
                    continue;
                }
                if (bestGoalState != null && current.score >= bestGoalCost) {
                    break;
                }
                expandedNodes++;

                if (current.state.regionId == targetRegionId) {
                    int goalCost = current.cost + intraRegionTravelCost(graph, current.state.regionId, current.state.point, targetPos);
                    if (goalCost < bestGoalCost) {
                        bestGoalCost = goalCost;
                        bestGoalState = current.state;
                    }
                }

                for (BotNavigationGraph.Edge edge : graph.getOutgoing(current.state.regionId)) {
                    edgeChecks++;
                    if (!isEdgeUsable(graph, map, edge)) {
                        continue;
                    }
                    usableEdges++;

                    boolean isPortal = edge.type == BotNavigationGraph.EdgeType.PORTAL;
                    // Portals are free on their own (edge.cost == 0). Charge PORTAL_USE_COOLDOWN_MS
                    // only when the bot enters a portal *through the exit* of the one it just took —
                    // i.e. it landed on a portal and immediately re-enters without walking. A
                    // viaPortal state's point IS the previous portal's exit, so this is exactly when
                    // that exit coincides with this portal's entry. That covers the "return to old
                    // position" round-trip and co-located A>B>C hops, but NOT A>B>walk>C>D (the bot
                    // walked off the exit first, so the entry points differ and it stays free).
                    boolean enteredThroughExit = current.state.viaPortal
                            && current.state.point.equals(edge.startPoint);
                    int edgeCost = isPortal && enteredThroughExit ? (int) PORTAL_USE_COOLDOWN_MS : edge.cost;
                    // A straight DROP (launchStepX==0) falls in place: it executes from the nearest
                    // in-window x to the bot (selectDropWaypoint) and lands at that same x, NOT from/at
                    // the authored window-midpoint start/end points. Cost the approach to that nearest
                    // in-window x AND land the next state there, so A* matches execution across the whole
                    // window. Otherwise a wide drop window inflates BOTH the approach (to the midpoint
                    // startPoint) and the downstream goal-walk (from the midpoint landing), which can
                    // lose a strictly-cheaper direct drop to a rope detour. Scoped to DROP+stepX==0
                    // only: directional drops and JUMPs keep their authored start/end geometry.
                    boolean straightDrop = edge.type == BotNavigationGraph.EdgeType.DROP && edge.launchStepX == 0;
                    Point approachPoint = straightDrop
                            ? edge.pointAtNearestLaunchX(current.state.point.x)
                            : edge.startPoint;
                    Point landingPoint = straightDrop
                            ? new Point(approachPoint.x, edge.endPoint.y)
                            : edge.endPoint;
                    int stepCost = intraRegionTravelCost(graph, current.state.regionId, current.state.point, approachPoint) + edgeCost;
                    // Per-bot positive jitter, stable per (bot, edge): different bots perceive
                    // different edges as slightly costlier and fan out onto distinct routes, while
                    // a single bot re-plans the same route every tick (no fluttering). Positive-only
                    // so reported cost never under-states true cost (keeps portal/direct-walk
                    // comparisons conservative).
                    if (randomized) {
                        stepCost += (int) Math.round(stepCost * JITTER_FRAC * hashFrac(routeSeed, edgeKey(edge)));
                    }
                    int tentativeCost = current.cost + stepCost;
                    SearchState nextState = new SearchState(edge.toRegionId, landingPoint, isPortal);
                    if (tentativeCost >= gScore.getOrDefault(nextState, Integer.MAX_VALUE)) {
                        continue;
                    }

                    relaxations++;
                    gScore.put(nextState, tentativeCost);
                    cameFrom.put(nextState, current.state);
                    cameByEdge.put(nextState, edge);
                    int fScore = tentativeCost + hValue(graph, edge.endPoint, targetPos, zeroHeuristic, randomized, epsilon);
                    open.add(new SearchNode(nextState, tentativeCost, fScore));
                    openPeak = Math.max(openPeak, open.size());
                }
            }

            List<BotNavigationGraph.Edge> path = reconstructPath(startState, bestGoalState, cameFrom, cameByEdge);
            profile = new PathfindProfile(
                    System.nanoTime() - startedAt,
                    expandedNodes,
                    staleNodes,
                    edgeChecks,
                    usableEdges,
                    relaxations,
                    openPeak,
                    bestGoalCost,
                    path.size());
            boolean usesPortal = false;
            for (BotNavigationGraph.Edge edge : path) {
                if (edge.type == BotNavigationGraph.EdgeType.PORTAL) {
                    usesPortal = true;
                    break;
                }
            }
            return new SearchOutcome(path, bestGoalCost, expandedNodes, usesPortal);
        } finally {
            if (instrument) {
                if (profile == null) {
                    profile = new PathfindProfile(
                            System.nanoTime() - startedAt,
                            0,
                            0,
                            0,
                            0,
                            0,
                            0,
                            Integer.MAX_VALUE,
                            0);
                }
                logSlowPathfind(graph, map, startPos, startRegionId, targetRegionId, targetPos, pathfindCaller, profile);
                BotPerformanceMonitor.recordPathfind(pathfindCaller, System.nanoTime() - startedAt);
            }
        }
    }

    /* Result of a single .runSearch call. */
    record SearchOutcome(List<BotNavigationGraph.Edge> path, int cost, int expandedNodes, boolean usesPortal) {
    }

    /* Side-by-side comparison of the production heuristic vs the admissible (h=0) optimal search. */
    record PathOptimality(int currentCost, int optimalCost, boolean currentUsesPortal,
                          boolean optimalUsesPortal, int currentExpanded, int optimalExpanded) {
        boolean reachable() {
            return currentCost != Integer.MAX_VALUE && optimalCost != Integer.MAX_VALUE;
        }

        boolean suboptimal() {
            return reachable() && currentCost > optimalCost;
        }

        int costDelta() {
            return reachable() ? currentCost - optimalCost : 0;
        }

        /* True when the heuristic walked a longer route while the optimal path took a portal. */
        boolean portalSkipped() {
            return suboptimal() && optimalUsesPortal && !currentUsesPortal;
        }
    }

    /*
     * Measurement helper: runs the same start/target search with the production heuristic and with
     * the admissible h=0 heuristic, returning both costs so callers can quantify how often (and by
     * how much) the current heuristic returns a non-optimal path. Not used on any production path.
     */
    static PathOptimality measureOptimality(BotNavigationGraph graph,
                                            MapleMap map,
                                            Point startPos,
                                            int startRegionId,
                                            int targetRegionId,
                                            Point targetPos) {
        SearchOutcome current = runSearch(graph, map, startPos, startRegionId, targetRegionId, targetPos,
                "measure", false, false, 0L);
        SearchOutcome optimal = runSearch(graph, map, startPos, startRegionId, targetRegionId, targetPos,
                "measure", true, false, 0L);
        return new PathOptimality(current.cost(), optimal.cost(), current.usesPortal(),
                optimal.usesPortal(), current.expandedNodes(), optimal.expandedNodes());
    }

    private static void logSlowPathfind(BotNavigationGraph graph,
                                        MapleMap map,
                                        Point startPos,
                                        int startRegionId,
                                        int targetRegionId,
                                        Point targetPos,
                                        String pathfindCaller,
                                        PathfindProfile profile) {
        if (profile.elapsedNs() < SLOW_PATHFIND_WARN_NS) {
            return;
        }
        long now = System.currentTimeMillis();
        long next = slowPathfindNextWarnAtMs.get();
        if (now < next || !slowPathfindNextWarnAtMs.compareAndSet(next, now + SLOW_PATHFIND_WARN_COOLDOWN_MS)) {
            slowPathfindSuppressed.incrementAndGet();
            return;
        }
        int suppressed = slowPathfindSuppressed.getAndSet(0);
        int regionCount = graph != null && graph.regions != null ? graph.regions.size() : -1;
        int outgoingFromStart = graph != null ? graph.getOutgoing(startRegionId).size() : -1;
        String caller = pathfindCaller == null || pathfindCaller.isBlank() ? "default" : pathfindCaller;
        int bestGoalCost = profile.bestGoalCost() == Integer.MAX_VALUE ? -1 : profile.bestGoalCost();
        log.warn(
                "Slow bot pathfind (suppressedSinceLast=" + suppressed
                        + "): caller={} took {} ms map={} startRegion={} targetRegion={} regions={} startOut={} startPos=({}, {}) targetPos=({}, {}) expanded={} stale={} edgeChecks={} usableEdges={} relaxations={} openPeak={} bestGoalCost={} resultEdges={}",
                caller,
                String.format("%.1f", profile.elapsedNs() / 1_000_000.0),
                map != null ? map.getId() : -1,
                startRegionId,
                targetRegionId,
                regionCount,
                outgoingFromStart,
                startPos != null ? startPos.x : -1,
                startPos != null ? startPos.y : -1,
                targetPos != null ? targetPos.x : -1,
                targetPos != null ? targetPos.y : -1,
                profile.expandedNodes(),
                profile.staleNodes(),
                profile.edgeChecks(),
                profile.usableEdges(),
                profile.relaxations(),
                profile.openPeak(),
                bestGoalCost,
                profile.resultEdges());
    }

    private static List<BotNavigationGraph.Edge> reconstructPath(SearchState startState,
                                                                 SearchState goalState,
                                                                 Map<SearchState, SearchState> cameFrom,
                                                                 Map<SearchState, BotNavigationGraph.Edge> cameByEdge) {
        if (goalState == null || !cameByEdge.containsKey(goalState)) {
            return List.of();
        }

        List<BotNavigationGraph.Edge> path = new ArrayList<>();
        SearchState cursor = goalState;
        while (!cursor.equals(startState)) {
            BotNavigationGraph.Edge edge = cameByEdge.get(cursor);
            if (edge == null) {
                return List.of();
            }

            path.add(0, edge);
            SearchState previousState = cameFrom.get(cursor);
            if (previousState == null) {
                return List.of();
            }
            cursor = previousState;
        }
        return path;
    }

    static BotNavigationGraph.Edge collapseLeadingWalkEdges(List<BotNavigationGraph.Edge> path) {
        BotNavigationGraph.Edge first = path.get(0);
        if (first.type != BotNavigationGraph.EdgeType.WALK) {
            return first;
        }

        if (!isNoMovementWalk(first.startPoint, first.endPoint)) {
            return first;
        }

        int totalCost = 0;
        int walkCount = 0;
        while (walkCount < path.size()) {
            BotNavigationGraph.Edge edge = path.get(walkCount);
            if (edge.type != BotNavigationGraph.EdgeType.WALK
                    || !isNoMovementWalk(edge.startPoint, edge.endPoint)) {
                break;
            }
            totalCost += edge.cost;
            walkCount++;
        }

        if (walkCount >= path.size()) {
            return null;
        }

        BotNavigationGraph.Edge next = path.get(walkCount);
        return new BotNavigationGraph.Edge(first.fromRegionId, next.toRegionId, next.type,
                next.startPoint, next.endPoint, next.launchMinX, next.launchMaxX, next.launchStepX, next.portalId,
                next.ropeX, next.ropeTopY, next.ropeBottomY, totalCost + next.cost);
    }

    private static boolean isEdgeUsable(BotNavigationGraph graph, Character bot, BotNavigationGraph.Edge edge) {
        return isEdgeUsable(graph, bot.getMap(), edge);
    }

    private static boolean sameEdge(BotNavigationGraph.Edge left, BotNavigationGraph.Edge right) {
        return left == right || (left != null
                && right != null
                && left.fromRegionId == right.fromRegionId
                && left.toRegionId == right.toRegionId
                && left.type == right.type
                && left.launchMinX == right.launchMinX
                && left.launchMaxX == right.launchMaxX
                && left.launchStepX == right.launchStepX
                && left.portalId == right.portalId
                && left.ropeX == right.ropeX
                && left.ropeTopY == right.ropeTopY
                && left.ropeBottomY == right.ropeBottomY
                && left.startPoint.equals(right.startPoint)
                && left.endPoint.equals(right.endPoint));
    }

    static boolean shouldRetainCommittedGroundEdge(BotNavigationGraph.Edge current,
                                                   BotNavigationGraph.Edge replacement) {
        if (current == null || replacement == null) {
            return false;
        }
        if (current.fromRegionId != replacement.fromRegionId
                || current.toRegionId != replacement.toRegionId) {
            return false;
        }
        // Equivalent first exits into the same downstream region can trade off a few pixels of
        // approach cost as the bot shuffles on the source platform. Replacing the committed edge
        // every AI tick creates oscillation loops like the John 2026-05-01 down-jump trace,
        // where nav flips between a straight DROP and a nearby JUMP before either can execute.
        return current.type != BotNavigationGraph.EdgeType.WALK
                && replacement.type != BotNavigationGraph.EdgeType.WALK;
    }

    private static boolean isEdgeUsable(BotNavigationGraph graph, MapleMap map, BotNavigationGraph.Edge edge) {
        return switch (edge.type) {
            case WALK, JUMP, DROP, CLIMB -> true;
            case PORTAL -> {
                Portal portal = map.getPortal(edge.portalId);
                yield portal != null && portal.getPortalStatus();
            }
        };
    }

    private static boolean usePortal(Character bot, int portalId) {
        Portal portal = bot.getMap().getPortal(portalId);
        if (portal == null || !portal.getPortalStatus()) {
            return false;
        }

        int oldMapId = bot.getMapId();
        Point oldPos = bot.getPosition();
        // Bots are clientless (shared BotClient) — warp the bot Character directly, never
        // portal.enterPortal(client). See GCPortals.
        GCPortals.enter(bot, portal);
        return bot.getMapId() != oldMapId || !bot.getPosition().equals(oldPos);
    }

    private static boolean isReadyForEdge(Point botPos, BotNavigationGraph.Edge edge) {
        int dx = Math.abs(botPos.x - edge.startPoint.x);
        int dy = Math.abs(botPos.y - edge.startPoint.y);

        return switch (edge.type) {
            case JUMP -> dx <= JUMP_READY_X_TOLERANCE && dy <= BotMovementManager.cfg.JUMP_Y_THRESH;
            case DROP, CLIMB, PORTAL -> dx <= EDGE_READY_X_TOLERANCE && dy <= BotMovementManager.cfg.JUMP_Y_THRESH * 2;
            default -> dx <= BotMovementManager.cfg.STOP_DIST + 8
                    && dy <= BotMovementManager.cfg.JUMP_Y_THRESH * 2;
        };
    }

    static boolean canExecuteJumpFromCurrentPosition(BotNavigationGraph graph,
                                                     MapleMap map,
                                                     Point botPos,
                                                     BotNavigationGraph.Edge edge) {
        if (edge.type != BotNavigationGraph.EdgeType.JUMP) {
            return false;
        }
        return isWithinJumpLaunchWindow(graph, botPos, edge);
    }

    private static boolean canExecuteSelectedJumpFromCurrentPosition(BotNavigationGraph graph,
                                                                     BotMovementState entry,
                                                                     MapleMap map,
                                                                     Point botPos,
                                                                     BotNavigationGraph.Edge edge) {
        if (!canExecuteJumpFromCurrentPosition(graph, map, botPos, edge)) {
            return false;
        }
        int launchX = selectedJumpLaunchX(entry, graph, edge);
        int tolerance = Math.max(1, BotPhysicsEngine.walkStep(map, entry != null ? entry.movementProfile : null));
        return Math.abs(botPos.x - launchX) <= tolerance;
    }

    private static boolean isReachableWithinRegion(BotNavigationGraph graph,
                                                   MapleMap map,
                                                   int regionId,
                                                   Point fromPos,
                                                   Point toPos) {
        BotNavigationGraph.Region region = graph.getRegion(regionId);
        if (region == null || fromPos == null || toPos == null) {
            return false;
        }
        if (region.isRopeRegion) {
            return fromPos.x == toPos.x;
        }

        int dir = Integer.compare(toPos.x, fromPos.x);
        Point previous = region.pointAt(fromPos.x);
        if (graph.findRegionId(map, previous) != regionId) {
            return false;
        }
        if (dir == 0) {
            return Math.abs(toPos.y - previous.y) <= BotMovementManager.cfg.JUMP_Y_THRESH;
        }

        for (int x = fromPos.x + dir; x != toPos.x + dir; x += dir) {
            Point current = region.pointAt(x);
            if (graph.findRegionId(map, current) != regionId) {
                return false;
            }
            if (!BotPhysicsEngine.isWalkableEndpointStep(Math.abs(current.x - previous.x), current.y - previous.y)) {
                return false;
            }
            previous = current;
        }
        return true;
    }

    static boolean isWithinJumpLaunchWindow(BotNavigationGraph graph,
                                            Point botPos,
                                            BotNavigationGraph.Edge edge) {
        if (botPos == null || edge.type != BotNavigationGraph.EdgeType.JUMP || !edge.containsLaunchX(botPos.x)) {
            return false;
        }

        BotNavigationGraph.Region fromRegion = graph.getRegion(edge.fromRegionId);
        if (fromRegion == null) {
            return false;
        }

        Point expectedLaunchPoint = fromRegion.pointAt(botPos.x);
        return Math.abs(botPos.y - expectedLaunchPoint.y) <= BotMovementManager.cfg.JUMP_Y_THRESH;
    }

    static boolean isWithinDropLaunchWindow(BotNavigationGraph graph,
                                            Point botPos,
                                            BotNavigationGraph.Edge edge) {
        if (botPos == null
                || edge.type != BotNavigationGraph.EdgeType.DROP
                || edge.launchStepX != 0
                || !edge.containsLaunchX(botPos.x)) {
            return false;
        }

        if (graph == null) {
            return Math.abs(botPos.y - edge.startPoint.y) <= BotMovementManager.cfg.JUMP_Y_THRESH;
        }

        BotNavigationGraph.Region fromRegion = graph.getRegion(edge.fromRegionId);
        if (fromRegion == null || fromRegion.isRopeRegion) {
            return false;
        }

        Point expectedLaunchPoint = fromRegion.pointAt(botPos.x);
        return Math.abs(botPos.y - expectedLaunchPoint.y) <= BotMovementManager.cfg.JUMP_Y_THRESH;
    }

    private static int selectedJumpLaunchX(BotMovementState entry,
                                           BotNavigationGraph graph,
                                           BotNavigationGraph.Edge edge) {
        if (entry == null || graph == null || edge == null || edge.type != BotNavigationGraph.EdgeType.JUMP) {
            return edge != null ? edge.startPoint.x : 0;
        }
        BotNavigationGraph.Region fromRegion = graph.getRegion(edge.fromRegionId);
        if (fromRegion == null || fromRegion.isRopeRegion) {
            return edge.startPoint.x;
        }
        if (sameEdge(entry.navJumpLaunchEdge, edge)
                && entry.navJumpLaunchX >= edge.launchMinX
                && entry.navJumpLaunchX <= edge.launchMaxX) {
            return entry.navJumpLaunchX;
        }

        int minX = Math.max(edge.launchMinX, fromRegion.minX);
        int maxX = Math.min(edge.launchMaxX, fromRegion.maxX);
        if (minX > maxX) {
            minX = edge.launchMinX;
            maxX = edge.launchMaxX;
        }

        int width = Math.max(0, maxX - minX);
        int margin = Math.min(width / 2, Math.max(1, BotPhysicsEngine.walkStep(entry.bot.getMap(), entry.movementProfile) * 2));
        int randomMinX = minX + margin;
        int randomMaxX = maxX - margin;
        if (randomMinX > randomMaxX) {
            randomMinX = minX;
            randomMaxX = maxX;
        }

        int selectedX = randomMinX >= randomMaxX
                ? randomMinX
                : ThreadLocalRandom.current().nextInt(randomMinX, randomMaxX + 1);
        entry.navJumpLaunchEdge = edge;
        entry.navJumpLaunchX = selectedX;
        return selectedX;
    }

    private static int intraRegionTravelCost(BotNavigationGraph graph, Point from, Point to) {
        int dx = Math.abs(to.x - from.x);
        return Math.max(0, (int) Math.round((dx * 1000.0) / Math.max(1.0, graph.movementProfile.walkVelocityPxs())));
    }

    private static int intraRegionTravelCost(BotNavigationGraph graph, int regionId, Point from, Point to) {
        BotNavigationGraph.Region region = graph.getRegion(regionId);
        if (region != null && region.isRopeRegion) {
            int travel = Math.abs(to.y - from.y);
            return Math.max(0, (int) Math.round((travel * 1000.0) / Math.max(1, BotMovementManager.cfg.CLIMB_SPEED_PXS)));
        }
        return intraRegionTravelCost(graph, from, to);
    }

    private static int heuristic(BotNavigationGraph graph, Point from, Point targetPos) {
        return intraRegionTravelCost(graph, from, targetPos);
    }

    // ponytail: route-diversification knobs — calibrated on map 10000 via BotRouteDiversityTest.
    // jitter spreads routes; epsilon trades diversity for a perf prune. At 0.55/0.15 the modal
    // corridor drops from 43% to ~28% of bots (12 distinct routes) for ~29% worst-case overhead.
    // Raising jitter further mostly buys overhead, not spread; lower epsilon = more spread, less prune.
    static double JITTER_FRAC = 0.55;    // per-edge cost perturbation 0..55%, stable per (bot, edge)
    static double EPSILON_SPAN = 0.15;   // weighted-A* heuristic inflation: epsilon in [1.0, 1.15) per bot
    private static final long EPSILON_SALT = 0xE95011L;

    /* Heuristic value: zeroSeed callers keep h=0/legacy; per-bot search uses an inflated (weighted) admissible h to prune. */
    private static int hValue(BotNavigationGraph graph, Point from, Point targetPos,
                              boolean zeroHeuristic, boolean randomized, double epsilon) {
        if (randomized) {
            return (int) Math.round(epsilon * heuristic(graph, from, targetPos));
        }
        return zeroHeuristic ? 0 : heuristic(graph, from, targetPos);
    }

    /* Per-bot route seed; non-zero so the search takes the randomized branch. */
    private static long routeSeed(Character bot) {
        return mix64(bot.getId()) | 1L;
    }

    /* Stable identity for an edge so jitter is deterministic per (bot, edge), not per tick. */
    private static long edgeKey(BotNavigationGraph.Edge edge) {
        long k = edge.toRegionId;
        k = k * 31 + edge.startPoint.x;
        k = k * 31 + edge.startPoint.y;
        k = k * 31 + edge.type.ordinal();
        return k;
    }

    /* SplitMix64 finalizer mixing seed and key into a stable fraction in [0, 1). */
    private static double hashFrac(long seed, long key) {
        long h = mix64(seed ^ (key * 0x9E3779B97F4A7C15L));
        return (h >>> 11) * 0x1.0p-53;
    }

    private static long mix64(long z) {
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    static boolean shouldUsePreciseWalkTarget(BotNavigationGraph.Edge edge) {
        return edge != null
                && edge.type == BotNavigationGraph.EdgeType.WALK
                && !isNoMovementWalk(edge.startPoint, edge.endPoint);
    }

    private static boolean isNoMovementWalk(Point start, Point end) {
        return Math.abs(end.x - start.x) <= NO_MOVEMENT_WALK_TOLERANCE
                && Math.abs(end.y - start.y) <= NO_MOVEMENT_WALK_TOLERANCE;
    }

    private static boolean canGrabRopeAtCurrentPosition(Point botPos, Rope rope) {
        return Math.abs(botPos.x - rope.x()) <= BotMovementManager.cfg.ROPE_GRAB_X
                && botPos.y >= BotPhysicsEngine.firstClimbableY(rope)
                && botPos.y <= rope.bottomY();
    }

    private static boolean canAttachToRopeFromTopPlatform(BotNavigationGraph.Edge edge, Point botPos, Rope rope) {
        return Math.abs(botPos.x - rope.x()) <= BotMovementManager.cfg.ROPE_GRAB_X
                && edge.endPoint.y == BotPhysicsEngine.firstClimbableY(rope)
                && botPos.y < rope.topY()
                && rope.topY() - botPos.y <= BotPhysicsEngine.cfg.MAX_SNAP_DROP;
    }

    private static boolean canGrabRopeFromTopPlatform(BotNavigationGraph.Edge edge, Point botPos, Rope rope) {
        return edge.startPoint.y <= rope.topY() + BotMovementManager.cfg.JUMP_Y_THRESH
                && Math.abs(botPos.x - rope.x()) <= BotMovementManager.cfg.ROPE_GRAB_X;
    }

    private static boolean canExecuteClimbEntryFromCurrentPosition(MapleMap map,
                                                                   Point botPos,
                                                                   BotNavigationGraph.Edge edge,
                                                                   Rope rope) {
        return rope != null && (canGrabRopeAtCurrentPosition(botPos, rope)
                || canAttachToRopeFromTopPlatform(edge, botPos, rope)
                || canGrabRopeFromTopPlatform(edge, botPos, rope)
                || canExecuteGroundRopeJumpEntryFromCurrentPosition(botPos, edge));
    }

    private static boolean canExecuteGroundRopeJumpEntryFromCurrentPosition(Point botPos,
                                                                           BotNavigationGraph.Edge edge) {
        if (botPos == null || edge == null || edge.type != BotNavigationGraph.EdgeType.CLIMB) {
            return false;
        }
        return edge.containsLaunchX(botPos.x)
                && Math.abs(botPos.y - edge.startPoint.y) <= BotMovementManager.cfg.JUMP_Y_THRESH * 2;
    }

    private static boolean canExecuteClimbExitFromCurrentPosition(BotNavigationGraph graph,
                                                                  MapleMap map,
                                                                  Point botPos,
                                                                  BotNavigationGraph.Edge edge) {
        if (edge.type != BotNavigationGraph.EdgeType.CLIMB) {
            return false;
        }

        if (edge.launchStepX != 0 && botPos.y != edge.startPoint.y) {
            Rope rope = findRopeForRegion(map, graph.getRegion(edge.fromRegionId));
            if (!isTopRopeJumpExitReady(rope, botPos, edge)) {
                // Rope-exit jump edges are authored from a specific climb height. Launching from
                // any other Y changes the ballistic arc; climb movement reaches the authored
                // first climbable pixel before this executes.
                return false;
            }
        }

        BotNavigationGraph.Region toRegion = graph.getRegion(edge.toRegionId);
        if (toRegion != null && toRegion.isRopeRegion) {
            return Math.abs(botPos.y - edge.startPoint.y) <= BotMovementManager.cfg.JUMP_Y_THRESH * 2;
        }

        if (edge.launchStepX == 0) {
            Rope rope = findRopeForRegion(map, graph.getRegion(edge.fromRegionId));
            return rope != null && isTopStepOffExit(rope, botPos, edge);
        }

        return Math.abs(botPos.y - edge.startPoint.y) <= BotMovementManager.cfg.JUMP_Y_THRESH * 2;
    }

    private static boolean isTopRopeJumpExitReady(Rope rope, Point botPos, BotNavigationGraph.Edge edge) {
        // Top-of-rope tolerance window only: the bot lands at firstClimbableY when grabbing
        // from above (canTopGrab/canTopStep), and the launch arc is invariant within the first
        // climbStep below the top. Non-top anchors are single-point launch windows whose arcs
        // are precomputed by simulateRopeJumpLanding — launching from any other Y misses the
        // destination. The bot reaches non-top anchors exactly via the precise-target snap in
        // BotMovementManager.shouldSnapToClimbTarget (sub-tick clamp; the only one in the
        // physics path), so the bypass `botPos.y == edge.startPoint.y` in
        // canExecuteClimbExitFromCurrentPosition covers those without tolerance here.
        if (rope == null || botPos == null || edge == null || edge.launchStepX == 0) {
            return false;
        }
        int firstClimbableY = BotPhysicsEngine.firstClimbableY(rope);
        return edge.startPoint.x == rope.x()
                && edge.startPoint.y == firstClimbableY
                && botPos.x == rope.x()
                && botPos.y >= firstClimbableY
                && botPos.y <= firstClimbableY + BotPhysicsEngine.climbStepPerTick() + 2;
    }

    private static void startClimbing(BotMovementState entry, Character bot, Rope rope, int climbY) {
        BotPhysicsEngine.attachToRope(entry, bot, rope, climbY);
        BotMovementManager.broadcastMovement(entry);
    }

    private static void setEdgeExecutionTarget(BotMovementState entry, BotNavigationGraph.Edge edge) {
        entry.navPreciseTarget = false;
        entry.navTargetPos = new Point(edge.endPoint);
    }

    private static Point adjustPathTarget(BotMovementState entry,
                                          BotNavigationGraph graph,
                                          int targetRegionId,
                                          Point rawTargetPos) {
        if (rawTargetPos == null || !entry.grinding || targetRegionId < 0) {
            return rawTargetPos;
        }

        BotNavigationGraph.Region targetRegion = graph.getRegion(targetRegionId);
        if (targetRegion == null || targetRegion.isRopeRegion) {
            return rawTargetPos;
        }

        int safeLeft = targetRegion.minX + BotMovementManager.cfg.GRIND_EDGE_MARGIN;
        int safeRight = targetRegion.maxX - BotMovementManager.cfg.GRIND_EDGE_MARGIN;
        if (safeLeft >= safeRight) {
            return rawTargetPos;
        }

        int clampedX = Math.max(safeLeft, Math.min(safeRight, rawTargetPos.x));
        return targetRegion.pointAt(clampedX);
    }

    private static int landingRegionId(BotNavigationGraph graph, BotPhysicsEngine.JumpLanding landing) {
        if (landing == null) {
            return -1;
        }
        return graph.regionIdByFootholdId.getOrDefault(landing.foothold().getId(), -1);
    }

    static int resolveCurrentRegionId(BotNavigationGraph graph,
                                      BotMovementState entry,
                                      MapleMap map,
                                      Point botPos) {
        if (entry.climbing || (entry.bot != null && CharacterStance.isClimbing(entry.bot.getStance()))) {
            // Rope climbing state is authoritative. Ground lookup below a rope often resolves to
            // the nearby platform instead of the rope region, which can replan from the wrong side
            // of the rope and bounce between entry/exit climb edges.
            int ropeX = entry.climbRope != null ? entry.climbRope.x() : botPos.x;
            int ropeRegionId = graph.findRopeRegionId(new Point(ropeX, botPos.y));
            if (ropeRegionId >= 0) {
                return ropeRegionId;
            }
        }
        if (entry.inAir) {
            // Airborne points do not have a meaningful "current region". A ground lookup from an
            // in-flight point resolves to whatever foothold is below the arc, which can be an
            // unrelated upper platform. That makes runtime navigation discard the committed jump
            // edge even though the authored graph and ballistic landing simulation still agree.
            return -1;
        }
        return graph.findRegionId(map, botPos);
    }

    static int resolveTargetRegionId(BotNavigationGraph graph,
                                     BotMovementState entry,
                                     MapleMap map,
                                     Point targetPos) {
        if (targetPos == null) {
            return -1;
        }

        // GCMoveSystem: the follow anchor is simply the followed character (set as entry.owner
        // by GCFollow). Replaces BotManager.resolveFollowAnchor (owner/formation resolution).
        Character followAnchor = entry.owner;
        if (entry.following
                && entry.moveTarget == null
                && entry.farmAnchor == null
                && !entry.shopVisitPending
                && !entry.grinding
                && followAnchor != null
                && followAnchor.getMap() == map) {
            // Follow mode + owner climbing: prioritise a rope target. The follow
            // resolver may have already snapped targetPos to a rope's X, so the
            // exact equality check below would miss — explicitly look for a rope
            // at targetPos, and fall back to the follow anchor's own rope region if none
            // is found there. This keeps the bot climbing onto rope alongside
            // the anchor instead of clamping to the platform below the rope.
            if (CharacterStance.isClimbing(followAnchor.getStance())) {
                int ropeRegionId = graph.findRopeRegionId(targetPos);
                if (ropeRegionId >= 0) {
                    return ropeRegionId;
                }
                return resolveCharacterRegionId(graph, map, followAnchor);
            }
            if (targetPos.equals(followAnchor.getPosition())) {
                return resolveCharacterRegionId(graph, map, followAnchor);
            }
        }

        return resolvePointTargetRegionId(graph, map, targetPos);
    }

    static int resolveCharacterRegionId(BotNavigationGraph graph,
                                        MapleMap map,
                                        Character character) {
        if (character == null) {
            return -1;
        }

        Point position = character.getPosition();
        if (position == null) {
            return -1;
        }

        if (CharacterStance.isClimbing(character.getStance())) {
            int ropeRegionId = graph.findRopeRegionId(position);
            if (ropeRegionId >= 0) {
                return ropeRegionId;
            }
        }

        return resolvePointTargetRegionId(graph, map, position);
    }

    static int resolvePointTargetRegionId(BotNavigationGraph graph,
                                          MapleMap map,
                                          Point position) {
        int ropeRegionId = graph.findRopeRegionId(position);
        if (ropeRegionId >= 0 && shouldPreferRopeRegion(map, position)) {
            return ropeRegionId;
        }
        return graph.findRegionId(map, position);
    }

    private static boolean shouldPreferRopeRegion(MapleMap map, Point position) {
        return BotPhysicsEngine.isGroundFarBelow(map, position);
    }

    private static boolean isRopeEntryEdge(BotNavigationGraph graph, BotNavigationGraph.Edge edge) {
        if (edge.type != BotNavigationGraph.EdgeType.CLIMB) {
            return false;
        }

        BotNavigationGraph.Region from = graph.getRegion(edge.fromRegionId);
        BotNavigationGraph.Region to = graph.getRegion(edge.toRegionId);
        return from != null && to != null && !from.isRopeRegion && to.isRopeRegion;
    }

    static boolean isTopStepOffExit(Rope rope, Point botPos, BotNavigationGraph.Edge edge) {
        if (rope == null || botPos == null || edge == null || edge.launchStepX != 0) {
            return false;
        }
        return edge.startPoint.y == rope.topY()
                && Math.abs(edge.endPoint.y - rope.topY()) <= BotMovementManager.cfg.JUMP_Y_THRESH * 2
                && botPos.y <= rope.topY() + BotMovementManager.cfg.JUMP_Y_THRESH * 2;
    }

    private static Rope findRopeForRegion(MapleMap map, BotNavigationGraph.Region region) {
        return BotNavigationGraphProvider.findRopeFromRegion(map, region);
    }

}
