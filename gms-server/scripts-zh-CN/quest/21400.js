var status = -1;

function start(mode, type, selection) {
    if (mode == -1) { // END CHAT
        return qm.dispose();
    }

    if (type == 0 && mode == 0) { // PREV
        status--;
    } else {
        status++;
    }

    switch (status) {
        case 0:
            return qm.sendAcceptDecline("最近有没有好好修炼？虽然知道你很忙，不过还是请你回一趟#b#m140000000##k。#b#p1201002##k又出现了奇怪的反应……很奇怪，这次的反应和上次不一样，有点更深厚、更沉重……的感觉。");
        case 1:
            if (type == 12 && mode == 0) {
                qm.sendOk("我不是在开玩笑！真的很奇怪……#p1201002#肯定是出了什么事了！");
                return qm.dispose();
            }
            qm.forceStartQuest();
            qm.sendOk("有种不祥的预感……请速速回去。虽然我从来没有见过#p1201002#，也没听过它的声音……不过我可以感觉到它的痛苦。#b只有#p1201002#的主人，你才能解决它的问题#k！");
        default:
            return qm.dispose();
    }
}