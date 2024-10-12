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

import org.gms.client.Client;
import org.gms.client.inventory.InventoryType;
import org.gms.client.inventory.Item;
import org.gms.client.inventory.manipulator.InventoryManipulator;
import org.gms.constants.inventory.ItemConstants;
import org.gms.net.AbstractPacketHandler;
import org.gms.net.packet.InPacket;
import org.gms.net.server.Server;
import org.gms.server.ItemInformationProvider;
import org.gms.server.ItemInformationProvider.RewardItem;
import org.gms.util.PacketCreator;
import org.gms.util.Pair;
import org.gms.util.Randomizer;

import java.util.List;

/**
 * @author Jay Estrella
 * @author kevintjuh93
 */
public final class ItemRewardHandler extends AbstractPacketHandler {
    @Override
    public final void handlePacket(InPacket p, Client c) {
        byte slot = (byte) p.readShort();
        int itemId = p.readInt(); // will load from xml I don't care.

        Item it = c.getPlayer().getInventory(InventoryType.USE).getItem(slot);   // null check here thanks to Thora
        if (it == null || it.getItemId() != itemId || c.getPlayer().getInventory(InventoryType.USE).countById(itemId) < 1) {
            return;
        }

        ItemInformationProvider ii = ItemInformationProvider.getInstance();
        Pair<Integer, List<RewardItem>> rewards = ii.getItemReward(itemId);
        for (RewardItem reward : rewards.getRight()) {
            if (!InventoryManipulator.checkSpace(c, reward.itemid, reward.quantity, "")) {
                c.sendPacket(PacketCreator.getShowInventoryFull());
                break;
            }
            if (Randomizer.nextInt(rewards.getLeft()) < reward.prob) {//Is it even possible to get an item with prob 1?
                if (ItemConstants.getInventoryType(reward.itemid) == InventoryType.EQUIP) {
                    final Item item = ii.getEquipById(reward.itemid);
                    if (reward.period != -1) {
                        // TODO is this a bug, meant to be 60 * 60 * 1000?
                        item.setExpiration(currentServerTime() + reward.period * 60 * 60 * 10);
                    }
                    InventoryManipulator.addFromDrop(c, item, false);
                } else {
                    InventoryManipulator.addById(c, reward.itemid, reward.quantity, "", -1);
                }
                InventoryManipulator.removeById(c, InventoryType.USE, itemId, 1, false, false);
                if (reward.worldmsg != null) {
                    String msg = reward.worldmsg;
                    msg = msg.replaceAll("/name", c.getPlayer().getName());
                    msg = msg.replaceAll("/item", ii.getName(reward.itemid));
                    Server.getInstance().broadcastMessage(c.getWorld(), PacketCreator.serverNotice(6, msg));
                }
                break;
            }
        }
        c.sendPacket(PacketCreator.enableActions());
    }
}
