package org.gms.extension.event;

import org.gms.client.Character;
import org.gms.extension.api.HostEvent;

/**
 * A trade invite was created targeting {@code invited}. Plugins owning headless invitees
 * (no client to accept the packet) subscribe and drive accept/decline themselves.
 */
public record TradeInviteEvent(Character invited, Character inviter) implements HostEvent {
}
