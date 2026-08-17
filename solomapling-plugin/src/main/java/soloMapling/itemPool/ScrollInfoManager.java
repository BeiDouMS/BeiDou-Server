package soloMapling.itemPool;

import org.gms.client.inventory.Equip;
import org.gms.constants.inventory.EquipType;
import org.gms.server.ItemInformationProvider;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.gms.constants.inventory.EquipType.getEquipTypeById;

public class ScrollInfoManager {

    public static class ScrollInfo {
        private final Integer percent_10;
        private final Integer percent_60;
        private final Integer percent_30;  // Nullable if not provided
        private final Integer percent_70;  // Nullable if not provided

        // Constructor for 2 variables (10, 60)
        public ScrollInfo(Integer percent_10, Integer percent_60) {
            this.percent_10 = percent_10;
            this.percent_60 = percent_60;
            this.percent_30 = null;
            this.percent_70 = null;
        }

        // Constructor for 4 variables (10, 60, 30, 70)
        public ScrollInfo(Integer percent_10, Integer percent_60, Integer percent_30, Integer percent_70) {
            this.percent_10 = percent_10;
            this.percent_60 = percent_60;
            this.percent_30 = percent_30;
            this.percent_70 = percent_70;
        }

        public Integer getPercent_10_or_30() {
            if (getPercent_10() == null) {
                return getPercent_30();
            }
            return getPercent_10();
        }

        public Integer getPercent_60_or_70() {
            if (getPercent_60() == null) {
                return getPercent_70();
            }
            return getPercent_60();
        }

        public Integer getPercent_10() {
            return this.percent_10;
        }

        public Integer getPercent_60() {
            return this.percent_60;
        }

        public Integer getPercent_30() {
            return this.percent_30;
        }

        public Integer getPercent_70() {
            return this.percent_70;
        }

        public boolean hasPercent_10_60() {
            return this.percent_10 != null && this.percent_60 != null;
        }

        // Check if 30 and 70 are available
        public boolean hasPercent_30_70() {
            return this.percent_30 != null && this.percent_70 != null;
        }
    }

    private static final Map<EquipType, Map<String, ScrollInfo>> scrollMap = new EnumMap<>(EquipType.class);

    // todo add all weapon type scrolls
    // todo add all armor type scrolls

    static {
        // Add single scroll options
        scrollMap.put(EquipType.CLAW, Map.of("watt", new ScrollInfo(2044702, 2044701)));
        scrollMap.put(EquipType.DAGGER, Map.of("watt", new ScrollInfo(2043302, 2043301)));

        scrollMap.put(EquipType.CAP, Map.of(
                "int", new ScrollInfo(null, null, 2040013, 2040012)
        ));

        // Add multiple options for COAT
        scrollMap.put(EquipType.COAT, Map.of(
                "luk", new ScrollInfo(null, null, 2040411, 2040410),
                "str", new ScrollInfo(null, null, 2040407, 2040406)
        ));

        scrollMap.put(EquipType.PANTS, Map.of(
                "dex", new ScrollInfo(null, null, 2040611, 2040610)
        ));

        scrollMap.put(EquipType.LONGCOAT, Map.of(
                "dex", new ScrollInfo(2040502, 2040501, 2040509, 2040508),
                "int", new ScrollInfo(2040514, 2040513, 2040519, 2040518),
                "luk", new ScrollInfo(null, null, 2040521, 2040520)
        ));

        scrollMap.put(EquipType.GLOVES, Map.of(
                "watt", new ScrollInfo(2040805, 2040804, 2040811, 2040810),
                "matt", new ScrollInfo(null, null, 2040815, 2040814),
                "int", new ScrollInfo(null, null, 2040815, 2040814)
        ));

        scrollMap.put(EquipType.SHOES, Map.of(
                "dex", new ScrollInfo(2040705, 2040704, 2040715, 2040714)
        ));

        scrollMap.put(EquipType.CAPE, Map.of(
                "str", new ScrollInfo(2041014, 2041013, 2041035, 2041034),
                "dex", new ScrollInfo(2041020, 2041019, 2041039, 2041038),
                "int", new ScrollInfo(2041017, 2041016, 2041037, 2041036),
                "luk", new ScrollInfo(2041023, 2041022, 2041041, 2041040)
        ));

        scrollMap.put(EquipType.EARRING, Map.of(
                "int", new ScrollInfo(2040302, 2040301, 2040305, 2040304),
                "matt", new ScrollInfo(2040302, 2040301, 2040305, 2040304),
                "dex", new ScrollInfo(2040307, 2040306, 2040307, 2040306)
        ));

        scrollMap.put(EquipType.SHIELD, Map.of(
                "watt", new ScrollInfo(null, null, 2040917, 2040916),
                "matt", new ScrollInfo(null, null, 2040922, 2040921),
                "luk", new ScrollInfo(null, null, 2040907, 2040906)
        ));

    }

    // todo the logic for checking 10/60 vs 30/70 might be wonky. need to test more
    // Retrieve ScrollInfo based on EquipType and type
    public static ScrollInfo getScrollInfo(Equip sellItem, String type) {
        EquipType equipType = getEquipTypeById(sellItem.getItemId());
        Map<String, ScrollInfo> typeMap = scrollMap.get(equipType);
        if (typeMap == null) {
            return null;
        }
        // check if this scroll exists in current version
        ScrollInfo currentScroll = typeMap.get(type);
        if (currentScroll == null) {
            return null;
        }

        if (currentScroll.hasPercent_10_60()) {
            if (!checkIfScrollExistsInCurrentVersion(currentScroll.getPercent_10())) {
                return null;
            }
        }
        if (currentScroll.hasPercent_30_70() && !currentScroll.hasPercent_10_60()) {
            if (!checkIfScrollExistsInCurrentVersion(currentScroll.getPercent_30())) {
                return null;
            }
        }
        return currentScroll;
    }

    public static boolean checkIfScrollExistsInCurrentVersion(int itemId) {
        if (ItemDatabase.getInstance().checkIfItemExistsInCurrentVersion(itemId)) {
            return true;
        }
        return false;
    }

    // Define the list of EquipTypes to check against
    // List of equips that can be Common
    private static final Set<EquipType> targetEquipTypes = EnumSet.of(
            EquipType.COAT,
            EquipType.PANTS,
            EquipType.EARRING,
            EquipType.LONGCOAT,
            EquipType.CAPE,
            EquipType.SHOES,
            EquipType.GLOVES,
            EquipType.CAP,
            EquipType.SHIELD
    );

    public static ScrollInfo getScrollInfo(Equip sellItem, ScrolledItemComparator comp) {
        String statType = comp.getHighestStatType();
        return getScrollInfo(sellItem, statType);
    }

    // Overloaded method for default scrolls if no specific type is needed
    public static ScrollInfo getScrollInfo(Equip sellItem) {
        ItemInformationProvider ii = ItemInformationProvider.getInstance();
        if (isTargetEquipType(sellItem.getItemId())) {
            int reqJob = ii.getEquipStats(sellItem.getItemId()).get("reqJob");
            if (reqJob == 0) {
                return getScrollTypeGuarantee(sellItem);
            }
            if (reqJob == 1) { // warrior

                return getScrollInfo(sellItem, "str");
            }
            if (reqJob == 2) { // mage
                if (getEquipTypeById(sellItem.getItemId()) == EquipType.GLOVES) {
                    return getScrollInfo(sellItem, "matt");
                }
                return getScrollInfo(sellItem, "int");
            }
            if (reqJob == 4) { // bow
                return getScrollInfo(sellItem, "dex");
            }
            if (reqJob == 8) { // thief
                ScrollInfo primary = getScrollInfo(sellItem, "luk");
                return (primary != null) ? primary : getScrollInfo(sellItem, "dex");
            }
        }

        // todo add checker for watt weapons, matt weapons, etc
        if (getEquipTypeById(sellItem.getItemId()) == EquipType.DAGGER ||
                getEquipTypeById(sellItem.getItemId()) == EquipType.CLAW) {
            return getScrollInfo(sellItem, "watt");
        }

        return getScrollInfo(sellItem, "default");
    }

    public static boolean isTargetEquipType(int itemId) {
        EquipType equipType = getEquipTypeById(itemId);
        return targetEquipTypes.contains(equipType);
    }


    public static ScrollInfo getScrollTypeGuarantee(Equip sellItem) {
        List<String> types = Arrays.asList("watt", "matt", "str", "luk", "dex", "int"); // Add more options as needed
        Collections.shuffle(types);
        for (String stat : types) {
            ScrollInfo ret = getScrollInfo(sellItem, stat);
            if (ret != null) {
                return ret;
            }
        }
        return null;
    }


    public static void main(String[] args) {
//        System.out.println(checkIfItemExistsInCurrentVersion(2070016, "thief.yaml")); // cilbi
//        System.out.println(checkIfScrollExistsInCurrentVersion(2070016)); //
//        System.out.println(checkIfScrollExistsInCurrentVersion(2040407)); //


    }

}
