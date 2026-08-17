package soloMapling.ArtificialPlayer.BotDecoratorSystem;

import org.gms.client.Character;
import org.gms.constants.inventory.EquipType;
import soloMapling.ArtificialPlayer.BotCustomization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static soloMapling.itemPool.ItemInformationProviderUtilities.getRandomEquip;
import static soloMapling.itemPool.ItemInformationProviderUtilities.getRandomEquipForWearing;

/**
 * Class to handle equipment rules with extensibility in mind
 */
public class EquipDecoratorRules {
    private final Map<EquipType, Double> equipTypesWithSkipChance = new HashMap<>();
    private final List<List<List<EquipType>>> mutuallyExclusiveGroups = new ArrayList<>();
    private final Random random = new Random();

    /**
     * Add an equipment type with a chance to skip it
     *
     * @param type       Equipment type
     * @param skipChance Chance to skip (0.0 to 1.0)
     */
    public void addEquipType(EquipType type, double skipChance) {
        equipTypesWithSkipChance.put(type, skipChance);
    }

    /**
     * Add multiple equipment types with a chance to skip them
     *
     * @param types      List of equipment types
     * @param skipChance Chance to skip (0.0 to 1.0)
     */
    public void addEquipTypes(List<EquipType> types, double skipChance) {
        for (EquipType type : types) {
            addEquipType(type, skipChance);
        }
    }

    /**
     * Add a group of mutually exclusive equipment options
     *
     * @param groups Two or more lists of equipment that are mutually exclusive
     */
    public void addMutuallyExclusiveGroup(List<EquipType>... groups) {
        mutuallyExclusiveGroups.add(Arrays.asList(groups));
    }

    /**
     * Apply all equipment rules and equip the character
     *
     * @param fakechar Character to equip
     */
    public void applyRules(Character fakechar) {
        // Handle individual equipment pieces with skip chances
        for (Map.Entry<EquipType, Double> entry : equipTypesWithSkipChance.entrySet()) {
            EquipType type = entry.getKey();
            Double skipChance = entry.getValue();

            // Skip if mutually exclusive (will be handled separately)
            if (isMutuallyExclusive(type)) {
                continue;
            }

            // Apply skip chance
            if (random.nextDouble() >= skipChance) {
                Integer itemId;
                try {
                    itemId = getRandomEquipForWearing(type, fakechar);
                    BotCustomization.EquipBot(fakechar, itemId);
                } catch (Exception e) {
//                    throw new RuntimeException(e);
                }
            }
        }

        // Handle mutually exclusive groups
        for (List<List<EquipType>> group : mutuallyExclusiveGroups) {
            // Select one random group from the mutually exclusive options
            List<EquipType> selectedGroup = group.get(random.nextInt(group.size()));

            // Apply all equipment in the selected group
            for (EquipType type : selectedGroup) {
                // Still respect individual skip chances if defined
                Double skipChance = equipTypesWithSkipChance.getOrDefault(type, 0.0);
                if (random.nextDouble() >= skipChance) {
                    int itemId = getRandomEquipForWearing(type, fakechar);
                    BotCustomization.EquipBot(fakechar, itemId);
                }
            }
        }
    }

    /**
     * Check if an equipment type is part of a mutually exclusive group
     *
     * @param type Equipment type to check
     * @return true if part of a mutually exclusive group
     */
    private boolean isMutuallyExclusive(EquipType type) {
        for (List<List<EquipType>> group : mutuallyExclusiveGroups) {
            for (List<EquipType> option : group) {
                if (option.contains(type)) {
                    return true;
                }
            }
        }
        return false;
    }
}