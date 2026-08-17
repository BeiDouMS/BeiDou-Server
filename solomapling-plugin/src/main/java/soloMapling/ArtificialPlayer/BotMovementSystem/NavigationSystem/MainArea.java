package soloMapling.ArtificialPlayer.BotMovementSystem.NavigationSystem;


import java.util.List;

public class MainArea extends NavigationElement {
    private List<Connector> connectors;

    public MainArea(int mapId, String recordingName) {
        this.mapId = mapId;
        this.recordingName = recordingName;
    }

    public List<Connector> getConnectors() {
        return connectors;
    }
}
