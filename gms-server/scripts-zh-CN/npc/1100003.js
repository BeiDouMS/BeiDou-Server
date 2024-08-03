/**
 ----------------------------------------------------------------------------------
 Skyferry Between Victoria Island, Ereve and Orbis.

 1100003 Kiriru (To Victoria Island From Ereve)

 Credits to: MapleSanta
 ----------------------------------------------------------------------------------
 **/

var menu = new Array("Victoria Island");
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
            cm.sendNext("如果你不感兴趣，那就算了...");
            cm.dispose();
            return;
        }
        status++;
        if (status == 0) {
            var display = "";
            for (var i = 0; i < menu.length; i++) {
                display += "\r\n#L" + i + "##b Victoria Island (1000 mesos)#k";
            }
            cm.sendNext("嗯，你好...又来了。你想离开圣地去别的地方吗？如果是的话，你来对地方了。我经营着一艘渡船，从#b圣地#k到#b金银岛#k，如果你愿意的话，我可以带你去#b金银岛#k...你需要支付#b1000#k金币的费用。\r\n");
        } else if (status == 1) {
            if (cm.getMeso() < 1000) {
                cm.sendNext("嗯... 你确定你有 #b1000#k 冒险币吗？检查一下你的背包，确保你有足够的冒险币。你必须支付费用，否则我不能让你上船...");
                cm.dispose();
            } else {
                cm.gainMeso(-1000);
                cm.warp(200090031);
                cm.dispose();
            }
        }
    }
}