package soloMapling.command;

import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.client.command.Command;
import soloMapling.ArtificialPlayer.BotCommandsPack.MegaphoneCommands;
import soloMapling.ArtificialPlayer.BotHelpers;
import soloMapling.ArtificialPlayer.BotTradeSystem.BotTradeCommands;
import soloMapling.server.ExecutorServiceManager;

import static soloMapling.server.SoloMaplingUtilities.isInteger;

public class TradeBotTestCommand extends Command {
    {
        setDescription("Trade Bot Commands.");
    }

    private static Character player;

    @Override
    public void execute(Client c, String[] params) {
        player = c.getPlayer();
        if (params.length == 0) {
            ExecutorServiceManager.getExecutorService().execute(() -> {
                player.yellowMessage("No Command Parameter Found. Try !tradebot help");
            });
            return;
        }
        if (params.length == 1) {
            ExecutorServiceManager.getExecutorService().execute(() ->
            {
                handleSingleInputCommand(params[0], c);
            });
            return;
        }
        if (params.length == 2) {
            ExecutorServiceManager.getExecutorService().execute(() ->
            {
                if (isInteger(params[1])) {
                    int commandNum = Integer.parseInt(params[1]);
                    handleStringIntCommand(params[0], commandNum, c);
                } else if (!isInteger(params[0]) && !isInteger(params[1])) {
                    String commandString1 = (params[0]);
                    String commandString2 = params[1];
                    handleStringStringCommand(commandString1, commandString2, c);
                } else {
                    player.yellowMessage("Second input not an integer");
                }
            });
            return;
        }
        if (params.length == 3) {
            ExecutorServiceManager.getExecutorService().execute(() ->
            {
                if (isInteger(params[1]) && isInteger(params[2])) {
                    int commandNum = Integer.parseInt(params[1]);
                    int commandNum2 = Integer.parseInt(params[2]);
                    handleStringIntIntCommand(params[0], commandNum, commandNum2, c);
                } else if (isInteger(params[1])) {
                    int commandNum = Integer.parseInt(params[1]);
                    String commandString = params[2];
                    handleStringIntStringCommand(params[0], commandNum, commandString);
                } else {
                    player.yellowMessage("Second input not an integer");
                }
            });
            return;
        }
    }

    public static void handleSingleInputCommand(String input, Client c) {
        switch (input.toLowerCase()) {
            case "help":
                printHelp();
                break;
            case "move":
                break;
            default:
                player.yellowMessage("Invalid command - Direct Command. Try !tradebot help");
                break;
        }
    }

    public static void handleStringIntIntCommand(String input, int input2, int input3, Client c) {
        Character fakechar = BotHelpers.getCharFromChannelStorage(input2);
        if (fakechar == null) {
            player.yellowMessage("Bot null");
            return;
        }

        player.yellowMessage("Command: " + input + ", int2: " + input2 + ", in3: " + input3);
        switch (input.toLowerCase()) {
            case "meso":
            case "addmeso":
            case "setmeso":
                BotTradeCommands.setMeso(fakechar, input3);
                break;
            case "addequip":
                // 1022060 white rac mask
                // 1002357
                BotTradeCommands.addCleanEquipToTrade(fakechar, input3, 1);
                break;
            case "additem":
                // 2340000 white scroll
                BotTradeCommands.addItemToTrade(fakechar, input3, 1, 1);
                break;
            case "swapitem":
                BotTradeCommands.swapScamEquipToTrade(fakechar, input3, 1);
                break;
            case "readitem":
                BotTradeCommands.readItemInPartnerSlot(fakechar, input3);
                break;
            default:
                player.yellowMessage("Invalid command - handleStringIntIntCommand");
                break;
        }
    }

    public static void handleStringIntCommand(String input, int input2, Client c) {
        Character fakechar = BotHelpers.getCharFromChannelStorage(input2);
        if (fakechar == null) {
            player.yellowMessage("Bot null for handleStringIntCommand");
            return;
        }

        switch (input.toLowerCase()) {
            case "Test":
                break;
            case "confirm":
                BotTradeCommands.confirmTrade(fakechar);
                break;
            case "checkconfirmed":
            case "checklocked":
            case "checkpartnerlocked":
            case "checkpartnerlock":
                BotTradeCommands.isPartnerLocked(fakechar);
                break;
            case "accept":
            case "accepttrade":
                BotTradeCommands.acceptTradeInvite(fakechar);
                break;
            case "cancel":
            case "decline":
            case "declinetrade":
                BotTradeCommands.declineTradeInvite(fakechar);
                break;
            case "request":
            case "requesttrade":
                BotTradeCommands.sendTradeRequestToPlayer(fakechar, c.getPlayer());
                break;
            case "readmeso":
                BotTradeCommands.readMeso(fakechar);
                break;
            case "readpmeso":
            case "readpartnermeso":
                BotTradeCommands.readPartnerMeso(fakechar);
                break;
            case "generaltest":
                BotTradeCommands.readPartnerFame(fakechar);
                BotTradeCommands.readPartnerLevel(fakechar);
                break;
            case "getoccupiedslots":
                BotTradeCommands.getOccupiedTradeSlots(fakechar);
                break;
            case "getfreeslots":
            case "getemptyslots":
                BotTradeCommands.getEmptyTradeSlots(fakechar);
                break;
            default:
                player.yellowMessage("Invalid command - handleStringIntCommand");
                break;
        }
    }

    public static void handleStringStringCommand(String input, String input2, Client c) {
        switch (input.toLowerCase()) {
            case "Test":
                break;
            default:
                player.yellowMessage("Invalid command - handleStringIntCommand");
                break;
        }
    }

    private static void printHelp() {
        player.yellowMessage("---- Trade Bot Commands (!tradebot) ----");
        player.yellowMessage("-- Trade Lifecycle --");
        player.yellowMessage("!tradebot request <cid>              - send trade request to player");
        player.yellowMessage("!tradebot accept <cid>               - accept trade invite");
        player.yellowMessage("!tradebot cancel/decline <cid>       - decline trade invite");
        player.yellowMessage("!tradebot confirm <cid>              - confirm trade");
        player.yellowMessage("-- Trade Items --");
        player.yellowMessage("!tradebot setmeso <cid> <amount>     - set bot's meso");
        player.yellowMessage("!tradebot addequip <cid> <itemid>    - add clean equip to trade");
        player.yellowMessage("!tradebot additem <cid> <itemid>     - add item to trade");
        player.yellowMessage("!tradebot swapitem <cid> <itemid>    - swap scam equip into trade");
        player.yellowMessage("-- Trade Info --");
        player.yellowMessage("!tradebot readitem <cid> <slot>      - read item in partner slot");
        player.yellowMessage("!tradebot readmeso <cid>             - read bot's meso");
        player.yellowMessage("!tradebot readpmeso <cid>            - read partner's meso");
        player.yellowMessage("!tradebot checkconfirmed <cid>       - check if partner locked");
        player.yellowMessage("!tradebot getoccupiedslots <cid>     - get occupied trade slots");
        player.yellowMessage("!tradebot getfreeslots <cid>         - get empty trade slots");
        player.yellowMessage("!tradebot generaltest <cid>          - read partner fame + level");
        player.yellowMessage("-- Chat --");
        player.yellowMessage("!tradebot chat <cid> <message>       - write trade chat");
    }

    public static void handleStringIntStringCommand(String input, int input2, String str) {
        Character fakechar = BotHelpers.getCharFromChannelStorage(input2);
        if (fakechar == null) {
            player.yellowMessage("Bot null");
            return;
        }

        switch (input.toLowerCase()) {
            case "chat":
            case "tradechat":
                BotTradeCommands.writeTradeChat(fakechar, str);
                break;
            default:
                player.yellowMessage("Invalid command Two Object");
                break;
        }

    }

}
