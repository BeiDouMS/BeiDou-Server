/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gms.net.server.channel.handlers;


import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.client.inventory.Inventory;
import org.gms.client.inventory.InventoryType;
import org.gms.client.inventory.Item;
import org.gms.client.inventory.manipulator.InventoryManipulator;
import org.gms.client.inventory.manipulator.KarmaManipulator;
import org.gms.config.GameConfig;
import org.gms.constants.inventory.ItemConstants;
import org.gms.net.AbstractPacketHandler;
import org.gms.net.packet.InPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gms.server.Marriage;
import org.gms.util.PacketCreator;
import org.gms.util.packets.WeddingPackets;

import java.util.Collections;
import java.util.List;

/**
 * @author Drago (Dragohe4rt)
 */
public final class WeddingHandler extends AbstractPacketHandler {
    private static final Logger log = LoggerFactory.getLogger(WeddingHandler.class);

    @Override
    public void handlePacket(InPacket p, Client c) {

        if (c.tryacquireClient()) {
            try {
                Character chr = c.getPlayer();
                final byte mode = p.readByte();

                if (mode == 6) { //additem
                    short slot = p.readShort();
                    int itemid = p.readInt();
                    short quantity = p.readShort();

                    Marriage marriage = c.getPlayer().getMarriageInstance();
                    if (marriage != null) {
                        try {
                            boolean groomWishlist = marriage.giftItemToSpouse(chr.getId());
                            String groomWishlistProp = "giftedItem" + (groomWishlist ? "G" : "B") + chr.getId();

                            int giftCount = marriage.getIntProperty(groomWishlistProp);
                            if (giftCount < GameConfig.getServerInt("wedding_gift_limit")) {
                                int cid = marriage.getIntProperty(groomWishlist ? "groomId" : "brideId");
                                if (chr.getId() != cid) {   // cannot gift yourself
                                    Character spouse = marriage.getPlayerById(cid);
                                    if (spouse != null) {
                                        InventoryType type = ItemConstants.getInventoryType(itemid);
                                        Inventory chrInv = chr.getInventory(type);

                                        Item newItem = null;
                                        chrInv.lockInventory();
                                        try {
                                            Item item = chrInv.getItem((byte) slot);
                                            if (item != null) {
                                                if (!item.isUntradeable()) {
                                                    if (itemid == item.getItemId() && quantity <= item.getQuantity()) {
                                                        newItem = item.copy();
                                                        newItem.setQuantity(quantity);
                                                        marriage.addGiftItem(groomWishlist, newItem);
                                                        InventoryManipulator.removeFromSlot(c, type, slot, quantity, false, false);

                                                        KarmaManipulator.toggleKarmaFlagToUntradeable(newItem);
                                                        marriage.setIntProperty(groomWishlistProp, giftCount + 1);

                                                        c.sendPacket(WeddingPackets.onWeddingGiftResult((byte) 0xB, marriage.getWishlistItems(groomWishlist), Collections.singletonList(newItem)));
                                                    }
                                                } else {
                                                    c.sendPacket(WeddingPackets.onWeddingGiftResult((byte) 0xE, marriage.getWishlistItems(groomWishlist), null));
                                                }
                                            }
                                        } finally {
                                            chrInv.unlockInventory();
                                        }

                                        if (newItem != null) {
                                            if (GameConfig.getServerBoolean("use_enforce_merchant_save")) {
                                                chr.saveCharToDB(false);
                                            }
                                            marriage.saveGiftItemsToDb(c, groomWishlist, cid);
                                        }
                                    } else {
                                        c.sendPacket(WeddingPackets.onWeddingGiftResult((byte) 0xE, marriage.getWishlistItems(groomWishlist), null));
                                    }
                                } else {
                                    c.sendPacket(WeddingPackets.onWeddingGiftResult((byte) 0xE, marriage.getWishlistItems(groomWishlist), null));
                                }
                            } else {
                                c.sendPacket(WeddingPackets.onWeddingGiftResult((byte) 0xC, marriage.getWishlistItems(groomWishlist), null));
                            }
                        } catch (NumberFormatException nfe) {
                        }
                    } else {
                        c.sendPacket(PacketCreator.enableActions());
                    }
                } else if (mode == 7) { // take items
                    p.readByte();    // invType
                    int itemPos = p.readByte();

                    Marriage marriage = chr.getMarriageInstance();
                    if (marriage != null) {
                        Boolean groomWishlist = marriage.isMarriageGroom(chr);
                        if (groomWishlist != null) {
                            Item item = marriage.getGiftItem(c, groomWishlist, itemPos);
                            if (item != null) {
                                if (Inventory.checkSpot(chr, item)) {
                                    marriage.removeGiftItem(groomWishlist, item);
                                    marriage.saveGiftItemsToDb(c, groomWishlist, chr.getId());

                                    InventoryManipulator.addFromDrop(c, item, true);

                                    c.sendPacket(WeddingPackets.onWeddingGiftResult((byte) 0xF, marriage.getWishlistItems(groomWishlist), marriage.getGiftItems(c, groomWishlist)));
                                } else {
                                    c.getPlayer().dropMessage(1, "Free a slot on your inventory before collecting this item.");
                                    c.sendPacket(WeddingPackets.onWeddingGiftResult((byte) 0xE, marriage.getWishlistItems(groomWishlist), marriage.getGiftItems(c, groomWishlist)));
                                }
                            } else {
                                c.getPlayer().dropMessage(1, "You have already collected this item.");
                                c.sendPacket(WeddingPackets.onWeddingGiftResult((byte) 0xE, marriage.getWishlistItems(groomWishlist), marriage.getGiftItems(c, groomWishlist)));
                            }
                        }
                    } else {
                        List<Item> items = c.getAbstractPlayerInteraction().getUnclaimedMarriageGifts();
                        try {
                            Item item = items.get(itemPos);
                            if (Inventory.checkSpot(chr, item)) {
                                items.remove(itemPos);
                                Marriage.saveGiftItemsToDb(c, items, chr.getId());

                                InventoryManipulator.addFromDrop(c, item, true);
                                c.sendPacket(WeddingPackets.onWeddingGiftResult((byte) 0xF, Collections.singletonList(""), items));
                            } else {
                                c.getPlayer().dropMessage(1, "Free a slot on your inventory before collecting this item.");
                                c.sendPacket(WeddingPackets.onWeddingGiftResult((byte) 0xE, Collections.singletonList(""), items));
                            }
                        } catch (Exception e) {
                            c.getPlayer().dropMessage(1, "You have already collected this item.");
                            c.sendPacket(WeddingPackets.onWeddingGiftResult((byte) 0xE, Collections.singletonList(""), items));
                        }
                    }
                } else if (mode == 8) { // out of Wedding Registry
                    c.sendPacket(PacketCreator.enableActions());
                } else {
                    log.warn("Unhandled wedding mode: {}", mode);
                }
            } finally {
                c.releaseClient();
            }
        }
    }
}
