package soloMapling.Environment;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Handles spawning objects onto platforms with organic scattering.
 *
 * Supports two platform types:
 * - FLAT: Can spawn at any X within bounds, Y is constant
 * - SLOPED: Must spawn at recorded reference points (or nearby), Y varies with X
 *
 * Objects are distributed across the platform in a way that:
 * - Avoids clustering (finds gaps between existing objects)
 * - Maintains organic/natural feel (doesn't place at exact gap centers)
 * - Handles crowded platforms gracefully
 */
public class PlatformSpawner {

    private static final Random random = new Random();

    /** Minimum horizontal spacing between objects (can be adjusted) */
    private static final int MIN_SPACING = 30;

    /** Random offset range for organic placement (won't place at exact gap center) */
    private static final double OFFSET_VARIANCE = 0.3; // 30% variance from center

    /**
     * Finds a good unoccupied point on the platform, considering existing objects.
     * Automatically handles FLAT vs SLOPED platforms.
     *
     * @param platform         The platform
     * @param occupiedPositions List of currently occupied positions on this platform
     * @return A Point representing a good location that statistically is less occupied
     */
    public static Point findUnoccupiedPoint(Platform platform, List<Point> occupiedPositions) {
        if (platform.isSloped()) {
            return findUnoccupiedPointSloped(platform, occupiedPositions);
        } else {
            return findUnoccupiedPointFlat(platform, occupiedPositions);
        }
    }

    /**
     * Finds multiple unoccupied points at once for batch calculations.
     *
     * @param platform          The platform
     * @param occupiedPositions List of currently occupied positions
     * @param count             Number of unoccupied points to find
     * @return List of unoccupied Points
     */
    public static List<Point> findUnoccupiedPoints(Platform platform, List<Point> occupiedPositions, int count) {
        List<Point> unoccupiedPoints = new ArrayList<>();
        List<Point> allOccupied = new ArrayList<>(occupiedPositions != null ? occupiedPositions : Collections.emptyList());

        for (int i = 0; i < count; i++) {
            Point unocc = findUnoccupiedPoint(platform, allOccupied);
            unoccupiedPoints.add(unocc);
            allOccupied.add(unocc); // Consider this spot occupied for next iteration
        }

        return unoccupiedPoints;
    }

    // ========== FLAT PLATFORM ==========

    /**
     * Finds unoccupied point for FLAT platforms.
     * Can use any X within bounds, Y is constant.
     */
    private static Point findUnoccupiedPointFlat(Platform platform, List<Point> occupiedPositions) {
        if (occupiedPositions == null || occupiedPositions.isEmpty()) {
            return randomPointOnFlatPlatform(platform);
        }

        // Sort occupied positions by X coordinate
        List<Point> sorted = new ArrayList<>(occupiedPositions);
        sorted.sort(Comparator.comparingInt(p -> p.x));

        // Find all gaps (including edges)
        List<Gap> gaps = findGapsFlat(platform, sorted);

        if (gaps.isEmpty()) {
            return randomPointOnFlatPlatform(platform);
        }

        Gap selectedGap = selectGapWeighted(gaps);
        int spawnX = pickPositionInGap(selectedGap);

        return new Point(spawnX, platform.getBaseY());
    }

    /**
     * Generates a random point anywhere on a flat platform.
     */
    private static Point randomPointOnFlatPlatform(Platform platform) {
        int x = platform.getMinX() + random.nextInt(Math.max(1, platform.getWidth()));
        return new Point(x, platform.getBaseY());
    }

    /**
     * Finds gaps on a flat platform.
     */
    private static List<Gap> findGapsFlat(Platform platform, List<Point> sortedOccupied) {
        List<Gap> gaps = new ArrayList<>();

        int minX = platform.getMinX();
        int maxX = platform.getMaxX();

        if (!sortedOccupied.isEmpty()) {
            int firstX = sortedOccupied.get(0).x;
            if (firstX - minX > MIN_SPACING) {
                gaps.add(new Gap(minX, firstX));
            }
        }

        for (int i = 0; i < sortedOccupied.size() - 1; i++) {
            int leftX = sortedOccupied.get(i).x;
            int rightX = sortedOccupied.get(i + 1).x;
            int gapSize = rightX - leftX;

            if (gapSize > MIN_SPACING) {
                gaps.add(new Gap(leftX, rightX));
            }
        }

        if (!sortedOccupied.isEmpty()) {
            int lastX = sortedOccupied.get(sortedOccupied.size() - 1).x;
            if (maxX - lastX > MIN_SPACING) {
                gaps.add(new Gap(lastX, maxX));
            }
        }

        return gaps;
    }

    // ========== SLOPED PLATFORM ==========

    /**
     * Finds unoccupied point for SLOPED platforms.
     * Must use coordinates from the recorded reference points.
     */
    private static Point findUnoccupiedPointSloped(Platform platform, List<Point> occupiedPositions) {
        List<Point> referencePoints = platform.getSortedPoints();

        if (referencePoints.isEmpty()) {
            // Fallback if no reference points
            return new Point(platform.getMinX(), platform.getBaseY());
        }

        if (occupiedPositions == null || occupiedPositions.isEmpty()) {
            // Pick a random reference point
            return copyPoint(referencePoints.get(random.nextInt(referencePoints.size())));
        }

        // Find reference points that are not too close to occupied positions
        List<Point> availablePoints = findAvailableReferencePoints(referencePoints, occupiedPositions);

        if (availablePoints.isEmpty()) {
            // Everything is crowded - just pick a random reference point
            return copyPoint(referencePoints.get(random.nextInt(referencePoints.size())));
        }

        // Weight selection by distance from nearest occupied (prefer more isolated spots)
        return selectPointByIsolation(availablePoints, occupiedPositions);
    }

    /**
     * Finds reference points that are not too close to any occupied position.
     */
    private static List<Point> findAvailableReferencePoints(List<Point> referencePoints, List<Point> occupied) {
        List<Point> available = new ArrayList<>();

        for (Point ref : referencePoints) {
            boolean tooClose = false;
            for (Point occ : occupied) {
                if (Math.abs(ref.x - occ.x) < MIN_SPACING) {
                    tooClose = true;
                    break;
                }
            }
            if (!tooClose) {
                available.add(ref);
            }
        }

        return available;
    }

    /**
     * Selects a point that is relatively isolated from occupied positions.
     * Uses weighted random selection favoring more isolated spots.
     */
    private static Point selectPointByIsolation(List<Point> candidates, List<Point> occupied) {
        if (candidates.size() == 1) {
            return copyPoint(candidates.get(0));
        }

        // Calculate isolation score for each candidate (distance to nearest occupied)
        List<Integer> isolationScores = new ArrayList<>();
        int totalScore = 0;

        for (Point candidate : candidates) {
            int minDist = Integer.MAX_VALUE;
            for (Point occ : occupied) {
                int dist = Math.abs(candidate.x - occ.x);
                minDist = Math.min(minDist, dist);
            }
            // Use sqrt to reduce extreme weighting toward very isolated spots
            int score = (int) Math.sqrt(minDist);
            isolationScores.add(score);
            totalScore += score;
        }

        // Weighted random selection
        if (totalScore == 0) {
            return copyPoint(candidates.get(random.nextInt(candidates.size())));
        }

        int randomValue = random.nextInt(totalScore);
        int cumulative = 0;

        for (int i = 0; i < candidates.size(); i++) {
            cumulative += isolationScores.get(i);
            if (randomValue < cumulative) {
                return copyPoint(candidates.get(i));
            }
        }

        return copyPoint(candidates.get(candidates.size() - 1));
    }

    // ========== SHARED UTILITIES ==========

    /**
     * Selects a gap using weighted random selection.
     * Larger gaps are more likely to be selected, but smaller gaps still have a chance.
     */
    private static Gap selectGapWeighted(List<Gap> gaps) {
        if (gaps.size() == 1) {
            return gaps.get(0);
        }

        int totalWeight = gaps.stream().mapToInt(Gap::size).sum();
        int randomValue = random.nextInt(totalWeight);
        int cumulative = 0;

        for (Gap gap : gaps) {
            cumulative += gap.size();
            if (randomValue < cumulative) {
                return gap;
            }
        }

        return gaps.get(gaps.size() - 1);
    }

    /**
     * Picks a position within a gap with organic offset.
     */
    private static int pickPositionInGap(Gap gap) {
        int center = gap.center();
        int halfSize = gap.size() / 2;

        int maxOffset = (int) (halfSize * OFFSET_VARIANCE);
        int offset = maxOffset > 0 ? random.nextInt(maxOffset * 2) - maxOffset : 0;

        int padding = Math.min(MIN_SPACING / 2, gap.size() / 4);
        int result = center + offset;
        result = Math.max(gap.startX + padding, result);
        result = Math.min(gap.endX - padding, result);

        return result;
    }

    /**
     * Creates a copy of a Point (since Point is mutable).
     */
    private static Point copyPoint(Point p) {
        return new Point(p.x, p.y);
    }

    /**
     * Represents a gap between two objects (or between platform edge and object).
     */
    private static class Gap {
        final int startX;
        final int endX;

        Gap(int startX, int endX) {
            this.startX = startX;
            this.endX = endX;
        }

        int size() {
            return endX - startX;
        }

        int center() {
            return startX + (size() / 2);
        }
    }
}
