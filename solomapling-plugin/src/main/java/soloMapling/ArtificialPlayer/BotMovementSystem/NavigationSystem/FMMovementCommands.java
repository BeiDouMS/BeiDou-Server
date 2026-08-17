package soloMapling.ArtificialPlayer.BotMovementSystem.NavigationSystem;

import org.gms.client.Character;

import java.awt.*;
import java.util.List;

import static soloMapling.ArtificialPlayer.BotCommandsPack.WarpCommands.getFMEntrancePortal;
import static soloMapling.ArtificialPlayer.BotLogic.isPointNear;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands.BotMoveSmallDistanceX;
import static soloMapling.ArtificialPlayer.BotMovementSystem.NavigationSystem.PathFinder.createPath;
import static soloMapling.ArtificialPlayer.BotMovementSystem.NavigationSystem.PathFinder.getNavElements;

public class FMMovementCommands {
//
//    private static int getRowByDoor(int door) {
//        if (door >= 1 && door <= 6) return 1;
//        if (door >= 7 && door <= 12) return 2;
//        if (door >= 13 && door <= 17) return 3;
//        if (door >= 18 && door <= 22) return 4;
//
//        if (door == -1) return 1; // arrows
//        if (door == -2) return 2;
//        if (door == -3) return 3;
//
//        throw new IllegalArgumentException("Invalid door number: " + door);
//    }
//
//    private static int getUpArrowByDoor(int door) {
//        if (door >= 1 && door <= 6) return -1;
//        if (door >= 7 && door <= 12) return -2;
//        if (door >= 13 && door <= 17) return -3;
//        if (door >= 18 && door <= 22) return 18; // since there isn't an "arrow4", we're using door 18 as the "Start"
//        throw new IllegalArgumentException("Invalid door number: " + door);
//    }
//
    public static Point getDoorPoint(int door) {
        return getDoorPosition(door);
    }

    public static Point getDoorPosition(int door) {
//        if (door < 0) {
//            return getFMEUpArrows(negDoorNumToArrowNum(door)).getPosition();
//        }
        return getFMEntrancePortal(door).getPosition();
    }
//
//    private static int getClosestDoor(Point pos) {
//        int minDistanceSquared = Integer.MAX_VALUE;
//        int closestDoor = -1;
//
//        for (int door = 1; door <= 22; door++) {
//            Point portalPos = getFMEntrancePortal(door).getPosition();
//            int dx = pos.x - portalPos.x;
//            int dy = pos.y - portalPos.y;
//            int distanceSquared = dx * dx + dy * dy;
//
//            if (distanceSquared < minDistanceSquared) {
//                minDistanceSquared = distanceSquared;
//                closestDoor = door;
//            }
//        }
//
//        return closestDoor;
//    }
//
//    // move general
//
//    public static void moveToEndDoor(Character fakechar, int endDoor) {
//        int closestDoor = getClosestDoor(fakechar.getPosition());
//        moveBetweenTwoFMDoors(fakechar, closestDoor, endDoor);
//    }
//

    public static void fmPathFinder(Character fakechar, Point endPt) {
        int mapId = fakechar.getMapId();
        Point start = fakechar.getPosition();
        PathFinder pf = new PathFinder(fakechar, endPt);
        List<String> path = createPath(mapId, start, endPt, PathFinder.PathType.RANDOM);
        List<NavigationElement> elements = getNavElements(mapId, path);
        pf.executePath(mapId, elements);
    }

    public static void moveToFMDoor(Character fakechar, int endDoor) {
        if (endDoor < 1 || endDoor > 22) {
            if (endDoor == 0) {
                throw new IllegalArgumentException("Doors must be between 1 and 22.");
            }
        }

        Point endPt = getDoorPosition(endDoor);
        fmPathFinder(fakechar, endPt);
        if (!isPointNear(fakechar.getPosition(), getDoorPosition(endDoor), 20)) {
            BotMoveSmallDistanceX(fakechar, getDoorPosition(endDoor));
        }
    }
//
//    // move between doors same row
//
//    public static MovementRecordingRaw getMovementRecordingFmDoors(int startDoor, int endDoor) {
//        int rangeEnd = getRowByDoor(endDoor);
//        return getMovementRecordingFmRows(rangeEnd);
//    }
//
//    public static MovementRecordingRaw getMovementRecordingFmRows(int row) {
//        if (row < 1 || row > 4) {
//            return null; // Handle invalid row values
//        }
//        String key = "fmerow" + row; // Dynamically generate the key
//        return getMovementRecordingRaw(FM_ENTRANCE, key);
//    }
//
//    public static MovementRecording getPathBetweenTwoDoorsSameRow(int startDoor, int endDoor) {
//        Point start = getDoorPosition(startDoor);
//        Point end = getDoorPosition(endDoor);
//
//        MovementRecordingRaw mvRaw = getMovementRecordingFmDoors(startDoor, endDoor);
//        StartEndPair startEndPair = mvRaw.getStartEndTimeCoordPairs(start, end);
//
//        return mvRaw.trimRecording2(startEndPair.getStartTimestamp(), startEndPair.getEndTimestamp());
//    }
//
//    public static void MoveBetweenTwoDoorsSameRow(Character fakechar, int startDoor, int endDoor) {
//        MovementRecording finalPath = getPathBetweenTwoDoorsSameRow(startDoor, endDoor);
//        MovementCommands.executeMovement(fakechar, finalPath);
//        if (!isPointNear(fakechar.getPosition(), getDoorPosition(endDoor), 20)) {
//            System.out.println("Distance greater than 20. moving small distance. End Door: " + endDoor);
//            BotMoveSmallDistanceX(fakechar, getDoorPosition(endDoor));
//        }
//    }
//
//    // move up rows
//    private static final String UP_ARROW_RECORDINGS = "fmeuparrows";
//
//    public static MovementRecording getPathBetweenFmRowsUp(int startRow, int endRow) {
//        List<Point> pts = getArrowPoints(startRow, endRow);
//        MovementRecordingRaw mvraw = getMovementRecordingRaw(FM_ENTRANCE, UP_ARROW_RECORDINGS);
//        StartEndPair p = mvraw.getStartEndTimeCoordPairs(pts.getFirst(), pts.getLast());
//        MovementRecording finalPath = mvraw.trimRecording2(p.getStartTimestamp(), p.getEndTimestamp());
//        return finalPath;
//    }
//
//    public static void moveBetweenFMRowsUp(Character fakechar, int startRow, int endRow) {
//        if (startRow >= endRow) {
//            return;
//        }
//        MovementRecording finalPath = getPathBetweenFmRowsUp(startRow, endRow);
//        MovementCommands.executeMovement(fakechar, finalPath);
//    }
//
////    public static List<Point> getArrowPoints(int startRow, int endRow) {
////        Point end;
////        int arrow1 = startRow - 1; // due to arrows starting from 0-2 (rows 1-3)
////        int arrow2 = endRow - 1;
////
////        Point start = getFMEUpArrows(arrow1).getPosition();
////        if (arrow2 == 3) { // Technically no "Arrow 3"
////            end = getDoorPosition(18);
////        } else {
////            end = getFMEUpArrows(arrow2).getPosition();
////        }
////        return List.of(start, end);
////    }
//
//    public static List<Point> getArrowPoints(int startRow, int endRow) {
//        Point start = getFMEUpArrows(startRow - 1).getPosition(); // Rows start from 1, arrows start from 0
//        Point end = (endRow == 4) ? getDoorPosition(18) : getFMEUpArrows(endRow - 1).getPosition();
//        return List.of(start, end);
//    }
//
//    private static final Map<Integer, Integer> FMEArrowPortalHash = new HashMap<>();
//
//    static {
//        // Initialize the map with the fixed mappings
//        FMEArrowPortalHash.put(-1, 0); // -1 door to Arrow 0
//        FMEArrowPortalHash.put(-2, 1);
//        FMEArrowPortalHash.put(-3, 2);
//    }
//
//    public static int negDoorNumToArrowNum(int number) {
//        if (!FMEArrowPortalHash.containsKey(number)) {
//            throw new IllegalArgumentException("Input must be -1, -2, or -3.");
//        }
//        return FMEArrowPortalHash.get(number);
//    }
//
//    // move between different rows up
//
//    // need this to be chainable todo
//    public static void moveBetweenTwoDifferentRowsUp(Character fakechar, int startDoor, int endDoor) {
//        moveFromDoorToUpArrow(fakechar, startDoor);
//        moveBetweenFMRowsUp(fakechar, getRowByDoor(startDoor), getRowByDoor(endDoor));
//        moveFromUpArrowToDoor(fakechar, endDoor);
//    }
//
//    public static void moveFromDoorToUpArrow(Character fakechar, int startDoor) {
//        int upArrow = getUpArrowByDoor(startDoor);
//        MoveBetweenTwoDoorsSameRow(fakechar, startDoor, upArrow);
//    }
//
//    public static void moveFromUpArrowToDoor(Character fakechar, int endDoor) {
//        int upArrow = getUpArrowByDoor(endDoor);
//        MoveBetweenTwoDoorsSameRow(fakechar, upArrow, endDoor);
//    }
//
//    // different rows down
//    // todo downward row not very robust
//
//    public static void moveBetweenTwoDifferentRowsDown(Character fakechar, int startDoor, int endDoor) {
////        dropToLowerRow(fakechar);
////        navigateToNearestDropDoor(fakechar);
//        int dropDoor = getNearestDropDoor(startDoor);
//        moveBetweenTwoFMDoors(fakechar, startDoor, dropDoor);
//        dropDownRow(fakechar);
//        moveBetweenTwoFMDoors(fakechar, getClosestDoor(fakechar.getPosition()), endDoor);
//    }
//
//    public static void dropToLowerRow(Character fakechar) {
//        navigateToNearestDropDoor(fakechar);
//        dropDownRow(fakechar);
//    }
//
//    public static int getNearestDropDoor(Character fakechar) {
//        int closestDoorNumber = getClosestDoor(fakechar.getPosition());
//        return getNearestDropDoor(closestDoorNumber);
//    }
//
//    public static int getNearestDropDoor(int closestDoor) {
//        List<Integer> dropDoors;
//
//        if (closestDoor >= 1 && closestDoor <= 6) {
//            return 1; // Row 1, no action needed
//        } else if (closestDoor >= 7 && closestDoor <= 12) {
//            dropDoors = Arrays.asList(8, 10);
//        } else if (closestDoor >= 13 && closestDoor <= 17) {
//            dropDoors = Arrays.asList(13, 15);
//        } else { // Row 4: doors 18-22
//            dropDoors = Arrays.asList(19, 21);
//        }
//
//        int dropDoor = -1;
//        int minDistance = Integer.MAX_VALUE;
//
//        for (int door : dropDoors) {
//            int distance = Math.abs(closestDoor - door);
//            if (distance < minDistance) {
//                minDistance = distance;
//                dropDoor = door;
//            } else if (distance == minDistance) {
//                // In case of tie, choose either door
//                if (random.nextBoolean()) {
//                    dropDoor = door;
//                }
//            }
//        }
//
//        return dropDoor;
//    }
//
//    public static void navigateToNearestDropDoor(Character fakechar) {
//        int dropDoor = getNearestDropDoor(fakechar);
//        moveToEndDoor(fakechar, dropDoor);
//    }
//
//    public static String getDropDownName(int door) {
//        if (door == 8) return "fmedrop21a"; // 8 vs 10
//        if (door == 10) return "fmedrop21b";
//
//        if (door == 13) return "fmedrop32a"; // 13 vs 15
//        if (door == 15) return "fmedrop32b";
//
//        if (door == 19) return "fmedrop43a"; // 19 vs 21
//        if (door == 21) return "fmedrop43b";
//
//        if (door >= 7 && door <= 12) return "fmedrop12a";
//        if (door >= 13 && door <= 17) return "fmedrop32a";
//        if (door >= 18 && door <= 22) return "fmedrop43a";
//        return null;
//    }
//
//    public static void dropDownRow(Character fakechar) {
//        String fmedrop = getDropDownName(getClosestDoor(fakechar.getPosition()));
//        if (fmedrop == null) {
//            return;
//        }
//        MovementRecording mvr = getMovementRecording(910000000, fmedrop);
//        BotMoveStreamHelper(mvr, fakechar, false, null);
//    }
//
}
