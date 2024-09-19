/*
This file is part of the OdinMS Maple Story Server
Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
Matthias Butz <matze@odinms.de>
Jan Christian Meyer <vimes@odinms.de>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation version 3 as published by
the Free Software Foundation. You may not use, modify or distribute
this program under any other version of the GNU Affero General Public
License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gms.net.server.channel.handlers;

import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.client.autoban.AutobanFactory;
import org.gms.client.autoban.AutobanManager;
import org.gms.net.AbstractPacketHandler;
import org.gms.net.packet.InPacket;
import org.gms.net.server.Server;
import org.gms.server.maps.MapleMap;
import org.gms.util.PacketCreator;

public final class HealOvertimeHandler extends AbstractPacketHandler {
    @Override
    public final void handlePacket(InPacket p, Client c) {
        Character chr = c.getPlayer();
        if (!chr.isLoggedInWorld()) {
            return;
        }

        AutobanManager abm = chr.getAutoBanManager();
        int timestamp = Server.getInstance().getCurrentTimestamp();
        p.skip(8);

        short healHP = p.readShort();
        if (healHP != 0) {
            abm.setTimestamp(8, timestamp, 28);  // thanks Vcoc & Thora for pointing out d/c happening here
            if ((abm.getLastSpam(0) + 1500) > timestamp) {
                AutobanFactory.FAST_HP_HEALING.addPoint(abm, "Fast hp healing");
            }

            MapleMap map = chr.getMap();
            int abHeal = (int) (77 * map.getRecovery() * 1.5); // thanks Ari for noticing players not getting healed in sauna in certain cases
            if (healHP > abHeal) {
                AutobanFactory.HIGH_HP_HEALING.autoban(chr, "Healing: " + healHP + "; Max is " + abHeal + ".");
                return;
            }

            chr.addHP(healHP);
            chr.getMap().broadcastMessage(chr, PacketCreator.showHpHealed(chr.getId(), healHP), false);
            abm.spam(0, timestamp);
        }
        short healMP = p.readShort();
        if (healMP != 0 && healMP < 1000) {
            abm.setTimestamp(9, timestamp, 28);
            if ((abm.getLastSpam(1) + 1500) > timestamp) {
                AutobanFactory.FAST_MP_HEALING.addPoint(abm, "Fast mp healing");
                return;     // thanks resinate for noticing mp being gained even after detection
            }
            chr.addMP(healMP);
            abm.spam(1, timestamp);
        }
    }
}
