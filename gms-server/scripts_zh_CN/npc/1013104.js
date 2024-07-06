function start() {
    if (cm.isQuestStarted(22007)) {
        if (!cm.haveItem(4032451)) {
            cm.gainItem(4032451, true);
            cm.sendNext("#b(你获得了一个蛋。把它交给尤塔。)");
        } else {
            cm.sendNext("#b(你已经获得了一个蛋。拿出你手上的蛋，然后把它交给尤塔。)");
        }
    } else {
        cm.sendNext("#b(你现在不需要拿蛋。)#k");
    }
    cm.dispose();
}