package org.gms.client.snapshot;

import java.util.List;
import java.util.Map;

/** 角色保存的私有深快照。只包含北斗现有持久化字段，排队后不再引用背包中的可变物品。 */
public class CharacterSnapshot {
    // ---- 头信息 ----
    public int worldId;
    public int accountId;
    public int charId;
    public String name;

    // ---- characters 行（北斗现有角色存档字段）----
    public CharStats stats = new CharStats();

    public static class CharStats {
        public int level;
        public int fame;
        public int str;
        public int dex;
        public int luk;
        public int int_;
        public int exp;
        public int gachaexp;
        public int hp;
        public int mp;
        public int maxhp;
        public int maxmp;
        public String sp;            // remainingSp[] 逗号拼接，与 saveCharToDB 一致
        public int ap;
        public int gm;
        public int skincolor;
        public int gender;
        public int job;
        public int hair;
        public int face;
        public int map;              // 已按 forcedReturn/死亡返回逻辑解析后的 map id
        public int meso;
        public int hpMpUsed;
        public int spawnpoint;       // 已按 findClosestPlayerSpawnpoint 解析
        public int party;
        public int buddyCapacity;
        public int messengerid;
        public int messengerposition;
        public int mountlevel;
        public int mountexp;
        public int mounttiredness;
        public int equipslots;
        public int useslots;
        public int setupslots;
        public int etcslots;
        public int monsterbookcover;
        public int vanquisherStage;
        public int dojoPoints;
        public int lastDojoStage;
        public int finishedDojoTutorial;
        public int vanquisherKills;
        public int matchcardwins;
        public int matchcardlosses;
        public int matchcardties;
        public int omokwins;
        public int omoklosses;
        public int omokties;
        public String dataString;
        public int fquest;
        public long jailexpire;
        public int partnerId;
        public int marriageItemId;
        public long lastExpGainTime;
        public int ariantPoints;
        public boolean partySearch;
    }

    // ---- monsterbook（角色卡片进度）----
    public Map<Integer, Integer> monsterbookCards;   // cardid -> level

    // ---- pets ----
    public List<PetInfo> pets;

    public static class PetInfo {
        public int petid;
        public String name;
        public int level;
        public int closeness;
        public int fullness;
        public boolean summoned;
        public int flag;
    }

    // ---- keymap ----
    public List<KeyBindingInfo> keymap;

    public static class KeyBindingInfo {
        public int key;
        public int type;
        public int action;
    }

    // ---- quickslot（账号级；null 表示无快捷栏或未变更）----
    public Long quickslot;

    // ---- skillmacros ----
    public List<SkillMacroInfo> skillmacros;

    public static class SkillMacroInfo {
        public int skill1;
        public int skill2;
        public int skill3;
        public String name;
        public int shout;
        public int position;
    }

    // ---- 背包物品（inventoryitems + inventoryequipment）----
    public List<ItemInfo> inventory;

    // ---- 仓库物品（inventoryitems，type=STORAGE）----
    public StorageInfo storage;

    // ---- 商城物品 + 余额 + 心愿单（accounts nx / CASH_* inventoryitems / wishlists）----
    public CashShopInfo cashshop;

    public static class ItemInfo {
        public int itemid;
        public short position;
        public short quantity;
        public String owner;
        public int petid;            // -1 表示无
        public short flag;
        public long expiration;
        public String giftFrom;
        public byte inventorytype;   // InventoryType.getType()
        // 装备属性（非装备物品留默认 0）
        public boolean equip;
        public byte upgradeslots;
        public byte level;
        public int itemlevel;
        public int itemexp;
        public short str, dex, int_, luk, hp, mp, watk, matk, wdef, mdef, acc, avoid, hands, speed, jump;
        public short vicious;
        public int ringid;
    }

    public static class StorageInfo {
        public long changeVersion;
        public int storageid;
        public int slots;
        public int meso;
        public List<ItemInfo> items;
    }

    public static class CashShopInfo {
        // 同进程在线存档使用的账号余额与商城物品。
        public int nxCredit;
        public int maplePoint;
        public int nxPrepaid;
        public int characterId;     // wishlists 按 charid 存
        public int factoryValue;    // ItemFactory.CASH_* 的 value，恢复时 getByValue
        public List<ItemInfo> items;
        public List<Integer> wishlists;
    }

    // ---- skills ----
    public List<SkillInfo> skills;

    public static class SkillInfo {
        public int skillid;
        public int skilllevel;
        public int masterlevel;
        public long expiration;
    }

    // ---- savedlocations ----
    public List<SavedLocationInfo> savedlocations;

    public static class SavedLocationInfo {
        public String locationtype;
        public int map;
        public int portal;
    }

    // ---- trocklocations（普通 0 / VIP 1）----
    public List<TrockInfo> trocklocations;

    public static class TrockInfo {
        public int mapid;
        public int vip;
    }

    // ---- buddies（pending=0 的可见好友）----
    public List<BuddyInfo> buddies;

    public static class BuddyInfo {
        public int buddyid;
        public String group;
    }

    // ---- area_info ----
    public List<AreaInfo> areaInfo;

    public static class AreaInfo {
        public short area;
        public String info;
    }

    // ---- eventstats ----
    public List<EventStatInfo> eventstats;

    public static class EventStatInfo {
        public String name;
        public int info;
    }

    // ---- quests（queststatus + questprogress + medalmaps）----
    public List<QuestInfo> quests;

    public static class QuestInfo {
        public int quest;
        public int status;
        public long time;          // completionTime/1000
        public long expires;
        public int forfeited;
        public int completed;
        public Map<Integer, String> progress;   // mob -> progress
        public List<Integer> medalmaps;
    }

    // ---- family_character（自身声望；cid = charId）----
    public FamilyRepInfo family;

    public static class FamilyRepInfo {
        public int reputation;
        public int todaysrep;
        public int totalreputation;
        public int reptosenior;
    }

}
