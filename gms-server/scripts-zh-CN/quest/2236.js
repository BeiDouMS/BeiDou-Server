var status = -1;

function start(mode, type, selection) {
    if (mode == -1) {
        qm.sendOk("在你决定是否要进行这个任务之后再跟我说。如果你决定不做，你不会错过任何机会。");
        qm.dispose();
    } else {
        if (mode == 0 && type > 0 || selection == 1) {
            qm.sendOk("在你决定是否要进行这个任务之后再跟我说。如果你决定不做，你不会错过任何机会。");
            qm.dispose();
            return;
        }

        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            qm.sendAcceptDecline("我能感觉到邪恶的力量。它们深藏在地牢里，非常强大。如果我们想驱赶这个地方的邪恶，我们必须把护身符放在地牢里的巫师岩石上。你愿意为我做这件事吗？");
        } else if (status == 1) {
            if (qm.haveItem(4032263)) {
                qm.gainItem(4032263, -6);
            }
            qm.gainItem(4032263, 6);
            qm.sendOk("拿着这些护身符，把它们放在地牢里的巫师岩石上。我给你总共6个护身符。");
            qm.forceStartQuest();
            qm.setQuestProgress(2236, 1,"000000");
            qm.dispose();
        }
    }
}

function end(mode, type, selection) {
    status++;

    if (status == 0) {
        if (qm.getQuestProgress(2236,1) == "111111") { // 检查任务进度
            qm.sendOk("我也感觉到了。巫师岩石的力量开始压倒邪恶的力量。我想睡眠森林现在安全了。邪恶已经被消灭。");
            qm.gainExp(60000 * qm.getPlayer().getExpRate());
            qm.forceCompleteQuest();
        } else {
            if (qm.haveItem(4032263)) {
                qm.gainItem(4032263, -6);
            }
            qm.gainItem(4032263, 6);

            qm.sendOk("哦，不好。我仍然感受到来自内部的不祥预兆。拿着这些护身符，将它们封印在巫师岩石上。我们指望你了。");
            qm.setQuestProgress(2236, 1,"000000");
        }
        qm.dispose();
    }
}
