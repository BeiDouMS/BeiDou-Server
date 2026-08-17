package soloMapling.server.EventMessageSystem;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class EventBus {
    private static final EventBus INSTANCE = new EventBus();

    private final Map<EventType, List<EventSubscriber>> subscribers = new ConcurrentHashMap<>();
    private final EventStore eventStore = new EventStore(5_000);

    private EventBus() {
    }

    public static EventBus getInstance() {
        return INSTANCE;
    }

    public void publish(GameEvent event) {
        eventStore.add(event);
        List<EventSubscriber> interestedSubscribers = subscribers.get(event.getType());
        if (interestedSubscribers == null) {
            return;
        }
        for (EventSubscriber subscriber : interestedSubscribers) {
            if (subscriber.matchesFilter(event)) {
                subscriber.onEvent(event);
            }
        }
    }

    public void subscribe(EventType type, EventSubscriber subscriber) {
        subscribers.computeIfAbsent(type, ignored -> new CopyOnWriteArrayList<>()).add(subscriber);
    }

    public void unsubscribe(EventType type, EventSubscriber subscriber) {
        List<EventSubscriber> eventSubscribers = subscribers.get(type);
        if (eventSubscribers != null) {
            eventSubscribers.remove(subscriber);
        }
    }

    public void unsubscribeAll(EventSubscriber subscriber) {
        for (EventType type : EventType.values()) {
            unsubscribe(type, subscriber);
        }
    }

    public EventStore getEventStore() {
        return eventStore;
    }
}
