package soloMapling.Environment;

import java.awt.Point;
import java.util.List;

/**
 * Represents a game platform derived from recorded movement data.
 *
 * Supports two modes:
 * - FLAT: Platform has a consistent Y level. Spawns can use any X within bounds.
 * - SLOPED: Platform has varying Y levels. Spawns must use coordinates from the recorded points.
 */
public class Platform {

    /**
     * Platform type determining how coordinates are handled.
     */
    public enum Type {
        /** Flat platform - Y is consistent, X can be any value within bounds */
        FLAT,
        /** Sloped platform - Y varies with X, must use recorded reference points */
        SLOPED
    }

    private final int minX;
    private final int maxX;
    private final int baseY; // For FLAT: the consistent Y. For SLOPED: average Y (informational only)
    private final List<Point> sortedPoints;
    private final Type type;

    public Platform(int minX, int maxX, int baseY, List<Point> sortedPoints, Type type) {
        this.minX = minX;
        this.maxX = maxX;
        this.baseY = baseY;
        this.sortedPoints = sortedPoints;
        this.type = type;
    }

    public int getMinX() {
        return minX;
    }

    public int getMaxX() {
        return maxX;
    }

    /**
     * For FLAT platforms: the consistent Y level.
     * For SLOPED platforms: the average Y (use getYAtX or sortedPoints for accurate positioning).
     */
    public int getBaseY() {
        return baseY;
    }

    /**
     * Returns the horizontal width of the platform.
     */
    public int getWidth() {
        return maxX - minX;
    }

    /**
     * Returns the sorted list of recorded points (sorted by X coordinate).
     * For SLOPED platforms, these are the valid spawn positions.
     * For FLAT platforms, these are reference points only.
     */
    public List<Point> getSortedPoints() {
        return sortedPoints;
    }

    public Type getType() {
        return type;
    }

    public boolean isFlat() {
        return type == Type.FLAT;
    }

    public boolean isSloped() {
        return type == Type.SLOPED;
    }

    /**
     * Estimates Y at a given X by interpolating between the two nearest recorded points.
     * Useful for SLOPED platforms when you need a Y at an arbitrary X.
     * For FLAT platforms, just returns baseY.
     */
    public int getYAtX(int x) {
        if (type == Type.FLAT) {
            return baseY;
        }

        if (sortedPoints.isEmpty()) {
            return baseY;
        }

        // Clamp x to bounds
        x = Math.max(minX, Math.min(maxX, x));

        // Find the two points surrounding x
        Point left = null;
        Point right = null;

        for (Point p : sortedPoints) {
            if (p.x <= x) {
                left = p;
            }
            if (p.x >= x && right == null) {
                right = p;
            }
        }

        // Edge cases
        if (left == null) return sortedPoints.get(0).y;
        if (right == null) return sortedPoints.get(sortedPoints.size() - 1).y;
        if (left.x == right.x) return left.y;

        // Linear interpolation
        double ratio = (double)(x - left.x) / (right.x - left.x);
        return (int)(left.y + ratio * (right.y - left.y));
    }

    @Override
    public String toString() {
        return String.format("Platform[type: %s, x: %d to %d, baseY: %d, width: %d, points: %d]",
                type, minX, maxX, baseY, getWidth(), sortedPoints.size());
    }
}
