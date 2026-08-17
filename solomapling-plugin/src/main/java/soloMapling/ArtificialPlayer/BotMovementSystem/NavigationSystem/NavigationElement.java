package soloMapling.ArtificialPlayer.BotMovementSystem.NavigationSystem;

import soloMapling.ArtificialPlayer.BotMovementSystem.InPacketReader;
import soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.MovementRecording;
import soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.MovementRecordingRaw;

public abstract class NavigationElement {
    protected int mapId;
    protected String recordingName;

    public int getMapId() {
        return mapId;
    }

    public String getRecordingName() {
        return recordingName;
    }

    public MovementRecordingRaw getMovementRecordingRaw() {
        return InPacketReader.getMovementRecordingRaw(getMapId(), getRecordingName());
    }

    public MovementRecording getMovementRecording() {
        return InPacketReader.getMovementRecording(getMapId(), getRecordingName());
    }

    public static NavigationElement parseNavigationType(int mapId, String pathName) {
        // Validate input parameters
        if (mapId < 0 || pathName == null || pathName.isEmpty()) {
            throw new IllegalArgumentException("Invalid input parameters");
        }

        if (pathName.startsWith("m")) {
            return new MainArea(mapId, pathName);
        } else if (pathName.startsWith("c")) {
            return new Connector(mapId, pathName);
        } else {
            throw new IllegalArgumentException("Invalid pathName format: must start with 'm' or 'c'");
        }
    }

}