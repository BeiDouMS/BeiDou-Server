package soloMapling.server.EventMessageSystem;

import java.util.concurrent.ConcurrentLinkedQueue;

/*
Wrapper around ConcurrentLinkedQueue<GameEvent>
Manages each bot's personal event queue. I.E. Personal Inbox for each bot.
Bots receive Events get stored here, waiting for processing
Push based
 */

public class BotEventBuffer {
    private final ConcurrentLinkedQueue<GameEvent> queue;
    private final int maxSize;

    public BotEventBuffer(int maxSize) {
        this.queue = new ConcurrentLinkedQueue<>();
        this.maxSize = maxSize;
    }

    public boolean add(GameEvent event) {
        if (queue.size() >= maxSize) {
            // Drop oldest event if at capacity
            queue.poll();
        }
        return queue.offer(event);
    }

    public GameEvent poll() {
        return queue.poll();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public int size() {
        return queue.size();
    }

    public void clear() {
        queue.clear();
    }
}
