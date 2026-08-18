package org.gms.extension.runtime;

import dev.maple.extension.api.HostEvent;
import dev.maple.extension.api.HostEventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

@SuppressWarnings({"rawtypes", "unchecked"})
public final class InMemoryHostEventBus implements HostEventBus {

    private static final Logger log = LoggerFactory.getLogger(InMemoryHostEventBus.class);

    private final Map<Class<? extends HostEvent>, List<Consumer>> listeners = new ConcurrentHashMap<>();

    @Override
    public <T extends HostEvent> void publish(T event) {
        if (event == null) {
            return;
        }
        List<Consumer> consumers = listeners.get(event.getClass());
        if (consumers == null || consumers.isEmpty()) {
            return;
        }
        for (Consumer consumer : consumers) {
            try {
                consumer.accept(event);
            } catch (Exception e) {
                log.warn("HostEventBus listener failed for {}", event.getClass().getSimpleName(), e);
            }
        }
    }

    @Override
    public <T extends HostEvent> void subscribe(Class<T> type, Consumer<T> listener) {
        listeners.computeIfAbsent(type, t -> new CopyOnWriteArrayList<>()).add(listener);
    }

    @Override
    public <T extends HostEvent> void unsubscribe(Class<T> type, Consumer<T> listener) {
        List<Consumer> consumers = listeners.get(type);
        if (consumers != null) {
            consumers.remove(listener);
        }
    }
}
