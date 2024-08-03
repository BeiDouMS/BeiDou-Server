/**
 ----------------------------------------------------------------------------------
 Skyferry Between Victoria Island, Ereve and Orbis.

 1100007 Kiriru (Victoria Island Station to Ereve)

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
            cm.sendNext("如果你不感兴趣，那就算了...");
            cm.dispose();
            return;
        }
        status++;
        if (status == 0) {
            var display = "";
            for (var i = 0; i < menu.length; i++) {
                display += "\r\n#L" + i + "##b Ereve (1000 mesos)#k";
            }
            cm.sendNext("嗯...那么...嗯...你是想离开金银岛去其他地区吗？你可以乘这艘船去#b圣地#k。在那里，你会看到明亮的阳光照在树叶上，感受到轻柔的微风拂过你的皮肤。那里是神兽和女皇所在的地方。你想去圣地吗？大约需要#b2分钟#k，费用是#b1000#k金币。\r\n");

        } else if (status == 1) {
            if (cm.getMeso() < 1000) {
                cm.sendNext("嗯... 你确定你有 #b1000#k 冒险币吗？检查一下你的背包，确保你有足够的冒险币。你必须支付费用，否则我不能让你上船...");
                cm.dispose();
            } else {
                cm.gainMeso(-1000);
                cm.warp(200090030);
                cm.dispose();
            }
        }
    }
}