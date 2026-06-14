var status;

function playerNearby(chrpos, portalpos) {
    try {
        return Math.sqrt(Math.pow((portalpos.getX() - chrpos.getX()), 2) + Math.pow((portalpos.getY() - chrpos.getY()), 2)) < 77;
    } catch (err) {
        return false;
    }
}

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            if (playerNearby(cm.getPlayer().getPosition(), cm.getMap().getPortal("chimney01").getPosition())) {
                cm.sendOk("嘿嘿，不要偷偷钻进别人家的烟囱哦。要是被记在圣诞老人的淘气名单上，今年的礼物可就危险了。");
            } else {
                cm.sendOk("呵呵呵，愿你在新的一年里健康、顺利，也收获满满的幸福。");
            }
        } else {
            cm.dispose();
        }
    }
}
