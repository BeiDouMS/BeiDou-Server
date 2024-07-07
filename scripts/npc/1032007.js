var status = 0;
var cost = 5000;

function start() {
    cm.sendYesNo("你好，我负责出售前往天空之城的船票。前往天空之城的航班从整点开始每15分钟有一趟，船票售价 #b" + cost + " 金币#k。你要购买 #b#t4031045##k吗？");
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0) {
            cm.sendNext("等你想好了再来吧。");
            cm.dispose();
            return;
        }
        status++;
        if (status == 1) {
            if (cm.getMeso() >= cost && cm.canHold(4031045)) {
                cm.gainItem(4031045, 1);
                cm.gainMeso(-cost);
                cm.dispose();
            } else {
                cm.sendOk("#r你的金币不足或者其它栏满了");
                cm.dispose();
            }
        }
    }
}
