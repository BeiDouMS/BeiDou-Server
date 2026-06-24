package org.gms.server.quest.medal;

import org.gms.client.Character;
import org.gms.client.QuestStatus;
import org.gms.client.inventory.Pet;
import org.gms.constants.game.DelayedQuestUpdate;
import org.gms.server.life.Monster;
import org.gms.server.quest.Quest;

/**
 * 特级挑战勋章的轻量化实现。
 *
 * <p>这些勋章原始数据里有“全服第一”“定期重置”“捐献排行”等机制，本服目前没有对应系统。
 * 这里统一改成可控的个人条件：脚本负责领奖判断，击杀和嘉年华结算负责写入任务进度。</p>
 */
public final class SpecialChallengeMedal {
    // 任务 ID：脚本和事件回调用这些 ID 读写对应任务状态。
    public static final int MAPLE_IDOL_QUEST_ID = 29500;
    public static final int HORNTAIL_SLAYER_QUEST_ID = 29501;
    public static final int PINK_BEAN_SLAYER_QUEST_ID = 29502;
    public static final int DONATION_KING_QUEST_ID = 29503;
    public static final int CARNIVAL_VICTORY_QUEST_ID = 29505;
    public static final int CARNIVAL_GENIUS_QUEST_ID = 29506;
    public static final int PET_OWNER_QUEST_ID = 29507;
    public static final int CHALLENGER_QUEST_ID = 29509;
    public static final int MONSTER_EXPERT_QUEST_ID = 29512;

    // 勋章道具 ID：脚本发奖时直接引用，避免脚本里散落魔法数字。
    public static final int MAPLE_IDOL_MEDAL_ID = 1142006;
    public static final int HORNTAIL_SLAYER_MEDAL_ID = 1142007;
    public static final int PINK_BEAN_SLAYER_MEDAL_ID = 1142008;
    public static final int DONATION_KING_MEDAL_ID = 1142014;
    public static final int CARNIVAL_VICTORY_MEDAL_ID = 1142077;
    public static final int CARNIVAL_GENIUS_MEDAL_ID = 1142078;
    public static final int PET_OWNER_MEDAL_ID = 1142082;
    public static final int CHALLENGER_MEDAL_ID = 1142084;
    public static final int MONSTER_EXPERT_MEDAL_ID = 1142083;

    // 领取阈值：集中在 Java 中，便于后续调整数值，不需要逐个改脚本。
    public static final int MAPLE_IDOL_REQUIRED_FAME = 1000;
    public static final int HORNTAIL_REQUIRED_KILLS = 1;
    public static final int PINK_BEAN_REQUIRED_KILLS = 1;
    public static final int DONATION_REQUIRED_MESO = 10000000;
    public static final int CARNIVAL_REQUIRED_WINS = 100;
    public static final int CARNIVAL_GENIUS_MIN_MATCHES = 50;
    public static final int CARNIVAL_GENIUS_WIN_RATE = 70;
    public static final int PET_REQUIRED_TAMENESS = 1400;
    public static final int MONSTER_BOOK_REQUIRED_CARDS = 30;

    // 击杀追踪只认最终 Boss 本体 ID，避免召唤物或阶段怪误计数。
    public static final int HORNTAIL_MOB_ID = 8810018;
    public static final int PINK_BEAN_MOB_ID = 8820001;

    // 任务进度槽位：复用 QuestStatus progress，不新增数据库字段。
    public static final int PROGRESS_KILLS = 0;
    public static final int PROGRESS_WINS = 1;
    public static final int PROGRESS_LOSSES = 2;

    private SpecialChallengeMedal() {
    }

    /**
     * 读取指定任务的进度槽位。
     *
     * <p>未开始任务或旧数据为空时，QuestStatus 可能返回空值；解析失败统一当作 0，避免脚本领取时报错。</p>
     */
    public static int getProgress(Character player, int questId, int progressId) {
        QuestStatus status = player.getQuest(Quest.getInstance(questId));
        try {
            return Integer.parseInt(status.getProgress(progressId));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    /**
     * 获取角色当前所有出战宠物中的最高亲密度。
     *
     * <p>这样不再依赖 WZ 里固定的宠物 ID 列表，新宠物也能正常参与勋章判断。</p>
     */
    public static int getMaxPetTameness(Character player) {
        int tameness = 0;
        for (Pet pet : player.getPets()) {
            if (pet != null && pet.getTameness() > tameness) {
                tameness = pet.getTameness();
            }
        }
        return tameness;
    }

    // 怪物博士只需要总卡片数，实际领取判断放在任务脚本中执行。
    public static int getMonsterBookCards(Character player) {
        return player.getMonsterBook().getTotalCards();
    }

    // 29505：嘉年华百战百胜者，只看怪物嘉年华2胜场是否达到固定阈值。
    public static boolean hasCarnivalVictoryMedalProgress(Character player) {
        return getProgress(player, CARNIVAL_VICTORY_QUEST_ID, PROGRESS_WINS) >= CARNIVAL_REQUIRED_WINS;
    }

    // 29506：嘉年华天才，需要先满足最低场次，再检查胜率。
    public static boolean hasCarnivalGeniusMedalProgress(Character player) {
        int wins = getProgress(player, CARNIVAL_GENIUS_QUEST_ID, PROGRESS_WINS);
        int losses = getProgress(player, CARNIVAL_GENIUS_QUEST_ID, PROGRESS_LOSSES);
        int matches = wins + losses;
        return matches >= CARNIVAL_GENIUS_MIN_MATCHES && wins * 100 >= matches * CARNIVAL_GENIUS_WIN_RATE;
    }

    /**
     * 怪物死亡时记录相关 Boss 勋章进度。
     *
     * <p>只给已经接取中的任务加进度；没有接任务时击杀不会被补记，保持任务流程可控。</p>
     */
    public static void onMonsterKilled(Character player, Monster monster) {
        int mobId = monster.getId();
        if (mobId == HORNTAIL_MOB_ID) {
            addStartedQuestProgress(player, HORNTAIL_SLAYER_QUEST_ID, PROGRESS_KILLS, HORNTAIL_REQUIRED_KILLS);
        } else if (mobId == PINK_BEAN_MOB_ID) {
            addStartedQuestProgress(player, PINK_BEAN_SLAYER_QUEST_ID, PROGRESS_KILLS, PINK_BEAN_REQUIRED_KILLS);
        }
    }

    /**
     * 怪物嘉年华结算时记录 29505 / 29506 进度。
     *
     * <p>本次需求只确认怪物嘉年华2，因此 cpq1 直接跳过。29505 只累计胜场，29506 同时累计胜负场用于算胜率。</p>
     */
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

    /**
     * 给已接取任务增加一个指定进度槽位。
     *
     * <p>这里集中处理“必须已接任务”“达到上限后不再增加”“通知客户端刷新任务面板”三个细节。</p>
     */
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
