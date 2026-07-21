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
import org.gms.server.maps.MapItem;
import org.gms.server.maps.MapObject;
import org.gms.util.PacketCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.Set;

/**
 * @author TheRamon
 * @author Ronan
 */
public final class PetLootHandler extends AbstractPacketHandler {
    private static final Logger log = LoggerFactory.getLogger(PetLootHandler.class);

    @Override
    public final void handlePacket(InPacket p, Client c) {
        Character chr = c.getPlayer();

        byte petIndex = chr.getPetIndex(p.readInt());
        Pet pet = chr.getPet(petIndex);
        if (pet == null || !pet.isSummoned()) {
            c.sendPacket(PacketCreator.enableActions());
            return;
        }

        p.skip(13);
        int oid = p.readInt();
        MapObject ob = chr.getMap().getMapObject(oid);
        try {
            MapItem mapitem = (MapItem) ob;
            if (mapitem.getMeso() > 0) {
                if (!chr.isEquippedMesoMagnet(petIndex)) {
                    c.sendPacket(PacketCreator.enableActions());
                    return;
                }

                if (chr.isEquippedPetItemIgnore(petIndex)) {
                    final Set<Integer> petIgnore = chr.getExcludedItems();
                    if (!petIgnore.isEmpty() && petIgnore.contains(Integer.MAX_VALUE)) {
                        c.sendPacket(PacketCreator.enableActions());
                        return;
                    }
                }
            } else {
                if (!chr.isEquippedItemPouch(petIndex)) {
                    c.sendPacket(PacketCreator.enableActions());
                    return;
                }

                if (chr.isEquippedPetItemIgnore(petIndex)) {
                    final Set<Integer> petIgnore = chr.getExcludedItems();
                    if (!petIgnore.isEmpty() && petIgnore.contains(mapitem.getItem().getItemId())) {
                        c.sendPacket(PacketCreator.enableActions());
                        return;
                    }
                }
            }

            // 距离反作弊：检测宠物是否真实在物品附近
            if (!checkPetPickupDistance(chr, pet, ob)) {
                c.sendPacket(PacketCreator.enableActions());
                return;
            }

            chr.pickupItem(ob, petIndex);
        } catch (NullPointerException | ClassCastException e) {
            c.sendPacket(PacketCreator.enableActions());
        }
    }

    /**
     * 检测宠物/玩家位置与物品位置的距离，防止全图吸物作弊。
     * 先检玩家位置（覆盖传送门瞬移、多平台等宠物滞后场景），再检宠物位置。
     * 宠物刚召唤(位置未初始化)时跳过检测，避免误判。
     *
     * @return true=距离合法，false=距离异常已拦截
     */
    private boolean checkPetPickupDistance(Character chr, Pet pet, MapObject ob) {
        Point itemPos = ob.getPosition();

        // 玩家预检：玩家本人就在物品附近→合法捡取，跳过宠物距离检测
        Point chrPos = chr.getPosition();
        if (Math.abs(chrPos.x - itemPos.x) <= 800 && Math.abs(chrPos.y - itemPos.y) <= 600) {
            return true;
        }

        // 传送补偿预检：玩家最近使用内传送门，捡取传送前位置的遗留物品时放行
        Point beforePos = chr.getPetLootTeleportBeforePos();
        if (beforePos != null
                && Math.abs(beforePos.x - itemPos.x) <= 800
                && Math.abs(beforePos.y - itemPos.y) <= 600) {
            return true;
        }

        Point petPos = pet.getPos();
        // 宠物刚召唤尚未移动时 pos 为 (0,0)，跳过检测
        if (petPos.x == 0 && petPos.y == 0) {
            return true;
        }

        int diffX = Math.abs(petPos.x - itemPos.x);
        int diffY = Math.abs(petPos.y - itemPos.y);

        // 全图真空：远超正常拾取范围，直接判定为作弊
        if (diffX > 800 || diffY > 600) {
            AutobanFactory.PET_ITEM_VAC.addPoint(chr.getAutoBanManager(),
                    "宠物" + pet.getName() + "地图ID：" + chr.getMapId() + "距离物品: " + diffX + " " + diffY);
            log.warn("宠物{}地图ID：{}距离物品: {} {}", pet.getName(), chr.getMapId(), diffX, diffY);
            return false;
        }

        // 短距真空：距离略超正常范围，但未到全图级别
        if (diffX > 400 || diffY > 400) {
            AutobanFactory.PET_SHORT_ITEM_VAC.addPoint(chr.getAutoBanManager(),
                    "宠物" + pet.getName() + "地图ID：" + chr.getMapId() + "距离物品: " + diffX + " " + diffY);
            log.warn("宠物{}地图ID：{}距离物品: {} {}", pet.getName(), chr.getMapId(), diffX, diffY);
            return false;
        }

        return true;
    }
}
