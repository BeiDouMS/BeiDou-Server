package org.gms.net.server.channel.handlers;

import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.manager.ServerManager;
import org.gms.net.AbstractPacketHandler;
import org.gms.net.packet.InPacket;
import org.gms.service.HpMpAlertService;

/**
 * @author lee
 */
public class SetHpMpAlertHandler extends AbstractPacketHandler {

    @Override
    public void handlePacket(InPacket p, Client c) {
        HpMpAlertService hpMpAlertService = ServerManager.getApplicationContext().getBean(HpMpAlertService.class);
        Character chr = c.getPlayer();
        hpMpAlertService.setHpAlert(chr.getId(), p.readByte());
        hpMpAlertService.setMpAlert(chr.getId(), p.readByte());

    }
}
