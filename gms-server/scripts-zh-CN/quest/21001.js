/*
    Author : kevintjuh93
*/

var status = -1;

function start(mode, type, selection) {
    status++;
    if (mode != 1) {
        if (type == 15 && mode == 0) {
            qm.sendNext("呜呜……战神拒绝了我的请求！");
            qm.dispose();
            return;
        } else {
            qm.dispose();
            return;
        }
    }
    if (status == 0) {
        qm.sendAcceptDecline("呜……我好害怕……请带我去见赫丽娜姐姐。");
    } else if (status == 1) {
        qm.gainItem(4001271, 1);
        qm.forceStartQuest();
        qm.warp(914000300, 0);
        qm.dispose();
    }
}

function end(mode, type, selection) {
    status++;
    if (mode != 1) {
        if (type == 1 && mode == 0) {
            qm.sendNext("那个孩子呢？请把孩子交给我！");
        }

        qm.dispose();
        return;
    }

    if (status == 0) {
        qm.sendYesNo("你平安回来了！那个孩子呢？！你把孩子带回来了吗？！");
    } else if (status == 1) {
        qm.sendNext("太好了，真是太好了……", 9);
    } else if (status == 2) {
        qm.sendNextPrev("快上船！我们没有多少时间了！", 3);
    } else if (status == 3) {
        qm.sendNextPrev("不能再耽搁了。黑魔法师的军队越来越近，如果现在不离开，我们就完了！", 9);
    } else if (status == 4) {
        qm.sendNextPrev("现在就走！", 3);
    } else if (status == 5) {
        qm.sendNextPrev("战神，拜托了！我知道你想留下来和黑魔法师战斗，但已经太晚了！把战斗交给其他英雄，和我们一起去金银岛吧！", 9);
    } else if (status == 6) {
        qm.sendNextPrev("不，我不能走！", 3);
    } else if (status == 7) {
        qm.sendNextPrev("赫丽娜，你先去金银岛吧。我保证以后一定会去找你。我不会有事的。我必须和其他英雄一起迎战黑魔法师！", 3);
    } else if (status == 8) {
        qm.gainItem(4001271, -1);
        qm.removeEquipFromSlot(-11);
        qm.forceCompleteQuest();

        qm.warp(914090010, 0); // Initialize Aran Tutorial Scenes
        qm.dispose();
    }
}
