function start() {
    if (cm.c.getPlayer().getMapId() == 103000100 || cm.c.getPlayer().getMapId() == 600010001) {
        cm.sendYesNo("每分钟都有前往" + (cm.c.getPlayer().getMapId() == 103000100 ? "新叶城（Masteria）" : "金银岛（Victoria Island）的（Kerning City）废弃都市") + "的地铁出发，票价为#b5000金币#k。您确定要购买#b#t" + (4031711 + parseInt(cm.c.getPlayer().getMapId() / 300000000)) + "##k吗？");
    } else if (cm.c.getPlayer().getMapId() == 600010002 || cm.c.getPlayer().getMapId() == 600010004) {
        cm.sendYesNo("列车启动前你想离开吗？车票将不会退款。");
    }
}

function action(mode, type, selection) {
    if (mode != 1) {
        cm.dispose();
        return;
    }
    if (cm.c.getPlayer().getMapId() == 103000100 || cm.c.getPlayer().getMapId() == 600010001) {
        var item = 4031711 + parseInt(cm.c.getPlayer().getMapId() / 300000000);

        if (!cm.canHold(item)) {
            cm.sendNext("你没有可用的ETC空余的栏位。");
        } else if (cm.getMeso() >= 5000) {
            cm.gainMeso(-5000);
            cm.gainItem(item, 1);
            cm.sendNext("请收好您的票。");
        } else {
            cm.sendNext("你没有足够的金币。");
        }
    } else {
        cm.warp(cm.c.getPlayer().getMapId() == 600010002 ? 600010001 : 103000100);
    }
    cm.dispose();
}