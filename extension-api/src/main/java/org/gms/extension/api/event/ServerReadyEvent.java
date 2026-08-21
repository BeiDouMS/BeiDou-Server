package org.gms.extension.api.event;

import org.gms.extension.api.HostEvent;

public record ServerReadyEvent(long readyAtEpochMs) implements HostEvent {
}
