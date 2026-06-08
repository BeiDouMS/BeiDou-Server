package org.gms.server.quest.medal;

import org.gms.client.Character;
import org.gms.client.FamilyEntry;
import org.gms.client.QuestStatus;
import org.gms.server.quest.Quest;

public final class OutstandingCitizenMedal {
    public static final int QUEST_ID = 29508;
    public static final int ELIGIBILITY_QUEST_ID = 29580;
    public static final int MEDAL_ID = 1142081;
    public static final int QUEST_NPC_ID = 9000040;

    private OutstandingCitizenMedal() {
    }

    public static boolean isEligible(Character player) {
        FamilyEntry familyEntry = player.getFamilyEntry();
        return player.isMarried()
                && player.getGuildId() > 0
                && familyEntry != null
                && familyEntry.getJuniorCount() >= 1;
    }

    public static void refreshEligibility(Character player) {
        if (player == null) {
            return;
        }

        Quest mainQuest = Quest.getInstance(QUEST_ID);
        Quest eligibilityQuest = Quest.getInstance(ELIGIBILITY_QUEST_ID);
        QuestStatus mainQuestStatus = player.getQuest(mainQuest);
        QuestStatus eligibilityQuestStatus = player.getQuest(eligibilityQuest);

        QuestStatus.Status mainStatus = getStatusOrNotStarted(mainQuestStatus);
        QuestStatus.Status eligibilityStatus = getStatusOrNotStarted(eligibilityQuestStatus);

        if (mainStatus != QuestStatus.Status.STARTED || !isEligible(player)) {
            if (eligibilityStatus != QuestStatus.Status.NOT_STARTED) {
                eligibilityQuest.reset(player);
            }
            return;
        }

        if (eligibilityStatus != QuestStatus.Status.STARTED) {
            eligibilityQuest.forceStart(player, QUEST_NPC_ID);
        }
    }

    public static void clearEligibility(Character player) {
        if (player == null) {
            return;
        }

        Quest eligibilityQuest = Quest.getInstance(ELIGIBILITY_QUEST_ID);
        QuestStatus eligibilityQuestStatus = player.getQuest(eligibilityQuest);
        if (getStatusOrNotStarted(eligibilityQuestStatus) != QuestStatus.Status.NOT_STARTED) {
            eligibilityQuest.reset(player);
        }
    }

    private static QuestStatus.Status getStatusOrNotStarted(QuestStatus questStatus) {
        return questStatus != null ? questStatus.getStatus() : QuestStatus.Status.NOT_STARTED;
    }
}
