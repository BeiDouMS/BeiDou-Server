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

var menu = ["Mu Lung", "Orbis", "Herb Town", "Mu Lung"];
var cost = [1500, 1500, 500, 1500];
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
            cm.sendNext("好的。如果你改变主意了，请告诉我。");
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
                btwmsg = "#bOrbis#k to #bMu Lung#k";
            } else if (cm.getPlayer().getMapId() == 250000100) {
                btwmsg = "#bMu Lung#k to #bOrbis#k";
            }
            if (cm.getPlayer().getMapId() == 251000000) {
                cm.sendYesNo("你好。旅行进行得怎么样？我一直在像你这样的旅行者运送到#b" + menu[3] + "#k，而且……你有兴趣吗？这种方式没有船稳定，所以你得紧紧抓住，但我可以比船快得多地到达那里。只要你支付#b" + cost[2] + "金币#k，我就会带你去那里。");
                status++;
            } else if (cm.getPlayer().getMapId() == 250000100) {
                cm.sendSimple("你好。旅行进行得如何？我知道用两条腿走路比我这样可以在天空中航行的人要困难得多。我一直在短时间内将像你这样的旅行者运送到其他地区，你有兴趣吗？如果是的话，请选择你想前往的城镇。");
            } else {
                cm.sendSimple("你好。旅行进行得怎么样？我一直在将像你这样的旅行者迅速运送到其他地区，你有兴趣吗？如果是的话，请选择你想前往的城镇。");
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