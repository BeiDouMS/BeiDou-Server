var status = 0;
var cost = 5000;

function start() {
    cm.sendYesNo("你好，我负责出售前往神秘岛-天空之城的船票。前往奥比斯的船每15分钟出发一次，从整点开始，票价为#b" + cost + "金币#k。你确定要购买#b#t4031045##k吗？");
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0) {
            cm.sendNext("你一定是有一些事情要在这里处理，对吧？");
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
                cm.sendOk("你确定你有 #b" + cost + " 冒险币#k 吗？如果是的话，请检查你的其它物品栏，看看是否已经满了。");
                cm.dispose();
            }
        }
    }
}