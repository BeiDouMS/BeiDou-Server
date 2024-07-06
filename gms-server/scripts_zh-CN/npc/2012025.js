function start() {
    if (cm.haveItem(4031576)) {
        var em = cm.getEventManager("Genie");
        if (em.getProperty("entry") == "true") {
            cm.sendYesNo("这将不是一次短途飞行，所以你需要先处理一些事情，我建议你在登机前先做好这些。你还想要登上魔灯吗？");
        } else {
            cm.sendOk("这个精灵正在准备起飞。很抱歉，你将不得不等下一班车。乘车时间表可通过售票亭的导游获取。");
            cm.dispose();
        }
    } else {
        cm.sendOk("确保你有阿里安特的船票才能在这个魔灯上旅行。检查你的背包。");
        cm.dispose();
    }
}

function action(mode, type, selection) {
    if (mode <= 0) {
        cm.sendOk("好的，如果你改变主意，就跟我说话！");
        cm.dispose();
        return;
    }

    var em = cm.getEventManager("Genie");
    if (em.getProperty("entry") == "true") {
        cm.warp(200000152);
        cm.gainItem(4031576, -1);
    } else {
        cm.sendOk("这个精灵正在准备起飞。很抱歉，你得等下一班车。乘车时间表可以通过售票亭的导游获取。");
    }

    cm.dispose();
}