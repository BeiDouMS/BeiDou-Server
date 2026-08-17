package org.gms.extension.api.event;

import org.gms.extension.api.HostEvent;

public record ServerShutdownEvent(long shutdownAtEpochMs) implements HostEvent {
}
