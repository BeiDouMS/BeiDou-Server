package soloMapling.ArtificialPlayer.GCMoveSystem;

import org.gms.client.Character;
import org.gms.server.maps.Foothold;
import org.gms.server.maps.MapleMap;
import org.gms.server.maps.Rope;

import java.awt.*;

// Ported from GreenCatMS. Credit: NutNNut.
final class BotFallbackMovementManager {
    private BotFallbackMovementManager() {
    }

    static Point resolveSteeringTarget(BotMovementState entry, Point botPos, Point targetPos) {
        Rope rope = selectNearbyRope(entry, botPos, targetPos);
        Point ledgeTarget = resolveFallbackLedgeTarget(entry, botPos, targetPos, rope);
        if (ledgeTarget != null) {
            return ledgeTarget;
        }
        if (rope == null) {
            return targetPos;
        }
        return new Point(rope.x(), botPos.y);
    }

    static boolean tryImmediateAction(BotMovementState entry, Point botPos, Point targetPos) {
        Character bot = entry.bot;
        MapleMap map = bot.getMap();
        Rope rope = selectNearbyRope(entry, botPos, targetPos);
        if (rope != null) {
            if (canDirectlyAttachToRope(botPos, rope)) {
                int attachY = Math.max(rope.topY(), Math.min(botPos.y, rope.bottomY()));
                BotPhysicsEngine.attachToRope(entry, bot, rope, attachY);
                BotMovementManager.broadcastMovement(entry);
                return true;
            }

            int ropeDx = rope.x() - botPos.x;
            int ropeJumpRange = Math.max(BotPhysicsEngine.cfg.ROPE_GRAB_X * 2,
                    BotPhysicsEngine.walkStep(map, entry.movementProfile) * 2);
            if (Math.abs(ropeDx) <= ropeJumpRange
                    && BotPhysicsEngine.canReachRopeFromGround(map, botPos, rope, entry.movementProfile)) {
                BotMovementManager.initiateRopeJump(entry, bot, ropeDx);
                return true;
            }
        }

        // In swim maps, leap upward off the platform to chase an airborne
        // target above (e.g. owner swimming overhead). Once airborne, swim
        // physics owns motion and steers horizontally toward the target.
        // Without this, bot stays grounded forever since walking on platforms
        // never closes vertical distance to a swimming target.
        if (shouldJumpUpIntoSwim(entry, botPos, targetPos)) {
            BotMovementManager.initiateJump(entry, bot, 0);
            return true;
        }

        if (shouldUseDownJump(entry, botPos, targetPos, rope)) {
            BotPhysicsEngine.queueDownJump(entry, bot);
            BotMovementManager.broadcastMovement(entry);
            return true;
        }

        Point steeringTarget = rope == null ? targetPos : new Point(rope.x(), targetPos.y);
        int stepX = BotMovementManager.resolveGroundStepX(entry, botPos, steeringTarget,
                BotMovementManager.cfg.STOP_DIST, BotMovementManager.cfg.FOLLOW_DIST);
        if (stepX == 0 || BotPhysicsEngine.canWalkGroundStep(map, botPos, stepX)) {
            return false;
        }

        if (shouldUseJump(entry, botPos, steeringTarget, stepX)) {
            BotMovementManager.initiateJump(entry, bot, steeringTarget.x - botPos.x);
            return true;
        }

        return false;
    }

    private static boolean shouldJumpUpIntoSwim(BotMovementState entry, Point botPos, Point targetPos) {
        if (entry == null || entry.bot == null || botPos == null || targetPos == null) {
            return false;
        }
        if (entry.inAir || entry.climbing || entry.jumpCooldownMs > 0 || entry.downJumpPending) {
            return false;
        }
        MapleMap map = entry.bot.getMap();
        if (map == null || !map.isSwim()) {
            return false;
        }
        // Target must be sufficiently above bot. dy < 0 = target higher in MS coords.
        int dy = targetPos.y - botPos.y;
        return dy < -Math.max(BotMovementManager.cfg.JUMP_Y_THRESH * 2, 60);
    }

    static boolean shouldWalkOffLedge(BotMovementState entry, Point botPos, Point targetPos, int stepX) {
        if (entry == null || !entry.graphWarmupFallback || botPos == null || targetPos == null || stepX == 0) {
            return false;
        }
        if (targetPos.y <= botPos.y + BotPhysicsEngine.cfg.MAX_SNAP_DROP) {
            return false;
        }
        Point ahead = new Point(botPos.x + stepX, botPos.y);
        return BotPhysicsEngine.isGroundFarBelow(entry.bot.getMap(), ahead);
    }

    private static Rope selectNearbyRope(BotMovementState entry, Point botPos, Point targetPos) {
        if (entry == null || entry.bot == null || botPos == null || targetPos == null) {
            return null;
        }

        int dy = targetPos.y - botPos.y;
        if (Math.abs(dy) < Math.max(BotMovementManager.cfg.JUMP_Y_THRESH * 2, 60)) {
            return null;
        }

        MapleMap map = entry.bot.getMap();
        int walkStep = BotPhysicsEngine.walkStep(map, entry.movementProfile);
        int searchX = Math.max(walkStep * 4, 90);
        Rope best = null;
        int bestScore = Integer.MAX_VALUE;
        for (Rope rope : map.getRopes()) {
            int dx = Math.abs(rope.x() - botPos.x);
            if (dx > searchX) {
                continue;
            }

            if (dy < 0) {
                if (rope.topY() >= botPos.y - BotPhysicsEngine.cfg.MAX_SNAP_DROP) {
                    continue;
                }
                if (rope.bottomY() < botPos.y - BotPhysicsEngine.cfg.MAX_SNAP_DROP) {
                    continue;
                }
                if (rope.topY() > targetPos.y + BotMovementManager.cfg.FOLLOW_Y_CAP) {
                    continue;
                }
            } else {
                if (rope.bottomY() <= botPos.y + BotPhysicsEngine.cfg.MAX_SNAP_DROP) {
                    continue;
                }
                if (rope.topY() > botPos.y + BotPhysicsEngine.cfg.MAX_SLOPE_UP) {
                    continue;
                }
                if (rope.bottomY() < targetPos.y - BotMovementManager.cfg.FOLLOW_Y_CAP) {
                    continue;
                }
            }

            int verticalPenalty = dy < 0
                    ? Math.max(0, rope.topY() - targetPos.y)
                    : Math.max(0, targetPos.y - rope.bottomY());
            int score = dx * 4 + verticalPenalty;
            if (score < bestScore) {
                best = rope;
                bestScore = score;
            }
        }
        return best;
    }

    private static boolean canDirectlyAttachToRope(Point botPos, Rope rope) {
        // Allow attach when bot is within rope's Y range, OR slightly above
        // rope.topY (within MAX_SNAP_DROP) so a player standing on a platform
        // whose surface meets the rope's head can transition into climbing
        // without first going airborne — same "press DOWN to grab from top"
        // motion as the real client. attachY snaps to rope.topY in the caller.
        return botPos != null
                && rope != null
                && Math.abs(botPos.x - rope.x()) <= BotPhysicsEngine.cfg.ROPE_GRAB_X
                && botPos.y >= rope.topY() - BotPhysicsEngine.cfg.MAX_SNAP_DROP
                && botPos.y <= rope.bottomY() + BotPhysicsEngine.cfg.MAX_SNAP_DROP;
    }

    private static Point resolveFallbackLedgeTarget(BotMovementState entry, Point botPos, Point targetPos, Rope rope) {
        if (entry == null || entry.bot == null || botPos == null || targetPos == null || rope != null) {
            return null;
        }
        MapleMap map = entry.bot.getMap();
        if (!shouldConsiderFallbackDrop(entry, map, botPos, targetPos)) {
            return null;
        }

        Foothold foothold = BotPhysicsEngine.findGroundFoothold(map, botPos);
        if (foothold == null) {
            return null;
        }

        Point left = walkOffTarget(map, foothold, entry.movementProfile, -1);
        Point right = walkOffTarget(map, foothold, entry.movementProfile, 1);
        Point best = chooseBetterLedgeTarget(botPos, targetPos, left, right);
        if (best == null) {
            return null;
        }
        return new Point(best.x, targetPos.y);
    }

    private static boolean shouldUseDownJump(BotMovementState entry, Point botPos, Point targetPos, Rope rope) {
        if (entry == null || botPos == null || targetPos == null || rope != null) {
            return false;
        }
        MapleMap map = entry.bot.getMap();
        if (!shouldConsiderFallbackDrop(entry, map, botPos, targetPos)) {
            return false;
        }
        // Ground maps need the full landing sim (bounded-drop rule included); in swim maps
        // the bot drops into open water — no landing foothold exists or is required.
        boolean canDrop = map != null && map.isSwim()
                ? BotPhysicsEngine.canStartDownJump(map, botPos)
                : BotPhysicsEngine.simulateDownJumpLanding(map, botPos) != null;
        if (!canDrop) {
            return false;
        }
        return Math.abs(targetPos.x - botPos.x) <= Math.max(BotMovementManager.cfg.FOLLOW_DIST,
                BotPhysicsEngine.walkStep(map, entry.movementProfile) * 4);
    }

    private static boolean shouldConsiderFallbackDrop(BotMovementState entry, MapleMap map, Point botPos, Point targetPos) {
        if (entry == null || map == null || botPos == null || targetPos == null) {
            return false;
        }
        int dy = targetPos.y - botPos.y;
        if (dy < Math.max(BotPhysicsEngine.cfg.MAX_SNAP_DROP * 3, 90)) {
            return false;
        }

        Foothold currentFoothold = BotPhysicsEngine.findGroundFoothold(map, botPos);
        Foothold targetFoothold = BotPhysicsEngine.findGroundFoothold(map, targetPos);
        return currentFoothold == null
                || targetFoothold == null
                || currentFoothold.getId() != targetFoothold.getId();
    }

    private static Point walkOffTarget(MapleMap map, Foothold foothold, BotMovementProfile profile, int direction) {
        if (map == null || foothold == null || direction == 0) {
            return null;
        }
        Point endpoint = direction < 0
                ? new Point(foothold.getX1(), foothold.getY1())
                : new Point(foothold.getX2(), foothold.getY2());
        int step = direction * Math.max(1, BotPhysicsEngine.walkStep(map, profile));
        Point ahead = new Point(endpoint.x + step, endpoint.y);
        return BotPhysicsEngine.isGroundFarBelow(map, ahead) ? ahead : null;
    }

    private static Point chooseBetterLedgeTarget(Point botPos, Point targetPos, Point left, Point right) {
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }

        int desiredDirection = Integer.compare(targetPos.x, botPos.x);
        if (desiredDirection < 0 && left.x <= botPos.x) {
            return left;
        }
        if (desiredDirection > 0 && right.x >= botPos.x) {
            return right;
        }

        int leftScore = Math.abs(targetPos.x - left.x) + Math.abs(botPos.x - left.x);
        int rightScore = Math.abs(targetPos.x - right.x) + Math.abs(botPos.x - right.x);
        return leftScore <= rightScore ? left : right;
    }

    private static boolean shouldUseJump(BotMovementState entry, Point botPos, Point steeringTarget, int stepX) {
        if (entry == null || botPos == null || steeringTarget == null || stepX == 0) {
            return false;
        }
        if (shouldWalkOffLedge(entry, botPos, steeringTarget, stepX)) {
            return false;
        }

        MapleMap map = entry.bot.getMap();
        int direction = Integer.signum(stepX);
        int jumpStep = direction * BotPhysicsEngine.walkStep(map, entry.movementProfile);
        BotPhysicsEngine.JumpLanding landing =
                BotPhysicsEngine.simulateJumpLanding(map, botPos, jumpStep, entry.movementProfile);
        return isUsefulJumpProbeLanding(botPos, steeringTarget, direction, landing);
    }

    private static boolean isUsefulJumpProbeLanding(Point botPos,
                                                    Point steeringTarget,
                                                    int direction,
                                                    BotPhysicsEngine.JumpLanding landing) {
        if (landing == null || landing.point() == null || direction == 0) {
            return false;
        }
        Point landingPoint = landing.point();
        int landingDx = landingPoint.x - botPos.x;
        if (Integer.signum(landingDx) != direction) {
            return false;
        }

        int distanceBefore = Math.abs(steeringTarget.x - botPos.x);
        int distanceAfter = Math.abs(steeringTarget.x - landingPoint.x);
        if (distanceAfter >= distanceBefore) {
            return false;
        }

        boolean targetIsAboveOrLevel = steeringTarget.y <= botPos.y + BotPhysicsEngine.cfg.MAX_SNAP_DROP;
        boolean landingIsAboveOrLevel = landingPoint.y <= botPos.y + BotPhysicsEngine.cfg.MAX_SNAP_DROP;
        return targetIsAboveOrLevel && landingIsAboveOrLevel;
    }
}
