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
import org.gms.client.Skill;
import org.gms.client.SkillFactory;
import org.gms.client.inventory.Equip;
import org.gms.client.inventory.Equip.ScrollResult;
import org.gms.client.inventory.Inventory;
import org.gms.client.inventory.InventoryType;
import org.gms.client.inventory.Item;
import org.gms.client.inventory.ModifyInventory;
import org.gms.client.inventory.manipulator.InventoryManipulator;
import org.gms.constants.id.ItemId;
import org.gms.constants.inventory.ItemConstants;
import org.gms.net.AbstractPacketHandler;
import org.gms.net.packet.InPacket;
import org.gms.server.ItemInformationProvider;
import org.gms.util.PacketCreator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Matze
 * @author Frz
 */
public final class ScrollHandler extends AbstractPacketHandler {

    @Override
    public final void handlePacket(InPacket p, Client c) {
        if (c.tryacquireClient()) {
            try {
                p.readInt(); // whatever...
                short scrollSlot = p.readShort();
                short equipSlot = p.readShort();
                byte ws = (byte) p.readShort();
                boolean whiteScroll = false; // white scroll being used?
                boolean legendarySpirit = false; // legendary spirit skill
                if ((ws & 2) == 2) {
                    whiteScroll = true;
                }

                ItemInformationProvider ii = ItemInformationProvider.getInstance();
                Character chr = c.getPlayer();
                Equip toScroll = (Equip) chr.getInventory(InventoryType.EQUIPPED).getItem(equipSlot);
                Skill LegendarySpirit = SkillFactory.getSkill(1003);
                if (chr.getSkillLevel(LegendarySpirit) > 0 && equipSlot >= 0) {
                    legendarySpirit = true;
                    toScroll = (Equip) chr.getInventory(InventoryType.EQUIP).getItem(equipSlot);
                }
                byte oldLevel = toScroll.getLevel();
                byte oldSlots = toScroll.getUpgradeSlots();
                Inventory useInventory = chr.getInventory(InventoryType.USE);
                Item scroll = useInventory.getItem(scrollSlot);
                Item wscroll = null;

                if (ItemConstants.isCleanSlate(scroll.getItemId()) && !ii.canUseCleanSlate(toScroll)) {
                    announceCannotScroll(c, legendarySpirit);
                    return;
                } else if (!ItemConstants.isModifierScroll(scroll.getItemId()) && toScroll.getUpgradeSlots() < 1) {
                    announceCannotScroll(c, legendarySpirit);   // thanks onechord for noticing zero upgrade slots freezing Legendary Scroll UI
                    return;
                }

                List<Integer> scrollReqs = ii.getScrollReqs(scroll.getItemId());
                if (scrollReqs.size() > 0 && !scrollReqs.contains(toScroll.getItemId())) {
                    announceCannotScroll(c, legendarySpirit);
                    return;
                }
                if (whiteScroll) {
                    wscroll = useInventory.findById(ItemId.WHITE_SCROLL);
                    if (wscroll == null) {
                        whiteScroll = false;
                    }
                }

                if (!ItemConstants.isChaosScroll(scroll.getItemId()) && !ItemConstants.isCleanSlate(scroll.getItemId())) {
                    if (!canScroll(scroll.getItemId(), toScroll.getItemId())) {
                        announceCannotScroll(c, legendarySpirit);
                        return;
                    }
                }

                Equip scrolled = (Equip) ii.scrollEquipWithId(toScroll, scroll.getItemId(), whiteScroll, 0, chr.isGM());
                ScrollResult scrollSuccess = Equip.ScrollResult.FAIL; // fail
                if (scrolled == null) {
                    scrollSuccess = Equip.ScrollResult.CURSE;
                } else if (scrolled.getLevel() > oldLevel || (ItemConstants.isCleanSlate(scroll.getItemId()) && scrolled.getUpgradeSlots() == oldSlots + 1) || ItemConstants.isFlagModifier(scroll.getItemId(), scrolled.getFlag())) {
                    scrollSuccess = Equip.ScrollResult.SUCCESS;
                }

                useInventory.lockInventory();
                try {
                    if (scroll.getQuantity() < 1) {
                        announceCannotScroll(c, legendarySpirit);
                        return;
                    }

                    if (whiteScroll && !ItemConstants.isCleanSlate(scroll.getItemId())) {
                        if (wscroll.getQuantity() < 1) {
                            announceCannotScroll(c, legendarySpirit);
                            return;
                        }

                        InventoryManipulator.removeFromSlot(c, InventoryType.USE, wscroll.getPosition(), (short) 1, false, false);
                    }

                    InventoryManipulator.removeFromSlot(c, InventoryType.USE, scroll.getPosition(), (short) 1, false);
                } finally {
                    useInventory.unlockInventory();
                }

                final List<ModifyInventory> mods = new ArrayList<>();
                if (scrollSuccess == Equip.ScrollResult.CURSE) {
                    if (!ItemId.isWeddingRing(toScroll.getItemId())) {
                        mods.add(new ModifyInventory(3, toScroll));
                        if (equipSlot < 0) {
                            Inventory inv = chr.getInventory(InventoryType.EQUIPPED);

                            inv.lockInventory();
                            try {
                                chr.unequippedItem(toScroll);
                                inv.removeItem(toScroll.getPosition());
                            } finally {
                                inv.unlockInventory();
                            }
                        } else {
                            Inventory inv = chr.getInventory(InventoryType.EQUIP);

                            inv.lockInventory();
                            try {
                                inv.removeItem(toScroll.getPosition());
                            } finally {
                                inv.unlockInventory();
                            }
                        }
                    } else {
                        scrolled = toScroll;
                        scrollSuccess = Equip.ScrollResult.FAIL;

                        mods.add(new ModifyInventory(3, scrolled));
                        mods.add(new ModifyInventory(0, scrolled));
                    }
                } else {
                    mods.add(new ModifyInventory(3, scrolled));
                    mods.add(new ModifyInventory(0, scrolled));
                }
                c.sendPacket(PacketCreator.modifyInventory(true, mods));
                chr.getMap().broadcastMessage(PacketCreator.getScrollEffect(chr.getId(), scrollSuccess, legendarySpirit, whiteScroll));
                if (equipSlot < 0 && (scrollSuccess == Equip.ScrollResult.SUCCESS || scrollSuccess == Equip.ScrollResult.CURSE)) {
                    chr.equipChanged();
                }
            } finally {
                c.releaseClient();
            }
        }
    }

    private static void announceCannotScroll(Client c, boolean legendarySpirit) {
        c.sendPacket(PacketCreator.getInventoryFull());

        if (legendarySpirit) {
            // c.sendPacket(PacketCreator.getScrollEffect(c.getPlayer().getId(), Equip.ScrollResult.FAIL, false, false));
            // 上面是原来的，下面三行是新加的，具体原理我也不懂，纯属瞎猫碰到死耗子。
            // 不更新Inventory的话，客户端会假死；legendarySpirit 不改成 true 的话，客户端匠人之魂就不会播放动画，取消和关闭按钮也不能恢复成可点击状态
            // 修复思路及推测结论：直接给身上的装备砸卷，当砸卷次数为0时服务端会发送else里的Inventory封包，而匠人之魂在次数为0时没发这个包（>0时有），所以由此推测 ->
            // 砸卷操作无论装备剩余次数是否为0，客户端都会向服务器发起砸卷请求，在这个过程中客户端会给背包加锁，客户端收到Inventory封包才会解除这个锁，所以原来没有这个封包的时候客户端的锁就解不了，导致假死。
            c.sendPacket(PacketCreator.getScrollEffect(c.getPlayer().getId(), ScrollResult.FAIL, true, false));
            c.getPlayer().message("由于砸卷次数不足或其他原因导致的砸卷失败，本次不消耗卷轴。");
        }
    }

    private static boolean canScroll(int scrollid, int itemid) {
        int sid = scrollid / 100;

        switch (sid) {
            case 20492: //scroll for accessory (pendant, belt, ring)
                return canScroll(ItemId.RING_STR_100_SCROLL, itemid) || canScroll(ItemId.DRAGON_STONE_SCROLL, itemid) ||
                        canScroll(ItemId.BELT_STR_100_SCROLL, itemid);

            default:
                return (scrollid / 100) % 100 == (itemid / 10000) % 100;
        }
    }
}
