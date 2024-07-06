var status = -1;

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 0 && type == 0) {
        status--;
    } else if (mode == -1) {
        cm.dispose();
        return;
    } else {
        status++;
    }
    if (status == 0) {
        cm.sendNext("你，注定成为龙之大师的人……你终于来了。");
    } else if (status == 1) {
        cm.sendNextPrev("去履行你作为龙之主的职责吧...");
    } else if (status == 2) {
        cm.warp(900090101, 0);
        cm.dispose();
    }
}