package soloMapling.ArtificialPlayer.BotTypes;

import org.gms.client.Character;
import org.gms.client.inventory.Item;
import soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands;
import soloMapling.ArtificialPlayer.BotMessagingSystem.ChatMessage;
import soloMapling.ArtificialPlayer.BotMessagingSystem.MessageQueue;
import soloMapling.ArtificialPlayer.BotSM;
import soloMapling.ArtificialPlayer.BotTradeSystem.BotTradeSM;
import soloMapling.FreeMarket.FMItem;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static soloMapling.ArtificialPlayer.BotTypeManager.BotType.NX_MERCHANT_BOT;
import static soloMapling.ArtificialPlayer.BotTypeManager.convertBotType;
import static soloMapling.BotLogger.log;
import static soloMapling.Environment.PlatformPlacement.botMoveToPlatformAnyUnoccupiedSpotDynamic;
import static soloMapling.Environment.PlatformPlacement.getCurrentPlatform;
import static soloMapling.Environment.PlatformPlacement.getMainPlatformIds;
import static soloMapling.FreeMarket.ArtificialShopGenerator.generateDarkScrollsList;
import static soloMapling.FreeMarket.ArtificialShopGenerator.generateItem;
import static soloMapling.FreeMarket.ArtificialShopGenerator.generatePotionsList;
import static soloMapling.FreeMarket.ArtificialShopGenerator.generateScrollsList;
import static soloMapling.FreeMarket.ArtificialShopGenerator.generateThiefStarsList;
import static soloMapling.FreeMarket.FMEconomyManager.priceAdjustmentRules;
import static soloMapling.itemPool.ItemInformationProviderUtilities.getItemName;
import static soloMapling.itemPool.ItemUtilities.getItemMarketValue;
import static soloMapling.server.SoloMaplingUtilities.getRandomElement;
import static soloMapling.server.SoloMaplingUtilities.random;
import static soloMapling.server.SoloMaplingUtilities.rollChanceInverse;

public class SellingMerchantBot extends BotSM {
    private SellingState sellingState = SellingState.RESET;
    private List<String> hint = Collections.singletonList(getChr().getName());
    private List<FMItem> itemsToSell;
    private int itemIndex = 0;
    private boolean movedDuringAdvertise = false;

    private enum SellingState {
        RESET,
        SELECT_ITEM,
        ADVERTISE,
        CHECK_TRADES,
        IDLE_ACTIONS
    }

    private static final List<String> FLAVOR_NODES = List.of("ScamMessages", "BeggingMessages", "RWTMessages", "FunnyMessages");

    public SellingMerchantBot(Character character) {
        super(character);
        dialoguePath = "MerchantBotDialogue.yaml";
        botType = "MerchantBot";
    }

    private void resetState() {
        itemIndex = 0;
        loadItemList();
        sellingState = SellingState.RESET;
    }

    private void loadItemList() {
        Supplier<List<FMItem>>[] generators = new Supplier[]{
                () -> generateScrollsList("A"),
                () -> generateDarkScrollsList("A"),
                () -> generateThiefStarsList("A"),
                () -> generatePotionsList("S")
        };
        itemsToSell = generators[random.nextInt(generators.length)].get();
    }

    private FMItem getCurrentItem() {
        if (itemsToSell == null || itemIndex >= itemsToSell.size()) {
            return null;
        }
        return itemsToSell.get(itemIndex);
    }

    private void selectNextItem() {
        if (itemsToSell == null || itemsToSell.isEmpty()) {
            loadItemList();
        }

        itemIndex++;
        if (itemIndex >= itemsToSell.size()) {
            itemIndex = 0;
            loadItemList();
        }

        FMItem currItem = getCurrentItem();
        if (currItem == null) {
            return;
        }

        Item item = generateItem(currItem.getItemId(), 1, 1);
        getTradeInventory().setItemForSaleMain(item);
        getTradeWants().resetTradeWants();
        int rawValue = getItemMarketValue(item);
        int adjValue = priceAdjustmentRules((int) (rawValue * 0.9));
        getTradeWants().setMesoWanted(adjValue);
        setTradeMode(BotTradeSM.TradeMode.SELLING);

        resetLastTradeResult();
        resetLastTradedCharacter();
    }

    private void advertise() {
        FMItem itm = getCurrentItem();
        if (itm == null) {
            return;
        }
        String itemName = getItemName(itm.getItemId());
        if (itemName != null) {
            String msg = buildSellingMessage(itemName);
            SocialCommands.BotSpeak(getChr(), msg);
        }
    }

    static String buildSellingMessage(String itemName) {
        List<String> prefixes = List.of("Selling", "S>", "S>>", "SELL>", "Selling>");
        List<String> suffixes = List.of("You Offer", "Offer", "Trade Me", "just trade me!", "PM me",
                "no lowball", "no noobs", "no scammers", "Pros only", "hotties only", "no nx h0es",
                "baddies only", "no weebs", "English Only", "No Spanish",
                "serious offers only", "dont waste my time", "legit only", "no time wasters");

        String msg = getRandomElement(prefixes) + " " + itemName + " " + getRandomElement(suffixes);

        int fillerCount = random.nextInt(4);
        for (int i = 0; i < fillerCount; i++) {
            msg += " @@@@@@@@";
        }

        msg = msg.replace("[", "").replace("]", "");

        if (random.nextDouble() < 0.15) {
            msg = msg.toUpperCase();
        }
        return msg;
    }

    // Dynamic movement lands on the exact picked pixel, so the old nudgeAwayFromOverlap
    // band-aid (recorded paths piling bots onto fixed endpoints) is no longer needed here.
    private boolean tryPlatformShuffleWhileAdvertising() {
        if (rollChanceInverse(10)) {
            botMoveToPlatformAnyUnoccupiedSpotDynamic(getChr(), getCurrentPlatform(getChr()));
            return true;
        } else if (rollChanceInverse(20)) {
            botMoveToPlatformAnyUnoccupiedSpotDynamic(getChr(), getRandomElement(List.of("m1", "m5")));
            return true;
        } else if (rollChanceInverse(30)) {
            botMoveToPlatformAnyUnoccupiedSpotDynamic(getChr(), getRandomElement(List.of("m1", "m2")));
            return true;
        } else if (rollChanceInverse(70)) {
            int currentMap = getChr().getMapId();
            botMoveToPlatformAnyUnoccupiedSpotDynamic(getChr(), getRandomElement(getMainPlatformIds(currentMap)));
            return true;
        }
        return false;
    }

    private void handleIdleActions() {
        if (movedDuringAdvertise) {
            movedDuringAdvertise = false;
            return;
        }
        if (rollChanceInverse(10)) {
            botMoveToPlatformAnyUnoccupiedSpotDynamic(getChr(), getCurrentPlatform(getChr()));
        } else if (rollChanceInverse(20)) {
            botMoveToPlatformAnyUnoccupiedSpotDynamic(getChr(), getRandomElement(List.of("m1", "m5")));
        } else if (rollChanceInverse(30)) {
            botMoveToPlatformAnyUnoccupiedSpotDynamic(getChr(), getRandomElement(List.of("m1", "m2")));
        } else if (rollChanceInverse(70)) {
            int currentMap = getChr().getMapId();
            botMoveToPlatformAnyUnoccupiedSpotDynamic(getChr(), getRandomElement(getMainPlatformIds(currentMap)));
        }
    }

    private boolean tryConvertToNXMerchant() {
        if (rollChanceInverse(100)) {
            convertBotType(getChr(), NX_MERCHANT_BOT);
            return true;
        }
        return false;
    }

    @Override
    public void updateState() {
        super.updateState();
        if (checkIfNotRunningOrPaused()) {
            return;
        }
        if (getState() == BotState.TRADING) {
            return;
        }
        getDebugger().debugLoggingFull(
                String.format("%s SellingMerchantBot: %s", getChr().getName(), sellingState),
                String.format("%s", sellingState));

        switch (sellingState) {
            case RESET:
                resetState();
                sellingState = SellingState.SELECT_ITEM;
                break;
            case SELECT_ITEM:
                selectNextItem();
                sellingState = SellingState.ADVERTISE;
                break;
            case ADVERTISE:
                if (rollChanceInverse(25)) {
                    getDialogueHandler().executeBotFlavorDialogue(getRandomElement(FLAVOR_NODES), this);
                } else {
                    advertise();
                }
                movedDuringAdvertise = tryPlatformShuffleWhileAdvertising();
                sellingState = SellingState.CHECK_TRADES;
                break;
            case CHECK_TRADES:
                checkForTrades();
                sellingState = SellingState.IDLE_ACTIONS;
                break;
            case IDLE_ACTIONS:
                handleIdleActions();
                if (tryConvertToNXMerchant()) {
                    return;
                }
                sellingState = SellingState.SELECT_ITEM;
                break;
            default:
                log("Unexpected state: " + sellingState);
                state = BotState.FINISHED;
                throw new IllegalStateException("Unexpected state: " + sellingState);
        }
    }

    @Override
    public void displayCommands(Character chr) {
        SocialCommands.displayPlayerChatCommands(chr, hint);
    }

    @Override
    public void processMessages() {
        try {
            ChatMessage message = MessageQueue.getInstance().getMessageWithTimeout("secondary", 1, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
