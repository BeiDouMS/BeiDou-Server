var status = -1;

function start(mode, type, selection) {
    if (mode == -1) { // END CHAT
        qm.dispose();
        return;
    }

    if (type == 0 && mode == 0) { // PREV
        status--;
    } else {
        status++;
    }

    switch (status) {
        case 0:
            qm.sendAcceptDecline("呃呃……吓死我了……快，快带到赫丽娜那边去！");
            break;
        case 1:
            if (type == 12 && mode == 0) { // DECLINE
                qm.sendNext("啊！战神大人拒绝了！");
                qm.dispose();
                break;
            }
            // ACCEPT
            qm.gainItem(4001271, 1);
            qm.forceStartQuest();
            qm.warp(914000300, 0);
            qm.dispose();
            break;
        default:
            qm.dispose();
            break;
    }
}

function end(mode, type, selection) {
    if (mode == -1) { // END CHAT
        qm.dispose();
        return;
    }

    if (type == 0 && mode == 0) { // PREV
        status--;
    } else {
        status++;
    }

    switch (status) {
        case 0:
            qm.sendYesNo("呵呵，平安回来了？孩子呢？孩子也带回来了吗？");
            break;
        case 1:
            if (type == 1 && mode == 0) { // NO
                qm.sendNext("孩子呢？孩子救出来了的话，就赶紧让我们看看。");
                qm.dispose();
                break;
            }
            // YES
            qm.sendNext("太好了……真是太好了。", 9);
            break;
        case 2:
            qm.sendNextPrev("赶快上船！已经没时间了！", 3);
            break;
        case 3:
            qm.sendNextPrev("啊，没错。现在不是感伤的时候。黑魔法师的气息越来越近！似乎他们已经察觉方舟的位置，得赶紧启航，不然就来不及了！", 9);
            break;
        case 4:
            qm.sendNextPrev("立刻出发！", 3);
            break;
        case 5:
            qm.sendNextPrev("战神！请你也上船吧！我们理解你渴望战斗的心情……不过，现在已经晚了！战斗就交给你的那些同伴吧，和我们一起去金银岛吧！", 9);
            break;
        case 6:
            qm.sendNextPrev("不行！", 3);
            break;
        case 7:
            qm.sendNextPrev("赫丽娜，你先出发去金银岛。一定要活着，我们一定会再见的。我要和同伴们一起同黑魔法师战斗！", 3);
            break;
        case 8:
            qm.gainItem(4001271, -1);
            qm.removeEquipFromSlot(-11);
            qm.forceCompleteQuest();

            qm.warp(914090010, 0); // Initialize Aran Tutorial Scenes
            qm.dispose();
            break;
        default:
            qm.dispose();
            break;
    }
}