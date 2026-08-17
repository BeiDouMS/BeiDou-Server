package soloMapling.server;

import org.gms.client.Character;
import org.gms.client.command.CommandsExecutor;
import soloMapling.ArtificialPlayer.BotCommandsPack.MapleMessengerCommands;
import soloMapling.ArtificialPlayer.BotDecoratorSystem.BotDecorateNX;
import soloMapling.ArtificialPlayer.BotDecoratorSystem.BotDecorationQueue;
import soloMapling.ArtificialPlayer.BotDecoratorSystem.NXItemPool;
import soloMapling.itemPool.EquipMetadataCache;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

import static soloMapling.ArtificialPlayer.BotDebugHandler.getChalkboardStatus;
import static soloMapling.ArtificialPlayer.BotDebugHandler.setBotChalkboard;
import static soloMapling.ArtificialPlayer.BotGeneration.getConsoleBot;
import static soloMapling.ArtificialPlayer.BotMessagingSystem.CharacterStorage.getAllBots;

interface Command {
    void execute(Character chr, String[] args);
}

public class MapleMessengerConsole {

    private static Set<Integer> connectedUsers = new HashSet<>();
    private static Set<Integer> botsLogging = new HashSet<>();
    private static final Map<String, Command> commandMap = new HashMap<>();
    private static final String helpString = "Available commands: mmc connect, mmc disconnect, botlog <id>, botlog <name>, chalkboard <id>, chalkboard <name>, setallchalk, removeallchalk, resetbotlog, cmd <chatCommand>, decoqueue enable|disable|start|stop|status, decoratenx enable|disable|status|cache|reload";


    private static boolean isUserConnected(int userId) {
        return connectedUsers.contains(userId);
    }

    private static void connectUser(int userId) {
        if (!isUserConnected(userId)) {
            connectedUsers.add(userId);
            MapleMessengerCommands.addBotToMessenger(SoloMaplingUtilities.getChr(userId), getConsoleBot());
            MapleMessengerCommands.sendColoredConsoleMessage(SoloMaplingUtilities.getChr(userId), SoloMaplingUtilities.getChr(userId).getName() + " connected to the console.");
        }
    }

    public static void disconnectUser(int userId) {
        connectedUsers.remove(userId);
        MapleMessengerCommands.removeBotFromMessenger(SoloMaplingUtilities.getChr(userId), getConsoleBot());
        MapleMessengerCommands.sendConsoleMessage(SoloMaplingUtilities.getChr(userId), SoloMaplingUtilities.getChr(userId).getName() + " disconnected from the console.");
    }

    public static boolean isLoggingBot(int userId) {
        return botsLogging.contains(userId);
    }

    private static void startLoggingBot(String name) {
        startLoggingBot(SoloMaplingUtilities.getChr(name).getId());
    }

    private static void startLoggingBot(int userId) {
        botsLogging.add(userId);
    }

    private static void stopLoggingBot(String name) {
        stopLoggingBot(SoloMaplingUtilities.getChr(name).getId());
    }

    private static void stopLoggingBot(int userId) {
        botsLogging.remove(userId);
    }

    private static void resetLoggingBot() {
        botsLogging.clear();
    }

    private static void toggleBotChalkboard(String name) {
        toggleBotChalkboard(SoloMaplingUtilities.getChr(name).getId());
    }

    private static void toggleBotChalkboard(int userId) {
        Character chr = SoloMaplingUtilities.getChr(userId);
        setBotChalkboard(chr, !getChalkboardStatus(chr));
    }

    // Register commands
    static {
        registerCommand("help", (chr, args) ->
                MapleMessengerCommands.sendConsoleMessage(chr, helpString));

        registerCommand("mmc", (chr, args) -> {
            if (args.length > 1) {
                switch (args[1]) {
                    case "connect":
                        connectUser(chr.getId());
                        break;
                    case "disconnect":
                        executeIfConnected(chr, () -> {
                            disconnectUser(chr.getId());
                        });
                        break;
                    default:
                        MapleMessengerCommands.sendConsoleMessage(chr, "Usage: mmc connect | mmc disconnect");
                        break;
                }
            } else {
                MapleMessengerCommands.sendConsoleMessage(chr, "Usage: mmc connect | mmc disconnect");
            }
        });

        registerBotLoggingCommand("botlog", true);   // For starting logging
        registerBotLoggingCommand("botunlog", false); // For stopping logging

        registerCommand("chalkboard", (chr, args) -> {
            executeIfConnected(chr, () -> {
                if (args.length > 1) {
                    handleBotChalkboard(args[1]); // false for stopping logging
                } else {
                    MapleMessengerCommands.sendColoredConsoleMessage(chr, "Usage: chalkboard <id|name>");
                }
            });
        });

        registerCommand("setallchalk", (chr, args) -> {
            executeIfConnected(chr, MapleMessengerConsole::setAllChalkboards);
        });

        registerCommand("removeallchalk", (chr, args) -> {
            executeIfConnected(chr, MapleMessengerConsole::removeAllChalkboards);
        });

        registerCommand("resetbotlog", (chr, agrs) -> {
            executeIfConnected(chr, MapleMessengerConsole::resetLoggingBot);
        });

        registerCommand("cmd", (chr, args) -> {
            executeIfConnected(chr, () -> {
                if (args.length > 1) {
                    chatCommandViaMMC(chr, args);
                } else {
                    MapleMessengerCommands.sendColoredConsoleMessage(chr, "Usage: cmd <chatCommand>");
                }
            });
        });

        registerCommand("decoqueue", (chr, args) -> {
            executeIfConnected(chr, () -> handleDecoQueue(chr, args));
        });

        registerCommand("decoratenx", (chr, args) -> {
            executeIfConnected(chr, () -> handleDecorateNx(chr, args));
        });

    }

    private static void handleDecorateNx(Character chr, String[] args) {
        if (args.length < 2) {
            MapleMessengerCommands.sendColoredConsoleMessage(chr,
                    "Usage: decoratenx enable|disable|status|cache|reload");
            return;
        }
        switch (args[1]) {
            case "enable":
                BotDecorateNX.ENABLED = true;
                MapleMessengerCommands.sendColoredConsoleMessage(chr,
                        "BotDecorateNX ENABLED (new bots will roll for NX cosmetics)");
                break;
            case "disable":
                BotDecorateNX.ENABLED = false;
                MapleMessengerCommands.sendColoredConsoleMessage(chr,
                        "BotDecorateNX DISABLED (new bots skip NX decoration)");
                break;
            case "status":
                MapleMessengerCommands.sendColoredConsoleMessage(chr,
                        "decoratenx: enabled=" + BotDecorateNX.ENABLED
                                + " | NXItemPool loaded=" + NXItemPool.isLoaded()
                                + " | EquipCache initialized=" + EquipMetadataCache.isInitialized());
                break;
            case "cache": {
                // Show EquipMetadataCache stats
                if (!EquipMetadataCache.isInitialized()) {
                    MapleMessengerCommands.sendColoredConsoleMessage(chr,
                            "EquipMetadataCache not initialized yet");
                    break;
                }
                EquipMetadataCache cache = EquipMetadataCache.get();
                int total = cache.all().size();
                int cashTotal = 0;
                for (org.gms.constants.inventory.EquipType et : org.gms.constants.inventory.EquipType.values()) {
                    cashTotal += cache.getCashByType(et).size();
                }
                MapleMessengerCommands.sendColoredConsoleMessage(chr,
                        "EquipMetadataCache: " + total + " equips total, "
                                + cashTotal + " cash/NX items");
                break;
            }
            case "reload": {
                // Force-reload NXItemPool (re-reads YAML + re-populates from cache)
                NXItemPool.forceReload();
                MapleMessengerCommands.sendColoredConsoleMessage(chr,
                        "NXItemPool reloaded from YAML + cache");
                break;
            }
            default:
                MapleMessengerCommands.sendColoredConsoleMessage(chr,
                        "Usage: decoratenx enable|disable|status|cache|reload");
        }
    }

    private static void handleDecoQueue(Character chr, String[] args) {
        if (args.length < 2) {
            MapleMessengerCommands.sendColoredConsoleMessage(chr,
                    "Usage: decoqueue enable|disable|start|stop|status");
            return;
        }
        switch (args[1]) {
            case "enable":
                BotDecorationQueue.ENABLED = true;
                MapleMessengerCommands.sendColoredConsoleMessage(chr,
                        "BotDecorationQueue ENABLED (call 'decoqueue start' to begin processing)");
                break;
            case "disable":
                BotDecorationQueue.ENABLED = false;
                BotDecorationQueue.stop();
                MapleMessengerCommands.sendColoredConsoleMessage(chr,
                        "BotDecorationQueue DISABLED and stopped");
                break;
            case "start":
                if (!BotDecorationQueue.ENABLED) {
                    MapleMessengerCommands.sendColoredConsoleMessage(chr,
                            "Cannot start: BotDecorationQueue is disabled. Run 'decoqueue enable' first.");
                    break;
                }
                if (BotDecorationQueue.isRunning()) {
                    MapleMessengerCommands.sendColoredConsoleMessage(chr,
                            "BotDecorationQueue is already running.");
                    break;
                }
                BotDecorationQueue.start();
                MapleMessengerCommands.sendColoredConsoleMessage(chr,
                        "BotDecorationQueue started.");
                break;
            case "stop":
                if (!BotDecorationQueue.isRunning()) {
                    MapleMessengerCommands.sendColoredConsoleMessage(chr,
                            "BotDecorationQueue is not running.");
                    break;
                }
                BotDecorationQueue.stop();
                MapleMessengerCommands.sendColoredConsoleMessage(chr,
                        "BotDecorationQueue stopped.");
                break;
            case "status":
                MapleMessengerCommands.sendColoredConsoleMessage(chr,
                        "decoqueue: enabled=" + BotDecorationQueue.ENABLED
                                + " running=" + BotDecorationQueue.isRunning()
                                + " pending=" + BotDecorationQueue.getPendingCount());
                break;
            default:
                MapleMessengerCommands.sendColoredConsoleMessage(chr,
                        "Usage: decoqueue enable|disable|start|stop|status");
        }
    }

    private static void registerBotLoggingCommand(String commandName, boolean startLogging) {
        registerCommand(commandName, (chr, args) -> {
            executeIfConnected(chr, () -> {
                if (args.length > 1) {
                    handleBotLog(args[1], startLogging);
                } else {
                    MapleMessengerCommands.sendColoredConsoleMessage(chr, "Usage: " + commandName + " <id|name>");
                }
            });
        });
    }

    private static void registerCommand(String name, Command command) {
        commandMap.put(name, command);
    }

    private static void executeIfConnected(Character chr, Runnable command) {
        if (isUserConnected(chr.getId())) {
            command.run();
        } else {
            MapleMessengerCommands.sendConsoleMessage(chr, "You must be connected to the console to use this command.");
        }
    }

    public static void executeCommand(Character chr, String input) {
        String noName = SoloMaplingUtilities.getTextAfterColon(input);
        if (noName == null) {
            return;
        }
        String[] parts = noName.split("\\s+"); // Split input by spaces
        String commandName = parts[0]; // The first word is the command name

        Command command = commandMap.get(commandName);
        if (command != null) {
            command.execute(chr, parts); // Pass the Character and the arguments
        } else {
            if (isUserConnected(chr.getId())) {
                MapleMessengerCommands.sendConsoleMessage(chr, "Unknown command: " + commandName);
            }
        }
    }

    private static void handleBotLog(String identifier, boolean startLogging) {
        if (SoloMaplingUtilities.isNumeric(identifier)) {
            int userId = Integer.parseInt(identifier);
            if (startLogging) {
                startLoggingBot(userId);
            } else {
                stopLoggingBot(userId);
            }
        } else {
            if (startLogging) {
                startLoggingBot(identifier);
            } else {
                stopLoggingBot(identifier);
            }
        }
    }

    private static void handleBotChalkboard(String identifier) {
        if (SoloMaplingUtilities.isNumeric(identifier)) {
            int userId = Integer.parseInt(identifier);
            toggleBotChalkboard(userId);
        } else {
            toggleBotChalkboard(identifier);
        }
    }

    private static void setAllChalkboards() {
        for (Integer botId : getAllBots().keySet()) {
            Character chr = SoloMaplingUtilities.getChr(botId);
            setBotChalkboard(chr, true);
        }
    }

    private static void removeAllChalkboards() {
        for (Integer botId : getAllBots().keySet()) {
            Character chr = SoloMaplingUtilities.getChr(botId);
            setBotChalkboard(chr, false);
        }
    }

    public static void sendMMCLogToConnected(String textLog) {
        if (connectedUsers.isEmpty()) {
            return;
        }
        for (Integer connectedUserId : connectedUsers) {
            MapleMessengerCommands.sendColoredConsoleMessage(SoloMaplingUtilities.getChr(connectedUserId), textLog);
        }
    }

    private static void chatCommandViaMMC(Character player, String[] args) {
        String combinedStringCommand = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        CommandsExecutor.getInstance().handle(player.getClient(), combinedStringCommand);
    }

}
