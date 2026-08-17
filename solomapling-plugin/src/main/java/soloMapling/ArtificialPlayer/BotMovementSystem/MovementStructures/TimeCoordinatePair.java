package soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures;

import java.awt.*;
import java.util.List;

public class TimeCoordinatePair {
    private final long timestamp;
    private final List<Point> points;
    private final List<Byte> stances;

    public TimeCoordinatePair(long timestamp, List<Point> points, List<Byte> stances) {
        this.timestamp = timestamp;
        this.points = points;
        this.stances = stances;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public List<Point> getPoints() {
        return this.points;
    }

    public List<Byte> getStances() {
        return this.stances;
    }
}