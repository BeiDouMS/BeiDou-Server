package org.gms.server.quest.medal;

import org.gms.client.Character;
import org.gms.client.QuestStatus;
import org.gms.client.inventory.Pet;
import org.gms.constants.game.DelayedQuestUpdate;
import org.gms.server.life.Monster;
import org.gms.server.quest.Quest;

public final class SpecialChallengeMedal {
    public static final int MAPLE_IDOL_QUEST_ID = 29500;
    public static final int HORNTAIL_SLAYER_QUEST_ID = 29501;
    public static final int PINK_BEAN_SLAYER_QUEST_ID = 29502;
    public static final int DONATION_KING_QUEST_ID = 29503;
    public static final int CARNIVAL_VICTORY_QUEST_ID = 29505;
    public static final int CARNIVAL_GENIUS_QUEST_ID = 29506;
    public static final int PET_OWNER_QUEST_ID = 29507;
    public static final int CHALLENGER_QUEST_ID = 29509;
    public static final int MONSTER_EXPERT_QUEST_ID = 29512;

    public static final int MAPLE_IDOL_MEDAL_ID = 1142006;
    public static final int HORNTAIL_SLAYER_MEDAL_ID = 1142007;
    public static final int PINK_BEAN_SLAYER_MEDAL_ID = 1142008;
    public static final int DONATION_KING_MEDAL_ID = 1142014;
    public static final int CARNIVAL_VICTORY_MEDAL_ID = 1142077;
    public static final int CARNIVAL_GENIUS_MEDAL_ID = 1142078;
    public static final int PET_OWNER_MEDAL_ID = 1142082;
    public static final int CHALLENGER_MEDAL_ID = 1142084;
    public static final int MONSTER_EXPERT_MEDAL_ID = 1142083;

    public static final int MAPLE_IDOL_REQUIRED_FAME = 1000;
    public static final int HORNTAIL_REQUIRED_KILLS = 1;
    public static final int PINK_BEAN_REQUIRED_KILLS = 1;
    public static final int DONATION_REQUIRED_MESO = 10000000;
    public static final int CARNIVAL_REQUIRED_WINS = 100;
    public static final int CARNIVAL_GENIUS_MIN_MATCHES = 50;
    public static final int CARNIVAL_GENIUS_WIN_RATE = 70;
    public static final int PET_REQUIRED_TAMENESS = 1400;
    public static final int MONSTER_BOOK_REQUIRED_CARDS = 30;

    public static final int HORNTAIL_MOB_ID = 8810018;
    public static final int PINK_BEAN_MOB_ID = 8820001;

    public static final int PROGRESS_KILLS = 0;
    public static final int PROGRESS_WINS = 1;
    public static final int PROGRESS_LOSSES = 2;

    private SpecialChallengeMedal() {
    }

    public static int getProgress(Character player, int questId, int progressId) {
        QuestStatus status = player.getQuest(Quest.getInstance(questId));
        try {
            return Integer.parseInt(status.getProgress(progressId));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    public static int getMaxPetTameness(Character player) {
        int tameness = 0;
        for (Pet pet : player.getPets()) {
            if (pet != null && pet.getTameness() > tameness) {
                tameness = pet.getTameness();
            }
        }
        return tameness;
    }

    public static int getMonsterBookCards(Character player) {
        return player.getMonsterBook().getTotalCards();
    }

    public static boolean hasCarnivalVictoryMedalProgress(Character player) {
        return getProgress(player, CARNIVAL_VICTORY_QUEST_ID, PROGRESS_WINS) >= CARNIVAL_REQUIRED_WINS;
    }

    public static boolean hasCarnivalGeniusMedalProgress(Character player) {
        int wins = getProgress(player, CARNIVAL_GENIUS_QUEST_ID, PROGRESS_WINS);
        int losses = getProgress(player, CARNIVAL_GENIUS_QUEST_ID, PROGRESS_LOSSES);
        int matches = wins + losses;
        return matches >= CARNIVAL_GENIUS_MIN_MATCHES && wins * 100 >= matches * CARNIVAL_GENIUS_WIN_RATE;
    }

    public static void onMonsterKilled(Character player, Monster monster) {
        int mobId = monster.getId();
        if (mobId == HORNTAIL_MOB_ID) {
            addStartedQuestProgress(player, HORNTAIL_SLAYER_QUEST_ID, PROGRESS_KILLS, HORNTAIL_REQUIRED_KILLS);
        } else if (mobId == PINK_BEAN_MOB_ID) {
            addStartedQuestProgress(player, PINK_BEAN_SLAYER_QUEST_ID, PROGRESS_KILLS, PINK_BEAN_REQUIRED_KILLS);
        }
    }

    public static void onMonsterCarnivalFinished(Character player, boolean cpq1, boolean won) {
        if (cpq1) {
            return;
        }

        if (won) {
            addStartedQuestProgress(player, CARNIVAL_VICTORY_QUEST_ID, PROGRESS_WINS, CARNIVAL_REQUIRED_WINS);
        }

        int progressId = won ? PROGRESS_WINS : PROGRESS_LOSSES;
        addStartedQuestProgress(player, CARNIVAL_GENIUS_QUEST_ID, progressId, Integer.MAX_VALUE);
    }

    private static void addStartedQuestProgress(Character player, int questId, int progressId, int cap) {
        Quest quest = Quest.getInstance(questId);
        QuestStatus status = player.getQuest(quest);
        if (status.getStatus() != QuestStatus.Status.STARTED) {
            return;
        }

        int progress = getProgress(player, questId, progressId);
        if (progress >= cap) {
            return;
        }

        status.setProgress(progressId, Integer.toString(Math.min(progress + 1, cap)));
        player.announceUpdateQuest(DelayedQuestUpdate.UPDATE, status, false);
    }
}
