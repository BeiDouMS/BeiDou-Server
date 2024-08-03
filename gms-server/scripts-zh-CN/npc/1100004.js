/**
 ----------------------------------------------------------------------------------
 Skyferry Between Victoria Island, Ereve and Orbis.

 1100004 Kiru (To Orbis)

 Credits to: MapleSanta
 ----------------------------------------------------------------------------------
 **/
var menu = new Array("Orbis");
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
                display += "\r\n#L" + i + "##b Orbis (1000 mesos)#k";
            }
            cm.sendNext("嗯...风势正好。你是不是想离开圣地去别的地方？这艘渡船开往神秘岛的天空之城。你在圣地需要办的事情都处理好了吗？如果你正好要去#b天空之城#k，我可以带你去那里。你怎么样？要去天空之城吗？\r\n");

        } else if (status == 1) {
            if (cm.getMeso() < 1000) {
                cm.sendNext("嗯... 你确定你有 #b1000#k 冒险币吗？检查一下你的背包，确保你有足够的冒险币。你必须支付费用，否则我不能让你上船...");
                cm.dispose();
            } else {
                cm.gainMeso(-1000);
                cm.warp(200090021);
                cm.dispose();
            }
        }
    }
}