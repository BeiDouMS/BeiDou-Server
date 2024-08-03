function start() {
    if (cm.haveItem(4031074)) {
        var em = cm.getEventManager("Trains");
        if (em.getProperty("entry") == "true") {
            cm.sendYesNo("你想去玩具城吗？");
        } else {
            cm.sendOk("到玩具城的火车已经开出，请耐心等待下一班。");
            cm.dispose();
        }
    } else {
        cm.sendOk("确保你有前往玩具城的车票才能乘坐这趟火车。检查你的背包。");
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
        cm.warp(200000122);
        cm.gainItem(4031074, -1);
        cm.dispose();
    } else {
        cm.sendOk("去玩具城的火车已经准备好了，下一班请耐心等候。");
        cm.dispose();
    }
}