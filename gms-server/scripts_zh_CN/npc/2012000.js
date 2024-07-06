var ticket = [4031047, 4031074, 4031331, 4031576];
var cost = [5000, 6000, 30000, 6000];
var mapNames = ["Ellinia of Victoria Island", "Ludibrium", "Leafre", "Ariant"];
var mapName2 = ["Ellinia of Victoria Island", "Ludibrium", "Leafre of Minar Forest", "Nihal Desert"];
var select;
var status = 0;

function start() {
    var where = "Hello, I'm in charge of selling tickets for the ship ride for every destination. Which ticket would you like to purchase?";
    for (var i = 0; i < ticket.length; i++) {
        where += "\r\n#L" + i + "##b" + mapNames[i] + "#k#l";
    }
    cm.sendSimple(where);
}

function action(mode, type, selection) {
    if (mode < 1) {
        cm.dispose();
    } else {
        status++;
        if (status == 1) {
            select = selection;
            cm.sendYesNo("到" + mapName2[select] + "的车程每隔" + (select == 0 ? 15 : 10) + "分钟一班，从整点开始，费用为#b" + cost[select] + "金币#k。你确定要购买#b#t" + ticket[select] + "##k吗？");
        } else if (status == 2) {
            if (cm.getMeso() < cost[select] || !cm.canHold(ticket[select])) {
                cm.sendOk("你确定你有 #b" + cost[select] + " 冒险币#k 吗？如果是的话，我建议你检查一下你的杂项物品栏，看看是不是已经满了。");
            } else {
                cm.gainMeso(-cost[select]);
                cm.gainItem(ticket[select], 1);
            }
            cm.dispose();
        }
    }
}