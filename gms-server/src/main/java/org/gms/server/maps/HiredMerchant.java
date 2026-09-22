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
package org.gms.server.maps;

import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.client.inventory.Inventory;
import org.gms.client.inventory.InventoryType;
import org.gms.client.inventory.Item;
import org.gms.client.inventory.ItemFactory;
import org.gms.client.inventory.manipulator.InventoryManipulator;
import org.gms.client.inventory.manipulator.KarmaManipulator;
import org.gms.client.processor.npc.FredrickProcessor;
import org.gms.config.GameConfig;
import org.gms.net.packet.Packet;
import org.gms.net.server.Server;
import org.gms.net.server.channel.Channel;
import org.gms.net.server.world.World;
import org.gms.server.ItemInformationProvider;
import org.gms.server.Trade;
import org.gms.util.DatabaseConnection;
import org.gms.util.I18nUtil;
import org.gms.util.PacketCreator;
import org.gms.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author XoticStory
 * @author Ronan - concurrency protection
 */
public class HiredMerchant extends AbstractMapObject {
    private static final Logger log = LoggerFactory.getLogger(HiredMerchant.class);
    private static final int VISITOR_HISTORY_LIMIT = 10;
    private static final int BLACKLIST_LIMIT = 20;
    // 客户端 EXIT 的退出原因码：收店用 0x12，客户端关窗并提示"已超过营业时间而关闭商店！"；
    // 与"店主正在整理物品"（0x11 + ROOM 0x12 维护提示）区分，关店不再被显示成整理中。
    private static final int EXIT_STATUS_CLOSED = 0x12;

    private final int ownerId;
    private final int itemId;
    private final int mesos = 0;
    private final int channel;
    private final int world;
    private final long start;
    private String ownerName = "";
    private String description = "";
    private final List<PlayerShopItem> items = new LinkedList<>();
    private final List<Pair<String, Byte>> messages = new LinkedList<>();
    private final List<SoldItem> sold = new LinkedList<>();
    private final AtomicBoolean open = new AtomicBoolean();
    private final AtomicBoolean closing = new AtomicBoolean();
    private volatile boolean published = false;
    private volatile boolean ownerBanned = false;
    private volatile boolean detached = false;
    private MapleMap map;
    private final Visitor[] visitors = new Visitor[3];
    private final LinkedList<PastVisitor> visitorHistory = new LinkedList<>();
    private final LinkedHashSet<String> blacklist = new LinkedHashSet<>(); // case-sensitive character names
    private final Lock visitorLock = new ReentrantLock(true);

    private record Visitor(Character chr, Instant enteredAt) {}

    public record PastVisitor(String chrName, Duration visitDuration) {}

    public HiredMerchant(final Character owner, String desc, int itemId) {
        this.setPosition(owner.getPosition());
        this.start = System.currentTimeMillis();
        this.ownerId = owner.getId();
        this.channel = owner.getClient().getChannel();
        this.world = owner.getWorld();
        this.itemId = itemId;
        this.ownerName = owner.getName();
        this.description = desc;
        this.map = owner.getMap();
    }

    public void broadcastToVisitorsThreadsafe(Packet packet) {
        visitorLock.lock();
        try {
            broadcastToVisitors(packet);
        } finally {
            visitorLock.unlock();
        }
    }

    private void broadcastToVisitors(Packet packet) {
        for (Visitor visitor : visitors) {
            if (visitor != null) {
                visitor.chr.sendPacket(packet);
            }
        }
    }

    public byte[] getShopRoomInfo() {
        visitorLock.lock();
        try {
            byte count = 0;
            if (this.isOpen()) {
                for (Visitor visitor : visitors) {
                    if (visitor != null) {
                        count++;
                    }
                }
            } else {
                count = (byte) (visitors.length + 1);
            }

            return new byte[]{count, (byte) (visitors.length + 1)};
        } finally {
            visitorLock.unlock();
        }
    }

    public boolean addVisitor(Character visitor) {
        visitorLock.lock();
        try {
            if (!isOpen()) return false;
            int i = this.getFreeSlot();
            if (i > -1) {
                visitors[i] = new Visitor(visitor, Instant.now());
                broadcastToVisitors(PacketCreator.hiredMerchantVisitorAdd(visitor, i + 1));
                this.getMap().broadcastMessage(PacketCreator.updateHiredMerchantBox(this));

                return true;
            }

            return false;
        } finally {
            visitorLock.unlock();
        }
    }

    public void removeVisitor(Character chr) {
        visitorLock.lock();
        try {
            int slot = getVisitorSlot(chr);
            if (slot < 0) { //Not found
                return;
            }

            Visitor visitor = visitors[slot];
            if (visitor != null && visitor.chr.getId() == chr.getId()) {
                visitors[slot] = null;
                addVisitorToHistory(visitor);
                broadcastToVisitors(PacketCreator.hiredMerchantVisitorLeave(slot + 1));
                this.getMap().broadcastMessage(PacketCreator.updateHiredMerchantBox(this));
            }
        } finally {
            visitorLock.unlock();
        }
    }

    private void addVisitorToHistory(Visitor visitor) {
        Duration visitDuration = Duration.between(visitor.enteredAt, Instant.now());
        visitorHistory.addFirst(new PastVisitor(visitor.chr.getName(), visitDuration));
        while (visitorHistory.size() > VISITOR_HISTORY_LIMIT) {
            visitorHistory.removeLast();
        }
    }

    public int getVisitorSlotThreadsafe(Character visitor) {
        visitorLock.lock();
        try {
            return getVisitorSlot(visitor);
        } finally {
            visitorLock.unlock();
        }
    }

    private int getVisitorSlot(Character visitor) {
        for (int i = 0; i < 3; i++) {
            if (visitors[i] != null && visitors[i].chr.getId() == visitor.getId()) {
                return i;
            }
        }
        return -1; //Actually 0 because of the +1's.
    }

    /**
     * @param maintenance 仅店主进店整理为 true：访客看到"商店主人正在整理物品"；
     *                    收店/到期为 false：用 EXIT+0x12 通知访客商店已关闭，不再复用整理中的提示。
     */
    private void removeAllVisitors(boolean maintenance) {
        visitorLock.lock();
        try {
            for (int i = 0; i < 3; i++) {
                Visitor visitor = visitors[i];

                if (visitor != null) {
                    final Character visitorChr = visitor.chr;
                    visitorChr.setHiredMerchant(null);
                    if (maintenance) {
                        visitorChr.sendPacket(PacketCreator.leaveHiredMerchant(i + 1, 0x11));
                        visitorChr.sendPacket(PacketCreator.hiredMerchantMaintenanceMessage());
                    } else {
                        visitorChr.sendPacket(PacketCreator.leaveHiredMerchant(i + 1, EXIT_STATUS_CLOSED));
                    }
                    visitors[i] = null;
                    addVisitorToHistory(visitor);
                }
            }

            this.getMap().broadcastMessage(PacketCreator.updateHiredMerchantBox(this));
        } finally {
            visitorLock.unlock();
        }
    }

    private void removeOwner(Character owner) {
        if (owner.getHiredMerchant() == this) {
            owner.sendPacket(PacketCreator.hiredMerchantOwnerLeave());
            owner.sendPacket(PacketCreator.leaveHiredMerchant(0x00, 0x03));
            owner.setHiredMerchant(null);
        }
    }

    public void withdrawMesos(Character chr) {
        if (isOwner(chr)) {
            synchronized (items) {
                if (ownerBanned || closing.get() || detached) return;
                chr.withdrawMerchantMesos();
            }
        }
    }

    public void takeItemBack(int slot, Character chr) {
        synchronized (items) {
            if (ownerBanned || closing.get() || detached || !isOwner(chr) || isOpen()
                    || slot < 0 || slot >= items.size()) return;
            PlayerShopItem shopItem = items.get(slot);
            if (shopItem.isExist()) {
                if (shopItem.getBundles() > 0) {
                    Item iitem = shopItem.getItem().copy();
                    iitem.setQuantity((short) (shopItem.getItem().getQuantity() * shopItem.getBundles()));

                    if (!Inventory.checkSpot(chr, iitem)) {
                        chr.sendPacket(PacketCreator.serverNotice(1, "Have a slot available on your inventory to claim back the item."));
                        chr.sendPacket(PacketCreator.enableActions());
                        return;
                    }

                    InventoryManipulator.addFromDrop(chr.getClient(), iitem, true);
                }

                removeFromSlot(slot);
                chr.sendPacket(PacketCreator.updateHiredMerchant(this, chr));
            }

            if (GameConfig.getServerBoolean("use_enforce_merchant_save")) {
                chr.saveCharToDB(false);
            }
        }
    }

    private static boolean canBuy(Client c, Item newItem) {    // thanks xiaokelvin (Conrad) for noticing a leaked test code here
        return InventoryManipulator.checkSpace(c, newItem.getItemId(), newItem.getQuantity(), newItem.getOwner()) && InventoryManipulator.addFromDrop(c, newItem, false);
    }

    private int getQuantityLeft(int itemid) {
        synchronized (items) {
            int count = 0;

            for (PlayerShopItem mpsi : items) {
                if (mpsi.getItem().getItemId() == itemid) {
                    count += (mpsi.getBundles() * mpsi.getItem().getQuantity());
                }
            }

            return count;
        }
    }

    public void buy(Client c, int item, short quantity) {
        synchronized (items) {
            if (!isOpen() || quantity < 1 || item < 0 || item >= items.size()) {   // thanks xiaokelvin for pointing out slot check missing
                c.sendPacket(PacketCreator.enableActions());
                return;
            }

            PlayerShopItem pItem = items.get(item);
            if (!pItem.isExist() || pItem.getBundles() < quantity) {
                c.sendPacket(PacketCreator.enableActions());
                return;
            }

            Item newItem = pItem.getItem().copy();

            newItem.setQuantity((short) ((pItem.getItem().getQuantity() * quantity)));
            if (newItem.getInventoryType().equals(InventoryType.EQUIP) && newItem.getQuantity() > 1) {
                c.sendPacket(PacketCreator.enableActions());
                return;
            }

            KarmaManipulator.toggleKarmaFlagToUntradeable(newItem);

            int price = (int) Math.min((float) pItem.getPrice() * quantity, Integer.MAX_VALUE);
            if (c.getPlayer().getMeso() >= price) {
                if (canBuy(c, newItem)) {
                    c.getPlayer().gainMeso(-price, false);
                    price -= Trade.getFee(price);  // thanks BHB for pointing out trade fees not applying here

                    synchronized (sold) {
                        sold.add(new SoldItem(c.getPlayer().getName(), pItem.getItem().getItemId(), newItem.getQuantity(), price));
                    }

                    pItem.setBundles((short) (pItem.getBundles() - quantity));
                    if (pItem.getBundles() < 1) {
                        pItem.setDoesExist(false);
                    }

                    if (GameConfig.getServerBoolean("use_announce_shop_item_sold")) {   // idea thanks to Vcoc
                        announceItemSold(newItem, price, getQuantityLeft(pItem.getItem().getItemId()));
                    }

                    Character owner = Server.getInstance().getWorld(world).getPlayerStorage().getCharacterById(ownerId);
                    if (owner != null) {
                        ownerName = owner.getName();
                        owner.addMerchantMesos(price);
                    } else {
                        try (Connection con = DatabaseConnection.getConnection()) {
                            long merchantMesos = 0;
                            try (PreparedStatement ps = con.prepareStatement("SELECT MerchantMesos FROM characters WHERE id = ?")) {
                                ps.setInt(1, ownerId);
                                try (ResultSet rs = ps.executeQuery()) {
                                    if (rs.next()) {
                                        merchantMesos = rs.getInt(1);
                                    }
                                }
                            }
                            merchantMesos += price;

                            try (PreparedStatement ps = con.prepareStatement("UPDATE characters SET MerchantMesos = ? WHERE id = ?", PreparedStatement.RETURN_GENERATED_KEYS)) {
                                ps.setInt(1, (int) Math.min(merchantMesos, Integer.MAX_VALUE));
                                ps.setInt(2, ownerId);
                                ps.executeUpdate();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    c.getPlayer().dropMessage(1, "Your inventory is full. Please clear a slot before buying this item.");
                    c.sendPacket(PacketCreator.enableActions());
                    return;
                }
            } else {
                c.getPlayer().dropMessage(1, "You don't have enough mesos to purchase this item.");
                c.sendPacket(PacketCreator.enableActions());
                return;
            }
            try {
                this.saveItems(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void announceItemSold(Item item, int mesos, int inStore) {
        String qtyStr = (item.getQuantity() > 1) ? " x " + item.getQuantity() : "";

        Character player = Server.getInstance().getWorld(world).getPlayerStorage().getCharacterById(ownerId);
        if (player != null && player.isLoggedInWorld()) {
            player.dropMessage(6, "[Hired Merchant] Item '" + ItemInformationProvider.getInstance().getName(item.getItemId()) + "'" + qtyStr + " has been sold for " + mesos + " mesos. (" + inStore + " left)");
        }
    }

    /** 与店主上架/取回操作共用客户端锁，再与购买串行停止营业。 */
    public void closeForBan() {
        World worldServer = Server.getInstance().getWorld(world);
        Character owner = worldServer == null ? null : worldServer.getPlayerStorage().getCharacterById(ownerId);
        Client ownerClient = owner == null ? null : owner.getClient();
        if (ownerClient != null) ownerClient.lockClient();
        try {
            synchronized (items) {
                ownerBanned = true;
                open.set(false);
            }
            forceCloseInternal(false);
        } finally {
            if (ownerClient != null) ownerClient.unlockClient();
        }
    }

    public boolean isClosedForBan() {
        return ownerBanned;
    }

    public void forceClose() {
        forceClose(false);
    }

    /**
     * @param expired 到期收店（营业时长耗尽）：店主在商店窗口内时用 EXIT+0x12 关窗并提示"已超过营业时间而关闭商店！"，
     *                不在窗口内则用普通提示告知；其他收店原因（封禁、关服、店主自行关店）维持原有退出流程。
     */
    public void forceClose(boolean expired) {
        World worldServer = Server.getInstance().getWorld(world);
        Character owner = worldServer == null ? null : worldServer.getPlayerStorage().getCharacterById(ownerId);
        Client ownerClient = owner == null ? null : owner.getClient();
        if (ownerClient != null) ownerClient.lockClient();
        try {
            forceCloseInternal(expired);
        } finally {
            if (ownerClient != null) ownerClient.unlockClient();
        }
    }

    private void forceCloseInternal(boolean expired) {
        if (!closing.compareAndSet(false, true)) {
            return;
        }

        Server server = Server.getInstance();
        World worldServer = server.getWorld(world);
        Channel channelServer = server.getChannel(world, channel);
        boolean authoritative = worldServer != null && worldServer.isHiredMerchantRegistered(this);
        Character owner = worldServer == null ? null : worldServer.getPlayerStorage().getCharacterById(ownerId);
        boolean returnToOwner = !ownerBanned && owner != null && owner.isLoggedInWorld() && this == owner.getHiredMerchant();
        if (authoritative && !returnToOwner) {
            synchronized (items) {
                open.set(false);
                try {
                    // 保存成功前保留注册和内存物品，失败后由定时收店重试。
                    saveItems(true);
                } catch (SQLException | RuntimeException e) {
                    closing.set(false);
                    log.error(I18nUtil.getLogMessage("HiredMerchant.close.saveFailed", ownerId), e);
                    return;
                }
            }
        }
        if (channelServer != null) {
            channelServer.removeHiredMerchant(ownerId, this);
        }

        if (map != null) {
            map.broadcastMessage(PacketCreator.removeHiredMerchantBox(getOwnerId()));
            map.removeMapObject(this);
        }

        if (!authoritative) {
            detached = true;
            visitorLock.lock();
            try {
                setOpen(false);
                if (map != null) {
                    removeAllVisitors(false);
                }
            } finally {
                visitorLock.unlock();
            }
            log.warn(I18nUtil.getLogMessage("HiredMerchant.close.staleInstance", ownerId, channel));
            map = null;
            return;
        }

        visitorLock.lock();
        try {
            setOpen(false);
            removeAllVisitors(false);

            if (returnToOwner) {
                if (expired) {
                    // 到期时店主正在商店窗口中：用 EXIT+0x12 关窗，客户端提示"已超过营业时间而关闭商店！"
                    owner.sendPacket(PacketCreator.leaveHiredMerchant(0x00, EXIT_STATUS_CLOSED));
                }
                closeOwnerMerchantAfterClaim(owner);
                map = null;
                return;
            }
        } finally {
            visitorLock.unlock();
        }

        if (expired && !ownerBanned && owner != null && owner.isLoggedInWorld()) {
            // 店主不在商店窗口内，无法用 EXIT 关窗提示，改用普通提示告知到期
            owner.dropMessage(6, I18nUtil.getMessage("HiredMerchant.expired.message1", description));
        }
        if (ownerBanned && owner != null) removeOwner(owner);
        synchronized (items) {
            detached = true;
            items.clear();
        }

        Character player = worldServer.getPlayerStorage().getCharacterById(ownerId);
        if (player != null) {
            player.setHasMerchant(false);
        } else {
            try (Connection con = DatabaseConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement("UPDATE characters SET HasMerchant = 0 WHERE id = ?", PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, ownerId);
                ps.executeUpdate();
            } catch (SQLException ex) {
                log.error(I18nUtil.getLogMessage("HiredMerchant.close.stateFailed", ownerId), ex);
            }
        }

        worldServer.unregisterHiredMerchant(this);
        map = null;
    }

    public void closeOwnerMerchant(Character chr) {
        if (isOwner(chr) && ownerBanned) {
            closeForBan();
            return;
        }
        if (this.isOwner(chr) && closing.compareAndSet(false, true)) {
            closeOwnerMerchantAfterClaim(chr);
        }
    }

    private void closeOwnerMerchantAfterClaim(Character chr) {
        this.closeShop(chr.getClient(), false);
        chr.setHasMerchant(false);
    }

    private void closeShop(Client c, boolean timeout) {
        map.removeMapObject(this);
        map.broadcastMessage(PacketCreator.removeHiredMerchantBox(ownerId));
        c.getChannelServer().removeHiredMerchant(ownerId, this);

        this.removeAllVisitors(false);
        this.removeOwner(c.getPlayer());

        try {
            List<PlayerShopItem> copyItems = getItems();
            if (check(c.getPlayer(), copyItems) && !timeout) {
                for (PlayerShopItem mpsi : copyItems) {
                    if (mpsi.isExist()) {
                        if (mpsi.getItem().getInventoryType().equals(InventoryType.EQUIP)) {
                            InventoryManipulator.addFromDrop(c, mpsi.getItem(), false);
                        } else {
                            InventoryManipulator.addById(c, mpsi.getItem().getItemId(), (short) (mpsi.getBundles() * mpsi.getItem().getQuantity()), mpsi.getItem().getOwner(), -1, mpsi.getItem().getFlag(), mpsi.getItem().getExpiration());
                        }
                    }
                }

                synchronized (items) {
                    items.clear();
                }
            }

            try {
                this.saveItems(timeout);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // thanks Rohenn for noticing a possible dupe scenario on closing shop
            Character player = c.getWorldServer().getPlayerStorage().getCharacterById(ownerId);
            if (player != null) {
                player.setHasMerchant(false);
            } else {
                try (Connection con = DatabaseConnection.getConnection();
                     PreparedStatement ps = con.prepareStatement("UPDATE characters SET HasMerchant = 0 WHERE id = ?", PreparedStatement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, ownerId);
                    ps.executeUpdate();
                }
            }

            if (GameConfig.getServerBoolean("use_enforce_merchant_save")) {
                c.getPlayer().saveCharToDB(false);
            }

            synchronized (items) {
                items.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Server.getInstance().getWorld(world).unregisterHiredMerchant(this);
        detached = true;
    }

    public synchronized void visitShop(Character chr) {
        visitorLock.lock();
        try {
            if (ownerBanned || closing.get() || detached) {
                chr.sendPacket(PacketCreator.getMiniRoomError(18));
                return;
            }
            if (this.isOwner(chr)) {
                this.setOpen(false);
                this.removeAllVisitors(true);

                chr.sendPacket(PacketCreator.getHiredMerchant(chr, this, false));
            } else if (!this.isOpen()) {
                chr.sendPacket(PacketCreator.getMiniRoomError(18));
                return;
            } else if (isBlacklisted(chr.getName())) {
                chr.sendPacket(PacketCreator.getMiniRoomError(17));
                return;
            } else if (!this.addVisitor(chr)) {
                chr.sendPacket(PacketCreator.getMiniRoomError(2));
                return;
            } else {
                chr.sendPacket(PacketCreator.getHiredMerchant(chr, this, false));
            }
            chr.setHiredMerchant(this);
        } finally {
            visitorLock.unlock();
        }
    }

    public String getOwner() {
        return ownerName;
    }

    public void clearItems() {
        synchronized (items) {
            if (ownerBanned || closing.get() || detached) return;
            items.clear();
        }
    }

    public int getOwnerId() {
        return ownerId;
    }

    public String getDescription() {
        return description;
    }

    public Character[] getVisitorCharacters() {
        visitorLock.lock();
        try {
            Character[] copy = new Character[3];
            for (int i = 0; i < visitors.length; i++) {
                Visitor visitor = visitors[i];
                if (visitor != null) {
                    copy[i] = visitor.chr;
                }
            }

            return copy;
        } finally {
            visitorLock.unlock();
        }
    }

    public List<PlayerShopItem> getItems() {
        synchronized (items) {
            return Collections.unmodifiableList(new ArrayList<>(items));
        }
    }

    public boolean hasItem(int itemid) {
        for (PlayerShopItem mpsi : getItems()) {
            if (mpsi.getItem().getItemId() == itemid && mpsi.isExist() && mpsi.getBundles() > 0) {
                return true;
            }
        }

        return false;
    }

    public boolean addItem(PlayerShopItem item) {
        synchronized (items) {
            if (ownerBanned || closing.get() || detached || items.size() >= 16) {
                return false;
            }

            items.add(item);
            return true;
        }
    }

    public void clearInexistentItems() {
        synchronized (items) {
            if (ownerBanned || closing.get() || detached) return;
            for (int i = items.size() - 1; i >= 0; i--) {
                if (!items.get(i).isExist()) {
                    items.remove(i);
                }
            }

            try {
                this.saveItems(false);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void removeFromSlot(int slot) {
        items.remove(slot);

        try {
            this.saveItems(false);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private int getFreeSlot() {
        for (int i = 0; i < 3; i++) {
            if (visitors[i] == null) {
                return i;
            }
        }
        return -1;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isPublished() {
        return published;
    }

    public boolean isOpen() {
        return open.get() && !ownerBanned && !closing.get() && !detached;
    }

    public boolean setOpen(boolean set) {
        if (!set) {
            open.set(false);
            return true;
        }
        synchronized (items) {
            if (ownerBanned || closing.get() || detached) return false;
            // 创建时已注册，发布/维护结束时再查封禁，补住封号与开店交错的窗口。
            try (Connection con = DatabaseConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement("SELECT a.banned, a.tempban > CURRENT_TIMESTAMP AS temporarily_banned FROM accounts a JOIN characters c ON c.accountid = a.id WHERE c.id = ?")) {
                ps.setInt(1, ownerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next() || rs.getInt("banned") == 1 || rs.getBoolean("temporarily_banned")) {
                        ownerBanned = true;
                        open.set(false);
                        return false;
                    }
                }
            } catch (SQLException | RuntimeException e) {
                ownerBanned = true;
                open.set(false);
                log.error(I18nUtil.getLogMessage("HiredMerchant.open.checkFailed", ownerId), e);
                return false;
            }
            open.set(true);
            published = true;
            return true;
        }
    }

    public int getItemId() {
        return itemId;
    }

    public boolean isOwner(Character chr) {
        return chr.getId() == ownerId;
    }

    public void sendMessage(Character chr, String msg) {
        String message = chr.getName() + " : " + msg;
        byte slot = (byte) (getVisitorSlot(chr) + 1);

        synchronized (messages) {
            messages.add(new Pair<>(message, slot));
        }
        broadcastToVisitorsThreadsafe(PacketCreator.hiredMerchantChat(message, slot));
    }

    public List<PlayerShopItem> sendAvailableBundles(int itemid) {
        List<PlayerShopItem> list = new LinkedList<>();
        List<PlayerShopItem> all = new ArrayList<>();

        if (!isOpen()) {
            return list;
        }

        synchronized (items) {
            all.addAll(items);
        }

        for (PlayerShopItem mpsi : all) {
            if (mpsi.getItem().getItemId() == itemid && mpsi.getBundles() > 0 && mpsi.isExist()) {
                list.add(mpsi);
            }
        }
        return list;
    }

    public void saveItems(boolean shutdown) throws SQLException {
        synchronized (items) {
            if (detached) return;
            List<Pair<Item, InventoryType>> itemsWithType = new ArrayList<>();
            List<Short> bundles = new ArrayList<>();

            for (PlayerShopItem pItems : getItems()) {
                Item newItem = pItems.getItem();
                short newBundle = pItems.getBundles();

                if (shutdown) { //is "shutdown" really necessary?
                    newItem.setQuantity(pItems.getItem().getQuantity());
                } else {
                    newItem.setQuantity(pItems.getItem().getQuantity());
                }
                if (newBundle > 0) {
                    itemsWithType.add(new Pair<>(newItem, newItem.getInventoryType()));
                    bundles.add(newBundle);
                }
            }

            try (Connection con = DatabaseConnection.getConnection()) {
                ItemFactory.MERCHANT.saveItems(itemsWithType, bundles, this.ownerId, con);
            }

            FredrickProcessor.insertFredrickLog(this.ownerId);
        }
    }

    private static boolean check(Character chr, List<PlayerShopItem> items) {
        List<Pair<Item, InventoryType>> li = new ArrayList<>();
        for (PlayerShopItem item : items) {
            Item it = item.getItem().copy();
            it.setQuantity((short) (it.getQuantity() * item.getBundles()));

            li.add(new Pair<>(it, it.getInventoryType()));
        }

        return Inventory.checkSpotsAndOwnership(chr, li);
    }

    public int getChannel() {
        return channel;
    }

    public int getTimeOpen() {
        double openTime = (System.currentTimeMillis() - start) / 60000;
        openTime /= 1440;   // heuristics since engineered method to count time here is unknown
        openTime *= 1318;

        return (int) Math.ceil(openTime);
    }

    public void clearMessages() {
        synchronized (messages) {
            messages.clear();
        }
    }

    public List<Pair<String, Byte>> getMessages() {
        synchronized (messages) {
            List<Pair<String, Byte>> msgList = new LinkedList<>();
            msgList.addAll(messages);

            return msgList;
        }
    }

    public List<PastVisitor> getVisitorHistory() {
        return Collections.unmodifiableList(visitorHistory);
    }

    public void addToBlacklist(String chrName) {
        visitorLock.lock();
        try {
            if (blacklist.size() >= BLACKLIST_LIMIT) {
                return;
            }
            blacklist.add(chrName);
        } finally {
            visitorLock.unlock();
        }
    }

    public void removeFromBlacklist(String chrName) {
        visitorLock.lock();
        try {
            blacklist.remove(chrName);
        } finally {
            visitorLock.unlock();
        }
    }

    public Set<String> getBlacklist() {
        return Collections.unmodifiableSet(blacklist);
    }

    private boolean isBlacklisted(String chrName) {
        visitorLock.lock();
        try {
            return blacklist.contains(chrName);
        } finally {
            visitorLock.unlock();
        }
    }

    public int getMapId() {
        return map.getId();
    }

    public MapleMap getMap() {
        return map;
    }

    public List<SoldItem> getSold() {
        synchronized (sold) {
            return Collections.unmodifiableList(sold);
        }
    }

    public int getMesos() {
        return mesos;
    }

    @Override
    public MapObjectType getType() {
        return MapObjectType.HIRED_MERCHANT;
    }

    @Override
    public void sendDestroyData(Client client) {}

    @Override
    public void sendSpawnData(Client client) {
        client.sendPacket(PacketCreator.spawnHiredMerchantBox(this));
    }

    public class SoldItem {

        int itemid, mesos;
        short quantity;
        String buyer;

        public SoldItem(String buyer, int itemid, short quantity, int mesos) {
            this.buyer = buyer;
            this.itemid = itemid;
            this.quantity = quantity;
            this.mesos = mesos;
        }

        public String getBuyer() {
            return buyer;
        }

        public int getItemId() {
            return itemid;
        }

        public short getQuantity() {
            return quantity;
        }

        public int getMesos() {
            return mesos;
        }
    }
}
