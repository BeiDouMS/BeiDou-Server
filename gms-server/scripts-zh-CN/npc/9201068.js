status = -1;
close = false;
oldSelection = -1;
var em;

function start() {
    em = cm.getEventManager("Subway");
    var text = "这是检票口.";
    var hasTicket = false;
    if (cm.haveItem(4031713) && cm.getPlayer().getMapId() == 600010001) {
        text += "\r\n#b#L0##t4031713#";
        hasTicket = true;
    }
    if (!hasTicket) {
        cm.sendOk("看起来你没有门票！你可以从贝尔那里买一张。");
        cm.dispose();
    } else {
        cm.sendSimple(text);
    }
}

function action(mode, type, selection) {
    status++;
    if (mode != 1) {
        if (mode == 0) {
            cm.sendNext("你一定是有一些事情要在这里处理，对吧？");
        }
        cm.dispose();
        return;
    }
    if (status == 0) {
        if (selection == 0) {
            if (em.getProperty("entry") == "true") {
                cm.sendYesNo("列车已经进站。请出示你的票，这样我就可以让你进去了。列车运行时间不会很长，你会安全到达目的地的。你觉得怎么样？想要坐这趟列车吗？");
            } else {
                cm.sendNext("我们将在列车开车前1分钟开始检票进入。进入之后请耐心等待几分钟。请注意，地铁将准时发车，我们将在那之前1分钟停止接收车票，请做好时间管理。");
                cm.dispose();
            }
        }
        oldSelection = selection;
    } else if (status == 1) {
        if (oldSelection == 0 && cm.haveItem(4031713)) {
            if (em.getProperty("entry") == "true") {
                cm.gainItem(4031713, -1);
                cm.warp(600010002);
            } else {
                cm.sendNext("我们将在列车开车前1分钟开始检票进入。进入之后请耐心等待几分钟。请注意，地铁将准时发车，我们将在那之前1分钟停止接收车票，请做好时间管理。");
            }
        } else {
            cm.sendNext("抱歉，您需要一张门票才能进入！");
        }

        cm.dispose();
    }
}