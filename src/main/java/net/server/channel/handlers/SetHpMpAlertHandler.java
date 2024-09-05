package net.server.channel.handlers;

import api.manager.ApiManager;
import client.Character;
import client.Client;
import net.AbstractPacketHandler;
import net.packet.InPacket;
import service.HpMpAlertService;

/**
 * @author lee
 */
public class SetHpMpAlertHandler extends AbstractPacketHandler {

    @Override
    public void handlePacket(InPacket p, Client c) {
        HpMpAlertService hpMpAlertService = ApiManager.getApplicationContext().getBean(HpMpAlertService.class);
        Character chr = c.getPlayer();
        hpMpAlertService.setHpAlert(chr.getId(), p.readByte());
        hpMpAlertService.setMpAlert(chr.getId(), p.readByte());

    }
}
