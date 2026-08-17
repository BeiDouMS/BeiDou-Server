package soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures;

import java.util.List;

/*
    Full Recording of a movement packet list.
    Can be created by using "startrecording" movement command
    Useful for playing back packets without too much overhead / conversion.
    Contains full packet data for movement, including teleporting, crazy jumping, every thing.
 */

public class MovementRecording {

    private final int mapId;
    private final String recordingName;
    private final List<MovementPacket> movementPacketList;

    public MovementRecording(int mapId, String recordingName, List<MovementPacket> movementPacketList) {
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

    public List<MovementPacket> getMovementPacketList() {
        return this.movementPacketList;
    }

}
