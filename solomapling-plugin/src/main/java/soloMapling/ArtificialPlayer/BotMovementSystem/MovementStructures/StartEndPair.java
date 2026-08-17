package soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures;

import java.awt.*;

public class StartEndPair {
    private final TimeCoordinatePair start;
    private final TimeCoordinatePair end;

    public StartEndPair(TimeCoordinatePair startPoint, TimeCoordinatePair endPoint) {
        this.start = startPoint;
        this.end = endPoint;
    }

    public TimeCoordinatePair getStart() {
        return start;
    }

    public TimeCoordinatePair getEnd() {
        return end;
    }

    public Point getStartPoint() {
        return getStart().getPoints().getFirst();
    }

    public Long getStartTimestamp() {
        return getStart().getTimestamp();
    }

    public Point getEndPoint() {
        return getEnd().getPoints().getLast();
    }

    public Long getEndTimestamp() {
        return getEnd().getTimestamp();
    }

}
