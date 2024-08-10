/*
        NPC Name:               Nineheart
        Description:            Quest - Are you sure you can leave?
*/

var status = -1;

function start(mode, type, selection) {
    if (mode == -1 || mode == 0 && type > 0) {
        qm.dispose();
        return;
    }

    if (mode == 1) {
        status++;
    } else {
        if (status == 1) {
            qm.sendNext("你什么时候才能意识到自己有多软弱。。。当你在维多利亚岛遇到麻烦时?");
            qm.dispose();
            return;
        }
        status--;
    }
    if (status == 0) {
        qm.sendNext("你终于在训练中成为了一名骑士。我想马上给你一个任务，但你看起来离自己能完成任务还有好几英里远。你确定你能像这样去维多利亚岛吗?");
    } else if (status == 1) {
        qm.sendAcceptDecline("去维多利亚岛由你决定，但是一个在训练中不能在战斗中照顾自己的骑士很可能会损害皇后无可挑剔的名声。作为这个岛上的首席战术家，我不能让这种事发生，周期。我要你继续训练直到时机成熟.");
    } else if (status == 2) {
        qm.forceCompleteQuest();
        qm.sendNext("#p1102000#, 训练教练，将帮助你训练成为一个有用的骑士。一旦你达到13级，我会给你分配一两个任务。所以在那之前，继续训练.");
    } else if (status == 3) {
        qm.sendPrev("哦，你知道如果你和 #p1101001# 交谈, 她会给你祝福吗？祝福对你的旅途一定有帮助.");
    } else if (status == 4) {
        qm.dispose();
    }
}

function end(mode, type, selection) {
    qm.dispose();
}