var status;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1 || (mode == 0 && status == 0)) {
        cm.dispose();
        return;
    } else if (mode == 0) {
        status--;
    } else {
        status++;
    }


    if (status == 0) {
        if (cm.isQuestStarted(21728)) {
            cm.sendOk("你寻找着线索，试图找到操纵者的蛛丝马迹，但似乎有一股强大的力量挡住了你的去路……最好返回找 #b#p1061019##k。");
            cm.setQuestProgress(21728, 21761, 0);
            cm.dispose();
            return;
        }

        cm.sendGetText("一个可疑的声音打破了沉默。 #b暗号#k ！");
    } else if (status == 1) {
        if (cm.getText() == "弗朗西斯是天才傀儡师！") {
            if (cm.isQuestStarted(20730) && cm.getQuestProgressInt(20730, 9300285) == 0) {
                cm.warp(910510001, 1);
            } else if (cm.isQuestStarted(21731) && cm.getQuestProgressInt(21731, 9300346) == 0) {
                cm.warp(910510001, 1);
            } else {
                cm.playerMessage(5, "尽管你说出了正确的答案，但一些神秘的力量正在阻挡进入的道路。");
            }

            cm.dispose();
        } else {
            cm.sendOk("#r错误！");
        }
    } else if (status == 2) {
        cm.dispose();
    }
}