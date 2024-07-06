function start() {
    if (cm.haveItem(4031331)) {
        var em = cm.getEventManager("Cabin");
        if (em.getProperty("entry") == "true") {
            cm.sendYesNo("你希望登上这班航班吗？");
        } else {
            cm.sendOk("飞机还没有到达。请尽快回来。");
            cm.dispose();
        }
    } else {
        cm.sendOk("确保你有一张叶尔弗航班的机票。检查你的背包。");
        cm.dispose();
    }
}

function action(mode, type, selection) {
    if (mode <= 0) {
        cm.sendOk("好的，如果你改变主意，就跟我说话！");
        cm.dispose();
        return;
    }

    var em = cm.getEventManager("Cabin");
    if (em.getProperty("entry") == "true") {
        cm.warp(200000132);
        cm.gainItem(4031331, -1);
    } else {
        cm.sendOk("飞机还没有到达。请尽快回来。");
    }
    cm.dispose();
}