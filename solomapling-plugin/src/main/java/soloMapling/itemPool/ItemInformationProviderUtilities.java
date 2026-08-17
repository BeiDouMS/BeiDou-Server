package soloMapling.itemPool;

import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.client.Job;
import org.gms.constants.inventory.EquipType;
import org.gms.server.ItemInformationProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import static java.lang.Math.max;
import static soloMapling.DebugUtilities.debugprint;
import static soloMapling.server.SoloMaplingUtilities.random;

public class ItemInformationProviderUtilities {

    // Define the ranges for equip types
    private static final Map<EquipType, int[]> equipTypeRanges = Map.of(
            EquipType.CAP, new int[]{1000000, 1003073},
            EquipType.COAT, new int[]{1040000, 1049000},
            EquipType.LONGCOAT, new int[]{1050000, 1052234},
            EquipType.PANTS, new int[]{1060000, 1062119},
            EquipType.SHOES, new int[]{1070000, 1072437},
            EquipType.GLOVES, new int[]{1080000, 1082262},
            EquipType.SHIELD, new int[]{1092000, 1092062},

            EquipType.CAPE, new int[]{1102000, 1102236},
            EquipType.EARRING, new int[]{1032000, 1032075},
            EquipType.ACCESSORY, new int[]{1022000, 1022104} // eye
    );


    private static final Map<EquipType, int[]> weaponTypeRanges = new HashMap<>();

    static {
        weaponTypeRanges.put(EquipType.SWORD, new int[]{1302000, 1302133});
        weaponTypeRanges.put(EquipType.AXE, new int[]{1312000, 1312046});
        weaponTypeRanges.put(EquipType.MACE, new int[]{1322000, 1322074});
        weaponTypeRanges.put(EquipType.DAGGER, new int[]{1332000, 1332088});
        weaponTypeRanges.put(EquipType.WAND, new int[]{1372000, 1372046});
        weaponTypeRanges.put(EquipType.STAFF, new int[]{1382000, 1382062});
        weaponTypeRanges.put(EquipType.SWORD_2H, new int[]{1402000, 1402072});
        weaponTypeRanges.put(EquipType.AXE_2H, new int[]{1412000, 1412046});
        weaponTypeRanges.put(EquipType.MACE_2H, new int[]{1422000, 1422047});
        weaponTypeRanges.put(EquipType.SPEAR, new int[]{1432000, 1432061});
        weaponTypeRanges.put(EquipType.POLEARM, new int[]{1442000, 1442103});
        weaponTypeRanges.put(EquipType.BOW, new int[]{1452000, 1452085});
        weaponTypeRanges.put(EquipType.CROSSBOW, new int[]{1462000, 1462075});
        weaponTypeRanges.put(EquipType.CLAW, new int[]{1472000, 1472100});
        weaponTypeRanges.put(EquipType.KNUCKLER, new int[]{1482000, 1482046});
        weaponTypeRanges.put(EquipType.PISTOL, new int[]{1492000, 1492048});
    }

    public static EquipType getRandomEquipType(Job jobStyle) {
        Random random = new Random();
        // Combine keys from both maps
//        List<EquipType> allKeys = new ArrayList<>();
//        allKeys.addAll(equipTypeRanges.keySet());

        List<EquipType> equipTypes = new ArrayList<>();

        // Shared equip types for all jobs
        equipTypes.addAll(List.of(
                EquipType.CAP,
                EquipType.COAT,
                EquipType.LONGCOAT,
                EquipType.PANTS,
                EquipType.GLOVES,
                EquipType.SHOES
        ));

        // Job-specific equip types
        switch (jobStyle) {
            case BEGINNER:
                equipTypes.addAll(List.of(
                        EquipType.SHIELD,
                        EquipType.CAPE,
                        EquipType.EARRING,
                        EquipType.ACCESSORY
                ));
                break;
            case WARRIOR:
                equipTypes.addAll(List.of(
                        EquipType.SHIELD,
                        EquipType.SWORD,
                        EquipType.SWORD_2H,
                        EquipType.AXE,
                        EquipType.AXE_2H,
                        EquipType.SPEAR,
                        EquipType.POLEARM
                ));
                break;
            case MAGICIAN:
                equipTypes.addAll(List.of(
                        EquipType.SHIELD,
                        EquipType.WAND,
                        EquipType.STAFF
                ));
                break;
            case BOWMAN:
            case CROSSBOWMAN:
                equipTypes.addAll(List.of(
                        EquipType.BOW,
                        EquipType.CROSSBOW
                ));
                break;
            case THIEF:
                equipTypes.addAll(List.of(
                        EquipType.SHIELD,
                        EquipType.CLAW,
                        EquipType.DAGGER
                ));
                break;
            default:
                break;
        }

        return equipTypes.get(random.nextInt(equipTypes.size()));
    }

    // Method to get the min and max range for an equip type
    public static int[] getIdRangeForEquipType(EquipType equipType) {
        if (equipTypeRanges.containsKey(equipType)) {
            return equipTypeRanges.getOrDefault(equipType, new int[]{0, 0}); // Default to 0-0 if not found
        }
        if (weaponTypeRanges.containsKey(equipType)) {
            return weaponTypeRanges.getOrDefault(equipType, new int[]{0, 0});
        }
        return new int[]{0, 0};
    }

//    public static int getRandomEquip(int jobType) {
//        // gets a random equip based on jobType (warr,bow,mage,thief,pirate,common)
//        return 0;
//    }


    private static final Map<Job, Integer> JOB_TO_REQ_MAP = Map.of(
            Job.WARRIOR, 1,
            Job.MAGICIAN, 2,
            Job.BOWMAN, 4,
            Job.THIEF, 8,
            Job.PIRATE, 16
    );

    public static int getReqJobViaJobStyle(Job jobStyle) {
        // getJobStyle() resolves the crossbow line to CROSSBOWMAN, which isn't in
        // JOB_TO_REQ_MAP - without this it falls back to reqJob 0 (classless gear).
        // Crossbowmen wear the same gear as bowmen, so normalize to BOWMAN (reqJob 4).
        // Done here rather than as a map entry so getJobStyleViaReqJob stays a clean
        // 1:1 reverse lookup (no two job styles mapping to value 4).
        if (jobStyle == Job.CROSSBOWMAN) {
            jobStyle = Job.BOWMAN;
        }
        return JOB_TO_REQ_MAP.getOrDefault(jobStyle, 0);
    }

    public static Job getJobStyleViaReqJob(int reqJob) {
        for (Map.Entry<Job, Integer> entry : JOB_TO_REQ_MAP.entrySet()) {
            if (entry.getValue() == reqJob) {
                return entry.getKey();
            }
        }
        return Job.BEGINNER; // Return null if not found
    }

    /**
     * Picks a random non-cash equip matching the bot's level window, job style and
     * gender, weighted towards lower ids (more classic equips). Pure in-memory
     * lookup against {@link EquipMetadataCache} — no WZ access.
     *
     * <p>Returns null if the cache isn't initialized yet (early spawns fall back
     * to QuickEquip / the decoration queue) or if nothing matches.
     */
    public static Integer getRandomEquip(EquipType eqType, int maxLevel, Job jobStyle, int gender) {
        if (!EquipMetadataCache.isInitialized()) {
            debugprint("[getRandomEquip] EquipMetadataCache not initialized - skipping " + eqType);
            return null;
        }

        int reqJob = getReqJobViaJobStyle(jobStyle);

        // Primary window (same as the legacy WZ-scan path):
        // maxLevel - max(25% of maxLevel, 10) <= reqLevel <= maxLevel
        int minLevel = maxLevel - max((int) (maxLevel * 0.25), 10);
        List<EquipMetadataCache.EquipEntry> candidates = EquipMetadataCache.get()
                .query(eqType)
                .nonCash()
                .forGender(gender)
                .levelBetween(minLevel, maxLevel)
                .forJobExact(reqJob)
                .asList();

        // Fallback for bots above the v83 gear ceiling (~lv120): the primary window
        // sits entirely above any gear that exists, which left high-level bots naked.
        // Re-window around the highest reqLevel that actually exists at-or-below the
        // bot's level, so e.g. a lv200 bot still gets the lv120 endgame piece instead
        // of nothing - the "next closest gear" the bot was missing.
        if (candidates.isEmpty()) {
            List<EquipMetadataCache.EquipEntry> belowCap = EquipMetadataCache.get()
                    .query(eqType)
                    .nonCash()
                    .forGender(gender)
                    .maxLevel(maxLevel)
                    .forJobExact(reqJob)
                    .asList();
            candidates = highestLevelBand(belowCap);
        }

        // Cache lists are built in ascending id order, which selectWeightedRandom
        // relies on to bias towards classic (low-id) equips; the filters above
        // preserve that order.
        List<Integer> validEquips = new ArrayList<>(candidates.size());
        for (EquipMetadataCache.EquipEntry entry : candidates) {
            // Skip omitted items (flag poles / junk that look bad on a bot). Dropping
            // them from the candidate list means the weighted pick simply lands on a
            // different item of the same slot - the bot just gets something else.
            if (EquipOmitList.isOmitted(entry.id)) continue;
            validEquips.add(entry.id);
        }
        return selectWeightedRandom(validEquips);
    }

    /**
     * From candidates at-or-below the bot's level, keep only those near the top of
     * what's available - within 25% (min 10 levels) of the highest reqLevel present.
     * Keeps a lv200 bot in lv110-120 gear rather than handing it a starter item.
     * Input order is preserved (callers rely on ascending id for weighting).
     */
    private static List<EquipMetadataCache.EquipEntry> highestLevelBand(
            List<EquipMetadataCache.EquipEntry> entries) {
        if (entries.isEmpty()) {
            return entries;
        }
        int cap = 0;
        for (EquipMetadataCache.EquipEntry e : entries) {
            cap = Math.max(cap, e.reqLevel);
        }
        int floor = cap - max((int) (cap * 0.25), 10);
        List<EquipMetadataCache.EquipEntry> band = new ArrayList<>();
        for (EquipMetadataCache.EquipEntry e : entries) {
            if (e.reqLevel >= floor) {
                band.add(e);
            }
        }
        return band;
    }

    /**
     * Selects a random integer from the list with weighted preference towards lower values.
     * The list is divided into intervals, and earlier intervals have higher selection probability.
     * Designed such that given a list of validEquips that you want to select at random, but prioritize "lower" numbers
     * meaning it will weigh more towards the classic equips, instead of the bizarre equips like maple tcg, or jank equips
     *
     * @param validEquips        List of integers (should be sorted from lowest to highest)
     * @param intervalPercentage The percentage size of each interval (e.g., 20 for 20% intervals)
     * @return A randomly selected integer from the list, or null if list is empty
     * @throws IllegalArgumentException if intervalPercentage is not between 1 and 100
     */
    public static Integer selectWeightedRandom(List<Integer> validEquips, int intervalPercentage) {
        if (validEquips == null || validEquips.isEmpty()) {
            return null;
        }

        if (intervalPercentage < 1 || intervalPercentage > 100) {
            throw new IllegalArgumentException("Interval percentage must be between 1 and 100");
        }

        int listSize = validEquips.size();
        int intervalSize = Math.max(1, (listSize * intervalPercentage) / 100);
        int numIntervals = (int) Math.ceil((double) listSize / intervalSize);

        // Create weights for each interval (earlier intervals get higher weights)
        // Using a simple linear decay: first interval gets weight numIntervals,
        // second gets numIntervals-1, etc.
        double[] intervalWeights = new double[numIntervals];
        double totalWeight = 0;

        for (int i = 0; i < numIntervals; i++) {
            intervalWeights[i] = numIntervals - i; // Linear decay
            totalWeight += intervalWeights[i];
        }

        // Select an interval based on weights
        double randomValue = random.nextDouble() * totalWeight;
        int selectedInterval = 0;
        double cumulativeWeight = 0;

        for (int i = 0; i < numIntervals; i++) {
            cumulativeWeight += intervalWeights[i];
            if (randomValue <= cumulativeWeight) {
                selectedInterval = i;
                break;
            }
        }

        // Calculate the bounds of the selected interval
        int startIndex = selectedInterval * intervalSize;
        int endIndex = Math.min(startIndex + intervalSize, listSize);

        // Randomly select an element from the chosen interval
        int randomIndex = startIndex + random.nextInt(endIndex - startIndex);

        return validEquips.get(randomIndex);
    }

    /**
     * Convenience method using 20% intervals as default
     */
    public static Integer selectWeightedRandom(List<Integer> validEquips) {
        return selectWeightedRandom(validEquips, 20);
    }


    public static boolean checkEquipWithinLevelRange(int levelReq, int maxLevel) {
        int minRange = max((int) (maxLevel * 0.25), 10);
        return checkEquipWithinLevelRange(levelReq, maxLevel, minRange);
    }

    private static boolean checkEquipWithinLevelRange(int levelReq, int maxLevel, int minimumRange) {
        return maxLevel - minimumRange <= levelReq && levelReq <= maxLevel;
    }

//    private static boolean checkTradeable(Integer statsIITradeBlock) {
//        return statsIITradeBlock != 1;
//    }
//
//    private static boolean checkQuestItem(Integer statsIIQuest) {
//        return statsIIQuest != 1;
//    }


    public static boolean checkEquipMatchJobStyle(int itemId, Job jobStyle) {
        ItemInformationProvider ii = ItemInformationProvider.getInstance();
        return ii.getEquipStats(itemId).get("reqJob") == getReqJobViaJobStyle(jobStyle);
    }

    public static Job getJobStyleFromItemId(Integer itemId) {
        ItemInformationProvider ii = ItemInformationProvider.getInstance();
        return getJobStyleViaReqJob(ii.getEquipStats(itemId).get("reqJob"));
    }

    public static Integer getRandomEquip(EquipType eqType, Character fakechar) {
        int maxLevel = fakechar.getLevel();
        Job jobStyle = fakechar.getJobStyle();
        int gender = fakechar.getGender();
        return getRandomEquip(eqType, maxLevel, jobStyle, gender);
    }

    /**
     * Tries to get random equip based on class. If null then it'll get a common one
     */
    public static Integer getRandomEquipForWearing(EquipType eqType, Character fakechar) {
        Integer randEq = getRandomEquip(eqType, fakechar);
        if (randEq != null) {
            return randEq;
        }
        int maxLevel = fakechar.getLevel();
        Job jobStyle = Job.BEGINNER;
        int gender = fakechar.getGender();
        return getRandomEquip(eqType, maxLevel, jobStyle, gender);
    }

    public static Integer getRandomEquip(EquipType eqType, int maxLevel, Job jobStyle) {
        int gender = Math.random() < 0.5 ? 0 : 1;
        return getRandomEquip(eqType, maxLevel, jobStyle, gender);
    }

    public static boolean checkEquipGenderMatch(int gender, int itemId) {
        List<Integer> allowed_digits = new ArrayList<>();

        // Specify the allowed digits
        if (gender == 0) { // Male
            allowed_digits.add(0); // Male
            allowed_digits.add(2); // Unisex
        } else if (gender == 1) { // Female
            allowed_digits.add(1); // Female
            allowed_digits.add(2); // Unisex
        } else { // Default to male
            allowed_digits.add(0); // Male
            allowed_digits.add(2); // Unisex
        }

        // Get the 4th digit from the left of the item ID
        int fourthDigit = getFourthDigit(itemId);

        // Check if the fourth digit matches one of the allowed digits
        return allowed_digits.contains(fourthDigit);
    }

    public static boolean checkTradeable(int itemId) {
        ItemInformationProvider ii = ItemInformationProvider.getInstance();
        return !ii.isUntradeableRestricted(itemId);
    }

    public static boolean checkQuestEquip(int itemId) {
        ItemInformationProvider ii = ItemInformationProvider.getInstance();
        return !ii.isQuestItem(itemId);
    }

    // Used to determine Gender. 0 = Male, 1 = Female, 2 = Unisex
    public static int getFourthDigit(int itemId) {
        String itemIdStr = Integer.toString(itemId); // Convert integer to string
        if (itemIdStr.length() >= 4) { // Ensure it has at least 4 digits
            return java.lang.Character.getNumericValue(itemIdStr.charAt(3)); // Get the digit at index 3
        } else {
            throw new IllegalArgumentException("ItemId must have at least 4 digits");
        }
    }

    public static int getRandomCashEquip(Character fakechar, EquipType eqType) {
        ItemInformationProvider ii = ItemInformationProvider.getInstance();
        List<Integer> itemList = getAllItemIdsByEquipType(eqType, false);
        Collections.shuffle(itemList);

        for (Integer itemId : itemList) {
            if (checkEquipGenderMatch(fakechar.getGender(), itemId) && // gender Match
                    ii.isCash(itemId) // cash Item
            ) {
                return itemId;
            }
        }
        return 0;
    }

    public static List<Integer> getAllItemIdsByEquipType(EquipType equipType, boolean ignoreCashItem) {
        ItemInformationProvider ii = ItemInformationProvider.getInstance();
        int range[] = getIdRangeForEquipType(equipType);
//        debugprint("Get all items in range start");
        List<Integer> itemIds = ii.getItemIdsInRange(range[0], range[1], ignoreCashItem);
//        debugprint("Get all items in range end");
        return itemIds;
    }

    public static List<Integer> getAllItemIdsByEquipType(EquipType equipType) {
        return getAllItemIdsByEquipType(equipType, true);
    }

    public static Integer getWzPrice(int itemId) {
        ItemInformationProvider ii = ItemInformationProvider.getInstance();
        int price = ii.getWholePrice(itemId);
        if (price <= 50) { // Random items have no value
            price = 5000000; // todo procedurally calculate value based on level?
        }
        return price;
    }

    public static String getItemName(int itemId) {
        return ItemInformationProvider.getInstance().getName(itemId);
    }

}
