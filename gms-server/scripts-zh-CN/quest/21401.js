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
            return qm.sendNext("……问我怎么会变成这样？……本来不太想说的……不是，当然瞒不过主人你了……");
        case 1:
            return qm.sendNextPrev("……你被冰封的数百年间，我也被冰在了冰窟里。那么长的时间，没有主人的陪伴……渐渐的，我的心里便出现了黑暗。")
        case 2:
            return qm.sendNextPrev("不过，你重新苏醒后，我心中的黑暗也跟着完全消失了。既然主人回来了，心里也没有什么可难过的了。本以为这样就没事了……没想到这只是我的错觉。")
        case 3:
            return qm.sendAcceptDecline("拜托了，战神……一定要阻止我。能够阻止我暴走的只有你了。我再也抑制不住内心中的黑暗了！无论如何，一定要#r打败暴走的我#k！")
        case 4:
            if (type == 12 && mode == 0) {
                return qm.dispose();
            }
            var em = qm.getEventManager("MahaBattle");
            if (!em.startInstance(qm.getPlayer())) {
                qm.sendOk("有人在挑战转职副本，请稍后再试。");
            } else {
                qm.startQuest();
            }
        default:
            return qm.dispose();
    }
}

function end(mode, type, selection) {
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
            return qm.sendNext("谢谢你，战神。多亏了你，才阻止了我的暴走。真是万幸……以主人的实力，这点小事当然不在话下了！");
        case 1:
            return qm.sendYesNo("现在来看，你的等级已经很高了。既然能够打倒暴走状态下的我……那么唤醒你过去全部的力量也应该是可以的了。");
        case 2:
            if (type == 1 && mode == 0) {
                return qm.dispose();
            }

            if (!qm.isQuestCompleted(21401)) {
                if (!qm.canHold(1142132)) {
                    qm.sendOk("背包中的#b装备栏#k至少需要一个空位来完成任务。");
                    return qm.dispose();
                }
                if (!qm.canHold(2280003, 1)) {
                    qm.sendOk("背包中的#物品栏#k至少需要一个空位来完成任务。");
                    return qm.dispose();
                }

                qm.gainItem(1142132, true);
                qm.gainItem(2280003, 1);
                qm.changeJobById(2112);

                qm.completeQuest();
            }
            qm.sendOk("沉睡的技能全都唤醒了……毕竟好久没用了，还需要熟悉熟悉。不过，应该进步会很快的。");
        default:
            return qm.dispose();
    }
}