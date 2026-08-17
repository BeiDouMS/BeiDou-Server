package soloMapling.ArtificialPlayer.BotMovementSystem.NavigationSystem;

import org.gms.client.Character;
import org.gms.server.maps.MapleMap;
import soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.MovementRecording;
import soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.MovementRecordingRaw;
import soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.StartEndPair;
import soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.TimeCoordinatePair;
import soloMapling.server.SoloMaplingUtilities;

import soloMapling.ArtificialPlayer.BotLogic;
import soloMapling.ArtificialPlayer.BotMovementSystem.PlayerReaction;
import soloMapling.ArtificialPlayer.BotMovementSystem.PlayerSnapshot;

import java.awt.*;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static soloMapling.ArtificialPlayer.BotMovementSystem.InPacketReader.getMovementRecordingRaw;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands.MidMovementCheck;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands.executeMovement;
import static soloMapling.ArtificialPlayer.BotMovementSystem.NavigationSystem.NavigationElement.parseNavigationType;
import static soloMapling.DebugUtilities.debugprint;


public class PathFinder {


    private final Character character;
    private final MapleMap map;
    private final Point endPt;

    private static final Random random = new Random();
    private static final Map<PathType, Double> WEIGHTS = new EnumMap<>(PathType.class);


    public PathFinder(Character fakechar, Point endPt) {
        this.character = fakechar;
        this.map = fakechar.getMap();
        this.endPt = endPt;
    }

    public static List<TimeCoordinatePair> coordPathBuilder(Character fakechar, Point endPt, PathType pathType) {
        int mapId = fakechar.getMapId();
        Point start = fakechar.getPosition();
        PathFinder pf = new PathFinder(fakechar, endPt);
        List<String> path = createPath(mapId, start, endPt, pathType);
        List<NavigationElement> elements = getNavElements(mapId, path);
        List<TimeCoordinatePair> tcp = pf.buildPointPath(mapId, elements);
        return tcp;
    }

    public static List<TimeCoordinatePair> coordPathBuilder(Character fakechar, Point endPt) {
//        int mapId = fakechar.getMapId();
//        Point start = fakechar.getPosition();
//        PathFinder pf = new PathFinder(fakechar, endPt);
//        List<String> path = createPath(mapId, start, endPt, getRandomPathType());
//        List<NavigationElement> elements = getNavElements(mapId, path);
//        List<TimeCoordinatePair> tcp = pf.buildPointPath(mapId, elements);
//        return tcp;
        return coordPathBuilder(fakechar, endPt, getRandomPathType());
    }

    public static List<TimeCoordinatePair> coordPathSplitter(List<TimeCoordinatePair> tcp, double percentageInterval) {
        // Validate inputs
        if (tcp == null || tcp.size() < 2) {
            return tcp;
        }
        if (percentageInterval <= 0 || percentageInterval >= 100) {
            throw new IllegalArgumentException("Percentage interval must be between 0 and 100");
        }

        List<TimeCoordinatePair> result = new ArrayList<>();
        int totalPoints = tcp.size();

        // Calculate number of intervals
        int numIntervals = (int) Math.ceil(100 / percentageInterval);

        // Add points at each interval
        for (int i = 1; i <= numIntervals; i++) {  // Start from 1 to skip 0%
            double percentage = i * percentageInterval / 100.0;

            // If percentage exceeds 100%, use the last point
            int index = percentage >= 1.0 ?
                    totalPoints - 1 :
                    (int) Math.round((totalPoints - 1) * percentage);

            // Ensure index is within bounds and avoid duplicates
            if (index > 0 && index < totalPoints &&
                    (result.isEmpty() || !result.get(result.size() - 1).equals(tcp.get(index)))) {
                result.add(tcp.get(index));
            }
        }

        // Ensure the last point is included if it wasn't added by the intervals
        if (result.isEmpty() || !result.get(result.size() - 1).equals(tcp.get(totalPoints - 1))) {
            result.add(tcp.get(totalPoints - 1));
        }

        return result;
    }

    public enum PathType {
        SHORTEST,
        RANDOM
    }

    static {
        // Define weights (probabilities should sum to 1.0)
        WEIGHTS.put(PathType.SHORTEST, 0.65);
        WEIGHTS.put(PathType.RANDOM, 0.35);
        // Add more types with their weights as needed:
        // WEIGHTS.put(PathType.SCENIC, 0.2);
        // WEIGHTS.put(PathType.SAFEST, 0.1);
    }

    public static PathType getRandomPathType() {
        double value = random.nextDouble();
        double cumulative = 0.0;

        for (Map.Entry<PathType, Double> entry : WEIGHTS.entrySet()) {
            cumulative += entry.getValue();
            if (value < cumulative) {
                return entry.getKey();
            }
        }

        // Fallback to first enum value in case of rounding errors
        return PathType.values()[0];
    }

    public static List<String> getRandomPath(MapGraph gr, String startNode, String endNode) {
        return MapGraph.getPathChain(gr.getRandomPath(startNode, endNode));
    }

    public static List<String> getShortestPath(MapGraph gr, String startNode, String endNode) {
        return MapGraph.getPathChain(gr.getShortestPath(startNode, endNode));
    }

    public static List<String> createPath(int mapId, Point startPoint, Point endPoint, PathType pathType) {
        MapGraph gr = new MapGraph(mapId);
        List<String> mainAreas = gr.getMainAreaOfPoint(startPoint);
        List<String> endAreas = gr.getMainAreaOfPoint(endPoint);
//        List<String> connectors = gr.getConnectorOfPoint(endPoint); //  gr.getMainAreaOfPoint(endPoint)

        String mainArear = SoloMaplingUtilities.getRandomElement(mainAreas);
        String endArea = SoloMaplingUtilities.getRandomElement(endAreas);

        switch (pathType) {
            case SHORTEST:
                return getShortestPath(gr, mainArear, endArea);
            case RANDOM:
                return getRandomPath(gr, mainArear, endArea);
            default:
                throw new IllegalArgumentException("Invalid path type: " + pathType);
        }
    }

//    public static void executePath(int mapId, List<NavigationElement> elements) {
//        for (int i = 0; i < elements.size(); i++) {
//            debugprint("Element index: " + i);
//            if (elements.get(i) instanceof MainArea) {
//                if (i == elements.size() - 1) {
//                    // if last, walk to end point
//                    MainArea mainArea = (MainArea) elements.get(i);
//                    MovementRecording finalPath = mainArea.getMovementRecording();
//                    // executeMovement(fakechar, finalPath); // todo
//                } else if (elements.get(i + 1) instanceof Connector) {
//                    MainArea mainArea = (MainArea) elements.get(i);
//                    String recName = mainArea.getRecordingName(); //
//
//                    Connector connector = (Connector) elements.get(i + 1);
//                    Point endpt = connector.getStartPosition();
////                    Point currPos = start; // char's current pos
//                    Point currPos = mainArea.getMovementRecordingRaw().getStartPoint(); // test only
//
//                    MovementRecording finalPath = getPathBetweenTwoPointsInMainArea(mapId, recName, currPos, endpt);
////                    executeMovement(fakechar, finalPath); // todo
//                }
//            } else if (elements.get(i) instanceof Connector) {
//                Connector connector = (Connector) elements.get(i);
//                MovementRecording finalPath = connector.getMovementRecording();
////                executeMovement(fakechar, finalPath); // todo
//            }
//        }
//
//    }

    public List<TimeCoordinatePair> buildPointPath(int mapId, List<NavigationElement> elements) {
        if (elements == null || elements.isEmpty()) {
            debugprint("No navigation elements provided");
            return null;
        }

        List<TimeCoordinatePair> tcp = new ArrayList<>();

        for (int i = 0; i < elements.size(); i++) {
            NavigationElement currentElement = elements.get(i);
            try {
                if (currentElement instanceof MainArea) {
                    tcp.addAll(currentElement.getMovementRecordingRaw().getTimeCoordPath());
                }
            } catch (Exception e) {
                debugprint(e);
                debugprint("Failed to execute navigation element at index current: " + currentElement.getRecordingName());
            }
        }
        return tcp;

    }

    public void executePath(int mapId, List<NavigationElement> elements) {
        if (elements == null || elements.isEmpty()) {
//            debugprint("No navigation elements provided");
            return;
        }

        for (int i = 0; i < elements.size(); i++) {
            NavigationElement currentElement = elements.get(i);
            NavigationElement nextElement = (i < elements.size() - 1) ? elements.get(i + 1) : null;

            try {
                boolean interrupted = executeElement(mapId, currentElement, nextElement);
                if (interrupted) {
                    break;
                }
            } catch (Exception e) {
                debugprint(e);
                debugprint("Failed to execute navigation element at index current: " + currentElement.getRecordingName());
            }
        }
    }

    /**
     * Aware variant of executePath. Passes a player-detection callback into the
     * movement packet loop so checks happen mid-walk (every ~5 packets), not
     * just between navigation elements.
     *
     * @param detectWidth   detection rectangle width  (e.g. 300)
     * @param detectHeight  detection rectangle height (e.g. 200)
     * @param ignoreWeight  RNG weight for ignoring the player
     * @param stopWeight    RNG weight for stopping and reacting
     * @param walkWeight    RNG weight for reacting while walking
     */
    public void executePathAware(int mapId, List<NavigationElement> elements,
                                  int detectWidth, int detectHeight,
                                  int ignoreWeight, int stopWeight, int walkWeight) {
        if (elements == null || elements.isEmpty()) {
//            debugprint("No navigation elements provided");
            return;
        }

        Set<Integer> reactedPlayerIds = new HashSet<>();

        // This callback runs inside the packet loop every N packets
        MidMovementCheck awarenessCheck = (bot) -> {
            List<org.gms.client.Character> nearbyPlayers = BotLogic.getRealPlayersInRange(
                    bot, detectWidth, detectHeight);

            for (org.gms.client.Character player : nearbyPlayers) {
                if (reactedPlayerIds.contains(player.getId())) {
                    continue;
                }

                PlayerReaction.ReactionType reaction = PlayerReaction.rollReaction(
                        ignoreWeight, stopWeight, walkWeight);

                switch (reaction) {
                    case IGNORE:
                        debugprint("PathFinderAware - Ignoring");
                        break;
                    case STOP_REACT:
                        debugprint("PathFinderAware - Stop_React");
                        PlayerSnapshot snapshot = PlayerReaction.executeStopReaction(bot, player);
                        reactedPlayerIds.add(player.getId());
                        debugprint("Bot " + bot.getName() + " stop-reacted to " + snapshot);
                        return true; // signal movement to stop
                    case WALK_REACT:
                        debugprint("PathFinderAware - Walk_React");
                        PlayerReaction.executeWalkReaction(bot);
                        reactedPlayerIds.add(player.getId());
                        break; // keep walking
                }
            }
            return false; // keep walking
        };

        executeElementsWithRepath(mapId, elements, awarenessCheck,
                detectWidth, detectHeight, ignoreWeight, stopWeight, walkWeight, reactedPlayerIds);
    }

    /**
     * Walks through nav elements. If a stop-react interrupts mid-walk,
     * re-pathfinds from the bot's current position to the original endpoint
     * and continues with the new path.
     */
    private void executeElementsWithRepath(int mapId, List<NavigationElement> elements,
                                            MidMovementCheck awarenessCheck,
                                            int detectWidth, int detectHeight,
                                            int ignoreWeight, int stopWeight, int walkWeight,
                                            Set<Integer> reactedPlayerIds) {
        for (int i = 0; i < elements.size(); i++) {
            NavigationElement currentElement = elements.get(i);
            NavigationElement nextElement = (i < elements.size() - 1) ? elements.get(i + 1) : null;

            try {
                boolean interrupted = executeElementAware(mapId, currentElement, nextElement, awarenessCheck);
                if (interrupted) {
                    // Stop-react happened — re-pathfind from current position to original endpoint
                    Point currentPos = this.character.getPosition();
                    debugprint("Re-pathfinding from " + currentPos + " to " + this.endPt);
                    List<String> newPath = createPath(mapId, currentPos, this.endPt, PathFinder.PathType.RANDOM);
                    List<NavigationElement> newElements = getNavElements(mapId, newPath);

                    // Recurse with the fresh path (reactedPlayerIds carries over)
                    executeElementsWithRepath(mapId, newElements, awarenessCheck,
                            detectWidth, detectHeight, ignoreWeight, stopWeight, walkWeight, reactedPlayerIds);
                    return; // the recursive call handled the rest
                }
            } catch (Exception e) {
                debugprint(e);
                debugprint("Failed to execute navigation element at index: " + currentElement.getRecordingName());
            }
        }
    }

    private boolean executeElementAware(int mapId, NavigationElement currentElement,
                                         NavigationElement nextElement, MidMovementCheck midCheck) {
        if (currentElement instanceof MainArea) {
            return executeMainAreaAware(mapId, (MainArea) currentElement, nextElement, midCheck);
        } else if (currentElement instanceof Connector) {
            return executeConnectorAware((Connector) currentElement, midCheck);
        } else {
            debugprint("Unknown navigation element type: {}");
            return false;
        }
    }

    private boolean executeMainAreaAware(int mapId, MainArea mainArea,
                                          NavigationElement nextElement, MidMovementCheck midCheck) {
        MovementRecording finalPath;

        if (nextElement == null) {
            finalPath = getPathBetweenTwoPointsInMainArea(
                    mapId, mainArea.getRecordingName(),
                    this.character.getPosition(), this.endPt);
        } else if (nextElement instanceof Connector) {
            Connector connector = (Connector) nextElement;
            finalPath = getPathBetweenTwoPointsInMainArea(
                    mapId, mainArea.getRecordingName(),
                    this.character.getPosition(), connector.getStartPosition());
        } else {
            debugprint("Unexpected element type after MainArea: {}");
            return false;
        }

        return executeMovement(this.character, finalPath, midCheck);
    }

    private boolean executeConnectorAware(Connector connector, MidMovementCheck midCheck) {
        MovementRecording finalPath = connector.getMovementRecording();
        return executeMovement(this.character, finalPath, midCheck);
    }

    private boolean executeElement(int mapId, NavigationElement currentElement, NavigationElement nextElement) {
        if (currentElement instanceof MainArea) {
            return executeMainArea(mapId, (MainArea) currentElement, nextElement);
        } else if (currentElement instanceof Connector) {
            return executeConnector((Connector) currentElement);
        } else {
            debugprint("Unknown navigation element type: {}");
            return false;
        }
    }

    private boolean executeMainArea(int mapId, MainArea mainArea, NavigationElement nextElement) {
        MovementRecording finalPath;

        if (nextElement == null) {
            finalPath = getPathBetweenTwoPointsInMainArea(
                    mapId,
                    mainArea.getRecordingName(),
                    this.character.getPosition(),
                    this.endPt
            );


        } else if (nextElement instanceof Connector) {
            // Calculate path to connector
            Connector connector = (Connector) nextElement;
            Point startPoint = this.character.getPosition();
            Point endPoint = connector.getStartPosition();

            finalPath = getPathBetweenTwoPointsInMainArea(
                    mapId,
                    mainArea.getRecordingName(),
                    startPoint,
                    endPoint
            );
        } else {
            debugprint("Unexpected element type after MainArea: {}");
            return false;
        }

        return executeMovement(this.character, finalPath);
    }

    private boolean executeConnector(Connector connector) {
        MovementRecording finalPath = connector.getMovementRecording();
        return executeMovement(this.character, finalPath);
    }

    public static List<NavigationElement> getNavElements(int mapId, List<String> path) {
        List<NavigationElement> elements = new ArrayList<>();
        for (String ele : path) {
            elements.add(parseNavigationType(mapId, ele));
        }
        return elements;
    }

    public static void main(String[] args) {
//        PathFinder pf = new PathFinder();
        int mapId = 100040000; // char
        Point start = new Point(-1272, 1993); // char curr position

        Point end = new Point(352, 456);

        List<String> path = createPath(mapId, start, end, PathType.RANDOM);

        List<NavigationElement> elements = getNavElements(mapId, path);

//        executePath(mapId, elements);
    }

    // todo move out
    public static MovementRecording getPathBetweenTwoPointsInMainArea(int mapId, String recName, Point start, Point end) {
        MovementRecordingRaw mvRaw = getMovementRecordingRaw(mapId, recName);
        StartEndPair startEndPair = mvRaw.getStartEndTimeCoordPairs(start, end);
        return mvRaw.trimRecording2(startEndPair.getStartTimestamp(), startEndPair.getEndTimestamp());
    }

    // ----- Aerial pathing (OPQ cloud reactors etc.) -----
    // Targets sitting "in the air" above platforms have a Y that doesn't match any
    // recorded ground path point. We project the airborne target onto the closest
    // recorded ground point (smallest |dx| within a Y tolerance), then delegate to
    // the normal ground-based path builder. The bot walks under the target; whatever
    // vertical interaction the caller needs (jump/attack) is handled separately.

    public static final int DEFAULT_AERIAL_Y_TOLERANCE = 100;

    public static class AerialPathResult {
        private final Point snappedPoint;
        private final List<TimeCoordinatePair> groundedPath;
        private final int yDelta;

        public AerialPathResult(Point snappedPoint, List<TimeCoordinatePair> groundedPath, int yDelta) {
            this.snappedPoint = snappedPoint;
            this.groundedPath = groundedPath;
            this.yDelta = yDelta;
        }

        public Point getSnappedPoint() { return snappedPoint; }
        public List<TimeCoordinatePair> getGroundedPath() { return groundedPath; }
        public int getYDelta() { return yDelta; }
    }

    public static AerialPathResult coordPathBuilderAerial(Character fakechar, Point airTarget) {
        return coordPathBuilderAerial(fakechar, airTarget, getRandomPathType(), DEFAULT_AERIAL_Y_TOLERANCE);
    }

    public static AerialPathResult coordPathBuilderAerial(Character fakechar, Point airTarget, PathType pathType) {
        return coordPathBuilderAerial(fakechar, airTarget, pathType, DEFAULT_AERIAL_Y_TOLERANCE);
    }

    public static AerialPathResult coordPathBuilderAerial(Character fakechar, Point airTarget, PathType pathType, int yTolerance) {
        int mapId = fakechar.getMapId();
        Point snapped = snapToGround(mapId, airTarget, yTolerance);
        if (snapped == null) {
            debugprint("coordPathBuilderAerial: no ground point within yTolerance=" + yTolerance + " for target " + airTarget);
            return null;
        }
        List<TimeCoordinatePair> tcp = coordPathBuilder(fakechar, snapped, pathType);
        return new AerialPathResult(snapped, tcp, airTarget.y - snapped.y);
    }

    public static Point snapToGround(int mapId, Point airTarget) {
        return snapToGround(mapId, airTarget, DEFAULT_AERIAL_Y_TOLERANCE);
    }

    // Scans every MainArea recording and returns the recorded Point with the
    // smallest horizontal distance to airTarget, gated by |dy| <= yTolerance.
    // Math.abs handles MapleStory's negative-coordinate maps without special casing.
    public static Point snapToGround(int mapId, Point airTarget, int yTolerance) {
        MapGraph gr = new MapGraph(mapId);
        Point best = null;
        int bestDx = Integer.MAX_VALUE;

        for (String area : gr.getMainAreas()) {
            try {
                MovementRecordingRaw raw = getMovementRecordingRaw(mapId, area);
                if (raw == null) continue;
                for (TimeCoordinatePair pair : raw.getTimeCoordPath()) {
                    for (Point candidate : pair.getPoints()) {
                        int dy = Math.abs(airTarget.y - candidate.y);
                        if (dy > yTolerance) continue;
                        int dx = Math.abs(airTarget.x - candidate.x);
                        if (dx < bestDx) {
                            bestDx = dx;
                            best = candidate;
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("snapToGround: error reading area " + area + ": " + e);
            }
        }
        return best;
    }

}

