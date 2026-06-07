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
import org.gms.client.inventory.Pet;
import org.gms.config.GameConfig;
import org.gms.net.packet.InPacket;
import org.gms.server.maps.MapObject;
import org.gms.server.maps.MapObjectType;
import org.gms.server.maps.MapItem;
import org.gms.server.movement.LifeMovementFragment;
import org.gms.util.PacketCreator;
import org.gms.exception.EmptyMovementException;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public final class MovePetHandler extends AbstractMovementPacketHandler {
    @Override
    public final void handlePacket(InPacket p, Client c) {
        int petId = p.readInt();
        p.readLong();
//        Point startPos = StreamUtil.readShortPoint(slea);
        List<LifeMovementFragment> res;

        try {
            res = parseMovement(p);
        } catch (EmptyMovementException e) {
            return;
        }
        Character player = c.getPlayer();
        byte slot = player.getPetIndex(petId);
        if (slot == -1) {
            return;
        }
        player.getPet(slot).updatePosition(res);
        player.getMap().broadcastMessage(player, PacketCreator.movePet(player.getId(), petId, slot, res), false);
        if (GameConfig.getServerBoolean("pet_itemvac")) {
            // 根据游戏config参数确定是否开启宠吸
            itemVac(player,slot);
        }

    }
    //解决宠吸物品过滤器问题 by pkoukk
    private final boolean shouldPickupItem(Character player, MapItem mapitem, byte petIndex) {
        if (mapitem == null || mapitem.isPickedUp()) {
            return false;
        }
        if (mapitem.getMeso() > 0) {
            if (!player.isEquippedMesoMagnet(petIndex)) {
                return false;
            }

            if (player.isEquippedPetItemIgnore(petIndex)) {
                final Set<Integer> petIgnore = player.getExcludedItems();
                return petIgnore.isEmpty() || !petIgnore.contains(Integer.MAX_VALUE);
            }
        } else {
            if (!player.isEquippedItemPouch(petIndex)) {
                return false;
            }

            if (player.isEquippedPetItemIgnore(petIndex)) {
                final Set<Integer> petIgnore = player.getExcludedItems();
                return petIgnore.isEmpty() || !petIgnore.contains(mapitem.getItem().getItemId());
            }
        }
        return true;
    }

    public void itemVac(Character player,byte slot) {
        Pet pet = player.getPet(slot);
        if (pet == null) return;
        List<MapObject> list = player.getMap().getMapObjectsInRange(pet.getPos(), 30000.0, Arrays.asList(MapObjectType.ITEM));//获取全屏物品列表
        for (MapObject item : list) {  // 遍历宠物身边一定范围内的物品列表
            MapItem mapItem = (MapItem) item;
            //不拾取不可见的任务道具
            if (mapItem.isPlayerDrop()||!player.needQuestItem(mapItem.getQuest(), mapItem.getItemId())) {
                continue;
            }
            if (shouldPickupItem(player, mapItem, slot) && (mapItem.getOwnerId() == player.getId()
                    || mapItem.getOwnerId() == player.getPartyId())) {
            player.pickupItem(item, (int) slot);  // 执行拾取操作
                }
        }
    }
}
