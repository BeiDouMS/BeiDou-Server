/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    Copyleft (L) 2016 - 2019 RonanLana (HeavenMS)

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
package org.gms.client.processor.action;

import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.client.inventory.Inventory;
import org.gms.client.inventory.InventoryType;
import org.gms.client.inventory.Item;
import org.gms.client.inventory.manipulator.InventoryManipulator;
import org.gms.config.GameConfig;
import org.gms.manager.ServerManager;
import org.gms.server.ItemInformationProvider;
import org.gms.server.StatEffect;
import org.gms.service.HpMpAlertService;
import org.gms.util.PacketCreator;

import java.util.List;

/**
 * @author Ronan - multi-pot consumption feature
 */
public class PetAutopotProcessor {

    private static class AutopotAction {

        private final Client c;
        private short slot;
        private final int itemId;

        private Item toUse;
        private List<Item> toUseList;

        private boolean hasHpGain, hasMpGain;
        private int maxHp, maxMp, curHp, curMp;
        private double incHp, incMp;

        private boolean cursorOnNextAvailablePot(Character chr) {
            if (toUseList == null) {
                toUseList = chr.getInventory(InventoryType.USE).linkedListById(itemId);
            }

            toUse = null;
            while (!toUseList.isEmpty()) {
                Item it = toUseList.remove(0);

                if (it.getQuantity() > 0) {
                    toUse = it;
                    slot = it.getPosition();

                    return true;
                }
            }

            return false;
        }

        public AutopotAction(Client c, short slot, int itemId) {
            this.c = c;
            this.slot = slot;
            this.itemId = itemId;
        }

        public void run() {
            Client c = this.c;
            HpMpAlertService hpMpAlertService = ServerManager.getApplicationContext().getBean(HpMpAlertService.class);
            Character chr = c.getPlayer();
            if (!chr.isAlive()) {
                c.sendPacket(PacketCreator.enableActions());
                return;
            }

            int useCount = 0, qtyCount = 0;
            StatEffect stat = null;

            maxHp = chr.getCurrentMaxHp();
            maxMp = chr.getCurrentMaxMp();

            curHp = chr.getHp();
            curMp = chr.getMp();

            Inventory useInv = chr.getInventory(InventoryType.USE);
            useInv.lockInventory();
            try {
                // 吃满就不要吃了，放到锁内判断，避免已排队的数据跳过限制
                if (curHp >= maxHp && curMp >= maxMp) {
                    c.sendPacket(PacketCreator.enableActions());
                    return;
                }
                toUse = useInv.getItem(slot);
                if (toUse != null) {
                    if (toUse.getItemId() != itemId) {
                        c.sendPacket(PacketCreator.enableActions());
                        return;
                    }

                    toUseList = null;

                    // from now on, toUse becomes the "cursor" for the current pot being used
                    if (toUse.getQuantity() <= 0) {
                        if (!cursorOnNextAvailablePot(chr)) {
                            c.sendPacket(PacketCreator.enableActions());
                            return;
                        }
                    }

                    stat = ItemInformationProvider.getInstance().getItemEffect(toUse.getItemId());
                    hasHpGain = stat.getHp() > 0 || stat.getHpRate() > 0.0;
                    hasMpGain = stat.getMp() > 0 || stat.getMpRate() > 0.0;

                    incHp = stat.getHp();
                    if (incHp <= 0 && hasHpGain) {
                        incHp = Math.ceil(maxHp * stat.getHpRate());
                    }

                    incMp = stat.getMp();
                    if (incMp <= 0 && hasMpGain) {
                        incMp = Math.ceil(maxMp * stat.getMpRate());
                    }

                    if (GameConfig.getServerBoolean("use_server_auto_pot")) { // 服务端同步警戒线
                        if (hasHpGain) {
                            double hpRatio = (hpMpAlertService.getHpAlertPer(chr.getId()) * maxHp) - curHp;
                            if (hpRatio > 0.0) {
                                qtyCount = (int) Math.ceil(hpRatio / incHp);
                            }
                        }

                        if (hasMpGain) {
                            double mpRatio = ((hpMpAlertService.getMpAlertPer(chr.getId()) * maxMp) - curMp);
                            if (mpRatio > 0.0) {
                                qtyCount = Math.max(qtyCount, (int) Math.ceil(mpRatio / incMp));
                            }
                        }
                    } else if (GameConfig.getServerBoolean("use_compulsory_auto_pot")) { // 原 HMS 方案
                        if (hasHpGain) {
                            double hpRatio = (GameConfig.getServerFloat("pet_auto_hp_ratio") * maxHp) - curHp;
                            if (hpRatio > 0.0) {
                                qtyCount = (int) Math.ceil(hpRatio / incHp);
                            }
                        }

                        if (hasMpGain) {
                            double mpRatio = ((GameConfig.getServerFloat("pet_auto_mp_ratio") * maxMp) - curMp);
                            if (mpRatio > 0.0) {
                                qtyCount = Math.max(qtyCount, (int) Math.ceil(mpRatio / incMp));
                            }
                        }
                    } else {
                        qtyCount = 1;
                    }

                    if (qtyCount < 0) {
                        qtyCount = 0;
                    }

                    while (true) {
                        short qtyToUse = (short) Math.min(qtyCount, toUse.getQuantity());
                        InventoryManipulator.removeFromSlot(c, InventoryType.USE, slot, qtyToUse, false);

                        curHp += (int) (incHp * qtyToUse);
                        curMp += (int) (incMp * qtyToUse);

                        useCount += qtyToUse;
                        qtyCount -= qtyToUse;

                        if (toUse.getQuantity() == 0 && qtyCount > 0) {
                            // depleted out the current slot, fetch for more

                            if (!cursorOnNextAvailablePot(chr)) {
                                break;    // no more pots available
                            }
                        } else {
                            break;    // gracefully finished it's job, quit the loop
                        }
                    }
                }
            } finally {
                useInv.unlockInventory();
            }

            if (stat != null) {
                for (int i = 0; i < useCount; i++) {
                    stat.applyTo(chr);
                }
            }

            chr.sendPacket(PacketCreator.enableActions());
        }
    }

    public static void runAutopotAction(Client c, short slot, int itemid) {
        AutopotAction action = new AutopotAction(c, slot, itemid);
        action.run();
    }

}
