package net.server.channel.handlers;

import client.Character;
import client.Client;
import client.FamilyEntitlement;
import client.MapleFamilyEntry;
import config.YamlConfig;
import net.AbstractPacketHandler;
import net.packet.InPacket;
import net.server.coordinator.world.MapleInviteCoordinator;
import net.server.coordinator.world.MapleInviteCoordinator.InviteResult;
import net.server.coordinator.world.MapleInviteCoordinator.InviteType;
import net.server.coordinator.world.MapleInviteCoordinator.MapleInviteResult;
import server.maps.MapleMap;
import tools.PacketCreator;

public class FamilySummonResponseHandler extends AbstractPacketHandler {

    @Override
    public void handlePacket(InPacket p, Client c) {
        if(!YamlConfig.config.server.USE_FAMILY_SYSTEM) return;
        p.readString(); //family name
        boolean accept = p.readByte() != 0;
        MapleInviteResult inviteResult = MapleInviteCoordinator.answerInvite(InviteType.FAMILY_SUMMON, c.getPlayer().getId(), c.getPlayer(), accept);
        if(inviteResult.result == InviteResult.NOT_FOUND) return;
        Character inviter = inviteResult.from;
        MapleFamilyEntry inviterEntry = inviter.getFamilyEntry();
        if(inviterEntry == null) return;
        MapleMap map = (MapleMap) inviteResult.params[0];
        if(accept && inviter.getMap() == map) { //cancel if inviter has changed maps
            c.getPlayer().changeMap(map, map.getPortal(0));
        } else {
            inviterEntry.refundEntitlement(FamilyEntitlement.SUMMON_FAMILY);
            inviterEntry.gainReputation(FamilyEntitlement.SUMMON_FAMILY.getRepCost(), false); //refund rep cost if declined
            inviter.sendPacket(PacketCreator.getFamilyInfo(inviterEntry));
            inviter.dropMessage(5, c.getPlayer().getName() + " has denied the summon request.");
        }
    }

}
