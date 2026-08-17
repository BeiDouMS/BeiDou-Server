package soloMapling.command;

import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.client.command.Command;
import soloMapling.ArtificialPlayer.BotHelpers;
import soloMapling.server.ExecutorServiceManager;

import static soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands.BotSpeak;
import static soloMapling.server.SoloMaplingUtilities.isInteger;

public class FMBotCommand extends Command {
    {
        setDescription("FmBot Commands.");
    }

    private static Character player;
    
    @Override
    public void execute(Client c, String[] params) {
        player = c.getPlayer();
        if (params.length == 0) {
            ExecutorServiceManager.getExecutorService().execute(() -> {
                player.yellowMessage("No Command Parameter Found. Try !fmbot help");
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
                player.yellowMessage("Invalid command - Direct Command");
                break;
        }
    }

    public static void handleStringIntIntCommand(String input, int input2, int input3, Client c) {
        Character fakechar = BotHelpers.getCharFromChannelStorage(input2);
        if (fakechar == null) {
            player.yellowMessage("Bot null");
            return;
        }

        player.yellowMessage("Command: " + input2 + ", arg: " + input3);
        switch (input.toLowerCase()) {
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
        player.yellowMessage("---- FM Bot Commands (!fmbot) ----");
        player.yellowMessage("!fmbot chat <cid> <message>      - bot speaks in chat");
    }

    public static void handleStringIntStringCommand(String input, int input2, String str) {
        Character fakechar = BotHelpers.getCharFromChannelStorage(input2);
        if (fakechar == null) {
            player.yellowMessage("Bot null");
            return;
        }

        switch (input.toLowerCase()) {
            case "chat":
                BotSpeak(fakechar, str);
                break;
            default:
                player.yellowMessage("Invalid command Two Object");
                break;
        }

    }

}
