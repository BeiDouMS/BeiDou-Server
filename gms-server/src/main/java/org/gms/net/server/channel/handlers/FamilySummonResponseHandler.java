package org.gms.net.server.channel.handlers;

import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.client.FamilyEntitlement;
import org.gms.client.FamilyEntry;
import org.gms.config.GameConfig;
import org.gms.net.AbstractPacketHandler;
import org.gms.net.packet.InPacket;
import org.gms.net.server.coordinator.world.InviteCoordinator;
import org.gms.net.server.coordinator.world.InviteCoordinator.InviteResult;
import org.gms.net.server.coordinator.world.InviteCoordinator.InviteResultType;
import org.gms.net.server.coordinator.world.InviteCoordinator.InviteType;
import org.gms.server.maps.MapleMap;
import org.gms.util.PacketCreator;

public class FamilySummonResponseHandler extends AbstractPacketHandler {

    @Override
    public void handlePacket(InPacket p, Client c) {
        if (!GameConfig.getServerBoolean("use_family_system")) {
            return;
        }
        p.readString(); //family name
        boolean accept = p.readByte() != 0;
        InviteResult inviteResult = InviteCoordinator.answerInvite(InviteType.FAMILY_SUMMON, c.getPlayer().getId(), c.getPlayer(), accept);
        if (inviteResult.result == InviteResultType.NOT_FOUND) {
            return;
        }
        Character inviter = inviteResult.from;
        FamilyEntry inviterEntry = inviter.getFamilyEntry();
        if (inviterEntry == null) {
            return;
        }
        MapleMap map = (MapleMap) inviteResult.params[0];
        if (accept && inviter.getMap() == map) { //cancel if inviter has changed maps
            c.getPlayer().changeMap(map, map.getPortal(0));
        } else {
            inviterEntry.refundEntitlement(FamilyEntitlement.SUMMON_FAMILY);
            inviterEntry.gainReputation(FamilyEntitlement.SUMMON_FAMILY.getRepCost(), false); //refund rep cost if declined
            inviter.sendPacket(PacketCreator.getFamilyInfo(inviterEntry));
            inviter.dropMessage(5, c.getPlayer().getName() + " has denied the summon request.");
        }
    }

}
