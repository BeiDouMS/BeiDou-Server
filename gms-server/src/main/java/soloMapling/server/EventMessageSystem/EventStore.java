package soloMapling.server.EventMessageSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class EventStore {
    private final GameEvent[] buffer;
    private final int capacity;
    private int head;
    private int size;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public EventStore(int capacity) {
        this.capacity = capacity;
        this.buffer = new GameEvent[capacity];
    }

    public void add(GameEvent event) {
        lock.writeLock().lock();
        try {
            buffer[head] = event;
            head = (head + 1) % capacity;
            if (size < capacity) {
                size++;
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<GameEvent> getEventsByType(EventType type) {
        return getEventsByType(type, Long.MAX_VALUE);
    }

    public List<GameEvent> getEventsByType(EventType type, long sinceTimestamp) {
        lock.readLock().lock();
        try {
            List<GameEvent> results = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                GameEvent event = buffer[i];
                if (event != null && event.getType() == type && event.getTimestamp() >= sinceTimestamp) {
                    results.add(event);
                }
            }
            return results;
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<GameEvent> getEventsByLocation(int world, int channel, Integer mapId) {
        lock.readLock().lock();
        try {
            List<GameEvent> results = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                GameEvent event = buffer[i];
                if (event != null && event.getWorld() == world && event.getChannel() == channel
                        && (mapId == null || event.getMap().getId() == mapId)) {
                    results.add(event);
                }
            }
            return results;
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<GameEvent> getRecentEvents(int count) {
        lock.readLock().lock();
        try {
            List<GameEvent> results = new ArrayList<>();
            int eventCount = Math.min(count, size);
            int start = (head - eventCount + capacity) % capacity;
            for (int i = 0; i < eventCount; i++) {
                GameEvent event = buffer[(start + i) % capacity];
                if (event != null) {
                    results.add(event);
                }
            }
            return results;
        } finally {
            lock.readLock().unlock();
        }
    }
}
