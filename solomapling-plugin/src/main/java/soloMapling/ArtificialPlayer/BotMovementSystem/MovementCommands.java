package soloMapling.ArtificialPlayer.BotMovementSystem;

import org.gms.client.Character;
import org.gms.net.packet.InPacket;
import org.gms.server.maps.Foothold;
import org.gms.server.maps.Portal;
import soloMapling.ArtificialPlayer.BotHelpers;
import soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.MovementPacket;
import soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.SingleMoveCommand;
import soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.MovementRecording;
import soloMapling.ArtificialPlayer.BotMovementSystem.NavigationSystem.NavigationElement;
import soloMapling.ArtificialPlayer.BotMovementSystem.NavigationSystem.PathFinder;
import soloMapling.ArtificialPlayer.GCMoveSystem.LodCounts;
import org.gms.util.PacketCreator;
import org.gms.exception.EmptyMovementException;

import org.gms.server.maps.MapleMap;

import java.awt.*;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Math.abs;
import static org.gms.net.server.channel.handlers.AbstractMovementPacketHandler.updatePositionBot;
import static soloMapling.ArtificialPlayer.BotLogic.isPointNear;
import static soloMapling.ArtificialPlayer.BotMovementSystem.InPacketReader.getMovementRecording;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementPacketConstructor.createArtificialStopPacket;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementPacketConstructor.createFallDownPacket;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementPacketConstructor.createIdleStandlingPacket;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementPacketConstructor.createSitPacket;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementPacketConstructor.createUnsitPacket;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementPacketConstructor.deconstructMovementInPacket;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementPacketConstructor.modifyMovementPacketWithOffset;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.MovementEnums.StanceValues.IDLE_LEFT;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.MovementEnums.StanceValues.IDLE_RIGHT;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.MovementEnums.StanceValues.JUMP_LEFT;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.MovementEnums.StanceValues.JUMP_RIGHT;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.MovementEnums.StanceValues.MOVING_LEFT;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.MovementEnums.StanceValues.MOVING_RIGHT;
//import static soloMapling.ArtificialPlayer.BotMovementSystem.NavigationSystem.PathFinderbeta.getXYCoordsFromPacket;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.MovementEnums.StanceValues.SIT_LEFT;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.MovementEnums.StanceValues.SIT_RIGHT;
import static soloMapling.ArtificialPlayer.BotHelpers.isBot;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.SingleMoveCommand.getConvertedStanceDirection;
import static soloMapling.ArtificialPlayer.BotMovementSystem.NavigationSystem.PathFinder.createPath;
import static soloMapling.ArtificialPlayer.BotMovementSystem.NavigationSystem.PathFinder.getNavElements;
import static soloMapling.ArtificialPlayer.BotCommandsPack.WarpCommands.botWarpMapOnPortal;
import static soloMapling.DebugUtilities.debugprint;

public class MovementCommands {

    private static final ConcurrentHashMap<Integer, Boolean> movementInterruptFlags = new ConcurrentHashMap<>();

    private static boolean isBotMovementInterrupted(Character fakechar) {
        return movementInterruptFlags.getOrDefault(fakechar.getId(), false);
    }


    private static boolean isStanding(Character fakechar) {
        byte stance = (byte) fakechar.getStance();
        return stance == IDLE_RIGHT || stance == IDLE_LEFT;
    }

    private static boolean isWalking(Character fakechar) {
        byte stance = (byte) fakechar.getStance();
        return stance == MOVING_RIGHT || stance == MOVING_LEFT;
    }

    private static boolean isJumping(Character fakechar) {
        byte stance = (byte) fakechar.getStance();
        return stance == JUMP_RIGHT || stance == JUMP_LEFT;
    }

    private static boolean isMidair(Character fakechar) {
        return fakechar.getFh() == fakechar.getPosition().y;
    }

    public static boolean facingLeft(Character fakechar) {
        switch (fakechar.getStance()) {
            case MOVING_LEFT, JUMP_LEFT, IDLE_LEFT -> {
                return true;
            }
        }
        return false;
    }

    /**
     * Callback that runs periodically during movement playback.
     * Return {@code true} to signal the movement loop should stop (bot reacted and stopped).
     * Return {@code false} to keep walking.
     */
    @FunctionalInterface
    public interface MidMovementCheck {
        boolean check(Character bot);
    }

    private static final int AWARENESS_CHECK_INTERVAL = 5; // check every N packets

    public static boolean BotMoveStreamHelper(MovementRecording mvr, Character fakechar,
                                           boolean useOffset, Point stopPoint) {
        return BotMoveStreamHelper(mvr, fakechar, useOffset, stopPoint, null);
    }

    public static boolean BotMoveStreamHelper(MovementRecording mvr, Character fakechar,
                                           boolean useOffset, Point stopPoint,
                                           MidMovementCheck midCheck) {
        List<MovementPacket> fullRecording = mvr.getMovementPacketList();
        fullInPacket:
        for (int i = 0; i < fullRecording.size(); i++) {
            if (isBotMovementInterrupted(fakechar)) {
                clearBotMovementInterrupt(fakechar);
                injectArtificialStopPacket(fakechar);
                return true;
            }

            MovementPacket mvp = fullRecording.get(i); // Current element

            InPacket packet = mvp.getPacket();
            InPacket packetCopy = packet.copy();

            if (useOffset) {
                packet = modifyMovementPacketWithOffset(packet, fakechar);
            }

            BotMove(packet, fakechar);

            if (stopPoint != null) {
                if (checkIfCharNearPosInPacket(packetCopy, stopPoint)) {
                    injectArtificialStopPacket(fakechar);
                    break fullInPacket;
                }
            }

            // Mid-movement awareness check — runs every N packets, not every packet
            if (midCheck != null && i > 0 && i % AWARENESS_CHECK_INTERVAL == 0) {
                if (midCheck.check(fakechar)) {
                    return true; // callback triggered a stop
                }
            }

            MovementPacket nextMvp = (i + 1 < fullRecording.size()) ? fullRecording.get(i + 1) : null; // Next element
            if (nextMvp != null) {
                if (!BotHelpers.waitBetweenTwoLong(mvp.getTimestamp(), nextMvp.getTimestamp())) {
                    // thread interrupted (e.g. Future.cancel(true)) - stop the replay cleanly
                    injectArtificialStopPacket(fakechar);
                    return true;
                }
            }
        }
        return false;
    }


    public static void BotMoveStreamOffset(MovementRecording mvr, Character fakechar) {
        BotMoveStreamHelper(mvr, fakechar, true, null);
    }

    public static void BotMoveStream(MovementRecording mvr, Character fakechar) {
        BotMoveStreamHelper(mvr, fakechar, false, null);
    }

    public static void BotMoveStreamUntilStopPoint(MovementRecording mvr, Character fakechar, Point stopPoint) {
        BotMoveStreamHelper(mvr, fakechar, false, stopPoint);
    }

    // delete-able, already have BotMoveStreamOffset w/ helper
//    public static void BotMoveStreamOffset(Map<Long, InPacket> reader, Character fakechar) {
//        for (Map.Entry<Long, InPacket> entry : reader.entrySet()) {
//            InPacket p = modifyMovementPacketWithOffset(entry.getValue(), fakechar);
//            BotMove(p, fakechar);
//
//            Map.Entry<Long, InPacket> nextEntry = getNextInPacketEntry(reader, entry.getKey());
//            if (nextEntry != null) {
//                BotHelpers.waitBetweenTwoLong(entry.getKey(), nextEntry.getKey());
//            }
//        }
//    }
//
//    public static void BotMoveStream(Map<Long, InPacket> reader, Character fakechar) {
//        for (Map.Entry<Long, InPacket> entry : reader.entrySet()) {
//            BotMove(entry.getValue(), fakechar);
//            Map.Entry<Long, InPacket> nextEntry = getNextInPacketEntry(reader, entry.getKey());
//            if (nextEntry != null) {
//                BotHelpers.waitBetweenTwoLong(entry.getKey(), nextEntry.getKey());
//            }
//        }
//    }


    private static Long getTimestampWhenCharNearPoint(List<MovementPacket> fullRecording, Character fakechar) {
        Point currentPosition = fakechar.getPosition();
        debugprint(currentPosition);
        Long timestamp = null;

        for (MovementPacket mvp : fullRecording) {
            InPacket packet = mvp.getPacket();
            if (checkIfCharNearPosInPacket(packet, currentPosition)) {
                timestamp = mvp.getTimestamp();
                debugprint("TIMESTAMP FOUND", timestamp);
                break;
            }
        }
        debugprint("TIMESTAMP:", timestamp);
        return timestamp;
    }

    public static boolean checkIfCharNearPosInPacket(InPacket packet, Point stopPoint) {
        List<SingleMoveCommand> deconstructedStream = deconstructMovementInPacket(packet);
        for (SingleMoveCommand movePack : deconstructedStream) {
            Point packetPoint = new Point(movePack.getXpos(), movePack.getYpos());
            boolean nearStopPoint = isPointNear(packetPoint, stopPoint, 80);
            if (nearStopPoint) {
                debugprint("CHAR AT STOP POINT", packetPoint);
                return true;
            }
        }
        return false;
    }

//    public static void injectIntoMovementStream(MovementRecording mvr, Character fakechar) {
//        injectIntoMovementStreamHelper(mvr, fakechar, null);
//    }
//
//    public static void injectIntoMovementStreamWithEndPoint(MovementRecording mvr, Character fakechar, Point endPt) {
//        injectIntoMovementStreamHelper(mvr, fakechar, endPt);
//    }

//    public static void injectIntoMovementStreamHelper(MovementRecording mvr, Character fakechar, Point endPt) {
//        List<MovementPacket> fullRecording = mvr.getMovementPacketList();
//        List<MovementPacket> copyRecording = fullRecording;
//        Long timestamp = getTimestampWhenCharNearPoint(copyRecording, fakechar); // it needs to copy reader otherwise those InPackets will be "read" from
//        boolean startProcessing = false;
//
//        FullMethod:
//        for (int i = 0; i < fullRecording.size(); i++) {
//            MovementPacket mvp = fullRecording.get(i); // Current element
//
////        for (Map.Entry<Long, InPacket> entry : reader.entrySet()) {
//            if (!startProcessing) {
//                if (mvp.getTimestamp() == (timestamp)) {
//                    debugprint("timestamp found in packet. start processing now");
//                    startProcessing = true;
//                } else {
//                    continue; // Skip until the startKey is found
//                }
//            }
//
//            InPacket packet = mvp.getPacket();
//            InPacket packetCopy = packet.copy();
//
//            // Stop at end points, this one does it before
//            if (endPt != null) {
//                for (Point pt : getXYCoordsFromPacket(packetCopy)) {
//                    if (isCharNear(pt, endPt)) { // fakechar.getPosition() // fakechar getPosition too slow
//                        injectArtificialStopPacket(fakechar);
//                        break FullMethod;
//                    }
//                }
//            }
//
//            BotMove(packet, fakechar);
//
//            MovementPacket nextMvp = (i + 1 < fullRecording.size()) ? fullRecording.get(i + 1) : null; // Next element
//            if (nextMvp != null) {
//                BotHelpers.waitBetweenTwoLong(mvp.getTimestamp(), nextMvp.getTimestamp());
//            }
//        }
//    }

    public static void injectArtificialStopPacket(Character fakechar) {
        if (isWalking(fakechar)) {
            try {
                InPacket stopPacket = createArtificialStopPacket(fakechar);
                BotMove(stopPacket, fakechar);
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public static void BotIdleStandingUpdate(Character fakechar) {
        if (!isStanding(fakechar)) {
            return;
        }
        // Fable Phase 1 (F4): don't build/broadcast the idle refresh when no real player
        // can see the map. On promotion the map-entry nudge pulls the next macro tick
        // (and this refresh) forward, so nothing looks stale when someone walks in.
        if (LodCounts.trackerRunning() && !LodCounts.isMapActive(fakechar.getMapId())) {
            return;
        }
        soloMapling.server.BotPerfStats.IDLE_BROADCASTS.increment();
        BotIdleStandingUpdateForced(fakechar);
    }

    public static void BotIdleStandingUpdateForced(Character fakechar) {
        InPacket stopPacket = createIdleStandlingPacket(fakechar);
        BotMove(stopPacket, fakechar);
    }

    public static void botFallDownPacket(Character fakechar) {
        InPacket fallDownPacket = createFallDownPacket(fakechar);
        BotMove(fallDownPacket, fakechar);
        BotIdleStandingUpdateForced(fakechar);
    }

    public static Foothold getFootHoldObject(Character fakechar) {
        Point pos = fakechar.getPosition();
        return fakechar.getMap().getFootholds().findBelow(pos);
    }

    public static int findFootHoldId(Character fakechar) {
        Foothold fh = getFootHoldObject(fakechar);
        if (fh != null) {
            return fh.getId();
        }
        return 0;
    }

    public static boolean executeMovement(Character fakechar, MovementRecording path) {
        return executeMovement(fakechar, path, null);
    }

    public static boolean executeMovement(Character fakechar, MovementRecording path, MidMovementCheck midCheck) {
        boolean interrupted = BotMoveStreamHelper(path, fakechar, false, null, midCheck);
        if (!interrupted) {
            injectArtificialStopPacket(fakechar);
        }
        return interrupted;
    }

    public static void pathFinderBeta(Character fakechar, Point endPt) {
        if (!tryAcquireMovementLock(fakechar)) return;
        try {
            pathFinderBetaUnlocked(fakechar, endPt);
        } finally {
            releaseMovementLock(fakechar);
        }
    }

    private static void pathFinderBetaUnlocked(Character fakechar, Point endPt) {
        int mapId = fakechar.getMapId();
        Point start = fakechar.getPosition();
        PathFinder pf = new PathFinder(fakechar, endPt);
        List<String> path = createPath(mapId, start, endPt, PathFinder.PathType.RANDOM);
        List<NavigationElement> elements = getNavElements(mapId, path);
        pf.executePath(mapId, elements);
    }

    /**
     * Aware pathfinder: walks the bot to endPt while checking for real players
     * between each navigation segment. Uses default detection range and weights.
     */
    public static void pathFinderAware(Character fakechar, Point endPt) {
        pathFinderAware(fakechar, endPt, 300, 200, 20, 60, 20);
    }

    /**
     * Aware pathfinder with configurable detection range and reaction weights.
     *
     * @param detectWidth   detection rectangle width
     * @param detectHeight  detection rectangle height
     * @param ignoreWeight  weight for ignoring a detected player
     * @param stopWeight    weight for stopping and reacting
     * @param walkWeight    weight for reacting while continuing to walk
     */
    public static void pathFinderAware(Character fakechar, Point endPt,
                                        int detectWidth, int detectHeight,
                                        int ignoreWeight, int stopWeight, int walkWeight) {
        if (!tryAcquireMovementLock(fakechar)) return;
        try {
            int mapId = fakechar.getMapId();
            Point start = fakechar.getPosition();
            PathFinder pf = new PathFinder(fakechar, endPt);
            List<String> path = createPath(mapId, start, endPt, PathFinder.PathType.RANDOM);
            List<NavigationElement> elements = getNavElements(mapId, path);
            pf.executePathAware(mapId, elements, detectWidth, detectHeight,
                    ignoreWeight, stopWeight, walkWeight);
        } finally {
            releaseMovementLock(fakechar);
        }
    }

    // OPQ / aerial-target navigation. Snaps an airborne point (e.g. a cloud reactor)
    // onto the closest recorded ground point, walks the bot there, and returns the
    // AerialPathResult so the caller can drive the vertical interaction after arrival.
    // Returns null if no ground point was within the Y tolerance.
    public static PathFinder.AerialPathResult pathFinderBetaAerial(Character fakechar, Point aerialPt) {
        if (!tryAcquireMovementLock(fakechar)) return null;
        try {
            int mapId = fakechar.getMapId();
            Point start = fakechar.getPosition();

            PathFinder.AerialPathResult result = PathFinder.coordPathBuilderAerial(fakechar, aerialPt);
            if (result == null) {
                return null;
            }

            Point snapped = result.getSnappedPoint();
            PathFinder pf = new PathFinder(fakechar, snapped);
            List<String> path = createPath(mapId, start, snapped, PathFinder.PathType.RANDOM);
            List<NavigationElement> elements = getNavElements(mapId, path);
            pf.executePath(mapId, elements);
            return result;
        } finally {
            releaseMovementLock(fakechar);
        }
    }

    public static void moveToPortal(Character fakechar, int portalId) {
        Portal portal = fakechar.getMap().getPortal(portalId);
        if (portal == null) return;
        Point portalPos = portal.getPosition();
        if (!tryAcquireMovementLock(fakechar)) return;
        try {
            pathFinderBetaUnlocked(fakechar, portalPos);
            smallMoveUnlocked(fakechar, portalPos);
        } finally {
            releaseMovementLock(fakechar);
        }
    }

    public static void moveToPortalAndEnter(Character fakechar, int portalId) {
        moveToPortal(fakechar, portalId);
        botWarpMapOnPortal(fakechar);
    }

    // Runtime test helper. Invoke from a live bot to verify aerial pathfinding:
    // pass a known reactor coordinate, watch console for the snapped point + yDelta,
    // and confirm the bot ends up directly under the target.
    public static void testAerialPathFinder(Character fakechar, Point aerialPt) {
        System.out.println("[testAerialPathFinder] mapId=" + fakechar.getMapId());
        System.out.println("  bot start pos: " + fakechar.getPosition());
        System.out.println("  aerial target: " + aerialPt);

        PathFinder.AerialPathResult result = pathFinderBetaAerial(fakechar, aerialPt);
        if (result == null) {
            System.out.println("[testAerialPathFinder] FAILED: no ground point within Y tolerance");
            return;
        }

        System.out.println("[testAerialPathFinder] arrived");
        System.out.println("  snapped to:    " + result.getSnappedPoint());
        System.out.println("  yDelta:        " + result.getYDelta() + " (negative = reactor sits above bot)");
        System.out.println("  bot final pos: " + fakechar.getPosition());
    }

    public static void botSitChair(Character fakechar, Integer chairId) {
        fakechar.setChair(chairId);
        int stance = fakechar.getStance();
        InPacket sitPacket = createSitPacket(fakechar);
        BotMove(sitPacket, fakechar);
        fakechar.setStance(getConvertedStanceDirection((byte) stance, SIT_RIGHT, SIT_LEFT));
        fakechar.getMap().broadcastMessage(fakechar, PacketCreator.showChair(fakechar.getId(), chairId), false);
    }

    public static void botCancelChair(Character fakechar) {
        fakechar.setChair(-1);
        int stance = fakechar.getStance();
        fakechar.getMap().broadcastMessage(fakechar, PacketCreator.showChair(fakechar.getId(), 0), false);
        InPacket unsitPacket = createUnsitPacket(fakechar);
        BotMove(unsitPacket, fakechar);
        fakechar.setStance(getConvertedStanceDirection((byte) stance, IDLE_RIGHT, IDLE_LEFT));
        fakechar.getMap().broadcastMessage(fakechar, PacketCreator.showChair(fakechar.getId(), 0), false);
    }

    enum FacingDirection {
        LEFT, RIGHT
    }

    enum MovementDirection {
        LEFT, RIGHT
    }

    // Mapping from direction combinations to movement recording keys
    private static final String[][] MOVEMENT_RECORDINGS_45 = {
            {"leftleft45", "leftright70"},
            {"rightleft45", "rightright70"}
    };

    private static final String[][] MOVEMENT_RECORDINGS_20 = {
            {"leftleft20", "leftright20"},
            {"rightleft20", "rightright20"}
    };

    /**
     * Plays the in-place "turnaroundtoleft" recording so the bot ends up facing left
     * without significantly changing its X coordinate. No-op if already facing left.
     */
    public static void microTurnAroundToLeft(Character fakechar) {
        if (fakechar == null || facingLeft(fakechar)) {
            return;
        }
        try {
            MovementRecording mvr = getMovementRecording(0, "turnaroundtoleft");
            BotMoveStreamOffset(mvr, fakechar);
        } catch (Exception e) {
            // Missing recording shouldn't break the caller.
        }
    }

    /**
     * Plays the in-place "turnaroundtoright" recording so the bot ends up facing right
     * without significantly changing its X coordinate. No-op if already facing right.
     */
    public static void microTurnAroundToRight(Character fakechar) {
        if (fakechar == null || !facingLeft(fakechar)) {
            return;
        }
        try {
            MovementRecording mvr = getMovementRecording(0, "turnaroundtoright");
            BotMoveStreamOffset(mvr, fakechar);
        } catch (Exception e) {
        }
    }

    /** Flips the bot's current facing via a micro turn-around (no X drift). */
    public static void microTurnAround(Character fakechar) {
        if (fakechar == null) {
            return;
        }
        if (facingLeft(fakechar)) {
            microTurnAroundToRight(fakechar);
        } else {
            microTurnAroundToLeft(fakechar);
        }
    }

    /**
     * Makes the bot face toward a target point. If the point is to the left
     * and the bot is facing right (or vice versa), performs a micro turn-around.
     * No-op if the bot is already facing the correct direction.
     */
    public static void botFaceTowardsPoint(Character fakechar, Point target) {
        if (fakechar == null || target == null) return;

        boolean targetIsLeft = target.getX() < fakechar.getPosition().getX();

        if (targetIsLeft && !facingLeft(fakechar)) {
            microTurnAroundToLeft(fakechar);
        } else if (!targetIsLeft && facingLeft(fakechar)) {
            microTurnAroundToRight(fakechar);
        }
    }

    public static void BotMoveSmallDistanceX(Character fakechar, Point endpos) {
        if (fakechar == null || endpos == null) {
            return;
        }
        int currPosX = (int) fakechar.getPosition().getX();
        double distance = Math.abs(currPosX - endpos.getX());
        if (distance <= 0) {
            return;
        }
        if (!tryAcquireMovementLock(fakechar)) return;
        try {
            smallMoveUnlocked(fakechar, endpos);
        } finally {
            releaseMovementLock(fakechar);
        }
    }

    private static void smallMoveUnlocked(Character fakechar, Point endpos) {
        int currPosX = (int) fakechar.getPosition().getX();
        double distance = Math.abs(currPosX - endpos.getX());
        if (distance <= 0) return;

        FacingDirection facingDirection = (fakechar.isFacingLeft()) ? FacingDirection.LEFT : FacingDirection.RIGHT;
        MovementDirection moveDirection = (endpos.getX() >= currPosX) ? MovementDirection.RIGHT : MovementDirection.LEFT;

        String recordingKey;
        if (distance < 40) {
            recordingKey = MOVEMENT_RECORDINGS_20[facingDirection.ordinal()][moveDirection.ordinal()];
        } else {
            recordingKey = MOVEMENT_RECORDINGS_45[facingDirection.ordinal()][moveDirection.ordinal()];
        }

        try {
            String recordingPath = "/smallMovement/" + recordingKey;
            MovementRecording mvr = getMovementRecording(0, recordingPath);
            BotMoveStreamHelper(mvr, fakechar, true, null);
        } catch (Exception e) {
            // Missing recording
        }
    }

    // generic todo?
    public static Map.Entry<Long, InPacket> getNextInPacketEntry(Map<Long, InPacket> map, long currentKey) {
        for (Map.Entry<Long, InPacket> entry : map.entrySet()) {
            if (entry.getKey() > currentKey) {
                return entry;
            }
        }
        return null;
    }

    public static void BotMove(InPacket p, Character chr) {
        p.skip(9);
        try {
            int movementDataStart = p.getPosition();
            updatePositionBot(p, chr, 0);
            long movementDataLength = p.getPosition() - movementDataStart; //how many bytes were read by updatePosition
            p.seek(movementDataStart);

            chr.getMap().moveBot(chr, chr.getPosition());

            if (chr.isHidden()) {
                chr.getMap().broadcastGMMessage(chr, PacketCreator.movePlayer(chr.getId(), p, movementDataLength), false);
            } else {
                chr.getMap().broadcastMessage(chr, PacketCreator.movePlayer(chr.getId(), p, movementDataLength), false);
            }
        } catch (EmptyMovementException e) {
        }
    }

    /**
     * Interrupts a bot's current movement. The bot will stop walking at roughly
     * wherever it is in the current movement packet sequence and play a proper
     * idle/stop animation. Call this from any thread — the movement loop picks
     * it up on its next packet iteration (within ~50-300ms).
     */
    public static void interruptBotMovement(Character fakechar) {
        if (fakechar == null) {
            return;
        }
        movementInterruptFlags.put(fakechar.getId(), true);
    }

    private static void clearBotMovementInterrupt(Character fakechar) {
        movementInterruptFlags.remove(fakechar.getId());
    }

    // ── Movement Lock ──────────────────────────────────────────────────
    private static final ConcurrentHashMap<Integer, AtomicBoolean> movementLocks = new ConcurrentHashMap<>();

    public static boolean tryAcquireMovementLock(Character fakechar) {
        AtomicBoolean lock = movementLocks.computeIfAbsent(fakechar.getId(), k -> new AtomicBoolean(false));
        return lock.compareAndSet(false, true);
    }

    public static void releaseMovementLock(Character fakechar) {
        AtomicBoolean lock = movementLocks.get(fakechar.getId());
        if (lock != null) lock.set(false);
    }

    public static boolean isBotMoving(Character fakechar) {
        AtomicBoolean lock = movementLocks.get(fakechar.getId());
        return lock != null && lock.get();
    }

    // ── Nudge Utilities ────────────────────────────────────────────────
    private static final int OVERLAP_THRESHOLD_X = 40;
    private static final int OVERLAP_THRESHOLD_Y = 30;
    private static final int NUDGE_DISTANCE = 20;

    public static void nudgeSmall(Character bot) {
        if (bot.getChair() > 0) {
            botCancelChair(bot);
        }
        int direction = ThreadLocalRandom.current().nextBoolean() ? NUDGE_DISTANCE : -NUDGE_DISTANCE;
        Point target = new Point(bot.getPosition().x + direction, bot.getPosition().y);
        BotMoveSmallDistanceX(bot, target);
    }

    public static boolean nudgeAwayFromOverlap(Character bot) {
        if (bot.getChair() > 0) return false;

        Point pos = bot.getPosition();
        MapleMap map = bot.getMap();
        if (map == null) return false;

        for (Character other : map.getAllPlayers()) {
            if (other.getId() == bot.getId()) continue;
            if (!isBot(other)) continue;
            Point otherPos = other.getPosition();
            if (Math.abs(pos.x - otherPos.x) < OVERLAP_THRESHOLD_X
                    && Math.abs(pos.y - otherPos.y) < OVERLAP_THRESHOLD_Y) {
                int direction = (pos.x >= otherPos.x) ? NUDGE_DISTANCE : -NUDGE_DISTANCE;
                Point target = new Point(pos.x + direction, pos.y);
                BotMoveSmallDistanceX(bot, target);
                return true;
            }
        }
        return false;
    }

    /**
     * Test command: starts a bot walking along a recording, then interrupts it
     * after {@code interruptDelayMs} milliseconds. Use this to verify the
     * interrupt-stop behaviour visually in-game.
     *
     * @param fakechar         the bot character
     * @param mvr              the movement recording to play
     * @param interruptDelayMs how many ms to wait before sending the interrupt
     */
    public static void testInterruptMovement(Character fakechar, MovementRecording mvr, long interruptDelayMs) {
        if (fakechar == null || mvr == null) {
            return;
        }
        clearBotMovementInterrupt(fakechar);

        // Schedule the interrupt on a separate thread
        new Thread(() -> {
            try {
                Thread.sleep(interruptDelayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            interruptBotMovement(fakechar);
        }, "BotMovementInterrupt-" + fakechar.getId()).start();

        // Play the recording on the current thread (blocks until done or interrupted)
        BotMoveStream(mvr, fakechar);

        // Clean up the flag so future movements aren't pre-interrupted
        clearBotMovementInterrupt(fakechar);
    }

    /**
     * Test command: starts a bot navigating via pathfinder to endPt, then
     * interrupts it after {@code interruptDelayMs} milliseconds.
     */
    public static void testInterruptPathfinder(Character fakechar, Point endPt, long interruptDelayMs) {
        if (fakechar == null || endPt == null) {
            return;
        }
        clearBotMovementInterrupt(fakechar);

        new Thread(() -> {
            try {
                Thread.sleep(interruptDelayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            interruptBotMovement(fakechar);
        }, "BotPathInterrupt-" + fakechar.getId()).start();

        pathFinderBeta(fakechar, endPt);

        clearBotMovementInterrupt(fakechar);
    }


}
