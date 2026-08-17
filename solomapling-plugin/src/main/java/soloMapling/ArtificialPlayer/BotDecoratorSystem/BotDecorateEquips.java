package soloMapling.ArtificialPlayer.BotDecoratorSystem;

import org.gms.client.Character;
import org.gms.client.Job;
import org.gms.constants.inventory.EquipType;
import soloMapling.ArtificialPlayer.BotCustomization;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static soloMapling.ArtificialPlayer.BotDecoratorSystem.JobPathLogic.determineBowmanPath;
import static soloMapling.ArtificialPlayer.BotDecoratorSystem.JobPathLogic.determineThiefPath;
import static soloMapling.ArtificialPlayer.BotDecoratorSystem.JobPathLogic.determineWarriorPath;
import static soloMapling.itemPool.ItemInformationProviderUtilities.getRandomEquipForWearing;

public class BotDecorateEquips {
    /**
     * Full decoration pass. Equip selection is an in-memory EquipMetadataCache
     * lookup, so this is cheap enough to run inline at spawn time. Everything
     * runs sequentially on the calling thread - do NOT fan out into async tasks,
     * or multiple threads will mutate the same Character's inventory concurrently.
     */
    public static void decorateBotEquips(Character fakechar) {
        // Level 1-9 bots all share the classless BEGINNER job; the class-aware
        // pass below would dress them in random junk, so hand them curated
        // starter gear instead and stop.
        if (BeginnerEquip.isBeginner(fakechar)) {
            BeginnerEquip.apply(fakechar);
            return;
        }
        equipTopBottom(fakechar);
        equipWeapon(fakechar);
        equipCapGloveShoes(fakechar);
        decorateBotAccessories(fakechar);
    }

    public static void decorateBotBaseArmors(Character fakechar) {
        // Define rule-based equipment configuration
        EquipDecoratorRules rules = new EquipDecoratorRules();

        // Add basic equipment types
        rules.addEquipType(EquipType.CAP, 0.25); // 25% chance to skip
        rules.addEquipType(EquipType.GLOVES, 0.25); // 25% chance to skip
        rules.addEquipType(EquipType.SHOES, 0.10); // 10% chance to skip

        // Apply the rules and equip the bot
        rules.applyRules(fakechar);
    }

    static void equipTopBottom(Character fakechar) {
        Integer coatId = getCoatId(fakechar);
        Integer pantsId = getPantsId(fakechar);
        Integer longcoatId = getLongcoatId(fakechar);

        if (coatId != null && pantsId != null) {
            BotCustomization.EquipBot(fakechar, coatId);
            BotCustomization.EquipBot(fakechar, pantsId);
        } else if (longcoatId != null) {
            BotCustomization.EquipBot(fakechar, longcoatId);
        } else if (coatId != null) {
            BotCustomization.EquipBot(fakechar, coatId);
        } else if (pantsId != null) {
            BotCustomization.EquipBot(fakechar, pantsId);
        } else {
            System.out.println("[equipTopBottom] No top/bottom/overall found for "
                    + fakechar.getName() + " (job=" + fakechar.getJob().name()
                    + " lv=" + fakechar.getLevel() + ")");
        }
    }

    private static Integer getCoatId(Character c) {
        return getRandomEquipForWearing(EquipType.COAT, c);
    }

    private static Integer getPantsId(Character c) {
        return getRandomEquipForWearing(EquipType.PANTS, c);
    }

    private static Integer getLongcoatId(Character c) {
        return getRandomEquipForWearing(EquipType.LONGCOAT, c);
    }

//    private static void equipCapGloveShoes(Character fakechar) {
//        Integer capId = getRandomEquipForWearing(EquipType.CAP, fakechar);
//        Integer glovesId = getRandomEquipForWearing(EquipType.GLOVES, fakechar);
//        Integer shoesId = getRandomEquipForWearing(EquipType.SHOES, fakechar);
//        Integer capeId = getRandomEquipForWearing(EquipType.CAPE, fakechar);
//        if (capId != null) {
//            BotCustomization.EquipBot(fakechar, capId);
//        }
//        if (glovesId != null) {
//            BotCustomization.EquipBot(fakechar, glovesId);
//        }
//        if (shoesId != null) {
//            BotCustomization.EquipBot(fakechar, shoesId);
//        }
//        if (capeId != null) {
//            BotCustomization.EquipBot(fakechar, capeId);
//        }
//    }

    private static void equipCapGloveShoes(Character fakechar) {
        equipIfPresent(fakechar, getCapId(fakechar));
        equipIfPresent(fakechar, getGlovesId(fakechar));
        equipIfPresent(fakechar, getShoesId(fakechar));
        equipIfPresent(fakechar, getCapeId(fakechar));
    }

    private static Integer getCapId(Character c) {
        return getRandomEquipForWearing(EquipType.CAP, c);
    }

    private static Integer getGlovesId(Character c) {
        return getRandomEquipForWearing(EquipType.GLOVES, c);
    }

    private static Integer getShoesId(Character c) {
        return getRandomEquipForWearing(EquipType.SHOES, c);
    }

    private static Integer getCapeId(Character c) {
        return getRandomEquipForWearing(EquipType.CAPE, c);
    }

    private static void equipIfPresent(Character c, Integer itemId) {
        if (itemId != null) {
            BotCustomization.EquipBot(c, itemId);
        }
    }


    /**
     * // currently not being used due to null pointers in logic
     * Equips the character with either Coat+Pants (preferred) or Longcoat (fallback)
     *
     * @param character The character to equip
     */
    private static void equipOutfit(Character character) {
        // First try to equip coat and pants (80% chance)
        Random random = new Random();
        boolean tryCoatAndPants = random.nextDouble() < 0.8; // 80% chance to try coat+pants first

        if (tryCoatAndPants) {
            // Try to find and equip a coat
            int coatId = getRandomEquipForWearing(EquipType.COAT, character);
            if (coatId > 0) { // Assuming getRandomEquip returns <= 0 if no suitable equip is found
                BotCustomization.EquipBot(character, coatId);

                // Now try to find and equip pants
                int pantsId = getRandomEquipForWearing(EquipType.PANTS, character);
                if (pantsId > 0) {
                    BotCustomization.EquipBot(character, pantsId);
                    return; // Successfully equipped coat and pants, we're done
                }

                // If we couldn't find pants, we should remove the coat
                // (Assuming there's a way to unequip - if not, we can skip this)
//                BotCustomization.UnequipBot(character, coatId);
            }
        }

        // If we're here, either:
        // 1. We decided to try longcoat first (20% chance), or
        // 2. We failed to equip both coat and pants
        // So let's try to equip a longcoat
        int longcoatId = getRandomEquipForWearing(EquipType.LONGCOAT, character);
        if (longcoatId > 0) {
            BotCustomization.EquipBot(character, longcoatId);
        } else {
            // If all else fails, make another attempt at coat and pants if we didn't try already
            if (!tryCoatAndPants) {
                int coatId = getRandomEquipForWearing(EquipType.COAT, character);
                if (coatId > 0) {
                    BotCustomization.EquipBot(character, coatId);

                    int pantsId = getRandomEquipForWearing(EquipType.PANTS, character);
                    if (pantsId > 0) {
                        BotCustomization.EquipBot(character, pantsId);
                    }
                }
            }
        }
    }

    public static void decorateBotAccessories(Character fakechar) {
        EquipDecoratorRules rules = new EquipDecoratorRules();
        rules.addEquipType(EquipType.CAPE, 0.50); // 25% chance to skip
        rules.addEquipType(EquipType.EARRING, 0.50); // 25% chance to skip
        rules.addEquipType(EquipType.ACCESSORY, 0.98); // 25% chance to skip
        rules.applyRules(fakechar);
    }

    /**
     * Determines appropriate weapons for a character based on their class and job advancement
     *
     * @param character The character to equip
     */
    public static void equipWeapon(Character character) {
        Random random = new Random();
        Job jobType = character.getJobStyle();
        int jobId = character.getJob().getId(); // Specific job ID
        int jobLevel = character.getJob().getJobTier(); // 0 for beginner, 1 for first job, etc.

        List<EquipType> possibleWeapons = new ArrayList<>();
        boolean useShield = false;

        // Determine possible weapons based on job type and advancement level
        switch (jobType) {
            case WARRIOR:
                if (jobLevel == 1) {
                    // Beginner warrior can use any warrior weapon
                    boolean useTwoHanded = random.nextBoolean();

                    if (useTwoHanded) {
                        List<EquipType> twoHandedOptions = List.of(
                                EquipType.SWORD_2H,
                                EquipType.AXE_2H,
                                EquipType.SPEAR,
                                EquipType.POLEARM
                        );
                        possibleWeapons.add(twoHandedOptions.get(random.nextInt(twoHandedOptions.size())));
                    } else {
                        List<EquipType> oneHandedOptions = List.of(
                                EquipType.SWORD,
                                EquipType.AXE
                        );
                        possibleWeapons.add(oneHandedOptions.get(random.nextInt(oneHandedOptions.size())));
                        useShield = true;
                    }
                } else {
                    // Determine warrior path based on job ID
                    Job warriorPath = determineWarriorPath(character);

                    switch (warriorPath) {
                        case FIGHTER: // Fighter, Crusader, Hero path
                        case PAGE: // Page, White Knight, Paladin path
                            boolean useTwoHanded = random.nextBoolean();

                            if (useTwoHanded) {
                                if (warriorPath == Job.FIGHTER) {
                                    // Fighters prefer axes
                                    possibleWeapons.add(random.nextBoolean() ?
                                            EquipType.AXE_2H : EquipType.SWORD_2H);
                                } else {
                                    // Pages prefer swords
                                    possibleWeapons.add(random.nextBoolean() ?
                                            EquipType.SWORD_2H : EquipType.AXE_2H);
                                }
                            } else {
                                if (warriorPath == Job.FIGHTER) {
                                    // Fighters prefer axes
                                    possibleWeapons.add(random.nextBoolean() ?
                                            EquipType.AXE : EquipType.SWORD);
                                } else {
                                    // Pages prefer swords
                                    possibleWeapons.add(random.nextBoolean() ?
                                            EquipType.SWORD : EquipType.AXE);
                                }
                                useShield = true;
                            }
                            break;

                        case SPEARMAN: // Spearman, Dragon Knight, Dark Knight path
                            if (random.nextBoolean()) {
                                possibleWeapons.add(EquipType.SPEAR);
                            } else {
                                possibleWeapons.add(EquipType.POLEARM);
                            }
                            break;
                    }
                }
                break;

            case MAGICIAN:
                boolean useWand = random.nextBoolean();
                if (useWand) {
                    possibleWeapons.add(EquipType.WAND);
                } else {
                    possibleWeapons.add(EquipType.STAFF);
                }

                // Mages can use shields with both wands and staves
                useShield = random.nextBoolean();
                break;

            case BOWMAN:
            case CROSSBOWMAN:
                // getJobStyle() resolves the crossbow line (320/321/322) to
                // CROSSBOWMAN rather than the broad BOWMAN, so it must share this
                // case or it falls through to default and gets a 1H sword.
                // determineBowmanPath below picks BOW vs CROSSBOW from the job id.
                if (jobLevel == 1) {
                    // Beginner bowman can use either bow or crossbow
                    possibleWeapons.add(random.nextBoolean() ? EquipType.BOW : EquipType.CROSSBOW);
                } else {
                    // Determine bowman path based on job ID
                    Job bowmanPath = determineBowmanPath(character);

                    switch (bowmanPath) {
                        case HUNTER: // Hunter, Ranger, Bowmaster path
                            possibleWeapons.add(EquipType.BOW);
                            break;
                        case CROSSBOWMAN: // Crossbowman, Sniper, Marksman path
                            possibleWeapons.add(EquipType.CROSSBOW);
                            break;
                    }
                }
                break;

            case THIEF:
                if (jobLevel == 1) {
                    // Beginner thief can use either claw or dagger
                    if (random.nextBoolean()) {
                        possibleWeapons.add(EquipType.CLAW);
                    } else {
                        possibleWeapons.add(EquipType.DAGGER);
                        useShield = true;
                    }
                } else {
                    // Determine thief path based on job ID
                    Job thiefPath = determineThiefPath(character);

                    switch (thiefPath) {
                        case ASSASSIN: // Assassin, Hermit, Night Lord path
                            possibleWeapons.add(EquipType.CLAW);
                            break;
                        case BANDIT: // Bandit, Chief Bandit, Shadower path
                            possibleWeapons.add(EquipType.DAGGER);
                            useShield = true;
                            break;
                    }
                }
                break;

            // Add other job types as needed
            default:
                // Default beginner weapon
                possibleWeapons.add(EquipType.SWORD);
                break;
        }

        // Equip the chosen weapon(s)
        for (EquipType weaponType : possibleWeapons) {
            equipIfPresent(character, getRandomEquipForWearing(weaponType, character));
        }

        // Equip shield if needed
        if (useShield) {
            equipIfPresent(character, getRandomEquipForWearing(EquipType.SHIELD, character));
        }
    }
}
