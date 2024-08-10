//@author kevintjuh93

var status = -1;

function start(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if (mode > 0) {
            status++;
        } else {
            qm.dispose();
        }
        if (status == 0) {
            qm.sendAcceptDecline("你好, #h0#. 欢迎来到枫树世界。现在是活动季，我们欢迎有礼物的新角色。你现在想要你的礼物吗?");
        } else if (status == 1) {
            qm.sendOk("打开你的库存打开它！这些礼物会让你看起来很时髦。哦，还有一件事！你会在30级得到另一份礼物。祝你好运!");
            qm.forceStartQuest();
            qm.forceCompleteQuest();
            qm.gainItem(2430191, 1, true);
        } else if (status == 2) {
            qm.dispose();
        }
    }
}