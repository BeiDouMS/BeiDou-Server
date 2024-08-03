/**
 ----------------------------------------------------------------------------------
 Whale Between Lith harbor and Rien.

 1200004 Puro

 Credits to: MapleSanta
 ----------------------------------------------------------------------------------
 **/

var menu = new Array("Rien");
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
                display += "\r\n#L" + i + "##b Rien (800 mesos)#k";
            }
            cm.sendNext("你考虑离开金银岛前往我们的城镇吗？如果你登上这艘船，我可以带你从#b明珠港#k到#b里恩#k，然后再返回。但你必须支付#b800#k金币的费用。你想去利恩吗？");

        } else if (status == 1) {
            if (cm.getMeso() < 800) {
                cm.sendNext("嗯... 你确定你有 #b800#k 金币吗？检查一下你的背包，确保你有足够的金币。你必须支付费用，否则我不能让你上船...");
                cm.dispose();
            } else {
                cm.gainMeso(-800);
                cm.warp(200090060);
                cm.dispose();
            }
        }
    }
}