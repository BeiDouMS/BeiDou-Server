var status = 0;
var maps = [104000000, 102000000, 100000000, 101000000, 103000000];
var cost = [1000, 1000, 1000, 800, 1000];
var selectedMap = -1;

function start() {
    cm.sendNext("你好，我是驾驶诺特勒斯中型出租车的司机。如果你想安全快速地从一个城镇到另一个城镇，那就乘坐我们的出租车吧。我们很乐意以实惠的价格带你到达目的地。");
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (status == 1 && mode == 0) {
            cm.dispose();
            return;
        } else if (status >= 2 && mode == 0) {
            cm.sendNext("这个城镇有很多值得一看的地方。当你需要去另一个城镇的时候，回来找我们吧。");
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 1) {
            var selStr = "";
            if (cm.getJobId() == 0) {
                selStr += "We have a special 90% discount for beginners.";
            }
            selStr += "Choose your destination, for fees will change from place to place.#b";
            for (var i = 0; i < maps.length; i++) {
                selStr += "\r\n#L" + i + "##m" + maps[i] + "# (" + (cm.getJobId() == 0 ? cost[i] / 10 : cost[i]) + " mesos)#l";
            }
            cm.sendSimple(selStr);
        } else if (status == 2) {
            cm.sendYesNo("你在这里没有其他事情要做了，是吗？你真的想去#b#m" + maps[selection] + "##k吗？这将花费你#b" + (cm.getJobId() == 0 ? cost[selection] / 10 : cost[selection]) + "金币#k。");
            selectedMap = selection;
        } else if (status == 3) {
            if (cm.getJobId() == 0) {
                mesos = cost[selectedMap] / 10;
            } else {
                mesos = cost[selectedMap];
            }

            if (cm.getMeso() < mesos) {
                cm.sendNext("你没有足够的金币。很抱歉，但没有金币的话，你将无法搭乘出租车。");
                cm.dispose();
                return;
            }

            cm.gainMeso(-mesos);
            cm.warp(maps[selectedMap], 0);
            cm.dispose();
        }
    }
}