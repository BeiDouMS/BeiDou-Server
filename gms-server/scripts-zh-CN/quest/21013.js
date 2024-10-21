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
            qm.sendSimple("英、英雄大人………我一直都很想见你。\r\n#b#L2#（做腼腆状。）#l");
            break;
        case 1:
            if (type == 4 && mode == 0) { // END CHAT
                qm.dispose();
                return;
            }
            qm.sendAcceptDecline("我从很久以前就想送英雄大人一件礼物……既然今天遇见了英雄，不知英雄能否赏脸收下我这份薄礼？");
            break;
        case 2:
            if (type == 12 && mode == 0) { // DECLINE
                qm.sendNext("对于英雄大人而言肯定是有帮助的。请你一定收下。");
                qm.dispose();
                break;
            }
            // YES
            if (!qm.isQuestStarted(21013)) {
                qm.forceStartQuest();
            }
            qm.sendNext("制作礼物的材料放在这附近的箱子里了。劳烦英雄大人找到这个箱子，把#b一段竹子#k和#b木材#k带来给我。然后我就能立刻把礼物做好。", 1);
            break;
        case 3:
            qm.guideHint(18);
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
            qm.sendNext("材料都拿来了吗？请稍等。这么混合一样……\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0#\r\n#v3010062# #t3010062# 1个\r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0# 95 exp");
            break;
        case 1:
            if (!qm.isQuestCompleted(21013)) {
                qm.forceCompleteQuest();
                qm.gainExp(95);
                qm.gainItem(4032309, -1);
                qm.gainItem(4032310, -1);
                qm.gainItem(3010062, 1);
            }
            qm.sendNextPrev("好了，椅子做好了！嘿嘿！就算是英雄肯定也会有需要歇歇的时候，所以我一直想送你一把椅子。", 1);
            break;
        case 2:
            qm.sendNextPrev("我想就算是英雄也不能永远活力充沛，肯定也有疲劳、困倦的时候。但真正的英雄是能够克服万难取得最后胜利的。", 1);
            break;
        case 3:
            qm.guideHint(19);
            qm.dispose();
            break;
        default:
            qm.dispose();
            break;
    }
}