package org.gms.extension.api;

import java.util.function.Consumer;

/**
 * Decoupled pub/sub between host engine hooks and plugins.
 */
public interface HostEventBus {

    <T extends HostEvent> void publish(T event);

    <T extends HostEvent> void subscribe(Class<T> type, Consumer<T> listener);

    <T extends HostEvent> void unsubscribe(Class<T> type, Consumer<T> listener);
}
