package soloMapling.FreeMarket;

import org.gms.client.inventory.Item;

import java.util.Random;
import java.util.*;

import static soloMapling.DebugUtilities.debugprint;


public class FMShopDescriptionManager {

    private final Random random = new Random();

    public static class DescriptionEntry {

        public enum DescriptionType {
            FMCLAN, // Top Only
            TOP_ITEM, TOP_EQUIPMENT, RANDOM_WORD_PHRASE, // Top/Bot
            OFFERABLE, RWT_CURRENCY, EMOJI, SHOP_TYPES,// Bot only
            WELCOME // overwrite whole
        }

        private DescriptionType type;
        private Set<String> allowedTiers = new HashSet<>();
        private Set<Integer> allowedRooms = new HashSet<>();
        private Generator generator;

        public interface Generator {
            String generate();
        }

        private Map<DescriptionType, Double> compatibility = new HashMap<>();

        public DescriptionEntry(
                DescriptionType type,
                List<String> allowedTiers,
                List<Integer> specificRooms,
                Generator generator
        ) {
            this.type = type;
            this.allowedTiers.addAll(allowedTiers);
            this.allowedRooms.addAll(specificRooms);
            this.generator = generator;
        }

        public void addRoomRange(int start, int end) {
            for (int i = start; i <= end; i++) {
                allowedRooms.add(i);
            }
        }

        public boolean isValidFor(String tier, int roomNumber) {
            return allowedTiers.contains(tier) && allowedRooms.contains(roomNumber);
        }

        public DescriptionEntry setCompatibility(DescriptionType type, double chance) {
            compatibility.put(type, chance);
            return this;
        }

        public double getCompatibility(DescriptionType nextType) {
            return compatibility.getOrDefault(nextType, 0.0);
        }

        public DescriptionType getType() {
            return type;
        }

        public String generate() {
            return generator.generate();
        }
    }

    public class ComboDescriptionStrategy {
        private List<DescriptionEntry> topDescriptions = new ArrayList<>();
        private List<DescriptionEntry> bottomDescriptions = new ArrayList<>();

        public void addTopDescriptionEntry(DescriptionEntry entry) {
            topDescriptions.add(entry);
        }

        public void addBotDescriptionEntry(DescriptionEntry entry) {
            bottomDescriptions.add(entry);
        }

        public String generateDescription(String tier, int roomNumber, HiredMerchantArtificial merchant) {
            shuffleEntries();

            DescriptionEntry top = null;
            DescriptionEntry bot = null;
            String topGenerated = null;

            // Hot Room Description
            boolean isHotRoom = (roomNumber == 1 || roomNumber == 2 || roomNumber == 7);
            if (isHotRoom && random.nextDouble() < 0.90) {
                for (DescriptionEntry entry : topDescriptions) {
                    if (entry.getType() == DescriptionEntry.DescriptionType.FMCLAN) {
                        String result = entry.generate();
                        if (result != null) {
                            top = entry;
                            topGenerated = result;
                            debugprint("Hot room FMCLAN: " + top);
                            break;
                        }
                    }
                }
            }

            // Other Room Descriptions
            if (top == null) {
                for (DescriptionEntry first : topDescriptions) {
                    if (first.isValidFor(tier, roomNumber)) {
                        String result = first.generate();
                        if (result != null) {
                            top = first;
                            topGenerated = result;
                            debugprint("First: " + top);
                            break;
                        }
                    }
                }
            }


            if (random.nextDouble() < 0.40) {
                for (DescriptionEntry second : bottomDescriptions) {
                    if (top == second) continue;
                    if (top == null) {
                        top = second;
                    }
                    if (isValidCombo(top, second)) {
                        bot = second;
                        debugprint("Bot: " + second);
                        break;
                    }
                }
            }

            if (top != null && bot != null) {
                debugprint("top bot gen");
                return topGenerated + " " + bot.generate();
            } else if (top != null) {
                debugprint("Top only gen");
                return topGenerated;
            } else {
                return "NOTHING GENERATED";
            }
        }

        private boolean isValidCombo(DescriptionEntry first, DescriptionEntry second) {
            if (first.getType() == null) {
                return false;
            }
            if (checkTypeCompatibility(first.getType(), second.getType())) {
                double chance = first.getCompatibility(second.getType());
                return random.nextDouble() < chance;
            } else {
                return false;
            }
        }


        protected static final Map<DescriptionEntry.DescriptionType, List<DescriptionEntry.DescriptionType>> COMPATIBILITY_RULES = Map.ofEntries(
                Map.entry(DescriptionEntry.DescriptionType.FMCLAN,
                        List.of(DescriptionEntry.DescriptionType.OFFERABLE,
                                DescriptionEntry.DescriptionType.RWT_CURRENCY,
                                DescriptionEntry.DescriptionType.EMOJI)),

                Map.entry(DescriptionEntry.DescriptionType.TOP_ITEM,
                        List.of(DescriptionEntry.DescriptionType.OFFERABLE,
                                DescriptionEntry.DescriptionType.RWT_CURRENCY,
                                DescriptionEntry.DescriptionType.EMOJI)),

                Map.entry(DescriptionEntry.DescriptionType.TOP_EQUIPMENT,
                        List.of(DescriptionEntry.DescriptionType.OFFERABLE,
                                DescriptionEntry.DescriptionType.RWT_CURRENCY,
                                DescriptionEntry.DescriptionType.EMOJI)),

                Map.entry(DescriptionEntry.DescriptionType.SHOP_TYPES,
                        List.of(DescriptionEntry.DescriptionType.OFFERABLE,
                                DescriptionEntry.DescriptionType.RWT_CURRENCY,
                                DescriptionEntry.DescriptionType.EMOJI)),

                Map.entry(DescriptionEntry.DescriptionType.RANDOM_WORD_PHRASE,
                        List.of(DescriptionEntry.DescriptionType.OFFERABLE,
                                DescriptionEntry.DescriptionType.EMOJI))
        );

        public static boolean checkTypeCompatibility(DescriptionEntry.DescriptionType first, DescriptionEntry.DescriptionType second) {
//            debugprint("Checking compatability first second: " + first + ", " + second);
            List<DescriptionEntry.DescriptionType> compatibleWithFirst = COMPATIBILITY_RULES.getOrDefault(first, List.of());
//            debugprint("compatWithfirst: " + compatibleWithFirst);
            return compatibleWithFirst.contains(second);
        }

        private void shuffleEntries() {
            Collections.shuffle(topDescriptions, random);
            Collections.shuffle(bottomDescriptions, random);
        }
    }

    private DescriptionEntry getFMClan() {
        return new DescriptionEntry(
                DescriptionEntry.DescriptionType.FMCLAN,
                List.of("S", "A"),
                List.of(1, 2, 7),
                FMShopDescGen::FMClanAdvertisement)
                .setCompatibility(DescriptionEntry.DescriptionType.OFFERABLE, 0.7)
                .setCompatibility(DescriptionEntry.DescriptionType.RWT_CURRENCY, 0.5)
                .setCompatibility(DescriptionEntry.DescriptionType.EMOJI, 0.5);
    }

    private DescriptionEntry getTopItem(HiredMerchantArtificial merchant) {
        DescriptionEntry topItem = new DescriptionEntry(
                DescriptionEntry.DescriptionType.TOP_ITEM,
                List.of("S", "A"),
                List.of(1),
                () -> {
                    String mostExpensiveItem = FMShopDescGen.getMostExpensiveItemName(merchant);
                    if (mostExpensiveItem != null) {
                        return FMShopDescGen.itemNameAcronymConverter(mostExpensiveItem);
                    }
                    return null;
                }
        );
        topItem.addRoomRange(1, 22);
        topItem.setCompatibility(DescriptionEntry.DescriptionType.OFFERABLE, 0.8);
        topItem.setCompatibility(DescriptionEntry.DescriptionType.EMOJI, 0.4);
        topItem.setCompatibility(DescriptionEntry.DescriptionType.RWT_CURRENCY, 0.3);

        return topItem;
    }

    private DescriptionEntry getTopEquip(HiredMerchantArtificial merchant) {
        DescriptionEntry topEquip = new DescriptionEntry(
                DescriptionEntry.DescriptionType.TOP_EQUIPMENT,
                List.of("S", "A"),
                List.of(13),
                () -> {
                    Item bestEquip = FMShopDescGen.getMostExpensiveEquipFromShop(merchant);
                    if (bestEquip != null) {
                        return FMShopDescGen.advertiseBestEquip(bestEquip);
                    }
                    String mostExpensiveItem = FMShopDescGen.getMostExpensiveItemName(merchant);
                    if (mostExpensiveItem != null) {
                        return FMShopDescGen.itemNameAcronymConverter(mostExpensiveItem);
                    }
                    return null;
                }
        );
        topEquip.addRoomRange(1, 22);
        topEquip.setCompatibility(DescriptionEntry.DescriptionType.OFFERABLE, 0.8);
        topEquip.setCompatibility(DescriptionEntry.DescriptionType.EMOJI, 0.4);
        topEquip.setCompatibility(DescriptionEntry.DescriptionType.RWT_CURRENCY, 0.3);

        return topEquip;
    }

    private DescriptionEntry getOfferable() {
        DescriptionEntry offerableDesc = new DescriptionEntry(
                DescriptionEntry.DescriptionType.OFFERABLE,
                List.of("S", "A"),
                List.of(1),
                FMShopDescGen::getOfferableDescription);
        offerableDesc.addRoomRange(1, 22);

        return offerableDesc;
    }

    private DescriptionEntry getRWTCurrency() {
        DescriptionEntry rwtCurrencyDesc = new DescriptionEntry(
                DescriptionEntry.DescriptionType.RWT_CURRENCY,
                List.of("S", "A"),
                List.of(1),
                FMShopDescGen::advertiseRWTCurrencies);
        rwtCurrencyDesc.addRoomRange(1, 9);

        return rwtCurrencyDesc;
    }

    private DescriptionEntry getEmoji() {
        DescriptionEntry emojiDesc = new DescriptionEntry(
                DescriptionEntry.DescriptionType.EMOJI,
                List.of("B"),
                List.of(9),
                FMShopDescGen::emojiFaces);
        emojiDesc.addRoomRange(9, 22);

        return emojiDesc;
    }

    private DescriptionEntry getRandomWordPhrase() {
        DescriptionEntry randWordPhraseDesc = new DescriptionEntry(
                DescriptionEntry.DescriptionType.RANDOM_WORD_PHRASE,
                List.of("S", "A", "B"),
                List.of(3),
                FMShopDescGen::randomShortWordsPhrases);
        randWordPhraseDesc.addRoomRange(3, 22);
        randWordPhraseDesc.setCompatibility(DescriptionEntry.DescriptionType.OFFERABLE, 0.5);
        randWordPhraseDesc.setCompatibility(DescriptionEntry.DescriptionType.EMOJI, 0.5);

        return randWordPhraseDesc;
    }

    private DescriptionEntry getWelcome(String owner) {
        DescriptionEntry welcomeDesc = new DescriptionEntry(
                DescriptionEntry.DescriptionType.WELCOME,
                List.of("B"),
                List.of(13),
                () -> {
                    return ("Welcome to " + owner + "'s Shop!");
                });
        welcomeDesc.addRoomRange(13, 22);

        return welcomeDesc;
    }

    private DescriptionEntry getShopTypeDesc(HiredMerchantArtificial merchant) {
        DescriptionEntry shopTypeDesc = new DescriptionEntry(
                DescriptionEntry.DescriptionType.SHOP_TYPES,
                List.of("S", "A", "B"),
                List.of(1),
                () -> {
                    String fileKey = mapShopTypeToFileKey(merchant.getPrimary());
                    return FMShopDescGen.getRandomStoreDescription(fileKey);
                }
        );
        shopTypeDesc.addRoomRange(1, 22);
        shopTypeDesc.setCompatibility(DescriptionEntry.DescriptionType.OFFERABLE, 0.6);
        shopTypeDesc.setCompatibility(DescriptionEntry.DescriptionType.RWT_CURRENCY, 0.3);
        shopTypeDesc.setCompatibility(DescriptionEntry.DescriptionType.EMOJI, 0.5);

        return shopTypeDesc;
    }

    private static String mapShopTypeToFileKey(HiredMerchantArtificial.shopTypes type) {
        if (type == null) return "common";
        return switch (type) {
            case Thief, Stars -> "thief";
            case Warrior -> "warrior";
            case Mage -> "mage";
            case Bowman -> "bowman";
            case Chair -> "chair";
            case Scroll, DarkScroll, Mastery -> "scrolls";
            case Potion -> "useable";
            case ETC -> "etc";
            default -> "common";
        };
    }

    public void generateDescription(HiredMerchantArtificial merchant) {
        ComboDescriptionStrategy strategy = new ComboDescriptionStrategy();

        // TOP pool: main description generators
        strategy.addTopDescriptionEntry(getFMClan());
        strategy.addTopDescriptionEntry(getTopItem(merchant));
        strategy.addTopDescriptionEntry(getTopEquip(merchant));
        strategy.addTopDescriptionEntry(getShopTypeDesc(merchant));
        strategy.addTopDescriptionEntry(getRandomWordPhrase());
        strategy.addTopDescriptionEntry(getRandomWordPhrase());
        strategy.addTopDescriptionEntry(getWelcome(merchant.getOwner()));

        // BOT pool: short add-on suffixes only
        strategy.addBotDescriptionEntry(getOfferable());
        strategy.addBotDescriptionEntry(getRWTCurrency());
        strategy.addBotDescriptionEntry(getEmoji());


        ////////////////////////////////////////////////////////////

        String tier = merchant.getTier();
        int room = merchant.getRoomNumber();
        String description = strategy.generateDescription(tier, room, merchant);
        debugprint("Tier " + tier + ", Room " + room + ":");
        debugprint(description);
        debugprint();
        merchant.setDescription(description);

        /////////////////////////////////////////////////////////////
//        String[] tiers = {"S", "S", "A", "B"};
//        int[] rooms = {1, 2, 3, 7};
//
//        debugprint("Combo Description Generation Demo:");
//        for (String tier : tiers) {
//            for (int room : rooms) {
//                String description = strategy.generateDescription(tier, room);
//                debugprint("Tier " + tier + ", Room " + room + ":");
//                debugprint(description);
//                debugprint();
//            }
//        }
    }


    /**
     * Debug toggle: when true, FM shop titles get "<itemCount> <tier>" appended
     * (e.g. "... 12 S") so you can eyeball tier distribution and stock at a glance
     * while standing in the FM. Leave false for normal play - real players never
     * need to see this. Flip to true when debugging shop generation.
     */
    private static final boolean DEBUG_APPEND_TIER_AND_SIZE = false;

    public static void appendRoomNumber(HiredMerchantArtificial merchant) {
        String airPad = "          ";
        String roomNumberFormat = "<" + (merchant.getMapId() % 100) + ">";
        appendMerchantDescription(merchant, (airPad + roomNumberFormat));

        int storeSize = merchant.getItems().size();
        if (DEBUG_APPEND_TIER_AND_SIZE) {
            appendMerchantDescription(merchant, " " + storeSize + " " + merchant.getTier());
        }
        if (storeSize == 0) {
            debugprint("Zero Shop " + merchant.getRoomNumber() + ", " + merchant.getPos());
        }
    }

    public static void prependMerchantDescription(HiredMerchantArtificial merchant, String description) {
        setMerchantDescription(merchant, (description) + merchant.getDescription());
    }

    public static void appendMerchantDescription(HiredMerchantArtificial merchant, String description) {
        setMerchantDescription(merchant, merchant.getDescription() + (description));
    }

    public static void setMerchantDescription(HiredMerchantArtificial merchant, String description) {
        merchant.setDescription(description);
    }

    /*
    todo
    Create TopDescriptions & BotDescriptions
    Create COMPATIBILITY_RULES map, links top to bot
    Have defaults, fine tune other stuff
    Test why some don't show up.
     */

    public static void main(String[] args) {
//        new FMShopDescriptionManager().demonstrateComboDescriptionGeneration();
    }

    /*
    Design Notes
    2. Create Shop Title Description
    [x] descriptive title (Zak helm, +10 Sauna Robe, Craven, 60% GFA)
    - advertisement (www.G4meK00.c0m - 1b=$5)
    [x] guild advertisement ( [S]LAYERS )
    [x] ascii art / border ('~.Rogues Den.~')
    [] randomize ign's: pro names, 1 word ign's, ly's, 'ings, pokemon i gns, anime characters, numbered igns, xXx xXx igns, classic maplestory famous ign's, other igns for fun
    [] shop description summarizing contents
    [x] funny sentences
    [x] non-ascii decorative stuff

     */

    /*
    0. Region Based Rules

    [] Ludi / Perion are casuals so its mostly random shit

    // Real life examples
    1. Guild / Clan Branding
    --[M]ushieMarket--
    Blackmarket
    FMHero
    [x] ai generated list
    [x] first letter emblem
    [x] base border chooser framework

    2. Brand embellish
        V O C A L <3
        [x] space between letters, caps'ed. only for 8 chars or less

    3. significant other shoutout

    4. general Description
        Description of Shop Archetype


        [x] Quitting Sale - Heavy Discounts %
        [2 test] Advert best items
        [2 test] Highlight best/most expensive items (10 atk wg, 43 atk Scarab, 101 att Kandine, etc etc)

        [pass] B> advert
        [x] Cheap

        [pass] single random digit, single random word
            - for us, just keep it super basic, just the first word i guess.
        [] Gacha

     5. Commonly Used Terms
        [x] Cheap
        [x]
            L/N/O
            L/O - Leave Offer
            H/O highest offer
            C/O closest offer
            Buy or Offer

        [x] Godly Items

        <Phrase>
        [Phrase]

    /*
    other non-ascii stuff borders
      6. Random shit
        - holiday celebration note
        -

     */

    // TOP ITEM NULL, NO TOP
}
