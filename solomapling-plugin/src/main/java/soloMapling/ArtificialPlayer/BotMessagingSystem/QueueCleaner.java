package soloMapling.ArtificialPlayer.BotMessagingSystem;

import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static soloMapling.BotLogger.log;
import static soloMapling.server.ExecutorServiceManager.getScheduledExecutorService;

public class QueueCleaner implements Runnable {

    private static final QueueCleaner cleaner = new QueueCleaner(MessageQueue.getInstance(), 10000);
    private final MessageQueue messageQueue;
    private final long expirationTime;
    private final ScheduledExecutorService scheduler = getScheduledExecutorService();

    public QueueCleaner(MessageQueue messageQueue, long expirationTime) {
        log("\n\nQueueCleaner OBJECT Created");
        this.messageQueue = messageQueue;
        this.expirationTime = expirationTime;
        this.scheduler.scheduleAtFixedRate(this, 0, 2, TimeUnit.SECONDS);
    }

    // Static method to access the singleton instance
    public static QueueCleaner getInstance() {
        log("Queue Cleaner getInstance");
        return cleaner;
    }

    @Override
    public void run() {
        long currentTime = System.currentTimeMillis();
        cleanQueue("primary", currentTime);
        cleanQueue("secondary", currentTime);
        cleanQueue("tertiary", currentTime);
    }

    private void cleanQueue(String queueName, long currentTime) {
        Collection<ChatMessage> messages = messageQueue.getQueue(queueName);
        messages.removeIf(message -> currentTime - message.getTimestamp() > expirationTime);
    }

    // Method to stop the scheduler gracefully
    public void stop() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
