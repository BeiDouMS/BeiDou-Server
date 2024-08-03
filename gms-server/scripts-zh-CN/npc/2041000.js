function start() {
    if (cm.haveItem(4031045)) {
        var em = cm.getEventManager("Trains");
        if (em.getProperty("entry") == "true") {
            cm.sendYesNo("你想去天空之城吗？");
        } else {
            cm.sendOk("飞往天空之城的火车已经启程，请耐心等待下一班。");
            cm.dispose();
        }
    } else {
        cm.sendOk("确保你有一张飞往天空之城的车票才能乘坐这列火车。检查你的物品栏。");
        cm.dispose();
    }
}

function action(mode, type, selection) {
    if (mode <= 0) {
        cm.sendOk("好的，如果你改变主意，就跟我说话！");
        cm.dispose();
        return;
    }

    var em = cm.getEventManager("Trains");
    if (em.getProperty("entry") == "true") {
        cm.warp(220000111);
        cm.gainItem(4031045, -1);
        cm.dispose();
    } else {
        cm.sendOk("飞往天空之城的火车已经准备好了，下一班请耐心等候。");
        cm.dispose();
    }
}