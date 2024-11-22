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
import org.gms.client.inventory.Inventory;
import org.gms.client.inventory.InventoryType;
import org.gms.client.inventory.Item;
import org.gms.client.inventory.manipulator.InventoryManipulator;
import org.gms.client.inventory.manipulator.KarmaManipulator;
import org.gms.config.GameConfig;
import org.gms.constants.game.GameConstants;
import org.gms.constants.id.ItemId;
import org.gms.constants.inventory.ItemConstants;
import org.gms.net.AbstractPacketHandler;
import org.gms.net.packet.InPacket;
import org.gms.util.I18nUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gms.server.ItemInformationProvider;
import org.gms.server.Trade;
import org.gms.server.maps.FieldLimit;
import org.gms.server.maps.HiredMerchant;
import org.gms.server.maps.MapObject;
import org.gms.server.maps.MapObjectType;
import org.gms.server.maps.MiniGame;
import org.gms.server.maps.MiniGame.MiniGameType;
import org.gms.server.maps.PlayerShop;
import org.gms.server.maps.PlayerShopItem;
import org.gms.server.maps.Portal;
import org.gms.util.PacketCreator;

import java.awt.*;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * @author Matze
 * @author Ronan - concurrency safety and reviewed minigames
 */
public final class PlayerInteractionHandler extends AbstractPacketHandler {
    private static final Logger log = LoggerFactory.getLogger(PlayerInteractionHandler.class);

    public enum Action {
        CREATE(0),
        INVITE(2),
        DECLINE(3),
        VISIT(4),
        ROOM(5),
        CHAT(6),
        CHAT_THING(8),
        EXIT(0xA),
        OPEN_STORE(0xB),
        OPEN_CASH(0xE),
        SET_ITEMS(0xF),
        SET_MESO(0x10),
        CONFIRM(0x11),
        TRANSACTION(0x14),
        ADD_ITEM(0x16),
        BUY(0x17),
        UPDATE_MERCHANT(0x19),
        UPDATE_PLAYERSHOP(0x1A),
        REMOVE_ITEM(0x1B),
        BAN_PLAYER(0x1C),
        MERCHANT_THING(0x1D),
        OPEN_THING(0x1E),
        PUT_ITEM(0x21),
        MERCHANT_BUY(0x22),
        TAKE_ITEM_BACK(0x26),
        MAINTENANCE_OFF(0x27),
        MERCHANT_ORGANIZE(0x28),
        CLOSE_MERCHANT(0x29),
        REAL_CLOSE_MERCHANT(0x2A),
        MERCHANT_MESO(0x2B),
        SOMETHING(0x2D),
        VIEW_VISITORS(0x2E),
        VIEW_BLACKLIST(0x2F),
        ADD_TO_BLACKLIST(0x30),
        REMOVE_FROM_BLACKLIST(0x31),
        REQUEST_TIE(0x32),
        ANSWER_TIE(0x33),
        GIVE_UP(0x34),
        EXIT_AFTER_GAME(0x38),
        CANCEL_EXIT_AFTER_GAME(0x39),
        READY(0x3A),
        UN_READY(0x3B),
        EXPEL(0x3C),
        START(0x3D),
        GET_RESULT(0x3E),
        SKIP(0x3F),
        MOVE_OMOK(0x40),
        SELECT_CARD(0x44);
        final byte code;

        Action(int code) {
            this.code = (byte) code;
        }

        public byte getCode() {
            return code;
        }
    }

    private static int establishMiniroomStatus(Character chr, boolean isMinigame) {
        if (isMinigame && FieldLimit.CANNOTMINIGAME.check(chr.getMap().getFieldLimit())) {
            return 11;
        }

        if (chr.getChalkboard() != null) {
            return 13;
        }

        if (chr.getEventInstance() != null) {
            return 5;
        }

        return 0;
    }

    @Override
    public final void handlePacket(InPacket p, Client c) {
        if (!c.tryacquireClient()) {    // thanks GabrielSin for pointing dupes within player interactions
            c.sendPacket(PacketCreator.enableActions());
            return;
        }

        try {
            byte mode = p.readByte();
            final Character chr = c.getPlayer();

            if (mode == Action.CREATE.getCode()) {
                if (!chr.isAlive()) {    // thanks GabrielSin for pointing this
                    chr.sendPacket(PacketCreator.getMiniRoomError(4));
                    return;
                }

                byte createType = p.readByte();
                if (createType == 3) {  // trade
                    Trade.startTrade(chr);
                } else if (createType == 1) { // omok mini game
                    int status = establishMiniroomStatus(chr, true);
                    if (status > 0) {
                        chr.sendPacket(PacketCreator.getMiniRoomError(status));
                        return;
                    }

                    String desc = p.readString();
                    String pw;

                    if (p.readByte() != 0) {
                        pw = p.readString();
                    } else {
                        pw = "";
                    }

                    int type = p.readByte();
                    if (type > 11) {
                        type = 11;
                    } else if (type < 0) {
                        type = 0;
                    }
                    if (!chr.haveItem(ItemId.MINI_GAME_BASE + type)) {
                        chr.sendPacket(PacketCreator.getMiniRoomError(6));
                        return;
                    }

                    MiniGame game = new MiniGame(chr, desc, pw);
                    chr.setMiniGame(game);
                    game.setPieceType(type);
                    game.setGameType(MiniGameType.OMOK);
                    chr.getMap().addMapObject(game);
                    chr.getMap().broadcastMessage(PacketCreator.addOmokBox(chr, 1, 0));
                    game.sendOmok(c, type);
                } else if (createType == 2) { // matchcard
                    int status = establishMiniroomStatus(chr, true);
                    if (status > 0) {
                        chr.sendPacket(PacketCreator.getMiniRoomError(status));
                        return;
                    }

                    String desc = p.readString();
                    String pw;

                    if (p.readByte() != 0) {
                        pw = p.readString();
                    } else {
                        pw = "";
                    }

                    int type = p.readByte();
                    if (type > 2) {
                        type = 2;
                    } else if (type < 0) {
                        type = 0;
                    }
                    if (!chr.haveItem(ItemId.MATCH_CARDS)) {
                        chr.sendPacket(PacketCreator.getMiniRoomError(6));
                        return;
                    }

                    MiniGame game = new MiniGame(chr, desc, pw);
                    game.setPieceType(type);
                    if (type == 0) {
                        game.setMatchesToWin(6);
                    } else if (type == 1) {
                        game.setMatchesToWin(10);
                    } else if (type == 2) {
                        game.setMatchesToWin(15);
                    }
                    game.setGameType(MiniGameType.MATCH_CARD);
                    chr.setMiniGame(game);
                    chr.getMap().addMapObject(game);
                    chr.getMap().broadcastMessage(PacketCreator.addMatchCardBox(chr, 1, 0));
                    game.sendMatchCard(c, type);
                } else if (createType == 4 || createType == 5) { // shop
                    if (!GameConstants.isFreeMarketRoom(chr.getMapId())) {
                        chr.sendPacket(PacketCreator.getMiniRoomError(15));
                        return;
                    }

                    int status = establishMiniroomStatus(chr, false);
                    if (status > 0) {
                        chr.sendPacket(PacketCreator.getMiniRoomError(status));
                        return;
                    }

                    if (!canPlaceStore(chr)) {
                        return;
                    }

                    String desc = p.readString();
                    p.skip(3);
                    int itemId = p.readInt();
                    if (chr.getInventory(InventoryType.CASH).countById(itemId) < 1) {
                        chr.sendPacket(PacketCreator.getMiniRoomError(6));
                        return;
                    }

                    if (ItemConstants.isPlayerShop(itemId)) {
                        PlayerShop shop = new PlayerShop(chr, desc, itemId);
                        chr.setPlayerShop(shop);
                        chr.getMap().addMapObject(shop);
                        shop.sendShop(c);
                        c.getWorldServer().registerPlayerShop(shop);
                        //c.sendPacket(PacketCreator.getPlayerShopRemoveVisitor(1));
                    } else if (ItemConstants.isHiredMerchant(itemId)) {
                        HiredMerchant merchant = new HiredMerchant(chr, desc, itemId);
                        chr.setHiredMerchant(merchant);
                        c.getWorldServer().registerHiredMerchant(merchant);
                        chr.getClient().getChannelServer().addHiredMerchant(chr.getId(), merchant);
                        chr.sendPacket(PacketCreator.getHiredMerchant(chr, merchant, true));
                    }
                }
            } else if (mode == Action.INVITE.getCode()) {
                int otherCid = p.readInt();
                Character other = chr.getMap().getCharacterById(otherCid);
                if (other == null || chr.getId() == other.getId()) {
                    return;
                }

                Trade.inviteTrade(chr, other);
            } else if (mode == Action.DECLINE.getCode()) {
                Trade.declineTrade(chr);
            } else if (mode == Action.VISIT.getCode()) {
                if (chr.getTrade() != null && chr.getTrade().getPartner() != null) {
                    if (!chr.getTrade().isFullTrade() && !chr.getTrade().getPartner().isFullTrade()) {
                        Trade.visitTrade(chr, chr.getTrade().getPartner().getChr());
                    } else {
                        chr.sendPacket(PacketCreator.getMiniRoomError(2));
                        return;
                    }
                } else {
                    if (isTradeOpen(chr)) {
                        return;
                    }

                    int oid = p.readInt();
                    MapObject ob = chr.getMap().getMapObject(oid);
                    if (ob instanceof PlayerShop shop) {
                        shop.visitShop(chr);
                    } else if (ob instanceof MiniGame game) {
                        p.skip(1);
                        String pw = p.available() > 1 ? p.readString() : "";

                        if (game.checkPassword(pw)) {
                            if (game.hasFreeSlot() && !game.isVisitor(chr)) {
                                game.addVisitor(chr);
                                chr.setMiniGame(game);
                                switch (game.getGameType()) {
                                    case OMOK:
                                        game.sendOmok(c, game.getPieceType());
                                        break;
                                    case MATCH_CARD:
                                        game.sendMatchCard(c, game.getPieceType());
                                        break;
                                }
                            } else {
                                chr.sendPacket(PacketCreator.getMiniRoomError(2));
                            }
                        } else {
                            chr.sendPacket(PacketCreator.getMiniRoomError(22));
                        }
                    } else if (ob instanceof HiredMerchant merchant && chr.getHiredMerchant() == null) {
                        merchant.visitShop(chr);
                    }
                }
            } else if (mode == Action.CHAT.getCode()) { // chat lol
                HiredMerchant merchant = chr.getHiredMerchant();
                if (chr.getTrade() != null) {
                    chr.getTrade().chat(p.readString());
                } else if (chr.getPlayerShop() != null) { //mini game
                    PlayerShop shop = chr.getPlayerShop();
                    if (shop != null) {
                        shop.chat(c, p.readString());
                    }
                } else if (chr.getMiniGame() != null) {
                    MiniGame game = chr.getMiniGame();
                    if (game != null) {
                        game.chat(c, p.readString());
                    }
                } else if (merchant != null) {
                    merchant.sendMessage(chr, p.readString());
                }
            } else if (mode == Action.EXIT.getCode()) {
                if (chr.getTrade() != null) {
                    Trade.cancelTrade(chr, Trade.TradeResult.PARTNER_CANCEL);
                } else {
                    chr.closePlayerShop();
                    chr.closeMiniGame(false);
                    chr.closeHiredMerchant(true);
                }
            } else if (mode == Action.OPEN_STORE.getCode() || mode == Action.OPEN_CASH.getCode()) {
                if (isTradeOpen(chr)) {
                    return;
                }

                if (mode == Action.OPEN_STORE.getCode()) {
                    p.readByte();    //01
                } else {
                    p.readShort();
                    int birthday = p.readInt();
                    if (!CashOperationHandler.checkBirthday(c, birthday)) { // birthday check here found thanks to lucasziron //感谢lucasziron，生日支票在这里找到了
                        c.sendPacket(PacketCreator.serverNotice(1, I18nUtil.getMessage("PlayerInteractionHandler.message1")));
                        return;
                    }

                    c.sendPacket(PacketCreator.hiredMerchantOwnerMaintenanceLeave());
                }

                if (!canPlaceStore(chr)) {    // thanks Ari for noticing player shops overlapping on opening time   //感谢阿里注意到玩家商店在开放时间重叠
                    return;
                }

                PlayerShop shop = chr.getPlayerShop();
                HiredMerchant merchant = chr.getHiredMerchant();
                if (shop != null && shop.isOwner(chr)) {
                    if (GameConfig.getServerBoolean("use_erase_permit_on_open_shop")) {
                        try {
                            InventoryManipulator.removeById(c, InventoryType.CASH, shop.getItemId(), 1, true, false);
                        } catch (RuntimeException re) {
                        } // fella does not have a player shop permit... //这家伙没有玩家商店许可证。。。
                    }

                    chr.getMap().broadcastMessage(PacketCreator.updatePlayerShopBox(shop));
                    shop.setOpen(true);
                } else if (merchant != null && merchant.isOwner(chr)) {
                    chr.setHasMerchant(true);
                    merchant.setOpen(true);
                    chr.getMap().addMapObject(merchant);
                    chr.setHiredMerchant(null);
                    chr.getMap().broadcastMessage(PacketCreator.spawnHiredMerchantBox(merchant));
                }
            } else if (mode == Action.READY.getCode()) {
                MiniGame game = chr.getMiniGame();
                game.broadcast(PacketCreator.getMiniGameReady(game));
            } else if (mode == Action.UN_READY.getCode()) {
                MiniGame game = chr.getMiniGame();
                game.broadcast(PacketCreator.getMiniGameUnReady(game));
            } else if (mode == Action.START.getCode()) {
                MiniGame game = chr.getMiniGame();
                if (game.getGameType().equals(MiniGameType.OMOK)) {
                    game.minigameMatchStarted();
                    game.broadcast(PacketCreator.getMiniGameStart(game, game.getLoser()));
                    chr.getMap().broadcastMessage(PacketCreator.addOmokBox(game.getOwner(), 2, 1));
                } else if (game.getGameType().equals(MiniGameType.MATCH_CARD)) {
                    game.minigameMatchStarted();
                    game.shuffleList();
                    game.broadcast(PacketCreator.getMatchCardStart(game, game.getLoser()));
                    chr.getMap().broadcastMessage(PacketCreator.addMatchCardBox(game.getOwner(), 2, 1));
                }
            } else if (mode == Action.GIVE_UP.getCode()) {
                MiniGame game = chr.getMiniGame();
                if (game.getGameType().equals(MiniGameType.OMOK)) {
                    if (game.isOwner(chr)) {
                        game.minigameMatchVisitorWins(true);
                    } else {
                        game.minigameMatchOwnerWins(true);
                    }
                } else if (game.getGameType().equals(MiniGameType.MATCH_CARD)) {
                    if (game.isOwner(chr)) {
                        game.minigameMatchVisitorWins(true);
                    } else {
                        game.minigameMatchOwnerWins(true);
                    }
                }
            } else if (mode == Action.REQUEST_TIE.getCode()) {
                MiniGame game = chr.getMiniGame();
                if (!game.isTieDenied(chr)) {
                    if (game.isOwner(chr)) {
                        game.broadcastToVisitor(PacketCreator.getMiniGameRequestTie(game));
                    } else {
                        game.broadcastToOwner(PacketCreator.getMiniGameRequestTie(game));
                    }
                }
            } else if (mode == Action.ANSWER_TIE.getCode()) {
                MiniGame game = chr.getMiniGame();
                if (p.readByte() != 0) {
                    game.minigameMatchDraw();
                } else {
                    game.denyTie(chr);

                    if (game.isOwner(chr)) {
                        game.broadcastToVisitor(PacketCreator.getMiniGameDenyTie(game));
                    } else {
                        game.broadcastToOwner(PacketCreator.getMiniGameDenyTie(game));
                    }
                }
            } else if (mode == Action.SKIP.getCode()) {
                MiniGame game = chr.getMiniGame();
                if (game.isOwner(chr)) {
                    game.broadcast(PacketCreator.getMiniGameSkipOwner(game));
                } else {
                    game.broadcast(PacketCreator.getMiniGameSkipVisitor(game));
                }
            } else if (mode == Action.MOVE_OMOK.getCode()) {
                int x = p.readInt(); // x point
                int y = p.readInt(); // y point
                int type = p.readByte(); // piece ( 1 or 2; Owner has one piece, visitor has another, it switches every game.)  //棋子（1或2；所有者有一个棋子，访问者有另一个，它会切换每个游戏。）
                chr.getMiniGame().setPiece(x, y, type, chr);
            } else if (mode == Action.SELECT_CARD.getCode()) {
                int turn = p.readByte(); // 1st turn = 1; 2nd turn = 0
                int slot = p.readByte(); // slot
                MiniGame game = chr.getMiniGame();
                int firstslot = game.getFirstSlot();
                if (turn == 1) {
                    game.setFirstSlot(slot);
                    if (game.isOwner(chr)) {
                        game.broadcastToVisitor(PacketCreator.getMatchCardSelect(game, turn, slot, firstslot, turn));
                    } else {
                        game.getOwner().sendPacket(PacketCreator.getMatchCardSelect(game, turn, slot, firstslot, turn));
                    }
                } else if ((game.getCardId(firstslot)) == (game.getCardId(slot))) {
                    if (game.isOwner(chr)) {
                        game.broadcast(PacketCreator.getMatchCardSelect(game, turn, slot, firstslot, 2));
                        game.setOwnerPoints();
                    } else {
                        game.broadcast(PacketCreator.getMatchCardSelect(game, turn, slot, firstslot, 3));
                        game.setVisitorPoints();
                    }
                } else if (game.isOwner(chr)) {
                    game.broadcast(PacketCreator.getMatchCardSelect(game, turn, slot, firstslot, 0));
                } else {
                    game.broadcast(PacketCreator.getMatchCardSelect(game, turn, slot, firstslot, 1));
                }
            } else if (mode == Action.SET_MESO.getCode()) {
                chr.getTrade().setMeso(p.readInt());
            } else if (mode == Action.SET_ITEMS.getCode()) {
                ItemInformationProvider ii = ItemInformationProvider.getInstance();
                InventoryType ivType = InventoryType.getByType(p.readByte());
                short pos = p.readShort();
                Item item = chr.getInventory(ivType).getItem(pos);
                short quantity = p.readShort();
                byte targetSlot = p.readByte();

                if (targetSlot < 1 || targetSlot > 9) {
                    log.warn(I18nUtil.getLogMessage("PlayerInteractionHandler.warn1"), chr.getName());
                    c.sendPacket(PacketCreator.enableActions());
                    return;
                }

                if (item == null) {
                    c.sendPacket(PacketCreator.serverNotice(1, I18nUtil.getMessage("PlayerInteractionHandler.message2")));
                    c.sendPacket(PacketCreator.enableActions());
                    return;
                }
                //此处判断是否为宠物或者其他现金物品
                if (!GameConfig.getServerBoolean("trade_limit_item_cash") && ii.isUnmerchable(item.getItemId())) {
                    if (ItemConstants.isPet(item.getItemId())) {
                        c.sendPacket(PacketCreator.serverNotice(1, I18nUtil.getMessage("PlayerInteractionHandler.message3")));
                    } else {
                        c.sendPacket(PacketCreator.serverNotice(1, I18nUtil.getMessage("PlayerInteractionHandler.message4")));
                    }
                    log.warn(I18nUtil.getLogMessage("PlayerInteractionHandler.warn2"), chr.getName(),item.getInventoryType().getName(),ItemInformationProvider.getInstance().getName(item.getItemId()),item.getItemId());
                    c.sendPacket(PacketCreator.enableActions());
                    return;
                }

                if (quantity < 1 || quantity > item.getQuantity()) {
                    c.sendPacket(PacketCreator.serverNotice(1, I18nUtil.getMessage("PlayerInteractionHandler.message5")));  //交易物品数量不足
                    c.sendPacket(PacketCreator.enableActions());
                    return;
                }

                Trade trade = chr.getTrade();
                if (trade != null) {
                    if ((quantity <= item.getQuantity() && quantity >= 0) || ItemConstants.isRechargeable(item.getItemId())) {
                        //此处判断不可交易/不可丢弃物品是否允许交易
                        if (!GameConfig.getServerBoolean("trade_limit_item_nodrop") && ii.isDropRestricted(item.getItemId())) { // ensure that undroppable items do not make it to the trade window    //确保不可丢弃的物品不会进入交易窗口
                            if (!KarmaManipulator.hasKarmaFlag(item)) {
                                log.warn(I18nUtil.getLogMessage("PlayerInteractionHandler.warn3"), chr.getName(),item.getInventoryType().getName(),item.getItemId());
                                c.sendPacket(PacketCreator.serverNotice(1, I18nUtil.getMessage("PlayerInteractionHandler.message6")));
                                c.sendPacket(PacketCreator.enableActions());
                                return;
                            }
                        }

                        Inventory inv = chr.getInventory(ivType);
                        inv.lockInventory();
                        try {
                            Item checkItem = chr.getInventory(ivType).getItem(pos);
                            if (checkItem != item || checkItem.getPosition() != item.getPosition()) {
                                c.sendPacket(PacketCreator.serverNotice(1, I18nUtil.getMessage("PlayerInteractionHandler.message2")));
                                c.sendPacket(PacketCreator.enableActions());
                                return;
                            }

                            Item tradeItem = item.copy();
                            if (ItemConstants.isRechargeable(item.getItemId())) {
                                quantity = item.getQuantity();
                            }

                            tradeItem.setQuantity(quantity);
                            tradeItem.setPosition(targetSlot);

                            if (trade.addItem(tradeItem)) {
                                InventoryManipulator.removeFromSlot(c, ivType, item.getPosition(), quantity, true);

                                trade.getChr().sendPacket(PacketCreator.getTradeItemAdd((byte) 0, tradeItem));
                                if (trade.getPartner() != null) {
                                    trade.getPartner().getChr().sendPacket(PacketCreator.getTradeItemAdd((byte) 1, tradeItem));
                                }
                            }
                        } catch (Exception e) {
                            log.warn(I18nUtil.getLogMessage("PlayerInteractionHandler.warn4"), chr, ii.getName(item.getItemId()), item.getQuantity(), targetSlot, e);
                        } finally {
                            inv.unlockInventory();
                        }
                    }
                }
            } else if (mode == Action.CONFIRM.getCode()) {
                Trade.completeTrade(chr);
            } else if (mode == Action.ADD_ITEM.getCode() || mode == Action.PUT_ITEM.getCode()) {
                if (isTradeOpen(chr)) {
                    return;
                }

                InventoryType ivType = InventoryType.getByType(p.readByte());
                short slot = p.readShort();
                short bundles = p.readShort();
                Item ivItem = chr.getInventory(ivType).getItem(slot);

                if (ivItem == null || ivItem.isUntradeable()) {
                    c.sendPacket(PacketCreator.serverNotice(1, I18nUtil.getMessage("PlayerInteractionHandler.message7")));
                    c.sendPacket(PacketCreator.enableActions());
                    return;
                } else if (ItemInformationProvider.getInstance().isUnmerchable(ivItem.getItemId())) {
                    if (ItemConstants.isPet(ivItem.getItemId())) {
                        c.sendPacket(PacketCreator.serverNotice(1, I18nUtil.getMessage("PlayerInteractionHandler.message8")));
                    } else {
                        c.sendPacket(PacketCreator.serverNotice(1, I18nUtil.getMessage("PlayerInteractionHandler.message9")));
                    }

                    c.sendPacket(PacketCreator.enableActions());
                    return;
                }

                short perBundle = p.readShort();

                if (ItemConstants.isRechargeable(ivItem.getItemId())) {
                    perBundle = 1;
                    bundles = 1;
                } else if (ivItem.getQuantity() < (bundles * perBundle)) {     // thanks GabrielSin for finding a dupe here //感谢GabrielSin在这里找到了一个骗子
                    c.sendPacket(PacketCreator.serverNotice(1, I18nUtil.getMessage("PlayerInteractionHandler.message7")));
                    c.sendPacket(PacketCreator.enableActions());
                    return;
                }

                int price = p.readInt();
                if (perBundle <= 0 || perBundle * bundles > 2000 || bundles <= 0 || price <= 0 || price > Integer.MAX_VALUE) {
                    AutobanFactory.PACKET_EDIT.alert(chr, chr.getName() + I18nUtil.getMessage("PlayerInteractionHandler.message10"));
                    log.warn(I18nUtil.getLogMessage("PlayerInteractionHandler.warn5"),
                            chr.getName(), perBundle, perBundle * bundles, bundles, price);
                    return;
                }

                Item sellItem = ivItem.copy();
                if (!ItemConstants.isRechargeable(ivItem.getItemId())) {
                    sellItem.setQuantity(perBundle);
                }

                PlayerShopItem shopItem = new PlayerShopItem(sellItem, bundles, price);
                PlayerShop shop = chr.getPlayerShop();
                HiredMerchant merchant = chr.getHiredMerchant();
                if (shop != null && shop.isOwner(chr)) {
                    if (shop.isOpen() || !shop.addItem(shopItem)) { // thanks Vcoc for pointing an exploit with unlimited shop slots    //感谢Vcoc指出了一个具有无限商店插槽的漏洞
                        c.sendPacket(PacketCreator.serverNotice(1, I18nUtil.getMessage("PlayerInteractionHandler.message11")));
                        return;
                    }

                    if (ItemConstants.isRechargeable(ivItem.getItemId())) {
                        InventoryManipulator.removeFromSlot(c, ivType, slot, ivItem.getQuantity(), true);
                    } else {
                        InventoryManipulator.removeFromSlot(c, ivType, slot, (short) (bundles * perBundle), true);
                    }

                    c.sendPacket(PacketCreator.getPlayerShopItemUpdate(shop));
                } else if (merchant != null && merchant.isOwner(chr)) {
                    if (ivType.equals(InventoryType.CASH) && merchant.isPublished()) {
                        c.sendPacket(PacketCreator.serverNotice(1, I18nUtil.getMessage("PlayerInteractionHandler.message12")));
                        return;
                    }

                    if (merchant.isOpen() || !merchant.addItem(shopItem)) { // thanks Vcoc for pointing an exploit with unlimited shop slots    //感谢Vcoc指出了一个具有无限商店插槽的漏洞
                        c.sendPacket(PacketCreator.serverNotice(1, I18nUtil.getMessage("PlayerInteractionHandler.message11")));
                        return;
                    }

                    if (ItemConstants.isRechargeable(ivItem.getItemId())) {
                        InventoryManipulator.removeFromSlot(c, ivType, slot, ivItem.getQuantity(), true);
                    } else {
                        InventoryManipulator.removeFromSlot(c, ivType, slot, (short) (bundles * perBundle), true);
                    }

                    c.sendPacket(PacketCreator.updateHiredMerchant(merchant, chr));

                    if (GameConfig.getServerBoolean("use_enforce_merchant_save")) {
                        chr.saveCharToDB(false);
                    }

                    try {
                        merchant.saveItems(false);   // thanks Masterrulax for realizing yet another dupe with merchants/Fredrick   //感谢Masterrulax再次欺骗商家/弗雷德里克
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    c.sendPacket(PacketCreator.serverNotice(1, I18nUtil.getMessage("PlayerInteractionHandler.message13")));
                }
            } else if (mode == Action.REMOVE_ITEM.getCode()) {
                if (isTradeOpen(chr)) {
                    return;
                }

                PlayerShop shop = chr.getPlayerShop();
                if (shop != null && shop.isOwner(chr)) {
                    if (shop.isOpen()) {
                        c.sendPacket(PacketCreator.serverNotice(1, I18nUtil.getMessage("PlayerInteractionHandler.message14")));
                        return;
                    }

                    int slot = p.readShort();
                    if (slot >= shop.getItems().size() || slot < 0) {
                        AutobanFactory.PACKET_EDIT.alert(chr, chr.getName() + I18nUtil.getMessage("PlayerInteractionHandler.message15"));
                        log.warn(I18nUtil.getLogMessage("PlayerInteractionHandler.warn6"), chr.getName(), slot);
                        c.disconnect(true, false);
                        return;
                    }

                    shop.takeItemBack(slot, chr);
                }
            } else if (mode == Action.MERCHANT_MESO.getCode()) {
                HiredMerchant merchant = chr.getHiredMerchant();
                if (merchant == null) {
                    return;
                }

                merchant.withdrawMesos(chr);

            } else if (mode == Action.VIEW_VISITORS.getCode()) {
                HiredMerchant merchant = chr.getHiredMerchant();
                if (merchant == null || !merchant.isOwner(chr)) {
                    return;
                }
                c.sendPacket(PacketCreator.viewMerchantVisitorHistory(merchant.getVisitorHistory()));
            } else if (mode == Action.VIEW_BLACKLIST.getCode()) {
                HiredMerchant merchant = chr.getHiredMerchant();
                if (merchant == null || !merchant.isOwner(chr)) {
                    return;
                }

                c.sendPacket(PacketCreator.viewMerchantBlacklist(merchant.getBlacklist()));
            } else if (mode == Action.ADD_TO_BLACKLIST.getCode()) {
                HiredMerchant merchant = chr.getHiredMerchant();
                if (merchant == null || !merchant.isOwner(chr)) {
                    return;
                }
                String chrName = p.readString();
                merchant.addToBlacklist(chrName);
            } else if (mode == Action.REMOVE_FROM_BLACKLIST.getCode()) {
                HiredMerchant merchant = chr.getHiredMerchant();
                if (merchant == null || !merchant.isOwner(chr)) {
                    return;
                }
                String chrName = p.readString();
                merchant.removeFromBlacklist(chrName);
            } else if (mode == Action.MERCHANT_ORGANIZE.getCode()) {
                HiredMerchant merchant = chr.getHiredMerchant();
                if (merchant == null || !merchant.isOwner(chr)) {
                    return;
                }

                merchant.withdrawMesos(chr);
                merchant.clearInexistentItems();

                if (merchant.getItems().isEmpty()) {
                    merchant.closeOwnerMerchant(chr);
                    return;
                }
                c.sendPacket(PacketCreator.updateHiredMerchant(merchant, chr));

            } else if (mode == Action.BUY.getCode() || mode == Action.MERCHANT_BUY.getCode()) {
                if (isTradeOpen(chr)) {
                    return;
                }

                int itemid = p.readByte();
                short quantity = p.readShort();
                if (quantity < 1) {
                    AutobanFactory.PACKET_EDIT.alert(chr, chr.getName() + I18nUtil.getMessage("PlayerInteractionHandler.message16"));
                    log.warn(I18nUtil.getLogMessage("PlayerInteractionHandler.warn7"), chr.getName(), itemid, quantity);
                    c.disconnect(true, false);
                    return;
                }
                PlayerShop shop = chr.getPlayerShop();
                HiredMerchant merchant = chr.getHiredMerchant();
                if (shop != null && shop.isVisitor(chr)) {
                    if (shop.buy(c, itemid, quantity)) {
                        shop.broadcast(PacketCreator.getPlayerShopItemUpdate(shop));
                    }
                } else if (merchant != null && !merchant.isOwner(chr)) {
                    merchant.buy(c, itemid, quantity);
                    merchant.broadcastToVisitorsThreadsafe(PacketCreator.updateHiredMerchant(merchant, chr));
                }
            } else if (mode == Action.TAKE_ITEM_BACK.getCode()) {
                if (isTradeOpen(chr)) {
                    return;
                }

                HiredMerchant merchant = chr.getHiredMerchant();
                if (merchant != null && merchant.isOwner(chr)) {
                    if (merchant.isOpen()) {
                        c.sendPacket(PacketCreator.serverNotice(1, I18nUtil.getMessage("PlayerInteractionHandler.message14")));
                        return;
                    }

                    int slot = p.readShort();
                    if (slot >= merchant.getItems().size() || slot < 0) {
                        AutobanFactory.PACKET_EDIT.alert(chr, chr.getName() + I18nUtil.getMessage("PlayerInteractionHandler.message17"));
                        log.warn(I18nUtil.getLogMessage("PlayerInteractionHandler.warn6", chr.getName(), slot));
                        c.disconnect(true, false);
                        return;
                    }

                    merchant.takeItemBack(slot, chr);
                }
            } else if (mode == Action.CLOSE_MERCHANT.getCode()) {
                if (isTradeOpen(chr)) {
                    return;
                }

                HiredMerchant merchant = chr.getHiredMerchant();
                if (merchant != null) {
                    merchant.closeOwnerMerchant(chr);
                }
            } else if (mode == Action.MAINTENANCE_OFF.getCode()) {
                if (isTradeOpen(chr)) {
                    return;
                }

                HiredMerchant merchant = chr.getHiredMerchant();
                if (merchant != null) {
                    if (merchant.isOwner(chr)) {
                        if (merchant.getItems().isEmpty()) {
                            merchant.closeOwnerMerchant(chr);
                        } else {
                            merchant.clearMessages();
                            merchant.setOpen(true);
                            merchant.getMap().broadcastMessage(PacketCreator.updateHiredMerchantBox(merchant));
                        }
                    }
                }

                chr.setHiredMerchant(null);
                c.sendPacket(PacketCreator.enableActions());
            } else if (mode == Action.BAN_PLAYER.getCode()) {
                p.skip(1);

                PlayerShop shop = chr.getPlayerShop();
                if (shop != null && shop.isOwner(chr)) {
                    shop.banPlayer(p.readString());
                }
            } else if (mode == Action.EXPEL.getCode()) {
                MiniGame miniGame = chr.getMiniGame();
                if (miniGame != null && miniGame.isOwner(chr)) {
                    Character visitor = miniGame.getVisitor();

                    if (visitor != null) {
                        visitor.closeMiniGame(false);
                        visitor.sendPacket(PacketCreator.getMiniGameClose(true, 5));
                    }
                }
            } else if (mode == Action.EXIT_AFTER_GAME.getCode()) {
                MiniGame miniGame = chr.getMiniGame();
                if (miniGame != null) {
                    miniGame.setQuitAfterGame(chr, true);
                }
            } else if (mode == Action.CANCEL_EXIT_AFTER_GAME.getCode()) {
                MiniGame miniGame = chr.getMiniGame();
                if (miniGame != null) {
                    miniGame.setQuitAfterGame(chr, false);
                }
            }
        } finally {
            c.releaseClient();
        }
    }

    private static boolean isTradeOpen(Character chr) {
        if (chr.getTrade() != null) {   // thanks to Rien dev team  //感谢Rien开发团队
            //Apparently there is a dupe exploit that causes racing conditions when saving/retrieving from the db with stuff like trade open.
            //显然，在使用诸如trade open之类的东西从数据库中保存/检索时，有一个欺骗漏洞会导致赛车状态。
            chr.sendPacket(PacketCreator.enableActions());
            return true;
        }

        return false;
    }

    private static boolean canPlaceStore(Character chr) {
        try {
            for (MapObject mmo : chr.getMap().getMapObjectsInRange(chr.getPosition(), 23000, Arrays.asList(MapObjectType.HIRED_MERCHANT, MapObjectType.PLAYER))) {
                if (mmo instanceof Character mc) {
                    if (mc.getId() == chr.getId()) {
                        continue;
                    }

                    PlayerShop shop = mc.getPlayerShop();
                    if (shop != null && shop.isOwner(mc)) {
                        chr.sendPacket(PacketCreator.getMiniRoomError(13));
                        return false;
                    }
                } else {
                    chr.sendPacket(PacketCreator.getMiniRoomError(13));
                    return false;
                }
            }

            Point cpos = chr.getPosition();
            Portal portal = chr.getMap().findClosestTeleportPortal(cpos);
            if (portal != null && portal.getPosition().distance(cpos) < 120.0) {
                chr.sendPacket(PacketCreator.getMiniRoomError(10));
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }
}
