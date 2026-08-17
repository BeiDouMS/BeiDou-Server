package soloMapling.ArtificialPlayer;

import org.gms.client.Character;
import soloMapling.ArtificialPlayer.BotMessagingSystem.ChatMessage;
import soloMapling.ArtificialPlayer.BotMessagingSystem.MessageQueue;

import java.util.ArrayList;
import java.util.List;

import static soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands.displayPlayerChatCommands;
import static soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands.expirePlayerChatCommands;
import static soloMapling.ArtificialPlayer.BotHelpers.isBot;
import static soloMapling.BotLogger.log;

// Lightweight numbered-option conversation over the inquirer/tertiary chat path (the Blackjack
// "Join" pattern): the Dispatcher marks the player an inquirer and calls the bot's displayCommands,
// which shows this menu as a hint balloon; the player answers by typing the number or a keyword;
// the owner's tick drains "tertiary" via poll() and dispatches the matched option. SocialBot keeps
// its own richer respondant/secondary loop - this is for bot types that just need a small menu.
public class BotOptionMenu {

    public interface Selection {
        void onSelect(int optionIndex, Character player);
    }

    private static final long MENU_TIMEOUT_MS = 30_000;

    private final BotSM owner;
    private final List<String> labels;
    private final List<List<String>> keywords; // per option; the option number always matches too
    private final Selection onSelect;

    private volatile boolean active = false;
    private volatile long lastActivityMs = 0;

    public BotOptionMenu(BotSM owner, List<String> labels, List<List<String>> keywords, Selection onSelect) {
        if (labels.size() != keywords.size()) {
            throw new IllegalArgumentException("labels/keywords size mismatch");
        }
        this.owner = owner;
        this.labels = labels;
        this.keywords = keywords;
        this.onSelect = onSelect;
    }

    public boolean isActive() {
        return active;
    }

    public void show(Character player) {
        displayPlayerChatCommands(player, labels);
        active = true;
        lastActivityMs = System.currentTimeMillis();
    }

    // Call once per owner tick. Non-blocking; messages for other bots' inquirers are requeued
    // (the tertiary queue is shared - same etiquette as BlackjackDealerBot.processJoinInquiries).
    public void poll() {
        if (!active) {
            return;
        }
        if (System.currentTimeMillis() - lastActivityMs > MENU_TIMEOUT_MS) {
            closeAll();
            return;
        }
        try {
            ChatMessage message = MessageQueue.getInstance().getMessageNonBlocking("tertiary");
            if (message == null) {
                return;
            }
            Character sender = message.getSender();
            if (sender == null || isBot(sender)) {
                return;
            }
            if (!owner.getInteractors().isInquirer(sender)) {
                MessageQueue.getInstance().requeueMessage("tertiary", message);
                return;
            }
            int idx = match(message.getContent());
            if (idx >= 0) {
                lastActivityMs = System.currentTimeMillis();
                onSelect.onSelect(idx, sender);
            }
        } catch (Exception e) {
            log("[BotOptionMenu] poll error: " + e.getMessage());
        }
    }

    public void close(Character player) {
        expirePlayerChatCommands(player);
        owner.getInteractors().removeInquirer(player);
        if (owner.getInteractors().getListInquirer().isEmpty()) {
            active = false;
        }
    }

    public void closeAll() {
        for (Character player : new ArrayList<>(owner.getInteractors().getListInquirer())) {
            expirePlayerChatCommands(player);
            owner.getInteractors().removeInquirer(player);
        }
        active = false;
    }

    // Stop this menu from polling WITHOUT touching the owner's inquirer list - for owners that
    // switch between menu variants on the same conversation (the inquirer stays; the replacement
    // menu's show() repaints the hint).
    public void deactivate() {
        active = false;
    }

    private int match(String content) {
        String lower = content.trim().toLowerCase();
        for (int i = 0; i < labels.size(); i++) {
            if (lower.equals(String.valueOf(i + 1))) {
                return i;
            }
        }
        for (int i = 0; i < keywords.size(); i++) {
            for (String kw : keywords.get(i)) {
                if (lower.contains(kw)) {
                    return i;
                }
            }
        }
        return -1;
    }
}
