package org.gms.server.maps;

/**
 * Represents a rope or ladder climbable object parsed from WZ ladderRope data.
 * isLadder: true when the WZ {@code l} field is 1 (ladder), false for rope.
 *
 * Part of the GCMoveSystem terrain model (GreenCat dynamic movement).
 */
public record Rope(int x, int y1, int y2, boolean isLadder) {
    /** Top of the rope (smaller y = higher on screen). */
    public int topY() { return Math.min(y1, y2); }
    /** Bottom of the rope (larger y = lower on screen). */
    public int bottomY() { return Math.max(y1, y2); }
}
