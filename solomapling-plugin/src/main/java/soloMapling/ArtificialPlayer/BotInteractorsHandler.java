package soloMapling.ArtificialPlayer;

import org.gms.client.Character;
import soloMapling.ArtificialPlayer.BotMessagingSystem.CharacterStorage;
import soloMapling.ArtificialPlayer.BotMessagingSystem.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class BotInteractorsHandler {

    private List<Character> respondant;
    private List<Character> inquirer;

    public BotInteractorsHandler() {
        this.respondant = new ArrayList<>();
        this.inquirer = new ArrayList<>();
    }

    public List<Character> getListRespondants() {
        return respondant;
    }

    public List<Character> getListInquirer() {
        return inquirer;
    }

    public void setRespondant(Character respondant) {
        CharacterStorage.addPlayer(respondant);
        this.respondant.add(respondant);
    }

    public Character getRespondant() {
        if (respondant == null || respondant.isEmpty()) {
            return null; // Early return if list is null or empty
        }
        return respondant.get(0);
    }

    public boolean isRespondant(Character character) {
        if (this.respondant != null) {
            return this.respondant.contains(character);
        }
        return false;
    }

    public void removeRespondant(Character character) {
        CharacterStorage.removePlayer(character);
        this.respondant.remove(character);
    }

    public void resetRespondant() {
        if (this.respondant != null) {
            for (Character character : this.respondant) {
                CharacterStorage.removePlayer(character);
            }
            this.respondant.clear();  // Clear all entries
        }
        this.respondant = new ArrayList<>(); // Set to an empty list
    }

    public void setInquirer(Character inquirer) {
        CharacterStorage.addInquirer(inquirer);
        if (!this.inquirer.contains(inquirer)) {
            this.inquirer.add(inquirer);
        }
    }

    public Character getInquirer() {
        return this.inquirer.get(0);
    }

    public boolean isInquirer(Character character) {
        if (this.inquirer != null) {
            return this.inquirer.contains(character);
        }
        return false;
    }

    public void removeInquirer(Character character) {
        CharacterStorage.removeInquirer(character);
        this.inquirer.remove(character);
    }

    public void resetInquirer() {
        if (this.inquirer != null) {
            for (Character character : this.inquirer) {
                CharacterStorage.removeInquirer(character);
            }
            this.inquirer.clear();  // Clear all entries
        }
        this.inquirer = new ArrayList<>(); // Set to an empty list
    }

    public boolean isMessageFromRespondant(ChatMessage message) {
        return message.getSender() == getRespondant();
    }

}
