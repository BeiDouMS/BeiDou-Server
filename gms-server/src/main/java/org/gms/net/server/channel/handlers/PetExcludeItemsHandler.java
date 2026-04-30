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
import org.gms.client.inventory.Pet;
import org.gms.net.AbstractPacketHandler;
import org.gms.net.packet.InPacket;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author BubblesDev
 * @author Ronan
 */
public final class PetExcludeItemsHandler extends AbstractPacketHandler {

    @Override
    public final void handlePacket(InPacket p, Client c) {
        final int petId = p.readInt();
        p.skip(4); // timestamp

        Character chr = c.getPlayer();
        byte petIndex = chr.getPetIndex(petId);
        if (petIndex < 0) {
            return;
        }

        final Pet pet = chr.getPet(petIndex);
        if (pet == null) {
            return;
        }

        Set<Integer> newExcludedItems = new LinkedHashSet<>();
        byte amount = p.readByte();
        for (int i = 0; i < amount; i++) {
            int itemId = p.readInt();
            if (itemId >= 0) {
                newExcludedItems.add(itemId);
            } else {
                AutobanFactory.PACKET_EDIT.alert(chr, "negative item id value in PetExcludeItemsHandler (" + itemId + ")");
                return;
            }
        }

        // 客户端会提交完整过滤列表，这里先确保旧配置已加载，再按差异增量更新数据库和内存。
        chr.loadPetExcludedItems(petId);
        chr.updatePetExcludedItems(petId, newExcludedItems);
        chr.commitExcludedItems();
    }
}
