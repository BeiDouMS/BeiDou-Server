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
                cm.sendOk("嘿，嘿~~请不要未经允许潜入别人家里，你可不想今年在圣诞老人的名单上被列为调皮的吧？");
            } else {
                cm.sendOk("嘿嘿嘿~~你有一个充满健康、实现和幸福的美好一年！");
            }
        } else {
            cm.dispose();
        }
    }
}