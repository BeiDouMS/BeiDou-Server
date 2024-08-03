/**
 ----------------------------------------------------------------------------------
 Skyferry Between Victoria Island, Ereve and Orbis.

 1100008 Kiru (Orbis Station)

 Credits to: MapleSanta
 ----------------------------------------------------------------------------------
 **/

var menu = new Array("Ereve");
var method;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();

    } else {
        if (mode == 0 && status == 0) {
            cm.dispose();
            return;
        } else if (mode == 0) {
            cm.sendNext("好的。如果你改变主意了，请告诉我。");
            cm.dispose();
            return;
        }
        status++;
        if (status == 0) {
            var display = "";
            for (var i = 0; i < menu.length; i++) {
                display += "\r\n#L" + i + "##b Ereve (1000 mesos)#k";
            }
            cm.sendNext("这艘船将驶向#b圣地#k，那里是一个浮空的岛屿，你会看到明亮的阳光照在树叶上，感受到轻柔的微风拂过你的皮肤，还有女皇——希纳斯。如果你有兴趣加入皇家骑士团，那么你一定要来这里看看。你有兴趣去圣地吗？这次旅行将花费你#b1000#k金币\r\n");

        } else if (status == 1) {
            if (cm.getMeso() < 1000) {
                cm.sendNext("嗯... 你确定你有 #b1000#k 冒险币吗？检查一下你的背包，确保你有足够的冒险币。你必须支付费用，否则我不能让你上船...");
                cm.dispose();
            } else {
                cm.gainMeso(-1000);
                cm.warp(200090020);
                cm.dispose();
            }
        }
    }
}