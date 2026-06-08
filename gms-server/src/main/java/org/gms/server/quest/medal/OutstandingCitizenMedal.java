package org.gms.server.quest.medal;

import org.gms.client.Character;
import org.gms.client.FamilyEntry;
import org.gms.client.QuestStatus;
import org.gms.server.quest.Quest;

public final class OutstandingCitizenMedal {
    public static final int QUEST_ID = 29508;
    public static final int ELIGIBILITY_QUEST_ID = 29580;
    public static final int MEDAL_ID = 1142081;

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
        Quest mainQuest = Quest.getInstance(QUEST_ID);
        Quest eligibilityQuest = Quest.getInstance(ELIGIBILITY_QUEST_ID);
        QuestStatus.Status mainStatus = player.getQuest(mainQuest).getStatus();
        QuestStatus.Status eligibilityStatus = player.getQuest(eligibilityQuest).getStatus();

        if (mainStatus != QuestStatus.Status.STARTED || !isEligible(player)) {
            if (eligibilityStatus != QuestStatus.Status.NOT_STARTED) {
                eligibilityQuest.reset(player);
            }
            return;
        }

        if (eligibilityStatus != QuestStatus.Status.STARTED) {
            eligibilityQuest.forceStart(player, 9000040);
        }
    }

    public static void clearEligibility(Character player) {
        Quest eligibilityQuest = Quest.getInstance(ELIGIBILITY_QUEST_ID);
        if (player.getQuest(eligibilityQuest).getStatus() != QuestStatus.Status.NOT_STARTED) {
            eligibilityQuest.reset(player);
        }
    }
}
