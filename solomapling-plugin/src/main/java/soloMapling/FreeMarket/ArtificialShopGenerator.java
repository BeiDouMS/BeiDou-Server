package soloMapling.FreeMarket;

import org.gms.client.Job;
import org.gms.client.inventory.InventoryType;
import org.gms.client.inventory.Item;
import org.gms.server.maps.PlayerShopItem;
import soloMapling.server.MapleVersionManager;
import soloMapling.itemPool.ItemNode;
import soloMapling.itemPool.QuantitySelector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

import static soloMapling.FreeMarket.EquipListGenerator.generateEquipList;
import static soloMapling.FreeMarket.EquipListGenerator.generateEquipListIIPU;
import static soloMapling.FreeMarket.FMEconomyManager.adjustFMPrices;
import static soloMapling.FreeMarket.FMEconomyManager.adjustFMQuantity;
import static soloMapling.itemPool.ItemSelector.getRandomItemFull;
import static soloMapling.itemPool.ItemSelector.pickRandomVariantId;
import static soloMapling.itemPool.QuantitySelector.distributedTierSelector;
import static soloMapling.server.SoloMaplingUtilities.getRandomNumber;
import static soloMapling.server.SoloMaplingUtilities.pickRandomItem;
import static soloMapling.FreeMarket.HiredMerchantArtificial.shopTypes.*;

/* python code helper for id's
# Input text
text = """

01060071
01060072
01060073

"""

# Process each line, strip leading zeroes, and join with commas
result = ', '.join(line.lstrip('0') for line in text.strip().splitlines())

# Output the result
print(result)

 */


public class ArtificialShopGenerator {

    static List<String> ALL_SCROLLS = List.of("Earring", "Overall", "Gloves", "Claw", "Dagger", "Shoes", "Gloves", "Cape", "ETC");
    static List<String> ALL_DARK_SCROLLS = List.of(
            "Earring", "Overall", "Shoes",
            "Gloves", "Cape", "Hat", "Top", "Bottom", "Shield", "ETC", "Special");
    static Random random = new Random();


    public static void generateShop(HiredMerchantArtificial merchant, Job classType) {
        boolean hotRoom = FMEconomyManager.isHotRoom(merchant.getMapId());
        for (FMEquip equipEntry : generateEquipListByClassType(classType, merchant.getTier(), hotRoom)) {
            addEquipToShop(merchant, equipEntry);
        }

        // Handle special cases
        switch (classType) {
            case THIEF:
                double starsChance = switch (merchant.getTier().toUpperCase()) {
                    case "S" -> 0.70;
                    case "A" -> 0.40;
                    default -> 0.10;
                };
                if (Math.random() < starsChance) {
                    generateSecondaryShop(merchant, Stars);
                }
//                List<String> thief_scrolls = List.of("Claw", "Dagger");
//                for (FMItem thiefScroll : generateScrollsList(thief_scrolls, tier)) {
//                    addUseableToShop(fakechar, thiefScroll);
//                }
                break;
            case WARRIOR:
            case MAGICIAN:
            case BOWMAN:
                // TODO: Add class-specific scrolls/weapons here
                break;
            case BEGINNER:
                break;
            default:
                break;
        }
    }

    private static List<FMEquip> generateEquipListByClassType(Job jobType, String tier, boolean hotRoom) {
        return switch (jobType) {
            case BEGINNER -> generateEquipListByJob(tier, ClassStyle.BEGINNER, hotRoom);
            case WARRIOR -> generateEquipListByJob(tier, ClassStyle.WARRIOR, hotRoom);
            case MAGICIAN -> generateEquipListByJob(tier, ClassStyle.MAGICIAN, hotRoom);
            case BOWMAN -> generateEquipListByJob(tier, ClassStyle.BOWMAN, hotRoom);
            case THIEF -> generateEquipListByJob(tier, ClassStyle.THIEF, hotRoom);
            default -> Collections.emptyList();
        };
    }

    private static final Map<HiredMerchantArtificial.shopTypes, Function<String, List<FMItem>>> SECONDARY_SHOP_GENERATORS = Map.of(
            Stars, ArtificialShopGenerator::generateThiefStarsList,
            Scroll, ArtificialShopGenerator::generateScrollsList,
            DarkScroll, ArtificialShopGenerator::generateDarkScrollsList,
            Potion, ArtificialShopGenerator::generatePotionsList,
            ETC, ArtificialShopGenerator::generateETCList,
            Mastery, ArtificialShopGenerator::generateMasteryBookList,
            Chair, ArtificialShopGenerator::generateChairList
    );

    public static void generateSecondaryShop(HiredMerchantArtificial merchant, HiredMerchantArtificial.shopTypes shopType) {
        List<FMItem> items = SECONDARY_SHOP_GENERATORS.getOrDefault(shopType, t -> {
                    throw new IllegalArgumentException("Unsupported ShopType: " + shopType);
                })
                .apply(merchant.getTier());

        if (items != null) {
            addUseableItemsToShop(merchant, items);
        }
    }

    private static void addUseableItemsToShop(HiredMerchantArtificial merchant, List<FMItem> items) {
        for (FMItem item : items) {
            addUseableToShop(merchant, item);
        }
    }

    private enum ClassStyle {
        BEGINNER("common.yaml", List.of("Earring", "Overall", "Gloves", "Shoes", "Cape", "Hat", "Bottom", "Top")),
        THIEF("thief.yaml", List.of("Weapon", "Hat", "Top", "Pants", "Shield")),
        MAGICIAN("mage.yaml", List.of("Weapon", "Hat", "Top", "Shield")),
        BOWMAN("bowman.yaml", List.of("Weapon", "Hat", "Top", "Pants")),
        WARRIOR("warrior.yaml", List.of("Weapon", "Hat", "Top", "Pants"));

        private final String yamlFile;
        private final List<String> equipTypes;

        ClassStyle(String yamlFile, List<String> equipTypes) {
            this.yamlFile = yamlFile;
            this.equipTypes = equipTypes;
        }

        public String getYamlFile() {
            return yamlFile;
        }

        public List<String> getEquipTypes() {
            return equipTypes;
        }
    }

    private static List<FMEquip> generateEquipListByJob(String tier, ClassStyle classStyle) {
        return generateEquipListByJob(tier, classStyle, false);
    }

    private static List<FMEquip> generateEquipListByJob(String tier, ClassStyle classStyle, boolean hotRoom) {
        List<FMEquip> equipList = generateEquipList(classStyle.getYamlFile(), classStyle.getEquipTypes(), tier, hotRoom);
        equipList.addAll(generateEquipListIIPU(tier, Job.valueOf(classStyle.name())));
        return equipList;
    }

    public static List<FMEquip> generateCommonEquipList(String tier) {
        return generateEquipListByJob(tier, ClassStyle.BEGINNER);
    }

    private static List<FMEquip> generateThiefEquipList(String tier) {
        return generateEquipListByJob(tier, ClassStyle.THIEF);
    }

    private static List<FMEquip> generateMageEquipList(String tier) {
        return generateEquipListByJob(tier, ClassStyle.MAGICIAN);
    }

    private static List<FMEquip> generateBowmanEquipList(String tier) {
        return generateEquipListByJob(tier, ClassStyle.BOWMAN);
    }

    private static List<FMEquip> generateWarriorEquipList(String tier) {
        return generateEquipListByJob(tier, ClassStyle.WARRIOR);
    }

    private static List<FMEquip> generateSpecialEquipList(String tier) {
        // todo Gacha, holiday, random,
        return null;
    }

    public static List<FMItem> generateThiefStarsList(String tier) {
        List<FMItem> itemList = new ArrayList<>() {
        };
        for (int i = 0; i < 2; i++) {
            tier = distributedTierSelector(tier);
            ItemNode item = getRandomItemFull("thief.yaml", "Stars", tier);
            int itemId = pickRandomVariantId(item.getVariantId());
            int price = item.getCurrentPrice();
            itemList.add(new FMItem(itemId, price, 1));
        }
        return itemList;
    }

    public static List<FMItem> generateScrollsList(String tier) {
        return generateScrollsListInternal(ALL_SCROLLS, tier);
    }

    public static List<FMItem> generateDarkScrollsList(String tier) {
        return generateDarkScrollsListInternal(ALL_DARK_SCROLLS, tier);
    }

    private static List<FMItem> generateScrollsListInternal(List<String> items, String tier) {
        return generateScrollsList(items, "scrolls.yaml", tier);
    }

    private static List<FMItem> generateDarkScrollsListInternal(List<String> items, String tier) {
        if (MapleVersionManager.getItemPoolVersion() >= 39) {
            return generateScrollsList(items, "darkscrolls.yaml", tier);
        }
        return null;
    }

    private static List<FMItem> generateScrollsList(List<String> items, String scrollList, String tier) {
        List<FMItem> itemList = new ArrayList<>() {
        };
        for (int i = 0; i < 4; i++) {
            String randomItem = pickRandomItem(items);
            tier = distributedTierSelector(tier);
            ItemNode item = getRandomItemFull(scrollList, randomItem, tier);
            if (item != null) {
                int itemId = pickRandomVariantId(item.getVariantId());
                int price = item.getCurrentPrice();
                int qty = quantitySelector("Scroll", tier); // getRandomIntInRange(5, 13);
                itemList.add(new FMItem(itemId, price, qty));
            }
        }
        removeDuplicates(itemList);
        return itemList;
    }

    public static List<FMItem> generatePotionsList(String tier) {
        List<FMItem> itemList = new ArrayList<>() {
        };
        String useablesList = "useables.yaml";
        List<String> items = List.of("Potions");
        for (int i = 0; i < 3; i++) {
            String randomItem = pickRandomItem(items);
            tier = distributedTierSelector(tier);
            ItemNode item = getRandomItemFull(useablesList, randomItem, tier);
            if (item != null) {
                int itemId = pickRandomVariantId(item.getVariantId());
                int price = item.getCurrentPrice();
                int qty = quantitySelector("Potion", tier); // getRandomIntInRange(5, 13);
                itemList.add(new FMItem(itemId, price, qty));
            }
        }
        removeDuplicates(itemList);
        return itemList;
    }

    private static List<FMItem> generateETCList(String tier) {
        List<FMItem> itemList = new ArrayList<>() {
        };
        String useablesList = "etc.yaml";
        List<String> items = List.of("CrystalRefined", "CrystalOre", "MineralRefined", "MineralOre",
                "JewelRefined", "JewelOre", "SkillRocks", "BossItems", "RandomETC");
        for (int i = 0; i < 3; i++) {
            String randomItem = pickRandomItem(items);
            tier = distributedTierSelector(tier);
            ItemNode item = getRandomItemFull(useablesList, randomItem, tier);
            if (item != null) {
                int itemId = pickRandomVariantId(item.getVariantId());
                int price = item.getCurrentPrice();
                int qty = quantitySelector("ETC", tier); // getRandomIntInRange(5, 10);
                itemList.add(new FMItem(itemId, price, qty));
            }
        }
        removeDuplicates(itemList);
        return itemList;
    }

    private static List<FMItem> generateMasteryBookList(String tier) {
        List<FMItem> itemList = new ArrayList<>() {
        };
        String useablesList = "masteryBook.yaml";
        List<String> items = List.of("MasteryBooks");
        for (int i = 0; i < 3; i++) {
            String randomItem = pickRandomItem(items);
            tier = distributedTierSelector(tier);
            ItemNode item = getRandomItemFull(useablesList, randomItem, tier);
            if (item != null) {
                int itemId = pickRandomVariantId(item.getVariantId());
                int price = item.getCurrentPrice();
                int qty = quantitySelector("Mastery", tier);
                itemList.add(new FMItem(itemId, price, qty));
            }
        }
        removeDuplicates(itemList);
        return itemList;
    }

    private static List<FMItem> generateChairList(String tier) {
        List<FMItem> itemList = new ArrayList<>() {
        };
        String chairList = "chair.yaml";
        List<String> items = List.of("Chairs");
        for (int i = 0; i < 1; i++) {
            String randomItem = pickRandomItem(items);
            tier = distributedTierSelector(tier);
            ItemNode item = getRandomItemFull(chairList, randomItem, tier);
            if (item != null) {
                int itemId = pickRandomVariantId(item.getVariantId());
                int price = item.getCurrentPrice();
                int qty = 1;
                itemList.add(new FMItem(itemId, price, qty));
            }
        }
        removeDuplicates(itemList);
        return itemList;
    }

    private static void addEquipToShop(HiredMerchantArtificial merchant, FMEquip fmEquip) {
        addItemToShop(merchant, fmEquip.getEquip(), 1, fmEquip.getPrice());
    }

    public static Item generateItem(int itemId, int position, int qty) {
        return new Item(itemId, (short) position, (short) qty);
    }

    private static void addUseableToShop(HiredMerchantArtificial merchant, FMItem item) {
        Item itemToSell = new Item(item.getItemId(), (short) 1, (short) 1);
        short perBundle = (short) 1; // num per bundle
        itemToSell.setQuantity(perBundle);
        addItemToShop(merchant, itemToSell, item.getQty(), item.getPrice());
    }

    private static void addUseableToShop(HiredMerchantArtificial merchant, int itemId, int quantity, int price) {
        Item itemToSell = new Item(itemId, (short) 1, (short) 1);
        short perBundle = (short) 1; // num per bundle
        itemToSell.setQuantity(perBundle);
        addItemToShop(merchant, itemToSell, quantity, price);
    }

    private static void addItemToShop(HiredMerchantArtificial merchant, Item sellItem, int quantity, int price) {
        short bundles = (short) adjustFMQuantity(merchant, quantity);
        int adjustedPrice = adjustFMPrices(merchant, price);
        PlayerShopItem shopItem = new PlayerShopItem(sellItem, bundles, adjustedPrice);
        merchant.addItem(shopItem);
    }

    private static int quantitySelector(String type, String tier) {
        return QuantitySelector.quantitySelector(type, tier);
    }


    private static void removeDuplicates(List<FMItem> items) {
        Set<Integer> seenItemIds = new HashSet<>();
        Iterator<FMItem> iterator = items.iterator();

        while (iterator.hasNext()) {
            FMItem currentItem = iterator.next();
            if (!seenItemIds.add(currentItem.getItemId())) {
                // If itemId is already in the set, remove the duplicate
                iterator.remove();
            }
        }
    }

    public static void setOneMesoShop(HiredMerchantArtificial merchant) {
        for (PlayerShopItem psItem : merchant.getItems()) {
            psItem.setPrice(1);
        }
    }

    public static void applyQuittingSaleDiscount(HiredMerchantArtificial merchant) {
        applyDiscountWholeStore(merchant, 0.7);
    }

    public static void applyCheapSaleDiscount(HiredMerchantArtificial merchant) {
        applyDiscountWholeStore(merchant, 0.85);
    }

    public static void applyDiscountWholeStore(HiredMerchantArtificial merchant, double percentage) {
        for (PlayerShopItem psItem : merchant.getItems()) {
            psItem.setPrice((int) (psItem.getPrice() * percentage));
        }
    }

    public static void buyoutSomeItems(HiredMerchantArtificial merchant) {
        for (PlayerShopItem psItem : merchant.getItems()) {
            boolean sold = Math.random() < 0.09; // 9% chance to sell out item
            if (sold) {
                psItem.setBundles((short) 0);
                if (psItem.getBundles() < 1) {
                    psItem.setDoesExist(false);
                }
            }
        }
    }

    public static void applyAdditionalShopMods(HiredMerchantArtificial merchant) {
        increaseQtyForUseEtcInHotrooms(merchant);
    }

    private static void increaseQtyForUseEtcInHotrooms(HiredMerchantArtificial merchant) {
        if (FMEconomyManager.isHotRoom(merchant.getMapId())) {
            for (PlayerShopItem psItem : merchant.getItems()) {
                if (psItem.getItem().getInventoryType() == InventoryType.USE ||
                        psItem.getItem().getInventoryType() == InventoryType.ETC) {
                    psItem.setBundles((short) (psItem.getBundles() * getRandomNumber(5, 8)));
                }
            }
        }
    }
}
