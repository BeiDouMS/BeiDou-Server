package soloMapling.ArtificialPlayer.BotDecoratorSystem;

import org.gms.client.Character;
import soloMapling.ArtificialPlayer.BotHelpers;

import java.util.Map;
import java.util.concurrent.*;

import static soloMapling.server.ExecutorServiceManager.runAsync;

/**
 * Deferred decoration queue for full (expensive) bot decoration.
 *
 * Bots are added to categorized queues (e.g. "fm", "henesys") after spawn.
 * A background scheduler processes one bot per category per tick, running
 * categories in parallel so no single category hogs all the processing.
 *
 * This is separate from {@link QuickEquip} which handles fast spawn-time
 * equipping from a tiny curated list.
 *
 * Toggle on/off via {@link #ENABLED}.
 */
public class BotDecorationQueue {

    public static boolean ENABLED = true;

    private static final Map<String, ConcurrentLinkedQueue<Integer>> queues = new ConcurrentHashMap<>();
    private static ScheduledExecutorService scheduler;

    // How many bots to process per category per tick. Decoration is an
    // in-memory EquipMetadataCache lookup now, so the queue can drain fast.
    private static final int BATCH_SIZE = 10;
    // Time between decoration ticks (ms)
    private static final long TICK_INTERVAL_MS = 250;
    // Delay before first tick after start (ms) - lets bots finish spawning
    private static final long INITIAL_DELAY_MS = 5000;

    /**
     * Queue a bot for deferred full decoration.
     *
     * @param category Bot type category (e.g. "fm", "henesys", "gacha").
     *                 Each category is processed independently in parallel.
     * @param botId    The bot's character ID.
     */
    public static void addBot(String category, int botId) {
        if (!ENABLED) return;
        queues.computeIfAbsent(category, k -> new ConcurrentLinkedQueue<>()).add(botId);
    }

    /**
     * Start the background decoration scheduler.
     * Call once after environment initialization.
     */
    public static void start() {
        if (!ENABLED) return;
        if (scheduler != null && !scheduler.isShutdown()) return;

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "BotDecorationQueue");
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleAtFixedRate(
                BotDecorationQueue::processTick,
                INITIAL_DELAY_MS,
                TICK_INTERVAL_MS,
                TimeUnit.MILLISECONDS
        );

        System.out.println("[BotDecorationQueue] Started - interval=" + TICK_INTERVAL_MS + "ms, batch=" + BATCH_SIZE);
    }

    /**
     * Stop the decoration scheduler.
     */
    public static void stop() {
        if (scheduler != null) {
            scheduler.shutdown();
            scheduler = null;
        }
    }

    /**
     * Process one tick: pull up to BATCH_SIZE bots from each category
     * and decorate them in parallel (one async task per category).
     */
    private static void processTick() {
        for (Map.Entry<String, ConcurrentLinkedQueue<Integer>> entry : queues.entrySet()) {
            ConcurrentLinkedQueue<Integer> queue = entry.getValue();

            for (int i = 0; i < BATCH_SIZE; i++) {
                Integer botId = queue.poll();
                if (botId == null) break;

                runAsync(() -> decorateBot(botId));
            }
        }
    }

    private static void decorateBot(int botId) {
        try {
            Character bot = BotHelpers.getCharFromChannelStorage(botId);
            if (bot == null) return;

            // Run the full expensive decoration (WZ lookups, job-specific gear, etc.)
            BotDecorateEquips.decorateBotEquips(bot);
        } catch (Exception e) {
            System.err.println("[BotDecorationQueue] Error decorating bot " + botId + ": " + e.getMessage());
        }
    }

    /**
     * Whether the background scheduler is currently running.
     */
    public static boolean isRunning() {
        return scheduler != null && !scheduler.isShutdown();
    }

    /**
     * Get the total number of bots waiting across all categories.
     */
    public static int getPendingCount() {
        return queues.values().stream().mapToInt(ConcurrentLinkedQueue::size).sum();
    }

    /**
     * Get the number of bots waiting in a specific category.
     */
    public static int getPendingCount(String category) {
        ConcurrentLinkedQueue<Integer> queue = queues.get(category);
        return queue != null ? queue.size() : 0;
    }
}
