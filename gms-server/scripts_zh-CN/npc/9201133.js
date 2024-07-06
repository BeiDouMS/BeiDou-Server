var map = 677000010;
var quest = 28283;
var status = -1;
var inHuntingGround;

function start(mode, type, selection) {
    inHuntingGround = (cm.getMapId() >= 677000010 && cm.getMapId() <= 677000012);
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        cm.dispose();
        return;
    }
    if (status == 0) {
        if (!inHuntingGround) {
            if (cm.isQuestStarted(quest)) {
                if (!cm.getPlayer().haveItemEquipped(1003036)) {
                    cm.sendOk("前方的道路有一股奇怪的气味... 进入之前装备 #rgas mask#k。");
                    cm.dispose();
                    return;
                }

                cm.sendYesNo("你想要移动到 #b#m" + map + "##k 吗？");
            } else {
                cm.sendOk("入口被一股奇怪的力量阻挡住了。");
                cm.dispose();
            }
        } else {
            if (cm.getMapId() == 677000011) {
                map = 677000012;
                cm.sendYesNo("你想要移动到 #b#m" + map + "##k 吗？");
            } else {
                map = 105050400;
                cm.sendYesNo("你想要#b离开这个地方#k吗？");
            }
        }
    } else {
        cm.warp(map, 0);
        cm.dispose();
    }
}