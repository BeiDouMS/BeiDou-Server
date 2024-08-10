var status = -1;

function start(mode, type, selection) {
    if (mode == -1 || mode == 0 && type > 0) {
        qm.dispose();
        return;
    }

    if (mode == 1) {
        status++;
    } else {
        if (status == 2) {
            qm.dispose();
            return;
        }
        status--;
    }
    if (status == 0) {
    	qm.sendNext("莫非你是前不久在#m101000000#的那个人？终于找到你了！我找你找得好辛苦，知道吗？", 8);
    } else if (status == 1) {
    	qm.sendNextPrev("你是谁？", 2);
    } else if (status == 2) {
    	qm.sendAcceptDecline("我？你想知道的话就来我的洞窟吧。我想好好招待你一番。点击接受按钮就能立刻移动到我家。我在那里等你。");
    } else if (status == 3) {
        qm.forceCompleteQuest();
        qm.warp(910510200, 0);
        qm.dispose();
    }
}

function end(mode, type, selection) {
    qm.dispose();
}