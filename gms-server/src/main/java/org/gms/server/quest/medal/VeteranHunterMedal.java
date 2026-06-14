package org.gms.server.quest.medal;

import org.gms.client.Character;
import org.gms.client.QuestStatus;
import org.gms.constants.game.DelayedQuestUpdate;
import org.gms.server.life.Monster;
import org.gms.server.quest.Quest;

public final class VeteranHunterMedal {
    public static final int QUEST_ID = 29400;
    public static final int MEDAL_ID = 1142004;
    public static final int REQUIRED_KILLS = 100000;
    public static final int PROGRESS_MOB_ID = 9999999;

    private VeteranHunterMedal() {
    }

    public static int getProgress(Character player) {
        QuestStatus status = player.getQuest(Quest.getInstance(QUEST_ID));
        try {
            return Integer.parseInt(status.getProgress(PROGRESS_MOB_ID));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    public static boolean isComplete(Character player) {
        return getProgress(player) >= REQUIRED_KILLS;
    }

    public static void onMonsterKilled(Character player, Monster monster) {
        Quest quest = Quest.getInstance(QUEST_ID);
        QuestStatus status = player.getQuest(quest);
        if (status.getStatus() != QuestStatus.Status.STARTED || isComplete(player)) {
            return;
        }

        if (!isEligibleKill(player, monster)) {
            return;
        }

        int progress = Math.min(getProgress(player) + 1, REQUIRED_KILLS);
        status.setProgress(PROGRESS_MOB_ID, Integer.toString(progress));
        player.announceUpdateQuest(DelayedQuestUpdate.UPDATE, status, false);
    }

    private static boolean isEligibleKill(Character player, Monster monster) {
        int monsterLevel = monster.getStats().getLevel();
        if (player.getLevel() >= 120) {
            return monsterLevel >= 120;
        }
        return monsterLevel > player.getLevel();
    }
}
