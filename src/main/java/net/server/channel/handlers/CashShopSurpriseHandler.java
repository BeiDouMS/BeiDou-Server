/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2019 RonanLana

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
package net.server.channel.handlers;

import client.Client;
import client.inventory.Item;
import net.AbstractPacketHandler;
import net.packet.InPacket;
import server.CashShop;
import server.CashShop.CashShopSurpriseResult;
import tools.PacketCreator;

import java.util.Optional;

/**
 * @author RonanLana
 * @author Ponk
 */
public class CashShopSurpriseHandler extends AbstractPacketHandler {

    @Override
    public final void handlePacket(InPacket p, Client c) {
        CashShop cs = c.getPlayer().getCashShop();
        if (!cs.isOpened()) {
            return;
        }

        long cashId = p.readLong();
        Optional<CashShopSurpriseResult> result = cs.openCashShopSurprise(cashId);
        if (result.isEmpty()) {
            c.sendPacket(PacketCreator.onCashItemGachaponOpenFailed());
            return;
        }

        Item usedCashShopSurprise = result.get().usedCashShopSurprise();
        Item reward = result.get().reward();
        c.sendPacket(PacketCreator.onCashGachaponOpenSuccess(c.getAccID(), usedCashShopSurprise.getCashId(),
                usedCashShopSurprise.getQuantity(), reward, reward.getItemId(), reward.getQuantity(), true));
    }
}
