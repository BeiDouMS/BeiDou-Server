/**
 ----------------------------------------------------------------------------------
 Whale Between Lith harbor and Rien.

 1200003 Puro

 Credits to: MapleSanta
 ----------------------------------------------------------------------------------
 **/

var menu = new Array("Lith Harboor");
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
                display += "\r\n#L" + i + "##b Lith Harbor (800 mesos)#k";
            }
            cm.sendSimple("你想离开里恩吗？登上这艘船，我会带你从里恩到明斯港，然后再返回。费用是800金币。你现在想去明斯港吗？大约需要一分钟才能到达那里。");

        } else if (status == 1) {
            if (cm.getMeso() < 800) {
                cm.sendNext("嗯... 你确定你有 #b800#k 冒险币吗？检查一下你的背包，确保你有足够的冒险币。你必须支付费用，否则我不能让你上船...");
                cm.dispose();
            } else {
                cm.gainMeso(-800);
                cm.warp(200090070);
                cm.dispose();
            }

        }
    }
}