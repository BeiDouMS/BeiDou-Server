package soloMapling.ArtificialPlayer.BotDecoratorSystem;

import org.gms.client.Character;
import org.gms.client.Job;

import java.util.Random;

public class JobPathLogic {

    /**
     * Determines which thief path a character is on
     * @param character The thief character
     * @return The thief path
     */
    public static Job determineThiefPath(Character character) {
        int jobId = character.getJob().getId();

        // Check if the job ID corresponds to the Assassin path
        if (jobId == 410 || jobId == 411 || jobId == 412) { // Assassin, Hermit, Night Lord IDs
            return Job.ASSASSIN;
        }
        // Check if the job ID corresponds to the Bandit path
        else if (jobId == 420 || jobId == 421 || jobId == 422) { // Bandit, Chief Bandit, Shadower IDs
            return Job.BANDIT;
        }
        // If job level > 0 but can't determine path, make a random choice
        else {
            return new Random().nextBoolean() ? Job.ASSASSIN : Job.BANDIT;
        }
    }

    /**
     * Determines which warrior path a character is on
     * @param character The warrior character
     * @return The warrior path
     */
    public static Job determineWarriorPath(Character character) {
        int jobId = character.getJob().getId();

        // Fighter path
        if (jobId == 110 || jobId == 111 || jobId == 112) { // Fighter, Crusader, Hero IDs
            return Job.FIGHTER;
        }
        // Page path
        else if (jobId == 120 || jobId == 121 || jobId == 122) { // Page, White Knight, Paladin IDs
            return Job.PAGE;
        }
        // Spearman path
        else if (jobId == 130 || jobId == 131 || jobId == 132) { // Spearman, Dragon Knight, Dark Knight IDs
            return Job.SPEARMAN;
        }
        // If can't determine, make a random choice
        else {
            Random random = new Random();
            int choice = random.nextInt(3);
            if (choice == 0) return Job.FIGHTER;
            else if (choice == 1) return Job.PAGE;
            else return Job.SPEARMAN;
        }
    }

    /**
     * Determines which bowman path a character is on
     * @param character The bowman character
     * @return The bowman path
     */
    public static Job determineBowmanPath(Character character) {
        int jobId = character.getJob().getId();

        // Hunter path
        if (jobId == 310 || jobId == 311 || jobId == 312) { // Hunter, Ranger, Bowmaster IDs
            return Job.HUNTER;
        }
        // Crossbowman path
        else if (jobId == 320 || jobId == 321 || jobId == 322) { // Crossbowman, Sniper, Marksman IDs
            return Job.CROSSBOWMAN;
        }
        // If can't determine, make a random choice
        else {
            return new Random().nextBoolean() ? Job.HUNTER : Job.CROSSBOWMAN;
        }
    }

}
