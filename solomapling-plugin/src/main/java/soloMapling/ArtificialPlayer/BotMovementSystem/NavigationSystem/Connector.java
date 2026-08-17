package soloMapling.ArtificialPlayer.BotMovementSystem.NavigationSystem;

import soloMapling.ArtificialPlayer.BotMovementSystem.InPacketReader;
import soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.MovementRecordingRaw;

import java.awt.*;

import static soloMapling.ArtificialPlayer.BotMovementSystem.InPacketReader.getMovementRecordingRaw;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.MovementEnums.FreeMarketValues.FM_ENTRANCE;

public class Connector extends NavigationElement {
    private MainArea nextMainArea;
    private MainArea prevMainArea;
    private Point startPosition;
    private Point endPosition;

    public Connector(int mapId, String recordingName) {
        this.mapId = mapId;
        this.recordingName = recordingName;
    }

    public Point getStartPosition() {
        return getMovementRecordingRaw().getStartPoint();
    }

    public Point getEndPosition() {
        return getMovementRecordingRaw().getEndPoint();
    }

    public String getNextMainArea() {
        return "m" + extractEndNumber(recordingName);
    }

    public String getPrevMainArea() {
        return "m" + extractStartNumber(recordingName);
    }

    private int extractStartNumber(String recordingName) {
        String[] parts = recordingName.split("-");
        return Integer.parseInt(parts[0].substring(1)); // Remove 'c' and parse number
    }

    private int extractEndNumber(String recordingName) {
        String[] parts = recordingName.split("-");
        return Integer.parseInt(parts[1]); // Directly parse the second part
    }


}
