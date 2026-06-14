var inMap = [540000000, 550000000, 551000000];

var toMap = [550000000, [551000000, 541000000], 550000000];

var cost = [42000, [10000, 0], 10000];

var toMapSp = [0, [2, 4], 4];

var status = 0;

var location = -1;

var travelCost = 0;

var travelMap = 0;

var travelSp = 0;

var startedTravel = false;



function start() {

    for (var i = 0; i < inMap.length; i++) {

        if (inMap[i] == cm.getPlayer().getMap().getId()) {

            location = i;

            break;

        }

    }

    if (location == -1) {

        cm.sendOk("我是马来西亚旅游向导奥黛丽。这里暂时没有可用的旅行路线。 ");

        cm.dispose();

        return;

    }

    if (cm.getPlayer().getMap().getId() == 540000000) {

        startedTravel = true;

    }

    if (inMap[location] == 550000000) {

        toMap[1][1] = cm.getPlayer().peekSavedLocation("WORLDTOUR");

        if (toMap[1][1] == -1) {

            toMap[1][1] = 541000000;

        }

    }



    var text = startedTravel ? "你好，我是#b#p9201135##k，负责前往#r马来西亚#k的旅行服务。由于你没有通过#b冒险岛旅游中心#k登记特别旅行套餐，本次搭乘费用会比较高。请选择目的地：\r\n" : "你好，我是#b#p9201135##k，#r马来西亚#k旅游向导。你想去哪里？\r\n";

    if (toMap[location] instanceof Array) {

        for (var j = 0; j < toMap[location].length; j++) {

            text += "\r\n#b#L" + j + "##m" + toMap[location][j] + "#" + (cost[location][j] > 0 ? " (" + cm.numberWithCommas(cost[location][j]) + " 金币)" : " (返回原来的地方)") + "#l";

        }

    } else {

        text += "\r\n#b#L0##m" + toMap[location] + "#" + (cost[location] > 0 ? " (" + cm.numberWithCommas(cost[location]) + " 金币)" : "") + "#l";

    }

    cm.sendSimple(text + "#k");

}



function action(mode, type, selection) {

    if (mode == -1) {

        cm.dispose();

        return;

    }

    if (mode == 0) {

        cm.sendNext("如果你需要搭乘旅行路线，随时来找我。祝你旅途愉快！");

        cm.dispose();

        return;

    }

    status++;

    if (status == 1) {

        if (toMap[location] instanceof Array) {

            travelCost = cost[location][selection];

            travelMap = toMap[location][selection];

            travelSp = toMapSp[location][selection];

        } else {

            travelCost = cost[location];

            travelMap = toMap[location];

            travelSp = toMapSp[location];

        }

        if (travelCost > 0) {

            cm.sendYesNo("前往#b#m" + travelMap + "##k需要#r" + cm.numberWithCommas(travelCost) + " 金币#k。现在要出发吗？");

        } else {

            cm.sendNext("准备结束马来西亚之旅了吗？希望你玩得愉快，一路平安！");

        }

    } else if (status == 2) {

        if (cm.getMeso() < travelCost) {

            cm.sendNext("你的金币不够，无法搭乘这条旅行路线。 ");

        } else {

            if (travelCost > 0) {

                cm.gainMeso(-travelCost);

                if (startedTravel) {

                    cm.getPlayer().saveLocation("WORLDTOUR");

                }

            } else {

                travelMap = cm.getPlayer().getSavedLocation("WORLDTOUR");

                if (travelMap == -1) {

                    travelMap = toMap[1][1];

                }

            }

            cm.warp(travelMap, travelSp);

        }

        cm.dispose();

    }

}

