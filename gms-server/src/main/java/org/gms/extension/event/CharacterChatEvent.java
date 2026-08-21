package org.gms.extension.event;

import org.gms.client.Character;
import org.gms.extension.api.HostEvent;

/**
 * A non-artificial character sent a general (map) chat message.
 */
public record CharacterChatEvent(Character character, String message) implements HostEvent {
}
