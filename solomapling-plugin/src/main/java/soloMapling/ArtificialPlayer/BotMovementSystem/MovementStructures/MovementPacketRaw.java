package soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/*
    Raw version of MovementPacket.
    The Packet is broken down to the number of Commands
    and each individual Packet Recording per packet
 */

public class MovementPacketRaw {

    private final long timestamp;
    private final int numCommands;
    private final List<SingleMoveCommand> recordList;

    public MovementPacketRaw(long timestamp, int numCommands, List<SingleMoveCommand> recordList) {
        this.timestamp = timestamp;
        this.numCommands = numCommands;
        this.recordList = recordList;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public int getNumCommands() {
        return this.numCommands;
    }

    public List<SingleMoveCommand> getRecordList() {
        return this.recordList;
    }

    public TimeCoordinatePair getTimeCoordinatePair() {
        List<Point> points = new ArrayList<>();
        for (SingleMoveCommand comm : getRecordList()) {
            points.add(comm.getPoint());
        }

        List<Byte> stances = new ArrayList<>();
        for (SingleMoveCommand comm2 : getRecordList()) {
            stances.add(comm2.getNewstate());
        }

        return new TimeCoordinatePair(getTimestamp(), points, stances);
    }



}
