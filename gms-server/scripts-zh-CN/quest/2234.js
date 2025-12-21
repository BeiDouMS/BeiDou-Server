/*
 * 任务 ID : 2234
 * 任务名称 : 享受名声冒险家的特权吧！
 * 任务描述 : 这次的课题是体验 #r名声冒险家的特权#k！#b族长艾尔#k 的要求是使用特权，将 #r当前名声值消耗至 500 以下#k。此外，为了确认你是否积累了足够的名声，需要将总名声值提升至 2,000 点。
 * End NPC : -1
 * 
 * @author ArthurZhu1992
 *
 */

let status = -1;

function end(mode, type, selection) {
    // 统一处理状态机逻辑
    if (mode == 1 && type != 1 && type != 11) {
        status++;
    } else {
        if ((type == 1 || type == 11) && mode == 1) {
            status++;
            selection = 1;
        } else if ((type == 1 || type == 11) && mode == 0) {
            status++;
            selection = 0;
        } else {
            qm.dispose();
            return;
        }
    }

    const player = qm.getPlayer();
    const familyEntry = player.getFamilyEntry();

    if (status == 0) {
        if (!qm.isQuestActive(2234)) {
            qm.forceStartQuest();
            qm.dispose();
            return;
        }

        // 检查声望值条件：总声望>=2000 且 当前声望<=500
        if (familyEntry != null &&
            familyEntry.getTotalReputation() >= 2000 &&
            familyEntry.getReputation() <= 500) {
            qm.sendNext("噢~！你终于体验到 #b名声冒险家的特权#k 了吗？呵呵呵，看看你剩下的名声值，看来确实已经充分体验过了。没想到你能在这么短的时间内达成这个目标，你的领导力简直是与生俱来的。");
        } else {
            qm.sendOk("唔，看来你还没能好好享受 #b名声冒险家的特权#k 呢？\n\n你需要将#r总名声值#k提升到 #b2,000#k 以上，并且通过使用特权将#r当前名声值#k消耗到 #b500#k 以下才行。");
            qm.dispose();
        }
    } else if (status == 1) {
        qm.sendNext("那么，今后也请继续努力成为一名优秀的导师。如果对家族系统还有什么疑问，或者对头衔感兴趣，欢迎随时来找我。");
    } else if (status == 2) {
        qm.gainExp(3000);
        qm.forceCompleteQuest();
        qm.dispose();
    }
}