/**
 -- Odin JavaScript --------------------------------------------------------------------------------
 Hak - Cabin <To Mu Lung>(200000141) / Mu Lung Temple(250000100) / Herb Town(251000000)
 -- By ---------------------------------------------------------------------------------------------
 Information
 -- Version Info -----------------------------------------------------------------------------------
 1.1 - Text and statement fix [Information]
 1.0 - First Version by Information
 ---------------------------------------------------------------------------------------------------
 **/

var menu = new Array("武陵", "天空之城", "百草堂");
var cost = new Array(1500, 1500, 500);
var hak;
var slct;
var display = "";
var btwmsg;
var method;


function start() {
    status = -1;
    hak = cm.getEventManager("Hak");
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
            cm.sendNext("改变想法随时跟我搭话吧。");
            cm.dispose();
            return;
        }
        status++;
        if (status == 0) {
            for (var i = 0; i < menu.length; i++) {
                if (cm.getPlayer().getMapId() == 200000141 && i < 1) {
                    display += "\r\n#L" + i + "##b" + menu[i] + "（" + cost[i] + "金币）#k";
                } else if (cm.getPlayer().getMapId() == 250000100 && i > 0 && i < 3) {
                    display += "\r\n#L" + i + "##b" + menu[i] + "（" + cost[i] + "金币）#k";
                }
            }
            if (cm.getPlayer().getMapId() == 200000141 || cm.getPlayer().getMapId() == 251000000) {
                btwmsg = "#b天空之城#k 到 #b武陵#k";
            } else if (cm.getPlayer().getMapId() == 250000100) {
                btwmsg = "#b武陵#k 到 #b天空之城#k";
            }
            if (cm.getPlayer().getMapId() == 251000000) {
                cm.sendYesNo("你好。旅行进行得怎么样？我一直负责载送像你这样的旅行者前往#b" + menu[0] + "#k。虽然没有船那么平稳，但只要抓紧一些，我能比船更快抵达。只要支付#b" + cost[2] + "金币#k，我现在就带你过去。");
                status++;
            } else if (cm.getPlayer().getMapId() == 250000100) {
                cm.sendSimple("怎么样？从 " + btwmsg + " 很快吧？如果你想继续出发，我也可以载你去其他地方。给我一些辛苦钱就行。\r\n" + display);
            } else {
                cm.sendSimple("如果你想从 " + btwmsg + "，给我些辛苦钱就行。我载你过去，比你自己走要快多了。怎么样？\r\n" + display);
            }
        } else if (status == 1) {
            slct = selection;
            cm.sendYesNo("你现在要移动到 #b" + menu[selection] + "#k 吗？如果你有 #b" + cost[selection] + " 金币#k，我现在就带你过去。");
        } else if (status == 2) {
            if (slct == 2) {
                if (cm.getMeso() < cost[2]) {
                    cm.sendNext("你确定你有足够的冒险币吗？");
                    cm.dispose();
                } else {
                    cm.gainMeso(-cost[2]);
                    cm.warp(251000000, 0);
                    cm.dispose();
                }
            } else {
                if (cm.getMeso() < cost[slct]) {
                    cm.sendNext("你确定你有足够的冒险币吗？");
                    cm.dispose();
                } else {
                    if (cm.getPlayer().getMapId() == 251000000) {
                        cm.gainMeso(-cost[2]);
                        cm.warp(250000100, 0);
                        cm.dispose();
                    } else {
                        var em = cm.getEventManager("Hak");
                        if (!em.startInstance(cm.getPlayer())) {
                            cm.sendOk("呃...我们目前接受的冒险岛玩家请求太多了...请稍后再试。");
                            cm.dispose();
                            return;
                        }

                        cm.gainMeso(-cost[slct]);
                        cm.dispose();
                    }
                }
            }
        }
    }
}