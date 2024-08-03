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

var menu = new Array("桃花仙境", "天空之城", "灵药幻境", "桃花仙境");
var cost = new Array(1500, 1500, 1500, 1500);
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
                    display += "\r\n#L" + i + "##b" + menu[i] + "(" + cost[i] + " mesos)#k";
                } else if (cm.getPlayer().getMapId() == 250000100 && i > 0 && i < 3) {
                    display += "\r\n#L" + i + "##b" + menu[i] + "(" + cost[i] + " mesos)#k";
                }
            }
            if (cm.getPlayer().getMapId() == 200000141 || cm.getPlayer().getMapId() == 251000000) {
                btwmsg = "#b天空之城#k 到 #b桃花仙境#k";
            } else if (cm.getPlayer().getMapId() == 250000100) {
                btwmsg = "#b桃花仙境#k 到 #b天空之城#k";
            }
            if (cm.getPlayer().getMapId() == 251000000) {
                cm.sendYesNo("你好。旅行进行得怎么样？我一直在像你这样的旅行者运送到#b" + menu[3] + "#k，而且……你有兴趣吗？这种方式没有船稳定，所以你得紧紧抓住，但我可以比船快得多地到达那里。只要你支付#b" + cost[3] + "金币#k，我就会带你去那里。");
                status++;
            } else if (cm.getPlayer().getMapId() == 250000100) {
                cm.sendSimple("怎么样？我从 " + btwmsg + "再到现在。我的速度很快的吧，如果你想返回 #b" + menu[1] + "#k，那么我们就立刻出发，不过还是得给我一些辛苦钱，价格是 #b" + cost[3] + " 金币#k。" + display);
            } else {
                cm.sendSimple("如果想从 " + btwmsg + "去的话。给我些辛苦钱就送你。我送你比起你走着去快多了。怎么样？\r\n" + display);
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