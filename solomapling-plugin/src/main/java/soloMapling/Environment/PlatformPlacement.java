package soloMapling.Environment;

import org.gms.client.Character;
import org.gms.server.maps.MapleMap;
import soloMapling.ArtificialPlayer.GCMoveSystem.GCMovement;
import soloMapling.ArtificialPlayer.BotGeneration;
import soloMapling.ArtificialPlayer.BotHelpers;
import soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands;
import soloMapling.server.ExecutorServiceManager;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static soloMapling.ArtificialPlayer.BotCustomization.getRandomChairId;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands.botSitChair;
import static soloMapling.ArtificialPlayer.BotGeneration.createBotPollReadiness;
import static soloMapling.ArtificialPlayer.BotMessagingSystem.CharacterStorage.getBotById;
import static soloMapling.DebugUtilities.debugprint;
import static soloMapling.DebugUtilities.fmt;
import static soloMapling.Environment.PlatformSpawner.findUnoccupiedPoint;
import static soloMapling.Environment.PlatformSpawner.findUnoccupiedPoints;
import static soloMapling.server.SoloMaplingUtilities.getMapleMapById;

// The platform-placement utility library extracted from EnvironmentManager: spawning bot batches
// onto recorded platforms (with unoccupied-spot selection and retry), the filler-bot line spawners,
// and the platform lookup/query helpers (which platform is a character on, which ids exist on a
// map). Used by the environment waves, several bot types (merchants, Henesys, OPQ), and the GM
// commands — it's placement mechanics, not world-startup logic, which is why it lives apart from
// the wave orchestration. Ours (SoloMapling).
public class PlatformPlacement {

    private static final String BASE_PATH = "src/main/java/soloMapling/ArtificialPlayer/BotMovementSystem/movementDataPackets";
    // Tolerance for Y-coordinate matching when determining if a character is on a platform;
    // characters within this vertical distance of the platform are considered "on" it.
    private static final int Y_TOLERANCE = 10;
    // Tolerance for X-coordinate proximity when checking if a character is near platform bounds
    // (buffer beyond the recorded min/max X values).
    private static final int X_TOLERANCE = 20;

    private static final Random random = new Random();

    public static List<Integer> spawnBotsOnMapOnPlatform(int numBots, int mapId, String platform_id) {
        Platform flatPlatform = PlatformParser.parsePlatform(mapId, platform_id);
        List<Point> occupied = Collections.synchronizedList(new ArrayList<>());
        ConcurrentLinkedQueue<Integer> characterIds = new ConcurrentLinkedQueue<>();
        AtomicInteger failureCount = new AtomicInteger(0);

        debugprint(fmt("Spawning {} bots on {} at platform: {}", numBots, mapId, platform_id));

        // Pre-generate all spawn points (must be sequential to avoid overlaps)
        List<Point> spawnPoints = new ArrayList<>();
        for (int i = 0; i < numBots; i++) {
            Point spawn = findUnoccupiedPoint(flatPlatform, occupied);
            occupied.add(spawn);
            spawnPoints.add(spawn);
        }

        // Use CountDownLatch to wait for all spawns to complete
        CountDownLatch latch = new CountDownLatch(numBots);

        for (Point spawn : spawnPoints) {
            ExecutorServiceManager.runAsync(() -> {
                try {
                    Character fakechar = createBotWithRetry(spawn, mapId, 5);
                    if (fakechar != null) {
                        characterIds.add(fakechar.getId());
                    } else {
                        failureCount.incrementAndGet();
                        debugprint(fmt("Failed to create bot at point {} after retries", spawn));
                    }
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    debugprint(fmt("Exception creating bot at {}: {}", spawn, e.getMessage()));
                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait for all spawns to complete with timeout
        try {
            boolean completed = latch.await(120, TimeUnit.SECONDS);
            if (!completed) {
                debugprint(fmt("Timeout waiting for bot spawns. Completed: {}/{}",
                        characterIds.size(), numBots));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            debugprint("Bot spawning interrupted");
        }

        if (failureCount.get() > 0) {
            debugprint(fmt("Spawning complete. Success: {}, Failed: {}",
                    characterIds.size(), failureCount.get()));
        }

        return new ArrayList<>(characterIds);
    }


    public static List<Integer> spawnBotsOnMapOnPlatformInRadius(int numBots, int mapId, String platform_id, Point center, int radius) {
        Platform flatPlatform = PlatformParser.parsePlatform(mapId, platform_id);
        List<Point> occupied = Collections.synchronizedList(new ArrayList<>());
        ConcurrentLinkedQueue<Integer> characterIds = new ConcurrentLinkedQueue<>();
        AtomicInteger failureCount = new AtomicInteger(0);

        debugprint(fmt("Spawning {} bots on {} at platform: {} within radius {} of ({},{})",
                numBots, mapId, platform_id, radius, center.x, center.y));

        // Pre-generate all spawn points within radius
        List<Point> spawnPoints = new ArrayList<>();
        for (int i = 0; i < numBots; i++) {
            Point spawn = findUnoccupiedPointInRadius(flatPlatform, occupied, center, radius);
            if (spawn == null) {
                debugprint(fmt("Could not find unoccupied point for bot {} within radius", i));
                continue;
            }
            occupied.add(spawn);
            spawnPoints.add(spawn);
        }

        CountDownLatch latch = new CountDownLatch(spawnPoints.size());

        for (Point spawn : spawnPoints) {
            ExecutorServiceManager.runAsync(() -> {
                try {
                    Character fakechar = createBotWithRetry(spawn, mapId, 5);
                    if (fakechar != null) {
                        characterIds.add(fakechar.getId());
                    } else {
                        failureCount.incrementAndGet();
                        debugprint(fmt("Failed to create bot at point {} after retries", spawn));
                    }
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    debugprint(fmt("Exception creating bot at {}: {}", spawn, e.getMessage()));
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            boolean completed = latch.await(120, TimeUnit.SECONDS);
            if (!completed) {
                debugprint(fmt("Timeout waiting for bot spawns. Completed: {}/{}",
                        characterIds.size(), spawnPoints.size()));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            debugprint("Bot spawning interrupted");
        }

        if (failureCount.get() > 0) {
            debugprint(fmt("Spawning complete. Success: {}, Failed: {}",
                    characterIds.size(), failureCount.get()));
        }

        return new ArrayList<>(characterIds);
    }

    public static List<Integer> spawnFillerBots(int numBots, int mapId, Point p1, Point p2) {
        int minX = Math.min(p1.x, p2.x);
        int maxX = Math.max(p1.x, p2.x);
        int baseY = p1.y;
        Platform adHocPlatform = new Platform(minX, maxX, baseY, List.of(p1, p2), Platform.Type.FLAT);

        List<Point> occupied = Collections.synchronizedList(new ArrayList<>());
        ConcurrentLinkedQueue<Integer> characterIds = new ConcurrentLinkedQueue<>();
        AtomicInteger failureCount = new AtomicInteger(0);

        debugprint(fmt("Spawning {} filler bots between ({},{}) and ({},{}) on map {}",
                numBots, p1.x, p1.y, p2.x, p2.y, mapId));

        List<Point> spawnPoints = findUnoccupiedPoints(adHocPlatform, occupied, numBots);
        occupied.addAll(spawnPoints);

        CountDownLatch latch = new CountDownLatch(spawnPoints.size());
        double chairChance = 0.20;

        for (Point spawn : spawnPoints) {
            ExecutorServiceManager.runAsync(() -> {
                try {
                    Character fakechar = createBotWithRetry(spawn, mapId, 5);
                    if (fakechar != null) {
                        characterIds.add(fakechar.getId());
                        if (Math.random() < chairChance) {
                            // Sit only after the spawn drop-down/turn-around finishes
                            ExecutorServiceManager.getScheduledExecutorService().schedule(
                                    () -> botSitChair(fakechar, getRandomChairId()),
                                    BotGeneration.SPAWN_CHOREOGRAPHY_MAX_MS + 500, TimeUnit.MILLISECONDS);
                        }
                    } else {
                        failureCount.incrementAndGet();
                        debugprint(fmt("Failed to create filler bot at point {} after retries", spawn));
                    }
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    debugprint(fmt("Exception creating filler bot at {}: {}", spawn, e.getMessage()));
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            boolean completed = latch.await(120, TimeUnit.SECONDS);
            if (!completed) {
                debugprint(fmt("Timeout waiting for filler bot spawns. Completed: {}/{}",
                        characterIds.size(), spawnPoints.size()));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            debugprint("Filler bot spawning interrupted");
        }

        if (failureCount.get() > 0) {
            debugprint(fmt("Filler spawning complete. Success: {}, Failed: {}",
                    characterIds.size(), failureCount.get()));
        }

        debugprint(fmt("Filler bots spawned: {}", characterIds.size()));
        return new ArrayList<>(characterIds);
    }

    public static List<Integer> spawnFillerBotsLockedY(int numBots, int mapId, Point p1, Point p2) {
        int minX = Math.min(p1.x, p2.x);
        int maxX = Math.max(p1.x, p2.x);
        int baseY = p1.y;
        Platform adHocPlatform = new Platform(minX, maxX, baseY, List.of(p1, p2), Platform.Type.FLAT);

        List<Point> occupied = Collections.synchronizedList(new ArrayList<>());
        ConcurrentLinkedQueue<Integer> characterIds = new ConcurrentLinkedQueue<>();
        AtomicInteger failureCount = new AtomicInteger(0);

        debugprint(fmt("Spawning {} filler bots (locked Y={}) between x={} and x={} on map {}",
                numBots, baseY, minX, maxX, mapId));

        List<Point> spawnPoints = findUnoccupiedPoints(adHocPlatform, occupied, numBots);
        for (Point sp : spawnPoints) {
            sp.y = baseY;
        }
        occupied.addAll(spawnPoints);

        CountDownLatch latch = new CountDownLatch(spawnPoints.size());
        double chairChance = 0.20;

        for (Point spawn : spawnPoints) {
            ExecutorServiceManager.runAsync(() -> {
                try {
                    Character fakechar = createBotWithRetry(spawn, mapId, 5);
                    if (fakechar != null) {
                        characterIds.add(fakechar.getId());
                        if (Math.random() < chairChance) {
                            // Sit only after the spawn drop-down/turn-around finishes
                            ExecutorServiceManager.getScheduledExecutorService().schedule(
                                    () -> botSitChair(fakechar, getRandomChairId()),
                                    BotGeneration.SPAWN_CHOREOGRAPHY_MAX_MS + 500, TimeUnit.MILLISECONDS);
                        }
                    } else {
                        failureCount.incrementAndGet();
                        debugprint(fmt("Failed to create filler bot at point {} after retries", spawn));
                    }
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    debugprint(fmt("Exception creating filler bot at {}: {}", spawn, e.getMessage()));
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            boolean completed = latch.await(120, TimeUnit.SECONDS);
            if (!completed) {
                debugprint(fmt("Timeout waiting for filler bot spawns. Completed: {}/{}",
                        characterIds.size(), spawnPoints.size()));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            debugprint("Filler bot spawning interrupted");
        }

        if (failureCount.get() > 0) {
            debugprint(fmt("Filler spawning complete. Success: {}, Failed: {}",
                    characterIds.size(), failureCount.get()));
        }

        debugprint(fmt("Filler bots spawned (locked Y): {}", characterIds.size()));
        return new ArrayList<>(characterIds);
    }

    private static Point findUnoccupiedPointInRadius(Platform platform, List<Point> occupied, Point center, int radius) {
        int maxAttempts = 100;
        for (int i = 0; i < maxAttempts; i++) {
            Point candidate = findUnoccupiedPoint(platform, occupied);
            if (Math.abs(candidate.x - center.x) <= radius && Math.abs(candidate.y - center.y) <= radius) {
                return candidate;
            }
        }
        return null;
    }


    public static Character createBotWithRetry(Point spawn, int mapId, int maxRetries) {
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                Character fakechar = createBotPollReadiness(spawn, mapId);
                if (fakechar != null) {
                    return fakechar;
                }
                // If null but no exception, brief pause before retry
                if (attempt < maxRetries) {
                    Thread.sleep(200 * attempt); // Exponential-ish backoff
                }
            } catch (Exception e) {
                debugprint(fmt("Attempt {}/{} failed for bot at {}: {}",
                        attempt, maxRetries, spawn, e.getMessage()));
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(200 * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return null;
                    }
                }
            }
        }
        return null;
    }

    public static void botMoveToPlatformAnyUnoccupiedSpot(Character fakechar, String platform) {
        int mapId = fakechar.getMapId();

        // todo verify platform name exists in mapId

        List<Point> occupiedPointsOnPlatform = getListOfCharacterCoordinates(getAllCharsOnPlatform(mapId, platform));
        Platform flatPlatform = PlatformParser.parsePlatform(mapId, platform);
        Point unoccupiedPt = findUnoccupiedPoint(flatPlatform, occupiedPointsOnPlatform);
        MovementCommands.pathFinderBeta(fakechar, unoccupiedPt);
    }

    public static void botMoveToPlatformAnyUnoccupiedSpotAware(Character fakechar, String platform) {
        int mapId = fakechar.getMapId();

        List<Point> occupiedPointsOnPlatform = getListOfCharacterCoordinates(getAllCharsOnPlatform(mapId, platform));
        Platform flatPlatform = PlatformParser.parsePlatform(mapId, platform);
        Point unoccupiedPt = findUnoccupiedPoint(flatPlatform, occupiedPointsOnPlatform);
        MovementCommands.pathFinderAware(fakechar, unoccupiedPt);
    }

    // Dynamic-engine sibling of botMoveToPlatformAnyUnoccupiedSpot: same occupancy-aware target
    // pick, but the walk runs on GCMovement, which lands on the exact pixel instead of a recorded
    // path's fixed endpoints (the cause of merchant bots stacking on "spots"). Skips while a
    // dynamic stroll is already in progress so FSM ticks can't thrash the target mid-walk.
    // Note: first use puts the bot under dynamic control for good — it holds the shared movement
    // lock, so recorded-path MovementCommands silently no-op for this bot from then on.
    public static void botMoveToPlatformAnyUnoccupiedSpotDynamic(Character fakechar, String platform) {
        if (fakechar == null || platform == null || GCMovement.isMoving(fakechar)) {
            return;
        }
        int mapId = fakechar.getMapId();
        List<Point> occupiedPointsOnPlatform = getListOfCharacterCoordinates(getAllCharsOnPlatform(mapId, platform));
        Platform flatPlatform = PlatformParser.parsePlatform(mapId, platform);
        Point unoccupiedPt = findUnoccupiedPoint(flatPlatform, occupiedPointsOnPlatform);
        if (unoccupiedPt == null) {
            return;
        }
        GCMovement.move(fakechar, unoccupiedPt.x, unoccupiedPt.y);
    }

    /**
     * Determines which platform a character is currently standing on.
     * <p>
     * Scans all platform CSV files for the character's map and finds the best match
     * based on position proximity. For flat platforms, checks if X is within bounds
     * and Y is close to baseY. For sloped platforms, uses interpolation to estimate
     * the expected Y at the character's X position.
     *
     * @param chr The character whose platform we want to find
     * @return The platform identifier (e.g., "m1", "m2") or null if not on any known platform
     */
    public static String getCurrentPlatform(Character chr) {
        Point currentPosition = chr.getPosition();
        int mapId = chr.getMapId();
        String platform = findPlatformAtPosition(mapId, currentPosition);
        //debugprint("MapID: ", mapId, "Platform: ", platform);
        return platform;
    }

    /**
     * Finds which platform a given position belongs to on a specific map.
     *
     * @param mapId    The map ID to search
     * @param position The position to check
     * @return The platform identifier or null if not found
     */
    public static String findPlatformAtPosition(int mapId, Point position) {
        List<String> platformIds = getAvailablePlatformIds(mapId);

        if (platformIds.isEmpty()) {
            return null;
        }

        String bestMatch = null;
        int bestScore = Integer.MAX_VALUE;

        for (String platformId : platformIds) {
            Platform platform = PlatformParser.parsePlatform(mapId, platformId);

            if (platform == null || platform.getSortedPoints().isEmpty()) {
                continue;
            }

            int score = calculatePlatformMatchScore(platform, position);

            // Score of -1 means position is definitely not on this platform
            if (score >= 0 && score < bestScore) {
                bestScore = score;
                bestMatch = platformId;
            }
        }

        return bestMatch;
    }

    /**
     * Calculates how well a position matches a platform.
     * Lower score = better match. Returns -1 if position is definitely not on the platform.
     *
     * @param platform The platform to check against
     * @param position The position to evaluate
     * @return Match score (lower is better) or -1 if not a match
     */
    private static int calculatePlatformMatchScore(Platform platform, Point position) {
        int x = position.x;
        int y = position.y;

        // Check if X is within platform bounds (with tolerance)
        if (x < platform.getMinX() - X_TOLERANCE || x > platform.getMaxX() + X_TOLERANCE) {
            return -1; // Definitely not on this platform
        }

        // Get the expected Y at this X position
        int expectedY = platform.getYAtX(x);
        int yDifference = Math.abs(y - expectedY);

        // If Y difference is too large, not on this platform
        if (yDifference > Y_TOLERANCE) {
            return -1;
        }

        // Score is based on how close we are to the expected Y
        // Also factor in how well we're within the X bounds (prefer being solidly within bounds)
        int xDistanceFromCenter = Math.abs(x - (platform.getMinX() + platform.getMaxX()) / 2);

        // Combined score: Y accuracy is more important, X centering is secondary
        return yDifference * 10 + (xDistanceFromCenter / 10);
    }

    /**
     * Gets all available platform IDs for a given map by scanning the directory.
     *
     * @param mapId The map ID
     * @return List of platform IDs (e.g., ["m1", "m2", "m3"])
     */
    public static List<String> getAvailablePlatformIds(int mapId) {
        List<String> platformIds = new ArrayList<>();
        File mapDir = new File(BASE_PATH + "/map" + mapId);

        if (!mapDir.exists() || !mapDir.isDirectory()) {
            return platformIds;
        }

        File[] files = mapDir.listFiles((dir, name) -> name.endsWith(".csv"));

        if (files != null) {
            for (File file : files) {
                String name = file.getName();
                // Remove .csv extension to get platform ID
                platformIds.add(name.substring(0, name.length() - 4));
            }
        }
        //debugprint("MapID", mapId, "Platform ids: ", platformIds);
        return platformIds;
    }

    public static List<String> getMainPlatformIds(int mapId) {
        return getAvailablePlatformIds(mapId).stream()
                .filter(id -> id.startsWith("m"))
                .collect(Collectors.toList());
    }

    public static List<String> getConnectorPlatformIds(int mapId) {
        return getAvailablePlatformIds(mapId).stream()
                .filter(id -> id.startsWith("c"))
                .collect(Collectors.toList());
    }

    /**
     * Gets all characters standing on a specific platform.
     * <p>
     * Cross-references all characters on the map against the platform's coordinate bounds.
     * Since platform coordinates are guide points (not every possible position), this method
     * uses interpolation and tolerance values to determine if a character is on the platform.
     *
     * @param mapId      The map ID
     * @param platformId The platform identifier (e.g., "m1")
     * @return List of characters currently on the specified platform
     */
    public static List<Character> getAllCharsOnPlatform(int mapId, String platformId) {
        List<Character> allCharsOnMap = getAllCharsOnMap(mapId);
        List<Character> charsOnPlatform = new ArrayList<>();

        Platform platform = PlatformParser.parsePlatform(mapId, platformId);

        if (platform == null || platform.getSortedPoints().isEmpty()) {
            return charsOnPlatform;
        }

        for (Character chr : allCharsOnMap) {
            if (isCharacterOnPlatform(chr, platform)) {
                charsOnPlatform.add(chr);
            }
        }
        //debugprint("CharsOnPlatform: ", charsOnPlatform, charsOnPlatform.size());
        return charsOnPlatform;
    }

    /**
     * Checks if a character is standing on a specific platform.
     *
     * @param chr      The character to check
     * @param platform The platform to check against
     * @return true if the character is on the platform, false otherwise
     */
    public static boolean isCharacterOnPlatform(Character chr, Platform platform) {
        Point position = chr.getPosition();
        return isPositionOnPlatform(position, platform);
    }

    /**
     * Checks if a position is on a specific platform.
     * <p>
     * For FLAT platforms: checks if X is within bounds and Y matches baseY (within tolerance).
     * For SLOPED platforms: checks if X is within bounds and Y matches the interpolated Y at that X.
     *
     * @param position The position to check
     * @param platform The platform to check against
     * @return true if the position is on the platform
     */
    public static boolean isPositionOnPlatform(Point position, Platform platform) {
        int x = position.x;
        int y = position.y;

        // Check X bounds with tolerance
        if (x < platform.getMinX() - X_TOLERANCE || x > platform.getMaxX() + X_TOLERANCE) {
            return false;
        }

        // Get expected Y at this X position (handles both flat and sloped)
        int expectedY = platform.getYAtX(x);

        // Check if actual Y is close enough to expected Y
        return Math.abs(y - expectedY) <= Y_TOLERANCE;
    }

    public static List<Character> getAllCharsOnMap(int mapId) {
        MapleMap map = getMapleMapById(mapId);
        return map.getAllPlayers();
    }

    public static List<Point> getCoordinatesOfAllCharsOnMap(int mapId) {
        return getListOfCharacterCoordinates(getAllCharsOnMap(mapId));
    }

    public static List<Point> getListOfCharacterCoordinates(List<Character> chars) {
        List<Point> characterCoords = new ArrayList<>();
        for (Character chr : chars) {
            characterCoords.add(chr.getPosition());
        }
        return characterCoords;
    }
}
