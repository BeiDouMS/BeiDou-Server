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
import org.gms.client.Ring;
import org.gms.client.inventory.Equip;
import org.gms.client.inventory.Inventory;
import org.gms.client.inventory.InventoryType;
import org.gms.client.inventory.Item;
import org.gms.client.inventory.manipulator.InventoryManipulator;
import org.gms.config.GameConfig;
import org.gms.constants.id.ItemId;
import org.gms.constants.inventory.ItemConstants;
import org.gms.dao.entity.CharactersDO;
import org.gms.dao.entity.ModifiedCashItemDO;
import org.gms.manager.ServerManager;
import org.gms.net.AbstractPacketHandler;
import org.gms.net.packet.InPacket;
import org.gms.net.server.Server;
import org.gms.service.CharacterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gms.server.CashShop;
import org.gms.server.CashShop.CashItemFactory;
import org.gms.server.ItemInformationProvider;
import org.gms.service.NoteService;
import org.gms.util.PacketCreator;
import org.gms.util.Pair;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.concurrent.TimeUnit.DAYS;

public final class CashOperationHandler extends AbstractPacketHandler {
    private static final Logger log = LoggerFactory.getLogger(CashOperationHandler.class);

    private final NoteService noteService;

    public CashOperationHandler(NoteService noteService) {
        this.noteService = noteService;
    }

    @Override
    public void handlePacket(InPacket p, Client c) {
        Character chr = c.getPlayer();
        CashShop cs = chr.getCashShop();

        if (!cs.isOpened()) {
            c.sendPacket(PacketCreator.enableActions());
            return;
        }

        if (c.tryacquireClient()) {     // thanks Thora for finding out an exploit within cash operations
            try {
                final int action = p.readByte();
                if (action == 0x03 || action == 0x1E) { // buy
                    p.readByte();
                    final int useNX = p.readInt();
                    final int snCS = p.readInt();
                    ModifiedCashItemDO cItem = CashItemFactory.getItem(snCS);
                    if (!canBuy(chr, cItem, cs.getCash(useNX))) {
                        log.error("Denied to sell cash item with SN {}", snCS); // preventing NPE here thanks to MedicOP
                        c.enableCSActions();
                        return;
                    }

                    if (action == 0x03) { // Item
                        if (ItemConstants.isCashStore(cItem.getItemId()) && chr.getLevel() < 16) {
                            c.enableCSActions();
                            return;
                        } else if (ItemConstants.isRateCoupon(cItem.getItemId()) && !GameConfig.getServerBoolean("use_supply_rate_coupons")) {
                            chr.dropMessage(1, "Rate coupons are currently unavailable to purchase.");
                            c.enableCSActions();
                            return;
                        } else if (ItemConstants.isMapleLife(cItem.getItemId()) && chr.getLevel() < 30) {
                            c.enableCSActions();
                            return;
                        }

                        Item item = cItem.toItem();
                        cs.gainCash(useNX, cItem, chr.getWorld());  // thanks Rohenn for noticing cash operations after item acquisition
                        cs.addToInventory(item);
                        c.sendPacket(PacketCreator.showBoughtCashItem(item, c.getAccID()));
                    } else { // Package
                        cs.gainCash(useNX, cItem, chr.getWorld());

                        List<Item> cashPackage = CashItemFactory.getPackage(cItem.getItemId());
                        for (Item item : cashPackage) {
                            cs.addToInventory(item);
                        }
                        c.sendPacket(PacketCreator.showBoughtCashPackage(cashPackage, c.getAccID()));
                    }
                    c.sendPacket(PacketCreator.showCash(chr));
                } else if (action == 0x04) {//TODO check for gender with gift
                    int birthday = p.readInt();
                    ModifiedCashItemDO cItem = CashItemFactory.getItem(p.readInt());
                    CharacterService characterService = ServerManager.getApplicationContext().getBean(CharacterService.class);
                    CharactersDO charactersDO = characterService.findByName(p.readString());
                    String message = p.readString();
                    if (!canBuy(chr, cItem, cs.getCash(CashShop.NX_PREPAID)) || message.isEmpty() || message.length() > 73) {
                        c.enableCSActions();
                        return;
                    }
                    if (!checkBirthday(c, birthday)) {
                        c.sendPacket(PacketCreator.showCashShopMessage((byte) 0xC4));
                        return;
                    } else if (charactersDO == null) {
                        c.sendPacket(PacketCreator.showCashShopMessage((byte) 0xA9));
                        return;
                    } else if (Objects.equals(charactersDO.getAccountid(), c.getAccID())) {
                        c.sendPacket(PacketCreator.showCashShopMessage((byte) 0xA8));
                        return;
                    }
                    cs.gainCash(4, cItem, chr.getWorld());
                    cs.gift(charactersDO.getId(), chr.getName(), message, cItem.getSn());
                    c.sendPacket(PacketCreator.showGiftSucceed(charactersDO.getName(), cItem));
                    c.sendPacket(PacketCreator.showCash(chr));

                    String noteMessage = chr.getName() + " has sent you a gift! Go check out the Cash Shop.";
                    noteService.sendNormal(noteMessage, chr.getName(), charactersDO.getName());

                    Character receiver = c.getChannelServer().getPlayerStorage().getCharacterByName(charactersDO.getName());
                    if (receiver != null) {
                        noteService.show(receiver);
                    }
                } else if (action == 0x05) { // Modify wish list
                    cs.clearWishList();
                    for (byte i = 0; i < 10; i++) {
                        int sn = p.readInt();
                        ModifiedCashItemDO cItem = CashItemFactory.getItem(sn);
                        if (cItem != null && cItem.isSelling() && sn != 0) {
                            cs.addToWishList(sn);
                        }
                    }
                    c.sendPacket(PacketCreator.showWishList(chr, true));
                } else if (action == 0x06) { // Increase Inventory Slots
                    p.skip(1);
                    int cash = p.readInt();
                    byte mode = p.readByte();
                    if (mode == 0) {
                        byte type = p.readByte();
                        if (cs.getCash(cash) < 4000) {
                            c.enableCSActions();
                            return;
                        }
                        int qty = 4;
                        if (!chr.canGainSlots(type, qty)) {
                            c.enableCSActions();
                            return;
                        }
                        cs.gainCash(cash, -4000);
                        if (chr.gainSlots(type, qty, false)) {
                            c.sendPacket(PacketCreator.showBoughtInventorySlots(type, chr.getSlots(type)));
                            c.sendPacket(PacketCreator.showCash(chr));
                        } else {
                            log.warn("Could not add {} slots of type {} for chr {}", qty, type, Character.makeMapleReadable(chr.getName()));
                        }
                    } else {
                        ModifiedCashItemDO cItem = CashItemFactory.getItem(p.readInt());
                        if(cItem == null) {
                            c.enableCSActions();
                            return;
                        }
                        int type = (cItem.getItemId() - 9110000) / 1000;
                        if (!canBuy(chr, cItem, cs.getCash(cash))) {
                            c.enableCSActions();
                            return;
                        }
                        int qty = 8;
                        if (!chr.canGainSlots(type, qty)) {
                            c.enableCSActions();
                            return;
                        }
                        cs.gainCash(cash, cItem, chr.getWorld());
                        if (chr.gainSlots(type, qty, false)) {
                            c.sendPacket(PacketCreator.showBoughtInventorySlots(type, chr.getSlots(type)));
                            c.sendPacket(PacketCreator.showCash(chr));
                        } else {
                            log.warn("Could not add {} slots of type {} for chr {}", qty, type, Character.makeMapleReadable(chr.getName()));
                        }
                    }
                } else if (action == 0x07) { // Increase Storage Slots
                    p.skip(1);
                    int cash = p.readInt();
                    byte mode = p.readByte();
                    if (mode == 0) {
                        if (cs.getCash(cash) < 4000) {
                            c.enableCSActions();
                            return;
                        }
                        int qty = 4;
                        if (!chr.getStorage().canGainSlots(qty)) {
                            c.enableCSActions();
                            return;
                        }
                        cs.gainCash(cash, -4000);
                        if (chr.getStorage().gainSlots(qty)) {
                            log.debug("Chr {} bought {} slots to their account storage.", c.getPlayer().getName(), qty);
                            chr.setUsedStorage();

                            c.sendPacket(PacketCreator.showBoughtStorageSlots(chr.getStorage().getSlots()));
                            c.sendPacket(PacketCreator.showCash(chr));
                        } else {
                            log.warn("Could not add {} slots to {}'s account.", qty, Character.makeMapleReadable(chr.getName()));
                        }
                    } else {
                        ModifiedCashItemDO cItem = CashItemFactory.getItem(p.readInt());

                        if (!canBuy(chr, cItem, cs.getCash(cash))) {
                            c.enableCSActions();
                            return;
                        }
                        int qty = 8;
                        if (!chr.getStorage().canGainSlots(qty)) {
                            c.enableCSActions();
                            return;
                        }
                        cs.gainCash(cash, cItem, chr.getWorld());
                        if (chr.getStorage().gainSlots(qty)) {    // thanks ABaldParrot & Thora for detecting storage issues here
                            log.debug("Chr {} bought {} slots to their account storage", c.getPlayer().getName(), qty);
                            chr.setUsedStorage();

                            c.sendPacket(PacketCreator.showBoughtStorageSlots(chr.getStorage().getSlots()));
                            c.sendPacket(PacketCreator.showCash(chr));
                        } else {
                            log.warn("Could not add {} slots to {}'s account", qty, Character.makeMapleReadable(chr.getName()));
                        }
                    }
                } else if (action == 0x08) { // Increase Character Slots
                    p.skip(1);
                    int cash = p.readInt();
                    ModifiedCashItemDO cItem = CashItemFactory.getItem(p.readInt());

                    if (!canBuy(chr, cItem, cs.getCash(cash))) {
                        c.enableCSActions();
                        return;
                    }
                    if (!c.canGainCharacterSlot()) {
                        chr.dropMessage(1, "You have already used up all 12 extra character slots.");
                        c.enableCSActions();
                        return;
                    }
                    cs.gainCash(cash, cItem, chr.getWorld());
                    if (c.gainCharacterSlot()) {
                        c.sendPacket(PacketCreator.showBoughtCharacterSlot(c.getCharacterSlots()));
                        c.sendPacket(PacketCreator.showCash(chr));
                    } else {
                        log.warn("Could not add a chr slot to {}'s account", Character.makeMapleReadable(chr.getName()));
                        c.enableCSActions();
                        return;
                    }
                } else if (action == 0x0D) { // Take from Cash Inventory
                    Item item = cs.findByCashId(p.readInt());
                    if (item == null) {
                        c.enableCSActions();
                        return;
                    }
                    if (chr.getInventory(item.getInventoryType()).addItem(item) != -1) {
                        cs.removeFromInventory(item);
                        c.sendPacket(PacketCreator.takeFromCashInventory(item));

                        if (item instanceof Equip equip) {
                            if (equip.getRingId() >= 0) {
                                Ring ring = Ring.loadFromDb(equip.getRingId());
                                chr.addPlayerRing(ring);
                            }
                        }
                    }
                } else if (action == 0x0E) { // Put into Cash Inventory
                    int cashId = p.readInt();
                    p.skip(4);

                    byte invType = p.readByte();
                    if (invType < 1 || invType > 5) {
                        c.disconnect(false, false);
                        return;
                    }

                    Inventory mi = chr.getInventory(InventoryType.getByType(invType));
                    Item item = mi.findByCashId(cashId);
                    if (item == null) {
                        c.enableCSActions();
                        return;
                    } else if (c.getPlayer().getPetIndex(item.getPetId()) > -1) {
                        chr.getClient().sendPacket(PacketCreator.serverNotice(1, "You cannot put the pet you currently equip into the Cash Shop inventory."));
                        c.enableCSActions();
                        return;
                    } else if (ItemId.isWeddingRing(item.getItemId()) || ItemId.isWeddingToken(item.getItemId())) {
                        chr.getClient().sendPacket(PacketCreator.serverNotice(1, "You cannot put relationship items into the Cash Shop inventory."));
                        c.enableCSActions();
                        return;
                    }
                    cs.addToInventory(item);
                    mi.removeSlot(item.getPosition());
                    c.sendPacket(PacketCreator.putIntoCashInventory(item, c.getAccID()));
                } else if (action == 0x1D) { //crush ring (action 28)
                    int birthday = p.readInt();
                    if (checkBirthday(c, birthday)) {
                        int toCharge = p.readInt();
                        int SN = p.readInt();
                        String recipientName = p.readString();
                        String text = p.readString();
                        ModifiedCashItemDO itemRing = CashItemFactory.getItem(SN);
                        Character partner = c.getChannelServer().getPlayerStorage().getCharacterByName(recipientName);
                        if (partner == null) {
                            chr.sendPacket(PacketCreator.serverNotice(1, "The partner you specified cannot be found.\r\nPlease make sure your partner is online and in the same channel."));
                        } else {

                          /*  if (partner.getGender() == chr.getGender()) {
                                chr.dropMessage(5, "You and your partner are the same gender, please buy a friendship ring.");
                                c.enableCSActions();
                                return;
                            }*/ //Gotta let them faggots marry too, hence why this is commented out <3 

                            if (itemRing.toItem() instanceof Equip eqp) {
                                Pair<Integer, Integer> rings = Ring.createRing(itemRing.getItemId(), chr, partner);
                                eqp.setRingId(rings.getLeft());
                                cs.addToInventory(eqp);
                                c.sendPacket(PacketCreator.showBoughtCashItem(eqp, c.getAccID()));
                                cs.gainCash(toCharge, itemRing, chr.getWorld());
                                cs.gift(partner.getId(), chr.getName(), text, eqp.getSN(), rings.getRight());
                                chr.getCrushRings().add(Ring.loadFromDb(rings.getLeft()));
                                noteService.sendWithFame(text, chr.getName(), partner.getName());
                                noteService.show(partner);
                            }
                        }
                    } else {
                        c.sendPacket(PacketCreator.showCashShopMessage((byte) 0xC4));
                    }

                    c.sendPacket(PacketCreator.showCash(c.getPlayer()));
                } else if (action == 0x20) {
                    int serialNumber = p.readInt();  // thanks GabrielSin for detecting a potential exploit with 1 meso cash items.
                    if (serialNumber / 10000000 != 8) {
                        c.sendPacket(PacketCreator.showCashShopMessage((byte) 0xC0));
                        return;
                    }

                    ModifiedCashItemDO item = CashItemFactory.getItem(serialNumber);
                    if (item == null || !item.isSelling()) {
                        c.sendPacket(PacketCreator.showCashShopMessage((byte) 0xC0));
                        return;
                    }

                    int itemId = item.getItemId();
                    int itemPrice = item.getPrice();
                    if (itemPrice <= 0) {
                        c.sendPacket(PacketCreator.showCashShopMessage((byte) 0xC0));
                        return;
                    }

                    if (chr.getMeso() >= itemPrice) {
                        if (chr.canHold(itemId)) {
                            chr.gainMeso(-itemPrice, false);
                            InventoryManipulator.addById(c, itemId, (short) 1, "", -1);
                            c.sendPacket(PacketCreator.showBoughtQuestItem(itemId));
                        }
                    }
                    c.sendPacket(PacketCreator.showCash(c.getPlayer()));
                } else if (action == 0x23) { //Friendship :3
                    int birthday = p.readInt();
                    if (checkBirthday(c, birthday)) {
                        int payment = p.readByte();
                        p.skip(3); //0s
                        int snID = p.readInt();
                        ModifiedCashItemDO itemRing = CashItemFactory.getItem(snID);
                        String sentTo = p.readString();
                        String text = p.readString();
                        Character partner = c.getChannelServer().getPlayerStorage().getCharacterByName(sentTo);
                        if (partner == null) {
                            c.sendPacket(PacketCreator.showCashShopMessage((byte) 0xBE));
                        } else {
                            // Need to check to make sure its actually an equip and the right SN...
                            if (itemRing.toItem() instanceof Equip eqp) {
                                Pair<Integer, Integer> rings = Ring.createRing(itemRing.getItemId(), chr, partner);
                                eqp.setRingId(rings.getLeft());
                                cs.addToInventory(eqp);
                                c.sendPacket(PacketCreator.showBoughtCashRing(eqp, partner.getName(), c.getAccID()));
                                cs.gainCash(payment, -itemRing.getPrice());
                                cs.gift(partner.getId(), chr.getName(), text, eqp.getSN(), rings.getRight());
                                chr.getFriendshipRings().add(Ring.loadFromDb(rings.getLeft()));
                                noteService.sendWithFame(text, chr.getName(), partner.getName());
                                noteService.show(partner);
                            }
                        }
                    } else {
                        c.sendPacket(PacketCreator.showCashShopMessage((byte) 0xC4));
                    }

                    c.sendPacket(PacketCreator.showCash(c.getPlayer()));
                } else if (action == 0x2E) { //name change
                    ModifiedCashItemDO cItem = CashItemFactory.getItem(p.readInt());
                    if (cItem == null || !canBuy(chr, cItem, cs.getCash(CashShop.NX_PREPAID))) {
                        c.sendPacket(PacketCreator.showCashShopMessage((byte) 0));
                        c.enableCSActions();
                        return;
                    }
                    if (cItem.getSn() == 50600000 && GameConfig.getServerBoolean("allow_cash_shop_name_change")) {
                        p.readString(); //old name
                        String newName = p.readString();
                        if (!Character.canCreateChar(newName) || chr.getLevel() < 10) { //(longest ban duration isn't tracked currently)
                            c.sendPacket(PacketCreator.showCashShopMessage((byte) 0));
                            c.enableCSActions();
                            return;
                        } else if (c.getTempBanCalendar() != null && (c.getTempBanCalendar().getTimeInMillis() + DAYS.toMillis(30)) > Calendar.getInstance().getTimeInMillis()) {
                            c.sendPacket(PacketCreator.showCashShopMessage((byte) 0));
                            c.enableCSActions();
                            return;
                        }
                        if (chr.registerNameChange(newName)) { //success
                            Item item = cItem.toItem();
                            c.sendPacket(PacketCreator.showNameChangeSuccess(item, c.getAccID()));
                            cs.gainCash(4, cItem, chr.getWorld());
                            cs.addToInventory(item);
                        } else {
                            c.sendPacket(PacketCreator.showCashShopMessage((byte) 0));
                        }
                    }
                    c.enableCSActions();
                } else if (action == 0x31) { //world transfer
                    ModifiedCashItemDO cItem = CashItemFactory.getItem(p.readInt());
                    if (cItem == null || !canBuy(chr, cItem, cs.getCash(CashShop.NX_PREPAID))) {
                        c.sendPacket(PacketCreator.showCashShopMessage((byte) 0));
                        c.enableCSActions();
                        return;
                    }
                    if (cItem.getSn() == 50600001 && GameConfig.getServerBoolean("allow_cash_shop_world_transfer")) {
                        int newWorldSelection = p.readInt();

                        int worldTransferError = chr.checkWorldTransferEligibility();
                        if (worldTransferError != 0 || newWorldSelection >= Server.getInstance().getWorldsSize() || Server.getInstance().getWorldsSize() <= 1) {
                            c.sendPacket(PacketCreator.showCashShopMessage((byte) 0));
                            return;
                        } else if (newWorldSelection == c.getWorld()) {
                            c.sendPacket(PacketCreator.showCashShopMessage((byte) 0xDC));
                            return;
                        } else if (c.getAvailableCharacterWorldSlots(newWorldSelection) < 1 || Server.getInstance().getAccountWorldCharacterCount(c.getAccID(), newWorldSelection) >= 3) {
                            c.sendPacket(PacketCreator.showCashShopMessage((byte) 0xDF));
                            return;
                        } else if (chr.registerWorldTransfer(newWorldSelection)) {
                            Item item = cItem.toItem();
                            c.sendPacket(PacketCreator.showWorldTransferSuccess(item, c.getAccID()));
                            cs.gainCash(4, cItem, chr.getWorld());
                            cs.addToInventory(item);
                        } else {
                            c.sendPacket(PacketCreator.showCashShopMessage((byte) 0));
                        }
                    }
                    c.enableCSActions();
                } else {
                    log.warn("Unhandled action: {}, packet: {}", action, p);
                }
            } finally {
                c.releaseClient();
            }
        } else {
            c.sendPacket(PacketCreator.enableActions());
        }
    }

    public static boolean checkBirthday(Client c, int idate) {
        int year = idate / 10000;
        int month = (idate - year * 10000) / 100;
        int day = idate - year * 10000 - month * 100;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);
        cal.set(year, month - 1, day);
        return c.checkBirthDate(cal);
    }

    private static boolean canBuy(Character chr, ModifiedCashItemDO item, int cash) {
        if (item != null && item.isSelling() && item.getPrice() <= cash) {
            log.info("玩家 {} 购买了现金道具 {} (SN {}) 花费 {}", chr, ItemInformationProvider.getInstance().getName(item.getItemId()), item.getSn(), item.getPrice());
            return true;
        } else {
            return false;
        }
    }
}
