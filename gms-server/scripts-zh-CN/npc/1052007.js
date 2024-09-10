var status = 0;
var ticketSelection = -1;
var text = "Here's the ticket reader.";
var hasTicket = false;
var NLC = false;
var em;

function start() {
    cm.sendSimple("选择你的目的地。\r\n#L0##b废都广场#l\r\n#L1#进入建筑工地#l\r\n#L2#新叶城#l");
}

function action(mode, type, selection) {
    em = cm.getEventManager("Subway");

    if (mode == -1) {
        cm.dispose();
        return;
    } else if (mode == 0) {
        cm.dispose();
        return;
    } else {
        status++;
    }
    if (status == 1) {
        if (selection == 0) {
            var em = cm.getEventManager("KerningTrain");
            if (!em.startInstance(cm.getPlayer())) {
                cm.sendOk("客运车已经满了。稍后再试一次。");
            }

            cm.dispose();

        } else if (selection == 1) {
            if (cm.haveItem(4031036) || cm.haveItem(4031037) || cm.haveItem(4031038)) {
                text += " You will be brought in immediately. Which ticket you would like to use?#b";
                for (var i = 0; i < 3; i++) {
                    if (cm.haveItem(4031036 + i)) {
                        text += "\r\n#b#L" + (i + 1) + "##t" + (4031036 + i) + "#";
                    }
                }
                cm.sendSimple(text);
                hasTicket = true;
            } else {
                cm.sendOk("看起来你好像没有门票！");
                cm.dispose();

            }
        } else if (selection == 2) {
            if (!cm.haveItem(4031711) && cm.getPlayer().getMapId() == 103000100) {
                cm.sendOk("看起来你没有门票！你可以从贝尔那里买一张。");
                cm.dispose();
                return;
            }
            if (em.getProperty("entry") == "true") {
                cm.sendYesNo("列车已经进站,请出示你的票，这样我就可以让你进去了。乘坐时间不会很长，你会安全到达目的地的。你觉得怎么样？想要乘坐这趟列车吗？");
            } else {
                cm.sendNext("我们将在列车开车前1分钟开始检票进入。进入之后请耐心等待几分钟。请注意，地铁将准时发车，我们将在那之前1分钟停止接收车票，请做好时间管理。");
                cm.dispose();

            }
        }
    } else if (status == 2) {
        if (hasTicket) {
            ticketSelection = selection;
            if (ticketSelection > -1) {
                cm.gainItem(4031035 + ticketSelection, -1);
                cm.warp(103000897 + (ticketSelection * 3), "st00");  // thanks IxianMace for noticing a few scripts having misplaced warp SP's
                hasTicket = false;
                cm.dispose();
                return;
            }
        }

        if (cm.haveItem(4031711)) {
            if (em.getProperty("entry") == "false") {
                cm.sendNext("我们将在列车开车前1分钟开始检票进入。进入之后请耐心等待几分钟。请注意，地铁将准时发车，我们将在那之前1分钟停止接收车票，请做好时间管理。");
            } else {
                cm.gainItem(4031711, -1);
                cm.warp(600010004);
            }

            cm.dispose();

        }
    }
}