package soloMapling.command;

import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.client.Job;
import org.gms.client.command.Command;
import org.gms.client.inventory.BodyPart;
import org.gms.server.maps.MapleMap;
import org.gms.server.maps.Portal;
import soloMapling.ArtificialPlayer.BotAttackSystem.BotAttackDriver;
import soloMapling.ArtificialPlayer.BotAttackSystem.BotBuffConfig;
import soloMapling.ArtificialPlayer.BotAttackSystem.BotBuffDriver;
import soloMapling.ArtificialPlayer.BotAttackSystem.BotBuffEffects;
import soloMapling.ArtificialPlayer.BotCommandsPack.MegaphoneCommands;
import soloMapling.ArtificialPlayer.BotDecoratorSystem.BotDecorateBody;
import soloMapling.ArtificialPlayer.BotDecoratorSystem.BotDecorateEquips;
import soloMapling.ArtificialPlayer.BotDecoratorSystem.BotDecorateNX;
import soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands;
import soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.MovementRecording;
import soloMapling.ArtificialPlayer.BotMessagingSystem.CharacterStorage;
import soloMapling.ArtificialPlayer.BotTypes.Blackjack.BlackjackDealerBot;
import soloMapling.ArtificialPlayer.BotTypes.TrainingBot;
import soloMapling.ArtificialPlayer.BotGrindSystem.MapMobIndex;
import soloMapling.ArtificialPlayer.BotGrindSystem.RestSpotFinder;
import soloMapling.ArtificialPlayer.BotPartySystem.BotPartyCommands;
import soloMapling.ArtificialPlayer.BotPartySystem.BotPartyQueue;
import soloMapling.ArtificialPlayer.BotPartySystem.BotRecruitManager;
import soloMapling.ArtificialPlayer.BotMessagingSystem.QueueMonitor;
import soloMapling.ArtificialPlayer.BotGeneration;
import soloMapling.ArtificialPlayer.BotHelpers;
import soloMapling.ArtificialPlayer.BotSM;
import soloMapling.ArtificialPlayer.BotTypeManager;
import soloMapling.ArtificialPlayer.SocialHotPotatoManager;
import soloMapling.server.ExecutorServiceManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static soloMapling.ArtificialPlayer.BotCommandsPack.DropCommands.botLoot;
import static soloMapling.ArtificialPlayer.BotCommandsPack.DropCommands.botLootLocation;
import static soloMapling.ArtificialPlayer.BotCommandsPack.DropCommands.botLootOwnerItems;
import static soloMapling.ArtificialPlayer.BotCommandsPack.DropCommands.botLootTargetCharactersItems;
import static soloMapling.ArtificialPlayer.BotCommandsPack.WarpCommands.botEnterPortalDropDown;
import static soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands.BotChatbubbleTyping;
import static soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands.displayPlayerChatCommands;
import static soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands.expirePlayerChatCommands;
import static soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands.BotSpeak;
import static soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands.displayPlayerChatCommands;
import static soloMapling.ArtificialPlayer.BotCommandsPack.DropCommands.botThrowToOwnerMeso;
import static soloMapling.ArtificialPlayer.BotCommandsPack.WarpCommands.botWarpMapOnPortal;
import static soloMapling.ArtificialPlayer.BotCustomization.EquipBot;
import static soloMapling.ArtificialPlayer.BotMovementSystem.InPacketReader.getMovementRecording;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands.BotMoveStream;
import static soloMapling.ArtificialPlayer.BotGeneration.removeBotFromServer;
import static soloMapling.ArtificialPlayer.BotGeneration.warpBotToLocation;
import static soloMapling.ArtificialPlayer.BotHelpers.convertItemIdToName;
import static soloMapling.ArtificialPlayer.BotLogic.readPlayerCashEquipBySlotName;
import static soloMapling.ArtificialPlayer.BotLogic.readPlayerEquipBySlotName;
//import static soloMapling.ArtificialPlayer.BotLogic.readPlayersEquip;
import static soloMapling.ArtificialPlayer.BotTypeManager.BotType.BLACKJACK_DEALER;
import static soloMapling.ArtificialPlayer.BotTypeManager.BotType.DICE_BOT;
import static soloMapling.ArtificialPlayer.BotTypeManager.BotType.FM_BOT;
import static soloMapling.ArtificialPlayer.BotTypeManager.BotType.GACHA_BOT;
import static soloMapling.ArtificialPlayer.BotTypeManager.BotType.HENESYS_BOT;
import static soloMapling.ArtificialPlayer.BotTypeManager.BotType.OPQ_BOT;
import static soloMapling.ArtificialPlayer.BotTypeManager.BotType.SELLING_MERCHANT_BOT;
import static soloMapling.ArtificialPlayer.BotTypeManager.BotType.BUYING_MERCHANT_BOT;
import static soloMapling.ArtificialPlayer.BotTypeManager.BotType.NX_MERCHANT_BOT;
import static soloMapling.ArtificialPlayer.BotTypeManager.BotType.SCROLL_BOT;
import static soloMapling.ArtificialPlayer.BotTypeManager.BotType.GAME_ZONE_HOST_BOT;
import static soloMapling.ArtificialPlayer.BotTypeManager.BotType.DROP_GAME_BOT;
import static soloMapling.ArtificialPlayer.BotTypeManager.BotType.HENESYS_JQ_BOT;
import static soloMapling.ArtificialPlayer.BotTypeManager.BotType.TUTORIAL_BOT;
import static soloMapling.ArtificialPlayer.BotTypeManager.BotType.TEST_ATTACK_BOT;
import static soloMapling.ArtificialPlayer.BotTypeManager.convertBotType;
import static soloMapling.ArtificialPlayer.BotTypeManager.manuallyStartBot;
import static soloMapling.ArtificialPlayer.BotTypeManager.manuallyStopBot;
import static soloMapling.DebugUtilities.debugprint;
import static soloMapling.server.NXCodeManager.createCompleteNXCode;
import static soloMapling.server.SoloMaplingUtilities.getMapleMapById;
import static soloMapling.server.SoloMaplingUtilities.isInteger;

public class ArtificialPlayerCommand extends Command {
    {
        setDescription("Artificial Player Commands Test.");
    }

    private static Character player;

    @Override
    public void execute(Client c, String[] params) {
        player = c.getPlayer();
        if (params.length == 0) {
            ExecutorServiceManager.getExecutorService().execute(() -> {
                player.yellowMessage("Please input an integer for cid. Try !bot help");
            });
            return;
        }
        // Class-spawn commands take string args (class name), so they can't go through
        // the integer-based length dispatch below - handle them up front.
        String first = params[0].toLowerCase();
        if (first.equals("spawn") || first.equals("spawnatk")) {
            boolean autoAttack = first.equals("spawnatk");
            ExecutorServiceManager.getExecutorService().execute(() -> handleSpawn(params, autoAttack, c));
            return;
        }
        if (first.equals("trainhere") || first.equals("traintest")) {
            ExecutorServiceManager.getExecutorService().execute(() -> handleTrainHere(params, c));
            return;
        }
        if (first.equals("grindstyle")) {
            ExecutorServiceManager.getExecutorService().execute(() -> handleGrindStyle(params, c));
            return;
        }
        if (params.length == 1) {
            ExecutorServiceManager.getExecutorService().execute(() ->
            {
                handleDirectCommand(params[0], c);
            });
            return;
        }
        if (params.length == 2) {
            ExecutorServiceManager.getExecutorService().execute(() ->
            {
                if (isInteger(params[1])) {
                    int commandNum = Integer.parseInt(params[1]);
                    handleNumberedCommand(params[0], commandNum, c);
                } else {
                    player.yellowMessage("Second input not an integer");
                }
            });
            return;
        }
        if (params.length == 3) {
            ExecutorServiceManager.getExecutorService().execute(() ->
            {
                boolean massCommand = params[0].toLowerCase().contains("mass");
                if (massCommand) {
                    int commandNum = Integer.parseInt(params[1]);
                    int commandNum2 = Integer.parseInt(params[2]);
                    handleMassCommand(params[0], commandNum, commandNum2, c);
                } else if (isInteger(params[1]) && isInteger(params[2])) {
                    int commandNum = Integer.parseInt(params[1]);
                    int commandNum2 = Integer.parseInt(params[2]);
                    handleTwoNumberedCommand(params[0], commandNum, commandNum2, c);
                } else if (isInteger(params[1])) {
                    int commandNum = Integer.parseInt(params[1]);
                    String commandString = params[2];
                    handleTwoObjectCommand(params[0], commandNum, commandString, c);
                } else {
                    player.yellowMessage("Second input not an integer");
                }
            });
            return;
        }
    }

    public static void handleDirectCommand(String input, Client c) {
        switch (input.toLowerCase()) {
            case "help":
                printHelp();
                break;
            case "create":
                BotTypeManager.createBots(c);
                break;
            case "dcmap":
            case "dcallmap":
            case "dchere":
                removeBotsOnMyMap();
                break;
            case "hint":
                List<String> lstr = List.of("Hey", "Test", "Cho");
                displayPlayerChatCommands(c.getPlayer(), lstr);
//                testCygnusHint(c.getPlayer(), c);
                break;
            case "expirehint":
                expirePlayerChatCommands(c.getPlayer());
                break;
            case "testslotinfo":
                testEqBySlot(c.getPlayer());
                break;
            case "viewqueue":
                QueueMonitor mon = new QueueMonitor();
                mon.run();
                break;
            case "nxcode":
                createCompleteNXCode("GERALTYENNEFER69");
                break;
            default:
                player.yellowMessage("Invalid command - Direct Command");
                break;
        }
    }

    public static void handleNumberedCommand(String input, int input2, Client c) {
        Character fakechar = BotHelpers.getCharFromChannelStorage(input2);
        if (fakechar == null) {
            player.yellowMessage("Bot null");
            return;
        }

        switch (input.toLowerCase()) {
            case "meso":
                testDropCommands(fakechar, c);
                break;
            case "buff": {
                java.util.List<Integer> buffs = BotBuffConfig.buffsForJob(fakechar.getJob());
                int casted = BotBuffDriver.forceBuff(fakechar);
                player.yellowMessage("Buff " + fakechar.getName() + " lv" + fakechar.getLevel()
                        + " (job " + fakechar.getJob() + "): cast " + casted + " of " + buffs.size()
                        + " " + buffs + (buffs.isEmpty() ? " - this job has no buffs configured" : ""));
                if (fakechar.getMapId() != player.getMapId()) {
                    player.yellowMessage("Note: bot is on map " + fakechar.getMapId() + ", you are on "
                            + player.getMapId() + " - the cast animation only shows to players on the bot's map.");
                }
                break;
            }
            case "attack": { // force the single-target attack
                reportAttack(fakechar, BotAttackDriver.forceSingle(fakechar), "attack");
                break;
            }
            case "attackaoe": { // force the sustained AoE attack (or report the bot has none)
                reportAttack(fakechar, BotAttackDriver.forceAoe(fakechar), "attackaoe");
                break;
            }
            case "attackult": { // force the throttled full-map ultimate (or report the bot has none)
                reportAttack(fakechar, BotAttackDriver.forceUltimate(fakechar), "attackult");
                break;
            }
            case "dicebot":
                DICE_BOT.createAndSetBot(fakechar);
                break;
            case "tutorialbot":
            case "tutbot":
                TUTORIAL_BOT.createAndSetBot(fakechar);
                break;
            case "setfmbot":
            case "fmbot":
                FM_BOT.createAndSetBot(fakechar);
                break;
            case "scrollingbot":
            case "scrollbot":
                SCROLL_BOT.createAndSetBot(fakechar);
                break;
            case "merchantbot":
            case "sellingbot":
                SELLING_MERCHANT_BOT.createAndSetBot(fakechar);
                break;
            case "buyingbot":
                BUYING_MERCHANT_BOT.createAndSetBot(fakechar);
                break;
            case "nxbot":
            case "nxmerchantbot":
                NX_MERCHANT_BOT.createAndSetBot(fakechar);
                break;
            case "gachabot":
                GACHA_BOT.createAndSetBot(fakechar);
                break;
            case "henesysbot":
                HENESYS_BOT.createAndSetBot(fakechar);
                break;
            case "gamezonehost":
            case "gzhbot":
                GAME_ZONE_HOST_BOT.createAndSetBot(fakechar);
                break;
            case "bjbot":
            case "bjdealerbot":
            case "blackjackbot":
                BLACKJACK_DEALER.createAndSetBot(fakechar);
                break;
            case "dropgamebot":
            case "dgbot":
                DROP_GAME_BOT.createAndSetBot(fakechar);
                break;
            case "opqbot":
                OPQ_BOT.createAndSetBot(fakechar);
                break;
            case "jqbot":
            case "henesysjqbot":
                HENESYS_JQ_BOT.createAndSetBot(fakechar);
                break;
            case "trainbot":
            case "trainingbot":
                // convert (stop→set→start) so the macro FSM ticks immediately on an already-running bot
                convertBotType(fakechar, BotTypeManager.BotType.TRAINING_BOT);
                player.yellowMessage("Bot " + fakechar.getId() + " is now a TrainingBot (town↔grind loop).");
                break;
            case "breaknow": {
                BotSM maybeTrainer = CharacterStorage.getBotById(fakechar.getId());
                if (maybeTrainer instanceof TrainingBot tb) {
                    player.yellowMessage(tb.forceBreakNow()
                            ? fakechar.getName() + " will take its break on the next grind tick."
                            : fakechar.getName() + " isn't grinding right now (breaks only fire mid-GRIND).");
                } else {
                    player.yellowMessage("Not a TrainingBot.");
                }
                break;
            }
            case "restspot": {
                BotSM maybeTrainer = CharacterStorage.getBotById(fakechar.getId());
                if (maybeTrainer instanceof TrainingBot) {
                    for (String line : RestSpotFinder.debug(fakechar)) {
                        player.yellowMessage(line);
                    }
                } else {
                    player.yellowMessage("Not a TrainingBot.");
                }
                break;
            }
            case "followbot":
            case "followerbot": {
                // the commanding GM becomes the leader (same handoff the dialogue recruit flow uses)
                BotRecruitManager.setPendingLeader(fakechar.getId(), c.getPlayer().getId());
                boolean converted = convertBotType(fakechar, BotTypeManager.BotType.FOLLOWER_BOT);
                player.yellowMessage(converted
                        ? "Bot " + fakechar.getName() + " is now a FollowerBot following YOU."
                        : "Conversion refused - bot is mid-trade.");
                break;
            }
            case "manualstart":
                manuallyStartBot(fakechar);
                break;
            case "manualstop":
                manuallyStopBot(fakechar);
                break;
            case "convertfmbot":
                convertBotType(fakechar, FM_BOT);
                break;
            case "convertscrollbot":
                convertBotType(fakechar, SCROLL_BOT);
                break;

            case "loot":
                botLoot(fakechar, 1000); // 1000 = at feet
                break;
            case "lootwide":
                botLoot(fakechar, 9000); // 3000 = 2.5 character widths
                break;
            case "lootlocation":
                botLootLocation(fakechar, c.getPlayer().getPosition());
                break;
            case "lootownitems":
                botLootOwnerItems(fakechar, fakechar.getPosition(), 12000);
                break;
            case "loottargetsitems":
                botLootTargetCharactersItems(fakechar, c.getPlayer(), c.getPlayer().getPosition(), 9000);
                break;
            case "warphere":
            case "warpbothere":
                MapleMap map = c.getPlayer().getMap();
                Point pos = c.getPlayer().getPosition();
                warpBotToLocation(fakechar, pos, map);
                break;
            case "enterportal":
                botWarpMapOnPortal(fakechar); // doesn't require hard coded id's
                break;
            case "disconnect":
            case "dc":
            case "remove":
                manuallyStopBot(fakechar);
                removeBotFromServer(fakechar);
                break;
            case "randombody":
                BotDecorateBody.decorateBotBody(fakechar);
                break;
            case "randomequips":
                BotDecorateEquips.decorateBotEquips(fakechar);
                break;
            case "decoratenx":
            case "nxdecorate":
                BotDecorateNX.applyForced(fakechar);
                player.yellowMessage("NX decoration applied to bot " + fakechar.getId()
                        + " (" + fakechar.getName() + ") tier=" + fakechar.getTier()
                        + " gender=" + fakechar.getGender());
                break;
            case "faceme":
                MovementCommands.botFaceTowardsPoint(fakechar, c.getPlayer().getPosition());
                player.yellowMessage("Bot " + fakechar.getId() + " facing towards you");
                break;
            case "nudge":
                MovementCommands.nudgeSmall(fakechar);
                player.yellowMessage("Nudged bot " + fakechar.getName() + " by 20 units.");
                break;
            case "nudgeoverlap":
                boolean nudged = MovementCommands.nudgeAwayFromOverlap(fakechar);
                player.yellowMessage(nudged
                        ? "Bot " + fakechar.getName() + " nudged away from nearby bot."
                        : "No overlapping bot found near " + fakechar.getName() + ".");
                break;

            // Party commands
            case "makeparty":
                player.yellowMessage("makeparty result: " + BotPartyCommands.botMakeParty(fakechar));
                break;
            case "leaveparty":
                BotPartyCommands.botLeaveParty(fakechar);
                break;
            case "acceptinv":
            case "acceptparty":
                player.yellowMessage("acceptPartyInvite result: " + BotPartyCommands.botAcceptPartyInvite(fakechar));
                break;
            case "rejectinv":
            case "rejectparty":
                player.yellowMessage("rejectPartyInvite result: " + BotPartyCommands.botRejectPartyInvite(fakechar));
                break;
            case "inviteparty":
            case "botinviteme":
                player.yellowMessage("botInvitePlayer result: " + BotPartyCommands.botInvitePlayer(fakechar, c.getPlayer()));
                break;
            case "checkpartyqueue":
                BotPartyQueue.PartyInviteEntry entry = BotPartyQueue.getInstance().getPartyInvite(fakechar);
                if (entry == null) {
                    player.yellowMessage("No pending party invite for " + fakechar.getName());
                } else {
                    player.yellowMessage("Pending invite: from=" + entry.getInviter().getName() + " partyId=" + entry.getPartyId());
                }
                break;

            default:
                player.yellowMessage("Invalid command - NumberedCommand");
                break;
        }

    }

    public static void handleMassCommand(String input, int input2, int input3, Client c) {
        player.yellowMessage("Mass Command: " + input + ", num: " + input2 + ", num2: " + input3);
        switch (input.toLowerCase()) {
            case "masscreate":
                BotTypeManager.massCreateBots(input2, input3, c);
                break;
            case "massfmbot":
                BotTypeManager.massFMBots(input2, input3);
                break;
            case "massmanualstart":
                BotTypeManager.massManualStart(input2, input3);
                break;
            case "massmanualstop":
                BotTypeManager.massManualStop(input2, input3);
                break;
        }
    }

    public static void handleTwoNumberedCommand(String input, int input2, int input3, Client c) {
        Character fakechar = BotHelpers.getCharFromChannelStorage(input2);
        if (fakechar == null) {
            player.yellowMessage("Bot null");
            return;
        }

        player.yellowMessage("Command: " + input2 + ", arg: " + input3);

        switch (input.toLowerCase()) {
            case "castbuff":
                boolean applied = BotBuffDriver.castSkill(fakechar, input3);
                player.yellowMessage("castbuff " + fakechar.getName() + " (job " + fakechar.getJob()
                        + ") skill " + input3 + " -> " + (applied ? "applied" : "FAILED (no such skill/effect)"));
                if (fakechar.getMapId() != player.getMapId()) {
                    player.yellowMessage("Note: bot is on map " + fakechar.getMapId() + ", you are on "
                            + player.getMapId() + " - the cast animation only shows to players on the bot's map.");
                }
                break;
            case "givebuff":
                // Bot casts (visual) and hands the REAL buff to you (the GM player).
                BotBuffEffects.givePartyBuff(fakechar, input3, java.util.List.of(player));
                player.yellowMessage("givebuff: " + fakechar.getName() + " -> you (skill " + input3 + ")");
                if (fakechar.getMapId() != player.getMapId()) {
                    player.yellowMessage("Note: bot is on map " + fakechar.getMapId() + ", you are on "
                            + player.getMapId() + " - you still get the buff, but the bot's cast animation only shows on its map.");
                }
                break;
            case "extbuff":
                // Bot casts (visual) and hands you the buff with a 10-minute duration.
                BotBuffEffects.giveExtendedBuff(fakechar, input3, java.util.List.of(player),
                        BotBuffEffects.EXTENDED_DURATION_MS);
                player.yellowMessage("extbuff: " + fakechar.getName() + " -> you (skill " + input3 + ", 10 min)");
                if (fakechar.getMapId() != player.getMapId()) {
                    player.yellowMessage("Note: bot is on map " + fakechar.getMapId() + ", you are on "
                            + player.getMapId() + " - you still get the buff, but the bot's cast animation only shows on its map.");
                }
                break;
            case "setlevel":
                if (input3 < 1 || input3 > 200) {
                    player.yellowMessage("setlevel: level must be 1-200");
                    break;
                }
                fakechar.setLevel(input3);
                player.yellowMessage("Set " + fakechar.getName() + " to level " + input3);
                break;
            case "setclass":
            case "setjob": {
                Job newJob = Job.getById(input3);
                if (newJob == null) {
                    player.yellowMessage("setjob: unknown job id " + input3
                            + " (e.g. 100 warrior, 200 magician, 230 cleric, 232 bishop, 412 nightlord)");
                    break;
                }
                fakechar.setJob(newJob);
                player.yellowMessage("Set " + fakechar.getName() + " to job " + newJob + " (" + input3
                        + ") - buffs: " + BotBuffConfig.buffsForJob(newJob));
                break;
            }
            case "equip":
                /*
                1702150 - NX weapon sword mercury
                1302063 - Flaming Katana
                1002577 - Pickpocket Pilfer
                1050018 - Blue Sauna Robe
                1072344 - Facestompers
                1082223 - Stormcaster Gloves
                 */
                EquipBot(fakechar, input3);
                break;
//            case "getslotinfo":
//                testEquipDestinationSlot(c.getPlayer(), input3);
//                break;

            case "sethair":
                fakechar.setHair(input3);
                break;
            case "bjplayer":
            case "bjplayerbot":
            case "bjaddplayer":
                // input2 = dealer bot ID, input3 = player bot ID to add
                BotSM dealerBot = CharacterStorage.getBotById(input2);
                Character playerToAdd = BotHelpers.getCharFromChannelStorage(input3);
                if (dealerBot instanceof BlackjackDealerBot && playerToAdd != null) {
                    BlackjackDealerBot bjDealer = (BlackjackDealerBot) dealerBot;
                    boolean added = bjDealer.getTable().addPlayer(playerToAdd);
                    bjDealer.getInteractors().setRespondant(playerToAdd);
                    player.yellowMessage(added
                            ? playerToAdd.getName() + " added to blackjack table."
                            : "Table is full or player already added.");
                } else {
                    player.yellowMessage("Dealer bot not found or not a BlackjackDealerBot, or player bot not found.");
                }
                break;
            case "loot":
                break;
            default:
                player.yellowMessage("Invalid command - Two Number");
                break;
        }
    }

    // String Int String
    public static void handleTwoObjectCommand(String input, int input2, String str, Client c) {
        Character fakechar = BotHelpers.getCharFromChannelStorage(input2);
        if (fakechar == null) {
            player.yellowMessage("Bot null");
            return;
        }

        switch (input.toLowerCase()) {
            case "chat":
                BotSpeak(fakechar, str);
                break;
            case "bubbletype":
                String dd = "Be very cautious when dealing with other players. You won't know their true intentions.";
                BotChatbubbleTyping(fakechar, dd, 150); // 150ms delay between updates
                break;
            case "playrecording":
            case "playrec":
                player.yellowMessage("Playing recording '" + str + "' on bot " + fakechar.getName() + " (map " + fakechar.getMapId() + ")");
                try {
                    MovementRecording mvr = getMovementRecording(fakechar.getMapId(), str);
                    BotMoveStream(mvr, fakechar);
                    player.yellowMessage("Recording '" + str + "' finished.");
                } catch (Exception e) {
                    player.yellowMessage("Failed to play recording: " + e.getMessage());
                }
                break;

            // Megaphone Commands
            case "smega":
                MegaphoneCommands.BotSuperMegaphone(fakechar, str);
                break;
            case "avatarsmega":
                MegaphoneCommands.BotAvatarMegaphone(fakechar, str);
                break;
            case "tv":
                MegaphoneCommands.BotMapleTV(fakechar, str);
                break;
            case "tvpartner":
            case "tvp":
                MegaphoneCommands.BotMapleTVPartner(fakechar, str, c.getPlayer());
                break;
            default:
                player.yellowMessage("Invalid command Two Object");
                break;
        }

    }

    private static final int SPAWN_MAX_COUNT = 20;

    /**
     * Backs {@code !bot spawn} / {@code !bot spawnatk}: spawn N geared bots of a chosen
     * class + job tier at the GM's position for attack testing. {@code spawnatk} also
     * turns each into a TestAttackBot that auto-swings at nearby mobs.
     *
     * params: [0]=spawn|spawnatk  [1]=class  [2]=jobtier(1-4) or exact level(10-200)  [3]=count(optional)
     */
    private static void handleSpawn(String[] params, boolean autoAttack, Client c) {
        String verb = params[0].toLowerCase();
        if (params.length < 3) {
            player.yellowMessage("Usage: !bot " + verb
                    + " <warrior|mage|bow|thief> <jobtier 1-4 | level 10-200> [count]");
            return;
        }

        Integer baseClass = parseGenre(params[1]);
        if (baseClass == null) {
            player.yellowMessage("Unknown class '" + params[1] + "'. Use warrior | mage | bow | thief.");
            return;
        }

        if (!isInteger(params[2])) {
            player.yellowMessage("Second arg must be a job tier (1-4) or an exact level (10-200).");
            return;
        }
        int[] band = resolveLevelBand(Integer.parseInt(params[2]));
        if (band == null) {
            player.yellowMessage("Second arg must be job tier 1-4 or level 10-200 (got " + params[2] + ").");
            return;
        }

        int count = 1;
        if (params.length >= 4 && isInteger(params[3])) {
            count = Integer.parseInt(params[3]);
        }
        if (count < 1) {
            count = 1;
        }
        if (count > SPAWN_MAX_COUNT) {
            count = SPAWN_MAX_COUNT;
            player.yellowMessage("Count capped at " + SPAWN_MAX_COUNT + ".");
        }

        MapleMap map = getMapleMapById(c.getPlayer().getMapId());
        Point pos = c.getPlayer().getPosition();

        List<Integer> spawned = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int botId = BotGeneration.createBot(pos, map, baseClass, band[0], band[1]);
            if (autoAttack) {
                Character fakechar = BotHelpers.getCharFromChannelStorage(botId);
                if (fakechar != null) {
                    TEST_ATTACK_BOT.createAndSetBot(fakechar);
                    BotTypeManager.startAttackTestBot(fakechar);
                }
            }
            spawned.add(botId);
        }

        String bandDesc = (band[0] == band[1]) ? ("lv" + band[0]) : ("lv" + band[0] + "-" + band[1]);
        player.yellowMessage("Spawned " + spawned.size() + " " + genreName(baseClass) + " bot(s) "
                + bandDesc + (autoAttack ? " [auto-attack]" : "") + ": cids " + spawned);
        if (!autoAttack) {
            player.yellowMessage("Drive with !bot attack <cid> / !bot attackaoe <cid>.");
        }
    }

    /** Map a class-genre word to the base-class id used by the decorator (1-4), or null if unknown. */
    private static Integer parseGenre(String s) {
        switch (s.toLowerCase()) {
            case "warrior", "war", "fighter" -> { return 1; }
            case "mage", "mag", "magician", "wizard" -> { return 2; }
            case "bow", "bowman", "archer", "bowmen" -> { return 3; }
            case "thief", "sin", "rogue", "assassin" -> { return 4; }
            default -> { return null; }
        }
    }

    private static String genreName(int baseClass) {
        return switch (baseClass) {
            case 1 -> "warrior";
            case 2 -> "mage";
            case 3 -> "bowman";
            case 4 -> "thief";
            default -> "unknown";
        };
    }

    /**
     * Resolve the second spawn arg into an inclusive [min,max] level band:
     * 1-4 = job tier (1st 10-29, 2nd 30-69, 3rd 70-119, 4th 120-200);
     * 10-200 = an exact level (band collapses to that level). Returns null if neither.
     */
    private static int[] resolveLevelBand(int tierOrLevel) {
        switch (tierOrLevel) {
            case 1: return new int[]{10, 29};
            case 2: return new int[]{30, 69};
            case 3: return new int[]{70, 119};
            case 4: return new int[]{120, 200};
            default:
                if (tierOrLevel >= 10 && tierOrLevel <= 200) {
                    return new int[]{tierOrLevel, tierOrLevel}; // exact level
                }
                return null;
        }
    }

    /*
     * !bot trainhere <job> [level] [count] — spawn N TrainingBots of a SPECIFIC job at your position and pin
     * them to grind THIS map (station-here), bypassing the normal map-discovery/travel. For testing a job's
     * combat + movement flavor (e.g. a 3rd-job Hermit's flash-jumps, an I/L Mage's teleports). Requires a map
     * WITH MOBS (station-here no-ops in a town). <job> takes a name or a numeric job id.
     */
    private static void handleTrainHere(String[] params, Client c) {
        if (params.length < 2) {
            player.yellowMessage("Usage: !bot trainhere <job> [level] [count]");
            player.yellowMessage("  job: name (hermit, nightlord, chiefbandit, shadower, assassin, bandit, "
                    + "fpwizard, ilwizard, cleric, fpmage, ilmage, priest, bishop) or a job id (e.g. 411).");
            return;
        }
        Job job = resolveJob(params[1]);
        if (job == null) {
            player.yellowMessage("Unknown job '" + params[1] + "'. Try a name (hermit, nightlord, ilmage...) or a job id.");
            return;
        }
        int baseClass = job.getJobNiche(); // 1=Warrior 2=Magician 3=Bowman 4=Thief 5=Pirate
        if (baseClass < 1 || baseClass > 4) {
            player.yellowMessage(job.name() + " isn't a supported training class (warrior/mage/bow/thief only).");
            return;
        }
        MapleMap map = getMapleMapById(c.getPlayer().getMapId());
        if (map == null || MapMobIndex.level(map.getId()) < 0) {
            player.yellowMessage("Stand on a map WITH MOBS (not a town) — 'grind here' needs mobs on the map.");
            return;
        }
        int level = defaultLevelForTier(job.getJobTier());
        if (params.length >= 3 && isInteger(params[2])) {
            int lv = Integer.parseInt(params[2]);
            if (lv >= 1 && lv <= 200) {
                level = lv;
            }
        }
        int count = 1;
        if (params.length >= 4 && isInteger(params[3])) {
            count = Integer.parseInt(params[3]);
        }
        count = Math.max(1, Math.min(count, SPAWN_MAX_COUNT));

        Point pos = c.getPlayer().getPosition();
        List<Integer> spawned = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            // Decorate ONCE with the forced job (level + class-coherent gear + NX in a single pass, the same
            // path !bot spawn uses) — re-decorating after createBot left the bot showing its base look.
            int botId = BotGeneration.createBot(pos, map, baseClass, level, level, job.getId());
            Character bot = BotHelpers.getCharFromChannelStorage(botId);
            if (bot == null) {
                continue;
            }
            BotRecruitManager.markStationHere(botId); // pin: first DECIDE grinds THIS map (no discovery/travel)
            BotTypeManager.BotType.TRAINING_BOT.createAndSetBot(bot);
            BotTypeManager.manuallyStartBot(bot);
            spawned.add(botId);
        }
        player.yellowMessage("Spawned " + spawned.size() + " " + job.name() + " TrainingBot(s) lv" + level
                + " pinned to this map (" + map.getId() + "); they start grinding here in ~7-10s. cids " + spawned);
    }

    /*
     * !bot grindstyle <cid|map> <camp|patrol|roam|stack|auto> — force a TrainingBot's grind archetype ('map' = every
     * training bot on your current map), for testing archetype behavior on arbitrary maps: ROAM on a
     * dense camp map (HHG / Forest of Golem), CAMP on a huge sparse one. 'auto' returns the bot to the
     * profile-resolved natural style. A live grind swaps in place; an idle bot applies it next grind.
     * PATROL/STACK join the list as their strategies land.
     */
    private static void handleGrindStyle(String[] params, Client c) {
        if (params.length < 3) {
            player.yellowMessage("Usage: !bot grindstyle <cid|map> <camp|patrol|roam|stack|auto>");
            return;
        }
        String target = params[1];
        String styleName = params[2];
        List<TrainingBot> targets = new ArrayList<>();
        if (target.equalsIgnoreCase("map")) {
            if (player == null || player.getMap() == null) {
                return;
            }
            for (Character chr : player.getMap().getCharacters()) {
                if (CharacterStorage.getBotById(chr.getId()) instanceof TrainingBot tb) {
                    targets.add(tb);
                }
            }
        } else if (isInteger(target)) {
            if (CharacterStorage.getBotById(Integer.parseInt(target)) instanceof TrainingBot tb) {
                targets.add(tb);
            }
        }
        if (targets.isEmpty()) {
            player.yellowMessage("No training bot found for '" + target + "' (need a TrainingBot cid, or 'map').");
            return;
        }
        int done = 0;
        String lastLine = null;
        for (TrainingBot tb : targets) {
            String line = tb.forceGrindStyle(styleName);
            if (line == null) {
                player.yellowMessage("Unknown grind style '" + styleName + "'. Use camp, patrol, roam, stack, or auto.");
                return;
            }
            lastLine = line;
            done++;
        }
        player.yellowMessage(done == 1 ? lastLine
                : "Forced grind style '" + styleName.toUpperCase() + "' on " + done + " training bot(s) on this map.");
    }

    /** Sensible default level per job tier when the trainhere caller doesn't specify one. */
    private static int defaultLevelForTier(int tier) {
        return switch (tier) {
            case 1 -> 25;
            case 2 -> 45;
            case 3 -> 90;
            case 4 -> 140;
            default -> 50;
        };
    }

    /** Resolve a job name (aliases, punctuation-insensitive) or a numeric job id to a Job. Null if unknown. */
    private static Job resolveJob(String s) {
        if (isInteger(s)) {
            return Job.getById(Integer.parseInt(s));
        }
        String k = s.toLowerCase().replaceAll("[^a-z0-9]", "");
        switch (k) {
            // thief
            case "assassin", "sin": return Job.ASSASSIN;
            case "hermit": return Job.HERMIT;
            case "nightlord", "nl": return Job.NIGHTLORD;
            case "bandit": return Job.BANDIT;
            case "chiefbandit", "cb": return Job.CHIEFBANDIT;
            case "shadower", "shad": return Job.SHADOWER;
            // magician
            case "fpwizard", "fpwiz": return Job.FP_WIZARD;
            case "ilwizard", "ilwiz": return Job.IL_WIZARD;
            case "cleric": return Job.CLERIC;
            case "fpmage": return Job.FP_MAGE;
            case "ilmage": return Job.IL_MAGE;
            case "priest": return Job.PRIEST;
            case "fparchmage": return Job.FP_ARCHMAGE;
            case "ilarchmage": return Job.IL_ARCHMAGE;
            case "bishop", "bish": return Job.BISHOP;
            default:
                try {
                    return Job.valueOf(s.toUpperCase().replaceAll("[^A-Z0-9]", "_"));
                } catch (IllegalArgumentException e) {
                    return null;
                }
        }
    }

    /** Disconnect every bot currently on the command user's map (leaves bots on other maps alone). */
    private static void removeBotsOnMyMap() {
        if (player == null || player.getMap() == null) {
            return;
        }
        // Snapshot first: removeBotFromServer mutates the map's live character collection.
        List<Character> bots = new ArrayList<>();
        for (Character chr : player.getMap().getCharacters()) {
            if (BotHelpers.isBot(chr)) {
                bots.add(chr);
            }
        }
        for (Character bot : bots) {
            removeBotFromServer(bot);
        }
        player.yellowMessage("Removed " + bots.size() + " bot(s) from map " + player.getMapId() + ".");
    }

    // Print the outcome of a forced bot attack (verb = "attack" / "attackaoe" / "attackult").
    private static void reportAttack(Character fakechar, BotAttackDriver.AttackResult res, String verb) {
        if (res.hit()) {
            player.yellowMessage(verb + " " + fakechar.getName() + " lv" + fakechar.getLevel()
                    + " -> hit '" + res.monsterName() + "' for " + res.damage()
                    + (res.killed() ? " (KILLED)" : ""));
        } else {
            player.yellowMessage(verb + " " + fakechar.getName() + " -> no hit: " + res.reason());
        }
        if (fakechar.getMapId() != player.getMapId()) {
            player.yellowMessage("Note: bot is on map " + fakechar.getMapId() + ", you are on "
                    + player.getMapId() + " - the swing only shows to players on the bot's map.");
        }
    }

    private static void printHelp() {
        player.yellowMessage("---- Bot Commands (!bot) ----");
        player.yellowMessage("-- Creation & Lifecycle --");
        player.yellowMessage("!bot create                      - create bots");
        player.yellowMessage("!bot manualstart <cid>           - start bot manually");
        player.yellowMessage("!bot manualstop <cid>            - stop bot manually");
        player.yellowMessage("!bot disconnect/dc/remove <cid>  - remove bot from server");
        player.yellowMessage("!bot dcmap/dchere                - remove ALL bots on your current map");
        player.yellowMessage("!bot masscreate <start> <end>    - create multiple bots");
        player.yellowMessage("!bot massmanualstart <s> <e>     - start multiple bots");
        player.yellowMessage("!bot massmanualstop <s> <e>      - stop multiple bots");
        player.yellowMessage("-- Class Spawn (attack testing) --");
        player.yellowMessage("!bot spawn <class> <tier> [n]    - spawn n geared bots, idle");
        player.yellowMessage("!bot spawnatk <class> <tier> [n] - spawn n & auto-attack here");
        player.yellowMessage("   class: warrior|mage|bow|thief  tier: 1-4 or exact lv 10-200  n<=20");
        player.yellowMessage("!bot trainhere <job> [lv] [n]    - spawn n TrainingBots of a job, grind THIS map");
        player.yellowMessage("   job: name (hermit, nightlord, chiefbandit, priest, ilmage...) or job id; needs a mob map");
        player.yellowMessage("!bot grindstyle <cid|map> <s>    - force grind archetype: camp|patrol|roam|stack|auto ('map' = all here)");
        player.yellowMessage("-- Set Bot Type --");
        player.yellowMessage("!bot fmbot <cid>                 - set as FM bot");
        player.yellowMessage("!bot scrollbot <cid>             - set as scroll bot");
        player.yellowMessage("!bot henesysbot <cid>            - set as Henesys bot");
        player.yellowMessage("!bot tutbot <cid>                - set as tutorial bot");
        player.yellowMessage("!bot dicebot <cid>               - set as dice bot");
        player.yellowMessage("!bot sellingbot <cid>            - set as selling merchant");
        player.yellowMessage("!bot buyingbot <cid>             - set as buying merchant");
        player.yellowMessage("!bot nxbot <cid>                 - set as NX merchant");
        player.yellowMessage("!bot gachabot <cid>              - set as gacha bot");
        player.yellowMessage("!bot gzhbot <cid>                - set as game zone host");
        player.yellowMessage("!bot blackjackbot <cid>          - set as blackjack dealer");
        player.yellowMessage("!bot dgbot <cid>                 - set as drop game bot");
        player.yellowMessage("!bot opqbot <cid>                - set as OPQ bot");
        player.yellowMessage("!bot jqbot <cid>                 - set as JQ bot");
        player.yellowMessage("-- Bot Conversion --");
        player.yellowMessage("!bot convertfmbot <cid>          - convert to FM bot");
        player.yellowMessage("!bot convertscrollbot <cid>      - convert to scroll bot");
        player.yellowMessage("!bot trainbot <cid>              - convert to TrainingBot");
        player.yellowMessage("!bot followbot <cid>             - convert to FollowerBot (follows YOU)");
        player.yellowMessage("!bot breaknow <cid>              - force a grinding TrainingBot's rest break");
        player.yellowMessage("!bot restspot <cid>              - dump ranked rest-spot candidates (why a break spot won)");
        player.yellowMessage("!bot massfmbot <start> <end>     - mass create FM bots");
        player.yellowMessage("-- Loot --");
        player.yellowMessage("!bot loot <cid>                  - loot at feet");
        player.yellowMessage("!bot lootwide <cid>              - loot wide area");
        player.yellowMessage("!bot lootlocation <cid>          - loot at your position");
        player.yellowMessage("!bot lootownitems <cid>          - loot bot's own items");
        player.yellowMessage("!bot loottargetsitems <cid>      - loot your items");
        player.yellowMessage("-- Combat --");
        player.yellowMessage("!bot attack <cid>                - force single-target attack");
        player.yellowMessage("!bot attackaoe <cid>             - force sustained AoE attack (or says none)");
        player.yellowMessage("!bot attackult <cid>             - force full-map ultimate (or says none)");
        player.yellowMessage("-- Appearance --");
        player.yellowMessage("!bot randombody <cid>            - random body decoration");
        player.yellowMessage("!bot randomequips <cid>          - random equip decoration");
        player.yellowMessage("!bot decoratenx <cid>            - apply NX decoration");
        player.yellowMessage("!bot equip <cid> <itemid>        - equip item on bot");
        player.yellowMessage("!bot sethair <cid> <hairid>      - set bot hair style");
        player.yellowMessage("-- Movement --");
        player.yellowMessage("!bot warphere <cid>              - warp bot to you");
        player.yellowMessage("!bot enterportal <cid>           - bot enters nearest portal");
        player.yellowMessage("!bot faceme <cid>                - bot faces towards you");
        player.yellowMessage("!bot nudge <cid>                 - nudge bot 20 units");
        player.yellowMessage("!bot nudgeoverlap <cid>          - nudge bot from overlap");
        player.yellowMessage("-- Party --");
        player.yellowMessage("!bot makeparty <cid>             - bot creates party");
        player.yellowMessage("!bot leaveparty <cid>            - bot leaves party");
        player.yellowMessage("!bot acceptparty <cid>           - bot accepts party invite");
        player.yellowMessage("!bot rejectparty <cid>           - bot rejects party invite");
        player.yellowMessage("!bot botinviteme <cid>           - bot invites you to party");
        player.yellowMessage("!bot checkpartyqueue <cid>       - check pending invite");
        player.yellowMessage("-- Blackjack --");
        player.yellowMessage("!bot bjaddplayer <dealer> <cid>  - add player to BJ table");
        player.yellowMessage("-- Chat & Megaphone --");
        player.yellowMessage("!bot chat <cid> <message>        - bot speaks in chat");
        player.yellowMessage("!bot bubbletype <cid> <msg>      - bot types in chat bubble");
        player.yellowMessage("!bot smega <cid> <message>       - super megaphone");
        player.yellowMessage("!bot avatarsmega <cid> <msg>     - avatar megaphone");
        player.yellowMessage("!bot tv <cid> <message>          - MapleTV");
        player.yellowMessage("!bot tvpartner <cid> <msg>       - MapleTV with partner");
        player.yellowMessage("-- Recordings --");
        player.yellowMessage("!bot playrec <cid> <name>        - play movement recording");
        player.yellowMessage("-- Utility --");
        player.yellowMessage("!bot meso <cid>                  - test drop commands");
        player.yellowMessage("!bot hint                        - display chat hint commands");
        player.yellowMessage("!bot expirehint                  - expire chat hint");
        player.yellowMessage("!bot testslotinfo                - test equip by slot");
        player.yellowMessage("!bot viewqueue                   - monitor message queue");
        player.yellowMessage("!bot nxcode                      - create test NX code");
    }

    private static void testEquipDestinationSlot(Character chr, int itemId) {
//        readPlayersEquip(chr, itemId);
    }

    private static void testEqBySlot(Character chr) {
//        short slotCheck = (short) slot;
//        readPlayerEquipBySlotId(chr, slotCheck);
        String owner = readPlayerEquipBySlotName(chr, BodyPart.WEAPON).getOwner();
        String itemName = convertItemIdToName(readPlayerEquipBySlotName(chr, BodyPart.WEAPON).getItemId());
        debugprint(owner, itemName);
        readPlayerCashEquipBySlotName(chr, BodyPart.WEAPON);

        readPlayerEquipBySlotName(chr, BodyPart.SHOES);
        readPlayerCashEquipBySlotName(chr, BodyPart.SHOES);

        readPlayerEquipBySlotName(chr, BodyPart.CAP);
        readPlayerCashEquipBySlotName(chr, BodyPart.CAP);

        readPlayerEquipBySlotName(chr, BodyPart.COAT);
        readPlayerCashEquipBySlotName(chr, BodyPart.COAT);

        readPlayerEquipBySlotName(chr, BodyPart.PANTS);
        readPlayerCashEquipBySlotName(chr, BodyPart.PANTS);

        readPlayerEquipBySlotName(chr, BodyPart.EAR_ACCESSORY);
        readPlayerCashEquipBySlotName(chr, BodyPart.EAR_ACCESSORY);

        readPlayerEquipBySlotName(chr, BodyPart.MEDAL);
    }


//    private static void testCygnusHint(Character fakechar, Client c) {
//        spawnCygnusGuide(fakechar,true);
//        List<String> commands = List.of("Cho", "Han", "mmmmmmmmm");
//        talkCygnusGuideCommands(fakechar, commands);
//    }

    private static void testDropCommands(Character fakechar, Client c) {
//        botDropMeso(fakechar, 1000);
//        botThrowMeso(fakechar, 1000, c.getPlayer().getPosition());
//
//        botDropEquip(fakechar, 1082223);
//        botThrowEquip(fakechar, 1082223, c.getPlayer().getPosition());
//
//        botDropItem(fakechar, 4002000);
//        botThrowItem(fakechar, 4002000, c.getPlayer().getPosition());
//
//        botDropItemQty(fakechar, 4002000, 10);
//        botThrowItemQty(fakechar, 4002000, 10, c.getPlayer().getPosition());

        botThrowToOwnerMeso(fakechar, 100, c.getPlayer());
//        botThrowToOwnerItem(fakechar, 4002000, c.getPlayer());
//        botThrowToOwnerItemQty(fakechar, 4002000, 10, c.getPlayer());
//        botThrowToOwnerEquip(fakechar, 1082223, c.getPlayer());
//
//        botThrowToOwnerMeso(fakechar, 100, fakechar);
//        botThrowToOwnerItem(fakechar, 4002000, fakechar);
//        botThrowToOwnerItemQty(fakechar, 4002000, 10, fakechar);
//        botThrowToOwnerEquip(fakechar, 1082223, fakechar);
    }


}
