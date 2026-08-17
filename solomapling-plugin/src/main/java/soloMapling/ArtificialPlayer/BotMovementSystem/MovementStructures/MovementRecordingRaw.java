package soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static soloMapling.ArtificialPlayer.BotLogic.isPointNear;
import static soloMapling.ArtificialPlayer.BotLogic.isPointNearSameY;
import static soloMapling.ArtificialPlayer.BotMovementSystem.InPacketReader.getMovementRecording;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.SingleMoveCommand.endsInJumpingStance;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.SingleMoveCommand.endsInStandingStance;
import static soloMapling.DebugUtilities.debugprint;

/*
    Full Raw Recording of a movement packet list.
    Can be created by using "startrecording" movement command
    Useful for determining the raw trajectory of player's x/y coordinates without needing to parse data from hex
 */

public class MovementRecordingRaw {

    private final int mapId;
    private final String recordingName;
    private final List<MovementPacketRaw> movementPacketList;
    private List<TimeCoordinatePair> timeCoordPath = null;

    public MovementRecordingRaw(int mapId, String recordingName, List<MovementPacketRaw> movementPacketList) {
        this.mapId = mapId;
        this.recordingName = recordingName;
        this.movementPacketList = movementPacketList;
    }

    public int getMapId() {
        return this.mapId;
    }

    public String getRecordingName() {
        return this.recordingName;
    }

    public List<MovementPacketRaw> getMovementPacketList() {
        return this.movementPacketList;
    }

    /*
    Some Movement Packets are recorded in a way where stand still stances are considered "Intersection" points
    This method can be used to quickly get those intersection point for Navigation Route calculations
     */
    public List<TimeCoordinatePair> getPacketsWithSelectStances(List<Byte> stances) {
        List<TimeCoordinatePair> packetsWithStances = new ArrayList<>();
        for (MovementPacketRaw rawPacket : getMovementPacketList()) {
            for (SingleMoveCommand comm : rawPacket.getRecordList()) {
                if (stances.contains(comm.getNewstate())) {
                    packetsWithStances.add(rawPacket.getTimeCoordinatePair());
                }
            }
        }
        return packetsWithStances;
    }

    public MovementPacketRaw getLastPacket() {
        return getMovementPacketList().getLast();
    }

    public MovementPacketRaw getFirstPacket() {
        return getMovementPacketList().getFirst();
    }

    public Point getEndPoint() {
        return getLastPacket().getRecordList().getLast().getPoint();
    }

    public Point getStartPoint() {
        return getFirstPacket().getRecordList().getFirst().getPoint();
    }

    public Long getEndPointTimestamp() {
        return getLastPacket().getTimestamp();
    }

    public List<TimeCoordinatePair> getTimeCoordPath() {
        if (timeCoordPath == null) {
            setTimeCoordPath();
        }
        return timeCoordPath;
    }

    public void setTimeCoordPath() {
        timeCoordPath = new ArrayList<>();
        for (MovementPacketRaw rawPacket : getMovementPacketList()) {
            timeCoordPath.add(rawPacket.getTimeCoordinatePair());
        }
    }

    public long getRecordingDuration() {
        return getMovementPacketList().getLast().getTimestamp() -
                getMovementPacketList().getFirst().getTimestamp();
    }

//    public List<TimeCoordinatePair> getTimeCoordPairsAtPoint(Point myPoint) {
//        List<TimeCoordinatePair> tcp = getTimeCoordPairsAtPoint(myPoint, 15);
//        if (tcp != null) {
//            return tcp;
//        } else {
//            tcp = getTimeCoordPairsAtPoint(myPoint, 25);
//            if (tcp != null) {
//                return tcp;
//            } else {
//                tcp = getTimeCoordPairsAtPoint(myPoint, 35);
//                if (tcp != null) {
//                    return tcp;
//                }
//            }
//        }
//        return new ArrayList<>();
//    }

    public List<TimeCoordinatePair> getTimeCoordPairsAtPoint(Point myPoint) {
        // Define search distances in ascending order
        int[] searchDistances = {40}; // {15,25,35} // if smaller, coordinates need to be tighter, might skip over

        // Try each distance until we find a result
        for (int distance : searchDistances) {
            List<TimeCoordinatePair> pairs = getTimeCoordPairsAtPoint(myPoint, distance);
            if (pairs != null && !pairs.isEmpty()) {
                return pairs;
            }
        }

        // Return empty list if no results found at any distance
//        debugprint("ERROR: getTimeCoordPairsAtPoint not found: " + myPoint);
        return new ArrayList<>();
    }

    /*
    Helper function to prevent bot movement ending in a non-standing state (such as jumping or walking)
    Unused
     */
//    public List<TimeCoordinatePair> trimTCPairsForNonStandingStances(List<TimeCoordinatePair> tcPair) {
//        // Iterate backwards to find the last index with standing stance
//        for (int i = tcPair.size() - 1; i >= 0; i--) {
//            if (endsInJumpingStance(tcPair.get(i).getStances())) {
//                // Found a standing stance, return everything up to and including this index
//                debugprint("Trimmed list ends in idle stance");
//                return tcPair.subList(0, i + 1);
//            }
//        }
//        // If no standing stance found at all, return default
//        debugprint("no standing stance found. returning default");
//        return tcPair;
//    }

    // Old getTimeCoordPairs
    // Modified to try and prevent stopping mid-jump.
    // Need to just make the recordings stop at platform edges
    public List<TimeCoordinatePair> getTimeCoordPairsAtPoint(Point myPoint, int maxDistance) {
        int seekCount = 0;
        final int MAX_SEEK = 3; // limit how far behind we look

        List<TimeCoordinatePair> timePointsInList = new ArrayList<>();
        for (TimeCoordinatePair pair : getTimeCoordPath()) {
            for (Point pts : pair.getPoints()) {
                if (isPointNearSameY(myPoint, pts, maxDistance, 5)) {
                    if (endsInJumpingStance(pair.getStances())) {
                        // check behind for a suitable packet if it ends in jump
                        for (int x = 1; x < MAX_SEEK; x++) {
                            TimeCoordinatePair tcp = getTimeCoordPath().get(seekCount - x);
                            if (!endsInJumpingStance(tcp.getStances())) {
                                timePointsInList.add(pair);
                                break;
                            }
                        }
                    } else {
                        timePointsInList.add(pair);
                    }
                }
            }
            seekCount += 1;
        }
//        for (TimeCoordinatePair tcp : timePointsInList) {
////            debugprint("tcp: ", tcp.getTimestamp(), tcp.getPoints(), tcp.getStances());
//        }
        if (timePointsInList.isEmpty()) {
            return null;
        }
        return timePointsInList;
    }


    public boolean arePointsWithinRecording(Point startPt, Point endPt) {
        return !getTimeCoordPairsAtPoint(startPt).isEmpty() &&
                !getTimeCoordPairsAtPoint(endPt).isEmpty();
    }

    public boolean isPointWithinRecording(Point pt) {
        return !getTimeCoordPairsAtPoint(pt).isEmpty();
    }

    public StartEndPair getStartEndTimeCoordPairs(Point startPt, Point endPt) {
        List<TimeCoordinatePair> startPairs = getTimeCoordPairsAtPoint(startPt);
        List<TimeCoordinatePair> endPairs = getTimeCoordPairsAtPoint(endPt);
//        endPairs = trimTCPairsForNonStandingStances(endPairs); //
        if (startPairs.isEmpty() && endPairs.isEmpty()) {
            debugprint("ERROR startPairs && endPairs Empty");
            return null;
        }
        if (startPairs.isEmpty() || endPairs.isEmpty()) {
            debugprint("ERROR startPairs OR endPairs Empty");
            return null;
        }

        TimeCoordinatePair latestValidStart = null;
        TimeCoordinatePair validEnd = null;

        // Iterate through all end pairs to find the valid pairs
        for (TimeCoordinatePair endPair : endPairs) {
            for (TimeCoordinatePair startPair : startPairs) {
                if (startPair.getTimestamp() <= endPair.getTimestamp()) {
                    // Update the latest valid start pair if necessary
                    if (latestValidStart == null || startPair.getTimestamp() > latestValidStart.getTimestamp()) {
                        latestValidStart = startPair;
                        validEnd = endPair; // Corresponding end pair
                    }
                }
            }
        }

        if (latestValidStart == null) {
            debugprint("ERROR no valid pair found");
            return null; // No valid pair found
        }
        return new StartEndPair(latestValidStart, validEnd);
    }

    public MovementRecordingRaw trimRecordingRaw(long startTimestamp, long endTimestamp) {
        List<MovementPacketRaw> trimmedList = new ArrayList<>();

        for (MovementPacketRaw packet : getMovementPacketList()) {
            long packetTimestamp = packet.getTimestamp();
            if (packetTimestamp >= startTimestamp && packetTimestamp <= endTimestamp) {
                trimmedList.add(packet);
            }
        }
        return new MovementRecordingRaw(getMapId(), getRecordingName(), trimmedList);
    }

    public MovementRecording trimRecording2(long startTimestamp, long endTimestamp) {
        MovementRecording mvr = getMovementRecording(getMapId(), getRecordingName());
        List<MovementPacket> trimmedList = new ArrayList<>();

        for (MovementPacket packet : mvr.getMovementPacketList()) {
            long packetTimestamp = packet.getTimestamp();
            if (packetTimestamp >= startTimestamp && packetTimestamp <= endTimestamp) {
                trimmedList.add(packet);
            }
        }
        return new MovementRecording(getMapId(), getRecordingName(), trimmedList);
    }

}
