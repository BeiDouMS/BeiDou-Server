function start() {
    if (cm.haveItem(4031045)) {
        var em = cm.getEventManager("Boats");
        if (em.getProperty("entry") == "true") {
            cm.sendYesNo("你要去天空之城吗？");
        } else {
            cm.sendOk("前往天空之城的船已经开走了，请耐心等待下一趟航班。");
            cm.dispose();
        }
    } else {
        cm.sendOk("#r请先找售票员购买船票。");
        cm.dispose();
    }
}

function action(mode, type, selection) {
    if (mode <= 0) {
        cm.sendOk("等你想好了再来吧。");
        cm.dispose();
        return;
    }
    var em = cm.getEventManager("Boats");
    if (em.getProperty("entry") == "true") {
        cm.warp(101000301);
        cm.gainItem(4031045, -1);
        cm.dispose();
    } else {
        cm.sendOk("前往天空之城的航班已经停止检票了，请耐心等待下一趟航班。");
        cm.dispose();
    }
}	