package org.gms.client.snapshot;

import org.gms.client.inventory.Equip;
import org.gms.client.inventory.Item;
import org.gms.client.inventory.InventoryType;

/**
 * 物品快照编解码：在 {@code Item}/{@code Equip} 与 {@link CharacterSnapshot.ItemInfo} 之间互转。
 * <p>只用 Item/Equip 的 public getter/setter 与构造器，无私有字段访问，故可放在 client.snapshot 包，
 * 供 {@code Character capture}（编码）与 {@code CharacterSnapshotService.save}（解码）共用，
 * 保证"落盘时读的字段"与"恢复时写的字段"完全一致。
 */
public final class SnapshotCodecs {

    private SnapshotCodecs() {
    }

    /** 把一个 {@link Item} 编码为快照记录。inventoryType 为该物品所属背包类型（{@link InventoryType#getType()}）。 */
    public static CharacterSnapshot.ItemInfo encodeItem(Item item, byte inventoryType) {
        CharacterSnapshot.ItemInfo info = new CharacterSnapshot.ItemInfo();
        info.itemid = item.getItemId();
        info.position = item.getPosition();
        info.quantity = item.getQuantity();
        info.owner = item.getOwner();
        info.petid = item.getPetId();
        info.flag = item.getFlag();
        info.expiration = item.getExpiration();
        info.giftFrom = item.getGiftFrom();
        info.inventorytype = inventoryType;

        if (item instanceof Equip eq) {
            info.equip = true;
            info.upgradeslots = eq.getUpgradeSlots();
            info.level = eq.getLevel();
            info.itemlevel = eq.getItemLevel();
            info.itemexp = eq.getItemExp();
            info.str = eq.getStr();
            info.dex = eq.getDex();
            info.int_ = eq.getInt();
            info.luk = eq.getLuk();
            info.hp = eq.getHp();
            info.mp = eq.getMp();
            info.watk = eq.getWatk();
            info.matk = eq.getMatk();
            info.wdef = eq.getWdef();
            info.mdef = eq.getMdef();
            info.acc = eq.getAcc();
            info.avoid = eq.getAvoid();
            info.hands = eq.getHands();
            info.speed = eq.getSpeed();
            info.jump = eq.getJump();
            info.vicious = eq.getVicious();
            info.ringid = eq.getRingId();
        }
        return info;
    }

    /** 把快照记录解码回 {@link Item}/{@code Equip}，供 {@code ItemFactory.saveItems} 写回 DB。 */
    public static Item decodeItem(CharacterSnapshot.ItemInfo info) {
        Item item;
        if (info.equip) {
            Equip eq = new Equip(info.itemid, info.position);
            eq.setQuantity(info.quantity);
            eq.setOwner(info.owner);
            eq.setFlag(info.flag);
            eq.setExpiration(info.expiration);
            eq.setGiftFrom(info.giftFrom);
            eq.setUpgradeSlots(info.upgradeslots);
            eq.setLevel(info.level);
            eq.setItemLevel((byte) info.itemlevel);
            eq.setItemExp(info.itemexp);
            eq.setStr(info.str);
            eq.setDex(info.dex);
            eq.setInt(info.int_);
            eq.setLuk(info.luk);
            eq.setHp(info.hp);
            eq.setMp(info.mp);
            eq.setWatk(info.watk);
            eq.setMatk(info.matk);
            eq.setWdef(info.wdef);
            eq.setMdef(info.mdef);
            eq.setAcc(info.acc);
            eq.setAvoid(info.avoid);
            eq.setHands(info.hands);
            eq.setSpeed(info.speed);
            eq.setJump(info.jump);
            eq.setVicious(info.vicious);
            eq.setRingId(info.ringid);
            item = eq;
        } else {
            item = Item.forPersistence(info.itemid, info.position, info.quantity, info.petid, info.flag);
            item.setOwner(info.owner);
            item.setExpiration(info.expiration);
            item.setGiftFrom(info.giftFrom);
        }
        return item;
    }

    /** 快照记录里的背包类型转 {@link InventoryType}。 */
    public static InventoryType toInventoryType(byte type) {
        return InventoryType.getByType(type);
    }
}
