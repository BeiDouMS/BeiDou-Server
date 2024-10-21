var status;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) { // END CHAT
        return cm.dispose();
    }

    if (type == 0 && mode == 0) { // PREV
        status--;
    } else {
        status++;
    }

    switch (status) {
        case 0:
            return cm.sendGetText("#b（要说出暗号才能进入。）");
        case 1:
            const mapObj = cm.getWarpMap(925040100);
            if (mapObj.countPlayers() > 0) {
                cm.sendOk("有人已经在前往封印神殿的路上了。");
                return cm.dispose();
            }
            if (cm.getText() == "道可道非常道") {
                if (cm.isQuestStarted(21747) && cm.getQuestProgressInt(21747, 9300351) == 0) {
                    mapObj.resetPQ();
                    mapObj.destroyNPC(1204020);
                    cm.warp(925040100, 0);
                } else {
                    cm.playerMessage(5, "虽然说对了暗号，但有股神秘力量把入口挡住了。");
                }
                return cm.dispose();
            }
            cm.sendOk("#r错误！");
            return cm.dispose();
        default:
            return cm.dispose();
    }
}