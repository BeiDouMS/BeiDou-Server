package soloMapling.ArtificialPlayer;

import org.gms.client.Character;
import soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands;

import java.util.Random;

import static soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands.botClearChalkboard;
import static soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands.botSetChalkboard;
import static soloMapling.ArtificialPlayer.BotMessagingSystem.CharacterStorage.getBotById;
import static soloMapling.BotLogger.log;
import static soloMapling.DebugUtilities.debugprint;
import static soloMapling.server.MapleMessengerConsole.isLoggingBot;
import static soloMapling.server.MapleMessengerConsole.sendMMCLogToConnected;

public class BotDebugHandler {

    boolean useChalkDebug;
    boolean logInteractors;
    Character chr;

    public BotDebugHandler(Character chr) {
        this.chr = chr;
        this.useChalkDebug = false;
        this.logInteractors = false;
    }

    public void setChalkDebug(boolean status) {
        useChalkDebug = status;
    }

    public boolean getChalkDebug() {
        return useChalkDebug;
    }

    public void setLogInteractors(boolean status) {
        logInteractors = status;
    }

    public boolean isLogInteractors() {
        return logInteractors;
    }

    public static void setBotChalkboard(Character fakechar, boolean status) {
        BotSM bot = getBotById(fakechar.getId());
        bot.getDebugger().setChalkDebug(status);
        if (!status) {
            botClearChalkboard(fakechar);
        }
    }

    public static boolean getChalkboardStatus(Character fakechar) {
        BotSM bot = getBotById(fakechar.getId());
        return bot.getDebugger().getChalkDebug();
    }

    public void debugLoggingFull(String BotLogMessage) {
        debugLoggingFull(BotLogMessage, null);
    }

    public void debugLoggingFull(String BotLogMessage, String chalkboardMessage) {
        // BotLogger BotLog.txt log file
        log(BotLogMessage);

        // MapleMessengerConsole Logging
        if (isLoggingBot(chr.getId())) {
            sendMMCLogToConnected(BotLogMessage);
        }

        // chalk board message - in game debugging
        if (chr != null && chalkboardMessage != null && useChalkDebug) {
            botSetChalkboard(chr, chalkboardMessage);
        }

    }

    public void logCurrentRespondantsAndInquirers(BotSM botSM) {
        if (!isLogInteractors()) {
            return;
        }
        StringBuilder respondantLine = new StringBuilder("Respondants: ");
        StringBuilder inquirerLine = new StringBuilder("Inquirers: ");

        for (Character character : botSM.getInteractors().getListRespondants()) {
            respondantLine.append(character.getName()).append(", ");
        }

        for (Character character : botSM.getInteractors().getListInquirer()) {
            inquirerLine.append(character.getName()).append(", ");
        }

        // Remove the trailing comma and space from each line
        if (respondantLine.length() > 12) {
            respondantLine.setLength(respondantLine.length() - 2);
        }
        if (inquirerLine.length() > 10) {
            inquirerLine.setLength(inquirerLine.length() - 2);
        }

        log(respondantLine.toString());
        log(inquirerLine.toString());
    }

    void handleDebugPrints(BotSM botSM) {
        botSM.getDebugger().debugLoggingFull("\n\n" + botSM.getChr().getName() + " State: " + botSM.state);
        botSM.getDebugger().logCurrentRespondantsAndInquirers(botSM);
//        debugprint("current state: " + botSM.state);
    }

    protected void debugBubble(String debugmsg, BotSM botSM) {
        String periods = ".".repeat(new Random().nextInt(4) + 1);
        String dbmsg = "db: " + debugmsg + " " + periods;
        SocialCommands.BotSpeak(botSM.getChr(), dbmsg); // BotChatBubble
        debugprint(dbmsg);
    }
}
