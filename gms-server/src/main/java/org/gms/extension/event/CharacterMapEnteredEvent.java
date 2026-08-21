package org.gms.extension.event;

import org.gms.client.Character;
import org.gms.extension.api.HostEvent;

/**
 * A non-artificial character entered a map. Plugins that simulate ambient bots
 * can react (greetings, LOD wake-ups) without the host importing plugin types.
 */
public record CharacterMapEnteredEvent(Character character, int mapId) implements HostEvent {
}
