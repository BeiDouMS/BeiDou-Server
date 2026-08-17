package soloMapling.ArtificialPlayer.BotTypes;

import org.gms.client.Character;
import org.gms.client.inventory.Equip;
import soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands;
import soloMapling.ArtificialPlayer.BotMessagingSystem.ChatMessage;
import soloMapling.ArtificialPlayer.BotMessagingSystem.MessageQueue;
import soloMapling.ArtificialPlayer.BotSM;
import soloMapling.ArtificialPlayer.BotTradeSystem.BotTradeSM;
import soloMapling.FreeMarket.FMEquip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static soloMapling.ArtificialPlayer.BotCommandsPack.MegaphoneCommands.BotItemMegaphone;
import static soloMapling.ArtificialPlayer.BotDialogueHandler.getRandomResolvedLine;
import static soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands.BotEmote;
import static soloMapling.ArtificialPlayer.BotCommandsPack.VFXCommands.botScrollFail;
import static soloMapling.ArtificialPlayer.BotCommandsPack.VFXCommands.botScrollSuccess;
import static soloMapling.ArtificialPlayer.BotLogic.generateCleanItemEquip;
import static soloMapling.BotLogger.log;
import static soloMapling.DebugUtilities.debugprint;
import static soloMapling.FreeMarket.ArtificialShopGenerator.generateCommonEquipList;
import static soloMapling.FreeMarket.FMEconomyManager.formatPriceToShorthand;
import static soloMapling.FreeMarket.FMEconomyManager.priceAdjustmentRules;
import static soloMapling.itemPool.UpgradeSimulator.getEquipMarketValue;

public class ScrollingBot extends BotSM {
    private ScrollingBotState scrollingBotState = ScrollingBotState.RESET;
    private List<String> hint = Collections.singletonList(getChr().getName());
    private List<FMEquip> itemsToScroll = new ArrayList<>();
    private int scrollToUse;
    private int listIndex = 0;

    private int successfulScrolls;
    private int upgradeSlots;
    private boolean[] scrollResults;
    private int currentPosition = 0;

    private long startTime;
    private long endTime;

    public ScrollingBot(Character character) {
        super(character);
        dialoguePath = "ScrollingBotDialogue.yaml";
        botType = "ScrollingBot";
    }

    private void setScrollingBotState(ScrollingBotState state) {
        this.scrollingBotState = state;
    }

    private static final Random random = new Random();

    private long waitUntilTime;

    private enum ScrollingBotState {
        RESET,
        SET_SCROLL_VARS,
        SCROLLING_ITEMS,
        ADVERTISE_SALES,
        WAITING_AFTER_AD
    }

    /*
    Start in place

    determine what items to scroll
        - weapon, armor, common item

    only do S> on specific outcomes
        - 85%+ 60%'ing
        - 40%+ on 10%'ing
        -> item smega

    based on item, scroll 5-10x of them. if its good outcome, add to S> list

    check trade queue. if traded, open it. list out options of S> list. if nothing to sell, say nothing for trade

    Keep scrolling, then wait 3 mins after last item to continue w/ other task
        -> bot state transfer to FM bot, merchant bot, travel bot
     */

    private void setScrollingItems() {
        itemsToScroll.clear();
        while (itemsToScroll.isEmpty()) {
            itemsToScroll = generateCommonEquipList("A");
        }
        FMEquip eq = itemsToScroll.get(listIndex);
        successfulScrolls = eq.getEquip().getLevel();
        Equip cleanItem = (Equip) generateCleanItemEquip(eq.getEquip().getItemId());
        upgradeSlots = cleanItem.getUpgradeSlots();
        scrollResults = generateExactScrollList(successfulScrolls, upgradeSlots);
        currentPosition = 0;
    }

    public boolean[] generateExactScrollList(int succScroll, int upgradeSlots) {
        // Creates a list of boolean that has a randomized success/fail order based on item's scrolled value

        boolean[] resultList = new boolean[upgradeSlots];
        for (int i = 0; i < succScroll; i++) {
            resultList[i] = true;
        }
        for (int i = succScroll; i < upgradeSlots; i++) {
            resultList[i] = false;
        }

        // Shuffle the array using Fisher-Yates algorithm
        for (int i = upgradeSlots - 1; i > 0; i--) {
            int randomIndex = (int) (Math.random() * (i + 1));
            boolean temp = resultList[i];
            resultList[i] = resultList[randomIndex];
            resultList[randomIndex] = temp;
        }
        return resultList;
    }

    private boolean scrollItem() {
        // Scrolls item 1 slot at a time

        // Get result from the current position
        if (currentPosition >= scrollResults.length) {
            return false; // Return false to indicate no more scrolls to process
        }

        boolean isSuccess = scrollResults[currentPosition];

        if (isSuccess) {
            botScrollSuccess(getChr());
            BotEmote(getChr(), 2);
        } else {
            botScrollFail(getChr());
            BotEmote(getChr(), 4);
        }
        currentPosition++;

        if (Math.random() < 0.20) {
            getDialogueHandler().executeBotFlavorDialogue(isSuccess ? "ScrollSuccess" : "ScrollFail", this);
        }

        waitForRandom(10000, 15000); // pace the next scroll 10-15s out (gated wait, no sleep)
        return true;
    }

    private void advertiseItemForSale() {
        setSellingItems();
    }

    private void setSellingItems() {
        Equip eqToSell = itemsToScroll.get(listIndex).getEquip();
        getTradeInventory().setItemForSaleMain(eqToSell);
        getTradeWants().resetTradeWants();
        int rawMesoValue = getEquipMarketValue(eqToSell);
        int adj = priceAdjustmentRules(rawMesoValue);
        getTradeWants().setMesoWanted(adj);
//        getTradeWants().setMesoWanted(0);
//        getTradeWants().addItemWanted(2022179, 2);
        String adBase = getRandomResolvedLine(this, "AdvertiseSale");
        String adLine = (adBase != null ? adBase : "").replace("%PRICE%", formatPriceToShorthand(adj));
        BotItemMegaphone(getChr(), adLine, eqToSell);
        setTradeMode(BotTradeSM.TradeMode.SELLING);
    }

    private void resetScrollingBotState() {
        setScrollingBotState(ScrollingBotState.RESET);
    }

    @Override
    public void updateState() {
        super.updateState();
        if (checkIfNotRunningOrPaused()) {
            return;
        }
        if (getState() == BotState.TRADING) { // getTradeHandler().verifyTradePartner()
            return;
        }
        getDebugger().debugLoggingFull(String.format("%s ScrollingBotState: %s", this.getChr().getName(), scrollingBotState), String.format("%s", scrollingBotState));

        switch (scrollingBotState) {
            case RESET:
                resetScrollingBotState();
                setScrollingBotState(ScrollingBotState.SET_SCROLL_VARS);
                break;
            case SET_SCROLL_VARS:
                setScrollingItems();
                setScrollingBotState(ScrollingBotState.SCROLLING_ITEMS);
                break;
            case SCROLLING_ITEMS:
                boolean continueScrolling = scrollItem();
                checkForTrades();
                if (!continueScrolling) {
                    setScrollingBotState(ScrollingBotState.ADVERTISE_SALES);
                    break;
                }
                break;
            case ADVERTISE_SALES:
                if (Math.random() < 0.10) {
                    advertiseItemForSale();
                }
                waitUntilTime = System.currentTimeMillis() + (5 * 60 * 1000) + random.nextInt(3 * 60 * 1000 + 1); // 5-8 min
                setScrollingBotState(ScrollingBotState.WAITING_AFTER_AD);
                break;
            case WAITING_AFTER_AD:
                checkForTrades();
                if (System.currentTimeMillis() >= waitUntilTime) {
                    setScrollingBotState(ScrollingBotState.SET_SCROLL_VARS);
                }
                break;
            default:
                log("Unexpected state: " + scrollingBotState);
                state = BotState.FINISHED;
                resetScrollingBotState();
                throw new IllegalStateException("Unexpected state: " + state);
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
            if (message == null) {
                return;
            }
//            handleBetCommand(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
