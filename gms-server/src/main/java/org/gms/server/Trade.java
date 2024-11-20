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
package org.gms.server;

import org.gms.client.Character;
import org.gms.client.inventory.Inventory;
import org.gms.client.inventory.InventoryType;
import org.gms.client.inventory.Item;
import org.gms.client.inventory.manipulator.InventoryManipulator;
import org.gms.client.inventory.manipulator.KarmaManipulator;
import org.gms.config.GameConfig;
import org.gms.constants.game.GameConstants;
import org.gms.net.server.coordinator.world.InviteCoordinator;
import org.gms.net.server.coordinator.world.InviteCoordinator.InviteResult;
import org.gms.net.server.coordinator.world.InviteCoordinator.InviteResultType;
import org.gms.net.server.coordinator.world.InviteCoordinator.InviteType;
import org.gms.util.I18nUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gms.util.PacketCreator;
import org.gms.util.Pair;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Matze
 * @author Ronan - concurrency safety + check available slots + trade results
                    并发安全+检查可用插槽+交易结果
 */
public class Trade {
    private static final Logger log = LoggerFactory.getLogger(Trade.class);

    public enum TradeResult {
        NO_RESPONSE(1),
        PARTNER_CANCEL(2),
        SUCCESSFUL(7),
        UNSUCCESSFUL(8),
        UNSUCCESSFUL_UNIQUE_ITEM_LIMIT(9),
        UNSUCCESSFUL_ANOTHER_MAP(12),
        UNSUCCESSFUL_DAMAGED_FILES(13);

        private final int res;

        TradeResult(int res) {
            this.res = res;
        }

        private byte getValue() {
            return (byte) res;
        }
    }

    private Trade partner = null;
    private final List<Item> items = new ArrayList<>();
    private List<Item> exchangeItems;
    private int meso = 0;
    private int exchangeMeso;
    private final AtomicBoolean locked = new AtomicBoolean(false);
    private final Character chr;
    private final byte number;
    private boolean fullTrade = false;

    public Trade(byte number, Character chr) {
        this.chr = chr;
        this.number = number;
    }
    /*
    金币税率处理函数
     */
    public static int getFee(long meso) {
        long fee = 0;
        if (meso >= 100000000) {
            fee = (meso * 6) / 100;
        } else if (meso >= 25000000) {
            fee = (meso * 5) / 100;
        } else if (meso >= 10000000) {
            fee = (meso * 4) / 100;
        } else if (meso >= 5000000) {
            fee = (meso * 3) / 100;
        } else if (meso >= 1000000) {
            fee = (meso * 18) / 1000;
        } else if (meso >= 100000) {
            fee = (meso * 8) / 1000;
        }
        return (int) fee;
    }

    private void lockTrade() {
        locked.set(true);
        partner.getChr().sendPacket(PacketCreator.getTradeConfirmation());
    }

    private void fetchExchangedItems() {
        exchangeItems = partner.getItems();
        exchangeMeso = partner.getMeso();
    }

    private void completeTrade() {
        byte result;
        boolean show = GameConfig.getServerBoolean("use_debug");
        items.clear();
        meso = 0;

        for (Item item : exchangeItems) {
            KarmaManipulator.toggleKarmaFlagToUntradeable(item);
            InventoryManipulator.addFromDrop(chr.getClient(), item, show);
        }

        if (exchangeMeso > 0) {//此处对金币交易进行扣税处理
            int fee = getFee(exchangeMeso);

            chr.gainMeso(exchangeMeso - fee, show, true, show);
            if (fee > 0) {
                chr.dropMessage(1, I18nUtil.getMessage("Trade.message.fee",fee,GameConstants.numberWithCommas(exchangeMeso - fee)));
            } else {
                chr.dropMessage(1, I18nUtil.getMessage("Trade.message.nofee",GameConstants.numberWithCommas(exchangeMeso)));
            }

            result = TradeResult.NO_RESPONSE.getValue();
        } else {
            result = TradeResult.SUCCESSFUL.getValue();
        }

        exchangeMeso = 0;
        if (exchangeItems != null) {
            exchangeItems.clear();
        }

        chr.sendPacket(PacketCreator.getTradeResult(number, result));
    }

    private void cancel(byte result) {
        boolean show = GameConfig.getServerBoolean("use_debug");

        for (Item item : items) {
            InventoryManipulator.addFromDrop(chr.getClient(), item, show);
        }
        if (meso > 0) {
            chr.gainMeso(meso, show, true, show);
        }
        meso = 0;
        if (items != null) {
            items.clear();
        }
        exchangeMeso = 0;
        if (exchangeItems != null) {
            exchangeItems.clear();
        }

        chr.sendPacket(PacketCreator.getTradeResult(number, result));
    }

    private boolean isLocked() {
        return locked.get();
    }

    private int getMeso() {
        return meso;
    }

    public void setMeso(int meso) {
        if (locked.get()) {
            throw new RuntimeException(I18nUtil.getLogMessage("Trade.info.setMeso.msg1"));
        }
        if (meso < 0) {
            log.warn(I18nUtil.getLogMessage("Trade.warn.setMeso.msg1"), chr.getName(),meso);
            return;
        }
        if (chr.getMeso() >= meso) {
            chr.gainMeso(-meso, false, true, false);
            this.meso += meso;
            chr.sendPacket(PacketCreator.getTradeMesoSet((byte) 0, this.meso));
            if (partner != null) {
                partner.getChr().sendPacket(PacketCreator.getTradeMesoSet((byte) 1, this.meso));
            }
        } else {
        }
    }

    public boolean addItem(Item item) {
        synchronized (items) {
            if (items.size() > 9) {
                return false;
            }
            for (Item it : items) {
                if (it.getPosition() == item.getPosition()) {
                    return false;
                }
            }

            items.add(item);
        }

        return true;
    }

    public void chat(String message) {
        chr.sendPacket(PacketCreator.getTradeChat(chr, message, true));
        if (partner != null) {
            partner.getChr().sendPacket(PacketCreator.getTradeChat(chr, message, false));
        }
    }

    public Trade getPartner() {
        return partner;
    }

    public void setPartner(Trade partner) {
        if (locked.get()) {
            return;
        }
        this.partner = partner;
    }

    public Character getChr() {
        return chr;
    }

    public List<Item> getItems() {
        return new LinkedList<>(items);
    }

    public int getExchangeMesos() {
        return exchangeMeso;
    }

    private boolean fitsMeso() {
        return chr.canHoldMeso(exchangeMeso - getFee(exchangeMeso));
    }

    private boolean fitsInInventory() {
        List<Pair<Item, InventoryType>> tradeItems = new LinkedList<>();
        for (Item item : exchangeItems) {
            tradeItems.add(new Pair<>(item, item.getInventoryType()));
        }

        return Inventory.checkSpotsAndOwnership(chr, tradeItems);
    }

    private boolean fitsUniquesInInventory() {
        List<Integer> exchangeItemids = new LinkedList<>();
        for (Item item : exchangeItems) {
            exchangeItemids.add(item.getItemId());
        }

        return chr.canHoldUniques(exchangeItemids);
    }

    private synchronized boolean checkTradeCompleteHandshake(boolean updateSelf) {
        Trade self, other;

        if (updateSelf) {
            self = this;
            other = this.getPartner();
        } else {
            self = this.getPartner();
            other = this;
        }

        if (self.isLocked()) {
            return false;
        }

        self.lockTrade();
        return other.isLocked();
    }

    private boolean checkCompleteHandshake() {  // handshake checkout thanks to Ronan   //感谢Ronan的握手结账
        if (this.getChr().getId() < this.getPartner().getChr().getId()) {
            return this.checkTradeCompleteHandshake(true);
        } else {
            return this.getPartner().checkTradeCompleteHandshake(false);
        }
    }
    //完成交易处理
    public static void completeTrade(Character chr) {
        Trade local = chr.getTrade();
        Trade partner = local.getPartner();
        if (local.checkCompleteHandshake()) {
            local.fetchExchangedItems();
            partner.fetchExchangedItems();

            if (!local.fitsMeso()) {
                cancelTrade(local.getChr(), TradeResult.UNSUCCESSFUL);
                chr.message(I18nUtil.getMessage("Trade.message.Mesos.Player"));
                partner.getChr().message(I18nUtil.getMessage("Trade.message.Mesos.Partner"));
                return;
            } else if (!partner.fitsMeso()) {
                cancelTrade(partner.getChr(), TradeResult.UNSUCCESSFUL);
                chr.message(I18nUtil.getMessage("Trade.message.Mesos.Player"));
                partner.getChr().message(I18nUtil.getMessage("Trade.message.Mesos.Partner"));
                return;
            }

            if (!local.fitsInInventory()) {//判断背包空间是否足够
                if (local.fitsUniquesInInventory()) {   //判断当前角色交易物品是否为唯一物品
                    //非唯一物品
                    cancelTrade(local.getChr(), TradeResult.UNSUCCESSFUL);
                    chr.message(I18nUtil.getMessage("Trade.message.fitsInInventory.Player"));
                    partner.getChr().message(I18nUtil.getMessage("Trade.message.fitsInInventory.Partner"));
                } else {
                    //唯一物品
                    cancelTrade(local.getChr(), TradeResult.UNSUCCESSFUL_UNIQUE_ITEM_LIMIT);
                    partner.getChr().message(I18nUtil.getMessage("Trade.message.fitsUniquesInInventory"));
                }
                return;
            } else if (!partner.fitsInInventory()) {//判断背包空间是否足够
                if (partner.fitsUniquesInInventory()) { //判断交易角色交易物品是否为唯一物品
                    //非唯一物品
                    cancelTrade(partner.getChr(), TradeResult.UNSUCCESSFUL);
                    chr.message(I18nUtil.getMessage("Trade.message.fitsInInventory.Player"));
                    partner.getChr().message(I18nUtil.getMessage("Trade.message.fitsInInventory.Partner"));
                } else {
                    //唯一物品
                    cancelTrade(partner.getChr(), TradeResult.UNSUCCESSFUL_UNIQUE_ITEM_LIMIT);
                    chr.message(I18nUtil.getMessage("Trade.message.fitsUniquesInInventory"));
                }
                return;
            }
            //默认限制15级以内的角色每天不可以交易超过100万金币
            int level = GameConfig.getServerInt("trade_limit_meso_under_level") ;
            int mesomax = GameConfig.getServerInt("trade_limit_meso_max");
            if(level == 0) level = 15;
            if(mesomax == 0) mesomax = 1000000;
            if (level != -1 && local.getChr().getLevel() <= level) {
                if (mesomax != -1 && local.getChr().getMesosTraded() + local.exchangeMeso > mesomax) {
                    cancelTrade(local.getChr(), TradeResult.NO_RESPONSE);
                    local.getChr().sendPacket(PacketCreator.serverNotice(1, I18nUtil.getMessage("Trade.message.Mesos.PerDayMax",level,mesomax)));
                    partner.getChr().sendPacket(PacketCreator.serverNotice(1, I18nUtil.getMessage("Trade.message.Mesos.PerDayMax1",level,mesomax)));
                    return;
                } else {
                    local.getChr().addMesosTraded(local.exchangeMeso);
                }
            } else if (level != -1 && partner.getChr().getLevel() <= level) {
                if (mesomax != -1 && partner.getChr().getMesosTraded() + partner.exchangeMeso > mesomax) {
                    cancelTrade(partner.getChr(), TradeResult.NO_RESPONSE);
                    partner.getChr().sendPacket(PacketCreator.serverNotice(1, I18nUtil.getMessage("Trade.message.Mesos.PerDayMax",level,mesomax)));
                    local.getChr().sendPacket(PacketCreator.serverNotice(1, I18nUtil.getMessage("Trade.message.Mesos.PerDayMax1",level,mesomax)));
                    return;
                } else {
                    partner.getChr().addMesosTraded(partner.exchangeMeso);
                }
            }

            logTrade(local, partner);
            local.completeTrade();
            partner.completeTrade();

            partner.getChr().setTrade(null);
            chr.setTrade(null);
        }
    }

    private static void cancelTradeInternal(Character chr, byte selfResult, byte partnerResult) {
        Trade trade = chr.getTrade();
        if (trade == null) {
            return;
        }

        trade.cancel(selfResult);
        if (trade.getPartner() != null) {
            trade.getPartner().cancel(partnerResult);
            trade.getPartner().getChr().setTrade(null);

            InviteCoordinator.answerInvite(InviteType.TRADE, trade.getChr().getId(), trade.getPartner().getChr().getId(), false);
            InviteCoordinator.answerInvite(InviteType.TRADE, trade.getPartner().getChr().getId(), trade.getChr().getId(), false);
        }
        chr.setTrade(null);
    }

    private static byte[] tradeResultsPair(byte result) {
        byte selfResult, partnerResult;

        if (result == TradeResult.PARTNER_CANCEL.getValue()) {
            partnerResult = result;
            selfResult = TradeResult.NO_RESPONSE.getValue();
        } else if (result == TradeResult.UNSUCCESSFUL_UNIQUE_ITEM_LIMIT.getValue()) {
            partnerResult = TradeResult.UNSUCCESSFUL.getValue();
            selfResult = result;
        } else {
            partnerResult = result;
            selfResult = result;
        }

        return new byte[]{selfResult, partnerResult};
    }

    private synchronized void tradeCancelHandshake(boolean updateSelf, byte result) {
        byte selfResult, partnerResult;
        Trade self;

        byte[] pairedResult = tradeResultsPair(result);
        selfResult = pairedResult[0];
        partnerResult = pairedResult[1];

        if (updateSelf) {
            self = this;
        } else {
            self = this.getPartner();
        }

        cancelTradeInternal(self.getChr(), selfResult, partnerResult);
    }

    private void cancelHandshake(byte result) {  // handshake checkout thanks to Ronan
        Trade partner = this.getPartner();
        if (partner == null || this.getChr().getId() < partner.getChr().getId()) {
            this.tradeCancelHandshake(true, result);
        } else {
            partner.tradeCancelHandshake(false, result);
        }
    }

    public static void cancelTrade(Character chr, TradeResult result) {
        Trade trade = chr.getTrade();
        if (trade == null) {
            return;
        }

        trade.cancelHandshake(result.getValue());
    }

    public static void startTrade(Character chr) {
        if (chr.getTrade() == null) {
            chr.setTrade(new Trade((byte) 0, chr));
        }
    }

    private static boolean hasTradeInviteBack(Character c1, Character c2) {
        Trade other = c2.getTrade();
        if (other != null) {
            Trade otherPartner = other.getPartner();
            if (otherPartner != null) {
                return otherPartner.getChr().getId() == c1.getId();
            }
        }

        return false;
    }

    public static void inviteTrade(Character c1, Character c2) {

        if ((c1.isGM() && !c2.isGM()) && c1.gmLevel() < GameConfig.getServerInt("minimum_gm_level_to_trade")) {//GM等级低于几级不允许与非GM角色进行交易
            c1.message(I18nUtil.getMessage("Trade.inviteTrade.GM.msg1"));
            log.info(I18nUtil.getLogMessage("Trade.info.inviteTrade.GM.msg1"), c1.getName(), c2.getName());
            cancelTrade(c1, TradeResult.NO_RESPONSE);
            return;
        }

        if ((!c1.isGM() && c2.isGM()) && c2.gmLevel() < GameConfig.getServerInt("minimum_gm_level_to_trade")) {
            c1.message(I18nUtil.getMessage("Trade.inviteTrade.GM.msg2"));
            log.info(I18nUtil.getLogMessage("Trade.info.inviteTrade.GM.msg1"), c1.getName(), c2.getName());
            cancelTrade(c1, TradeResult.NO_RESPONSE);
            return;
        }

        if (InviteCoordinator.hasInvite(InviteType.TRADE, c1.getId())) {
            if (hasTradeInviteBack(c1, c2)) {
                c1.message(I18nUtil.getMessage("Trade.inviteTrade.hasTradeInviteBack.msg1"));
            } else {
                c1.message(I18nUtil.getMessage("Trade.inviteTrade.hasTradeInviteBack.msg2"));
            }

            return;
        } else if (c1.getTrade().isFullTrade()) {
            c1.message(I18nUtil.getMessage("Trade.inviteTrade.hasTradeInviteBack.msg3"));
            return;
        }

        if (InviteCoordinator.createInvite(InviteType.TRADE, c1, c1.getId(), c2.getId())) {
            if (c2.getTrade() == null) {
                c2.setTrade(new Trade((byte) 1, c2));
                c2.getTrade().setPartner(c1.getTrade());
                c1.getTrade().setPartner(c2.getTrade());

                c1.sendPacket(PacketCreator.getTradeStart(c1.getClient(), c1.getTrade(), (byte) 0));
                c2.sendPacket(PacketCreator.tradeInvite(c1));
            } else {
                c1.message(I18nUtil.getMessage("Trade.inviteTrade.createInvite.msg1"));
                cancelTrade(c1, TradeResult.NO_RESPONSE);
                InviteCoordinator.answerInvite(InviteType.TRADE, c2.getId(), c1.getId(), false);
            }
        } else {
            c1.message(I18nUtil.getMessage("Trade.inviteTrade.createInvite.msg2"));
            cancelTrade(c1, TradeResult.NO_RESPONSE);
        }
    }

    public static void visitTrade(Character c1, Character c2) {
        InviteResult inviteRes = InviteCoordinator.answerInvite(InviteType.TRADE, c1.getId(), c2.getId(), true);

        InviteResultType res = inviteRes.result;
        if (res == InviteResultType.ACCEPTED) {
            if (c1.getTrade() != null && c1.getTrade().getPartner() == c2.getTrade() && c2.getTrade() != null && c2.getTrade().getPartner() == c1.getTrade()) {
                c2.sendPacket(PacketCreator.getTradePartnerAdd(c1));
                c1.sendPacket(PacketCreator.getTradeStart(c1.getClient(), c1.getTrade(), (byte) 1));
                c1.getTrade().setFullTrade(true);
                c2.getTrade().setFullTrade(true);
            } else {
                c1.message(I18nUtil.getMessage("Trade.inviteTrade.visitTrade.msg1"));
            }
        } else {
            c1.message(I18nUtil.getMessage("Trade.inviteTrade.visitTrade.msg2"));
            cancelTrade(c1, TradeResult.NO_RESPONSE);
        }
    }

    public static void declineTrade(Character chr) {
        Trade trade = chr.getTrade();
        if (trade != null) {
            if (trade.getPartner() != null) {
                Character other = trade.getPartner().getChr();
                if (InviteCoordinator.answerInvite(InviteType.TRADE, chr.getId(), other.getId(), false).result == InviteResultType.DENIED) {
                    other.message(I18nUtil.getMessage("Trade.inviteTrade.declineTrade.msg1",chr.getName()));
                }

                other.getTrade().cancel(TradeResult.PARTNER_CANCEL.getValue());
                other.setTrade(null);

            }
            trade.cancel(TradeResult.NO_RESPONSE.getValue());
            chr.setTrade(null);
        }
    }

    public boolean isFullTrade() {
        return fullTrade;
    }

    public void setFullTrade(boolean fullTrade) {
        this.fullTrade = fullTrade;
    }

    /*
    交易结果后台输出
     */
    private static void logTrade(Trade trade1, Trade trade2) {
        try {
            String name1 = trade1.getChr().getName();
            String name2 = trade2.getChr().getName();
            StringBuilder message = new StringBuilder();
//        message.append(I18nUtil.getLogMessage("Trade.info.inviteTrade.logTrade.msg1" ) + "\n");
            message.append(I18nUtil.getLogMessage("Trade.info.inviteTrade.logTrade.msg1", name1, name2) + "\n");
            //Trade 1 to trade 2
//        message.append(I18nUtil.getLogMessage("Trade.info.inviteTrade.logTrade.msg2" ) + "\n");
            message.append(I18nUtil.getLogMessage("Trade.info.inviteTrade.logTrade.msg2", name1, name2, trade1.getExchangeMesos(), getFormattedItemLogMessage(trade1.getItems())) + "\n");

            //Trade 2 to trade 1
//        message.append(I18nUtil.getLogMessage("Trade.info.inviteTrade.logTrade.msg2" ) + "\n");
            message.append(I18nUtil.getLogMessage("Trade.info.inviteTrade.logTrade.msg2", name2, name1, trade2.getExchangeMesos(), getFormattedItemLogMessage(trade2.getItems())) + "\n");

            log.info(message.toString());
        } catch (Exception e) {
            log.error("交易结果出现异常：",e);
        }
    }

    private static String getFormattedItemLogMessage(List<Item> items) {
        StringJoiner sj = new StringJoiner(", ", "[", "]");
        ItemInformationProvider ii = ItemInformationProvider.getInstance();
        for (Item item : items) {
            String itemName = ii.getName(item.getItemId());
//            sj.add(String.format("%dx %s (%d)", item.getQuantity(), itemName, item.getItemId()));
            sj.add(I18nUtil.getLogMessage("Trade.info.inviteTrade.logTrade.msg3" , item.getQuantity(), itemName, item.getItemId()) + "\n");
        }
        return sj.toString();
    }
}