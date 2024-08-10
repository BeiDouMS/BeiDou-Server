var status = -1;

function end(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            qm.dispose();
            return;
        }

        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            if (qm.isQuestCompleted(3523) || qm.isQuestCompleted(3524) || qm.isQuestCompleted(3525) || qm.isQuestCompleted(3526) || qm.isQuestCompleted(3527) || qm.isQuestCompleted(3529) || qm.isQuestCompleted(3539)) {
                qm.completeQuest();
                qm.sendOk("你现在已经重新拥有所有的记忆了。你现在可以前往 #m270020000#.");
            } else {
                qm.sendOk("你还没有与你的第一位老师确认你的记忆吗？");
            }
        } else if (status == 1) {
            qm.dispose();
        }
    }
}
