package org.gms.extension.event;

import org.gms.client.Character;
import org.gms.extension.api.HostEvent;

/**
 * A party invite was created and delivered to {@code invited}. Published for every invite so a
 * plugin owning the invited character (a bot has no real client to answer the invite packet) can
 * answer it on the character's behalf.
 */
public record PartyInviteEvent(Character invited, Character inviter, int partyId) implements HostEvent {
}
