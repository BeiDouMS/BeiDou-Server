package org.gms.server.quest.medal;

import org.gms.client.Character;
import org.gms.client.QuestStatus;
import org.gms.server.quest.Quest;

public final class DynamicHairMedal {
    public static final int QUEST_ID = 29020;
    public static final int REQUIRED_CHANGES = 50;

    private DynamicHairMedal() {
    }

    public static void onHairChanged(Character player, int oldHair, int newHair) {
        QuestStatus status = player.getQuestNoAdd(Quest.getInstance(QUEST_ID));
        if (oldHair / 10 == newHair / 10 || status == null || status.getStatus() != QuestStatus.Status.STARTED) {
            return;
        }

        int progress = getProgress(status);
        if (progress < REQUIRED_CHANGES) {
            player.setQuestProgress(QUEST_ID, 0, Integer.toString(Math.min(progress + 1, REQUIRED_CHANGES)));
        }
    }

    private static int getProgress(QuestStatus status) {
        try {
            return Integer.parseInt(status.getProgress(0));
        } catch (NumberFormatException nfe) {
            return 0;
        }
    }
}
