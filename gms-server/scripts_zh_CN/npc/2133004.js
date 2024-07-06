var status = -1;

function start() {
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
            if (!cm.haveItem(4001163) || !cm.isEventLeader()) {
                cm.sendYesNo("让你的队长在这里给我看魔法紫石。\r\n\r\n或者你想要#r离开这片森林#k吗？现在离开意味着抛弃你的伙伴，记住这一点。");
            } else {
                cm.sendNext("太好了，你有了紫色魔法石。我会带你们去通往石头祭坛的路。跟我来吧。");
            }
        } else if (status == 1) {
            if (!cm.haveItem(4001163)) {
                cm.warp(930000800, 0);
            } else {
                cm.getEventInstance().warpEventTeam(930000600);
            }

            cm.dispose();
        }
    }
}