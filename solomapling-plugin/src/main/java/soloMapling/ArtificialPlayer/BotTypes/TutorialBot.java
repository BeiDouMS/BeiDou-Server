package soloMapling.ArtificialPlayer.BotTypes;

import org.gms.client.Character;
import org.gms.client.Job;
import org.gms.client.inventory.BodyPart;
import org.gms.client.inventory.Equip;
import org.gms.client.inventory.Item;
import org.gms.server.ItemInformationProvider;
import org.gms.server.maps.MapItem;
import org.gms.server.maps.MapObject;
import soloMapling.ArtificialPlayer.BotCommandsPack.DropCommands;
import soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands;
import soloMapling.ArtificialPlayer.BotHelpers;
import soloMapling.ArtificialPlayer.BotLogic;
import soloMapling.ArtificialPlayer.BotMessagingSystem.ChatMessage;
import soloMapling.ArtificialPlayer.BotMessagingSystem.MessageQueue;
import soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.MovementRecording;
import soloMapling.ArtificialPlayer.BotSM;
import soloMapling.ArtificialPlayer.BotTradeSystem.BotTradeCommands;
import soloMapling.server.BotTiming;
import soloMapling.server.NXCodeManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static soloMapling.ArtificialPlayer.BotCommandsPack.DropCommands.botLootSelectedItems;
import static soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands.BotEmote;
import static soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands.BotSpeak;
import static soloMapling.ArtificialPlayer.BotCommandsPack.DropCommands.botThrowEquipsInARow;
import static soloMapling.ArtificialPlayer.BotCommandsPack.VFXCommands.botScrollSuccess;
import static soloMapling.ArtificialPlayer.BotHelpers.convertItemIdToName;
import static soloMapling.ArtificialPlayer.BotLogic.checkForItemOnFloor;
import static soloMapling.ArtificialPlayer.BotLogic.readPlayerEquipBySlotName;
import static soloMapling.ArtificialPlayer.BotLogic.waitForPlayerInRange;
import static soloMapling.ArtificialPlayer.BotMovementSystem.InPacketReader.getMovementRecording;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands.BotMoveStream;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands.botFaceTowardsPoint;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands.pathFinderBeta;
import static soloMapling.BotLogger.log;
import static soloMapling.server.SoloMaplingUtilities.random;

public class TutorialBot extends BotSM {
    private TutorialBotState tutorialBotState = TutorialBotState.RESET;
    private Boolean runTutorial;
    private Boolean tutPicked;
    private Boolean wantsAdmin;
    private String generatedNXCode;
    private String generatedNXCode2;
    private List<Integer> noobsHelped = new ArrayList<>();
    private List<String> hint = Collections.singletonList(getChr().getName());

    private int wepId;
    private Equip playersWep;

    private long startTime;
    private long endTime;

    // Mark of Beta, White Maple Dana, Newspaper Hat
    final private int[] items = {1002419, 1002515, 1002418};

    private static final int POWER_ELIXIR = 2000005;
    private static final int ONYX_APPLE = 2022179;
    private static final int TOWN_SCROLL_HENESYS = 2030000;
    private static final int ILBI_STARS = 2070006;
    private static final int TRADE_MESOS = 1_000_000_000;

    private static final int MAPLE_KANDAYO = 1472032;
    private static final int BLUE_SAUNA_ROBE = 1050018;
    private static final int RED_SAUNA_ROBE = 1051017;

    public TutorialBot(Character character) {
        super(character);
        dialoguePath = "TutorialBotDialogue.yaml";
        botType = "TutorialBot";
    }

    private void setTutorialBotState(TutorialBot.TutorialBotState state) {
        this.tutorialBotState = state;
    }

    private void resetTutorialBotState() {
        setTutorialBotState(TutorialBotState.RESET);
        runTutorial = null;
        tutPicked = null;
        wantsAdmin = null;
        generatedNXCode = null;
        generatedNXCode2 = null;
        hint = Collections.singletonList(getChr().getName());
        wepId = 0;
        playersWep = null;

        startTime = System.currentTimeMillis();
        endTime = 0;
    }

    private enum TutorialBotState {
        RESET,
        WAIT_FOR_PLAYER_IN_RANGE,
        START,
        INQUIRE,
        WAIT_FOR_INQUIRY_RESPONSE,
        ASK_ADMIN,
        WAIT_ADMIN_RESPONSE,
        GRANT_ADMIN,
        GIFT_TRADE,
        WAIT_TRADE_ACCEPT,
        TUTORIAL_1,
        TUTORIAL_2,
        TUTORIAL_2_WAIT_PICK,
        TUTORIAL_3,
        TUTORIAL_3_WAIT_DROP,
        TUTORIAL_3_UPGRADE,
        SEND_OFF,
        RETURN
    }

    @Override
    public void updateState() {
        super.updateState();
        if (checkIfNotRunningOrPaused()) {
            return;
        }
        getDebugger().debugLoggingFull(String.format("%s TutorialBotState: %s", this.getChr().getName(), tutorialBotState), String.format("%s", tutorialBotState));
        switch (tutorialBotState) {
            case RESET:
                resetTutorialBotState();
                setTutorialBotState(TutorialBotState.WAIT_FOR_PLAYER_IN_RANGE);
                break;
            case WAIT_FOR_PLAYER_IN_RANGE:
                waitForNewPlayer();
                botWaitingForPlayerFlavorText();
                if (getInteractors().getRespondant() != null) {
                    setTutorialBotState(TutorialBotState.START);
                }
                break;
            case START:
                greetPlayer();
                setTutorialBotState(TutorialBotState.INQUIRE);
                break;
            case INQUIRE:
                inquirePlayer();
                setTutorialBotState(TutorialBotState.WAIT_FOR_INQUIRY_RESPONSE);
                break;
            case WAIT_FOR_INQUIRY_RESPONSE:
                waitForResponse();
                if (runTutorial == null) {
                    return;
                }
                if (runTutorial) {
                    setTutorialBotState(TutorialBotState.ASK_ADMIN);
                } else {
                    setTutorialBotState(TutorialBotState.SEND_OFF);
                }
                break;
            case ASK_ADMIN:
                askAdmin();
                setTutorialBotState(TutorialBotState.WAIT_ADMIN_RESPONSE);
                break;
            case WAIT_ADMIN_RESPONSE:
                waitForResponse();
                if (wantsAdmin == null) {
                    return;
                }
                if (wantsAdmin) {
                    setTutorialBotState(TutorialBotState.GRANT_ADMIN);
                } else {
                    setTutorialBotState(TutorialBotState.TUTORIAL_1);
                }
                break;
            case GRANT_ADMIN:
                grantAdmin();
                setTutorialBotState(TutorialBotState.GIFT_TRADE);
                break;
            case GIFT_TRADE:
                initiateGiftTrade();
                setTutorialBotState(TutorialBotState.WAIT_TRADE_ACCEPT);
                break;
            case WAIT_TRADE_ACCEPT:
                if (waitForTradeAccept()) {
                    placeTradeItems();
                    setTutorialBotState(TutorialBotState.TUTORIAL_1);
                }
                break;
            case TUTORIAL_1:
                tutorial_1();
                setTutorialBotState(TutorialBotState.TUTORIAL_2);
                break;
            case TUTORIAL_2:
                tutorial_2();
                setTutorialBotState(TutorialBotState.TUTORIAL_2_WAIT_PICK);
                break;
            case TUTORIAL_2_WAIT_PICK:
                waitForResponse();
                if (tutPicked == null) {
                    return;
                }
                if (tutPicked) {
                    setTutorialBotState(TutorialBotState.TUTORIAL_3);
                }
                waitFor(2000); // beat before TUTORIAL_3 ticks
                break;
            case TUTORIAL_3:
                tutorial_3();
                if (this.wepId != 0) {
                    setTutorialBotState(TutorialBotState.TUTORIAL_3_WAIT_DROP);
                } else {
                    setTutorialBotState(TutorialBotState.SEND_OFF);
                }
                break;
            case TUTORIAL_3_WAIT_DROP:
                waitForPlayerToDropWeapon();
                if (this.playersWep == null) {
                    return;
                }
                setTutorialBotState(TutorialBotState.TUTORIAL_3_UPGRADE);
                break;
            case TUTORIAL_3_UPGRADE:
                upgradeAndReturnWeapon();
                setTutorialBotState(TutorialBotState.SEND_OFF);
                break;
            case SEND_OFF:
                send_off();
                setTutorialBotState(TutorialBotState.RETURN);
                break;
            case RETURN:
                returnOrigin();
                getInteractors().resetRespondant();
                setTutorialBotState(TutorialBotState.RESET);
                break;
            default:
                log("Unexpected state: " + tutorialBotState);
                state = BotState.FINISHED;
                resetTutorialBotState();
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
            handleMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void waitForNewPlayer() {
        Character noob = waitForPlayerInRange(getChr(), 500, 150);
        if (noob != null) {
            if (noobsHelped.contains(noob.getId())) {
                return;
            }
            getInteractors().setRespondant(noob);
        }
    }

    private void handleInquiryResponse(String content) {
        if (content.contains("yes")) {
            runTutorial = true;
        } else if (content.contains("no")) {
            runTutorial = false;
        }
    }

    private void handleSelectedHat(String itemName, Integer itemIdSelected) {
        BotSpeak(getChr(), String.format("You've selected %s! Enjoy it!", itemName));
        tutPicked = true;
        int[] leftoverHats = Arrays.stream(items).filter(item -> item != itemIdSelected).toArray();
        BotTiming.after(2500, () -> lootLeftoverHats(leftoverHats));
        waitFor(3000); // hold ticks until the leftover-hat loot lands
    }

    private void handleTutPickResponse(String content) {
        HashMap<String, Integer> itemNameToIdMap = BotLogic.makeItemIdFromNameMap(items);
        for (int itemId : items) {
            String itemName = BotHelpers.convertItemIdToName(itemId).toLowerCase();
            if (content.contains(itemName)) {
                Integer itemIdSelected = itemNameToIdMap.get(itemName);
                handleSelectedHat(itemName, itemIdSelected);
                return;
            }
        }
        // If no matching item is found
        BotSpeak(getChr(), "Did you type the name in correctly?");
        BotEmote(getChr(), 6);
    }

    private void lootLeftoverHats(int[] itemsToLoot) {
        botLootSelectedItems(getChr(), itemsToLoot);
    }

    private void handleMessage(ChatMessage message) {
        if (!getInteractors().isMessageFromRespondant(message)) {
            return;
        }
        String content = message.getContent().toLowerCase();
        if (tutorialBotState == TutorialBotState.WAIT_FOR_INQUIRY_RESPONSE) {
            handleInquiryResponse(content);
        } else if (tutorialBotState == TutorialBotState.WAIT_ADMIN_RESPONSE) {
            handleAdminResponse(content);
        } else if (tutorialBotState == TutorialBotState.TUTORIAL_2_WAIT_PICK) {
            handleTutPickResponse(content);
        }
    }

    private void handleAdminResponse(String content) {
        if (content.contains("yes")) {
            wantsAdmin = true;
        } else if (content.contains("no")) {
            wantsAdmin = false;
        }
    }

    private void askAdmin() {
        getDialogueHandler().executeBotDialogue("AskAdmin", TutorialBot.this);
        hint = List.of("Yes", "No");
        displayCommands(getInteractors().getRespondant());
        startTime = System.currentTimeMillis();
        endTime = startTime + (30 * 1000);
    }

    private void grantAdmin() {
        Character player = getInteractors().getRespondant();

        player.setLevel(149);
        player.levelUp(false);

        player.changeJob(Job.getById(412));
        player.equipChanged();

        player.setGMLevel(6);
        player.getClient().setGMLevel(6);

        player.gainAp(750, false);
        player.gainSp(450, 0, false);

        player.updateMaxHpMaxMp(10000, 10000);
        player.updateHpMp(10000);

        generatedNXCode = NXCodeManager.generateGiftCardCode();
        NXCodeManager.createCompleteNXCode(generatedNXCode, 25000);

        generatedNXCode2 = NXCodeManager.generateGiftCardCode();
        NXCodeManager.createCompleteNXCode(generatedNXCode2, 25000);

        Map<String, String> replacements = Map.of(
                "%NX_CODE%", generatedNXCode,
                "%NX_CODE_2%", generatedNXCode2
        );
        getDialogueHandler().executeBotDialogueWithReplacementStrings("GrantAdmin", replacements, TutorialBot.this);

        log("[TutorialBot] Granted admin to " + player.getName()
                + " | NX code 1: " + generatedNXCode
                + " | NX code 2: " + generatedNXCode2);
    }

    private void initiateGiftTrade() {
        Character player = getInteractors().getRespondant();
        getDialogueHandler().executeBotDialogue("GiftTradeIntro", TutorialBot.this);
        BotTiming.after(2000, () -> BotTradeCommands.sendTradeRequestToPlayer(getChr(), player));
        waitFor(2500); // trade request lands at +2s
        startTime = System.currentTimeMillis();
        endTime = startTime + (30 * 1000);
    }

    private boolean waitForTradeAccept() {
        if (getChr().getTrade() == null) {
            return false;
        }
        if (getChr().getTrade().isFullTrade()) {
            return true;
        }
        if (System.currentTimeMillis() > endTime) {
            BotTradeCommands.cancelTrade(getChr());
            BotSpeak(getChr(), "No worries, the gifts will be waiting for you next time.");
            setTutorialBotState(TutorialBotState.TUTORIAL_1);
            return false;
        }
        return false;
    }

    private void placeTradeItems() {
        Character player = getInteractors().getRespondant();
        int robeId = player.isMale() ? BLUE_SAUNA_ROBE : RED_SAUNA_ROBE;

        // ~6s of gift choreography on a chain; dies quietly if the player cancels
        BotTiming.chain()
                .stopUnless(() -> getChr().getTrade() != null)
                .pause(1000)
                .run(() -> BotTradeCommands.writeTradeChat(getChr(), "Here are some goodies to get you started!"))
                .pause(1500)
                .run(() -> BotTradeCommands.setMeso(getChr(), TRADE_MESOS))
                .pause(500)
                .run(() -> BotTradeCommands.addItemToTrade(getChr(), POWER_ELIXIR, 1000, 1))
                .pause(300)
                .run(() -> BotTradeCommands.addItemToTrade(getChr(), ONYX_APPLE, 100, 2))
                .pause(300)
                .run(() -> BotTradeCommands.addItemToTrade(getChr(), TOWN_SCROLL_HENESYS, 100, 3))
                .pause(300)
                .run(() -> BotTradeCommands.addItemToTrade(getChr(), ILBI_STARS, 800, 4))
                .pause(300)
                .run(() -> BotTradeCommands.addCleanEquipToTrade(getChr(), MAPLE_KANDAYO, 5))
                .pause(300)
                .run(() -> BotTradeCommands.addCleanEquipToTrade(getChr(), robeId, 6))
                .pause(500)
                .run(() -> BotTradeCommands.writeTradeChat(getChr(), "1B mesos, potions, scrolls, stars, and gear. All yours!"))
                .pause(1000)
                .run(() -> BotTradeCommands.confirmTrade(getChr()))
                .start();
        waitFor(7500); // hold TUTORIAL_1 until the gift trade confirms
    }

    private void greetPlayer() {
        botFaceTowardsPoint(getChr(), getInteractors().getRespondant().getPosition());
        Map<String, String> replacements = Map.of("%RESP_NAME%", getInteractors().getRespondant().getName());
        getDialogueHandler().executeBotDialogueWithReplacementStrings("Greeting", replacements, TutorialBot.this);
    }

    private void inquirePlayer() {
        getDialogueHandler().executeBotDialogue("Inquiry", TutorialBot.this);
        hint = List.of("Yes", "No");
        displayCommands(getInteractors().getRespondant());
        startTime = System.currentTimeMillis();
        endTime = startTime + (20 * 1000);
    }

    private void waitForResponse() {
        if (System.currentTimeMillis() < endTime) {
            processMessages();
        } else {
            SocialCommands.BotSpeak(getChr(), "Talk to me again if you're ready.");
            state = BotState.FINISHED;
            resetTutorialBotState();
        }
    }

    private void tutorial_1() {
        Map<String, String> replacements = Map.of("%CHR_NAME%", getChr().getName());
        getDialogueHandler().executeBotDialogueWithReplacementStrings("Tutorial_1", replacements, TutorialBot.this);
    }

    private void tutorial_2() {
        tutorial_2_dialog();
        tutorial_2_drops();
        startTime = System.currentTimeMillis();
        endTime = startTime + (40 * 1000);
    }

    private void tutorial_2_dialog() {
        getDialogueHandler().executeBotDialogue("Tutorial_2", TutorialBot.this);
    }

    private void tutorial_2_drops() {
        Point centerPos = getChr().getPosition();
        botThrowEquipsInARow(getChr(), items, centerPos, 75);
        displayTutorialDropOptions();
    }

    private void displayTutorialDropOptions() {
        List<String> tutDropOptions = new ArrayList<>();
        for (int itemId : items) {
            String itemName = BotHelpers.convertItemIdToName(itemId);
            tutDropOptions.add(itemName);
        }
        hint = tutDropOptions;
        displayCommands(getInteractors().getRespondant());
    }

    private void tutorial_3() {
        this.wepId = analyzePlayersWeapon();
    }

    private void waitForPlayerToDropWeapon() {
        this.playersWep = lootPlayersDroppedWeapon(this.wepId);
    }

    // Deliberate synchronous choreography (same ruling as BlackjackDealerBot):
    // the dialogue steps block for YAML-configured durations, so this strictly
    // sequential script must run on its own tick — a chain + guessed waitFor
    // releases the FSM mid-script (bot was seen doing two states at once).
    private void upgradeAndReturnWeapon() {
        BotHelpers.blockingSleep(2000);
        lootDialog(this.wepId);
        Equip scrolledWep = scrollPlayersDroppedWeapon(this.playersWep);
        returnUpgradedWeapon(scrolledWep);
    }

    private int analyzePlayersWeapon() {
        int playersWeaponId;
        try {
            playersWeaponId = readPlayerEquipBySlotName(getInteractors().getRespondant(), BodyPart.WEAPON).getItemId();
        } catch (Exception e) {
            return 0;
        }
        String weaponName = convertItemIdToName(playersWeaponId);
        Map<String, String> replacements = Map.of("%WPN_NAME%", weaponName);
        getDialogueHandler().executeBotDialogueWithReplacementStrings("AnalyzePlayersWeapon", replacements, TutorialBot.this);
        return playersWeaponId;
    }

    private Equip lootPlayersDroppedWeapon(int weaponId) {
        List<MapObject> playersWeaponOnFloor = checkForItemOnFloor(getInteractors().getRespondant(), getInteractors().getRespondant().getPosition(), weaponId);
        Equip itm = null;
        for (MapObject mapObj : playersWeaponOnFloor) {
            MapItem mapItem = (MapItem) mapObj;
            if (mapItem.getItemId() == weaponId) {
                Item itemFound = mapItem.getItem();
                itm = (Equip) itemFound;
            }
        }
        if (!playersWeaponOnFloor.isEmpty()) {
            DropCommands.lootItemListOnFloor(getChr(), playersWeaponOnFloor);
        }

        return itm;
    }

    private void lootDialog(int weaponId) {
        String weaponName = convertItemIdToName(weaponId);
        Map<String, String> replacements = Map.of("%WPN_NAME%", weaponName);
        getDialogueHandler().executeBotDialogueWithReplacementStrings("LootDialogue", replacements, TutorialBot.this);
        BotHelpers.blockingSleep(6000);
    }

    private Equip scrollPlayersDroppedWeapon(Equip playersWep) {
        Item scrolledWep = ItemInformationProvider.getInstance().scrollEquipWithId(playersWep, 2043003, false, 0, false);
        scrolledWep.setOwner(getInteractors().getRespondant().getName());
        botScrollSuccess(getChr());
        BotEmote(getChr(), 2);
        BotHelpers.blockingSleep(3000);
        upgradeDialog();
        return (Equip) scrolledWep;
    }

    private void upgradeDialog() {
        getDialogueHandler().executeBotDialogue("UpgradeDialogue", TutorialBot.this);
    }

    private void returnUpgradedWeapon(Equip scrolledWep) {
        DropCommands.botThrowEquip(getChr(), scrolledWep, getInteractors().getRespondant().getPosition());
        returnDialog();
    }

    private void returnDialog() {
        getDialogueHandler().executeBotDialogue("ReturnDialogue", TutorialBot.this);
    }

    // Deliberate synchronous choreography — dialogue durations are YAML-driven.
    private void send_off() {
        send_off_dialog();
        escort_player_to_portal();
        send_off_parting_gift();
    }

    private void send_off_dialog() {
        getDialogueHandler().executeBotDialogue("SendoffDialogue", TutorialBot.this);
    }

    private void escort_player_to_portal() {
        pathFinderBeta(getChr(), new Point(927,485));
        BotHelpers.blockingSleep(2000);
    }

    private void send_off_parting_gift() {
        getDialogueHandler().executeBotDialogue("SendoffPartingGiftDialogue", TutorialBot.this);
        DropCommands.botThrowItem(getChr(), 2022179, getInteractors().getRespondant().getPosition());
        noobsHelped.add(getInteractors().getRespondant().getId());
        waitFor(1000); // trailing beat before RETURN ticks
    }

    private void returnOrigin() {
        returnToWaitingSpot();
    }

    private void returnToWaitingSpot() {
        String tutorial_return = "tutorial2";
//        List<MovementPacket> mvp = readPacketsFromFile(getChr().getMapId(), tutorial_return);
        MovementRecording mvr = getMovementRecording(getChr().getMapId(), tutorial_return);
        BotMoveStream(mvr, getChr());
        waitFor(1000); // settle beat before RESET ticks
    }

    private void botWaitingForPlayerFlavorText() {
        if (random.nextInt(100) < 6) {
            getDialogueHandler().executeBotFlavorDialogue("Flavor", TutorialBot.this);
        }
    }
}

/*
[] todo make hats dropped unlootable. then after selected, make the one remaining lootable
 */
