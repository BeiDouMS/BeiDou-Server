var status = -1;

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
        return;
    }

    if (mode == 0) {
        if (status == 0) {
            cm.sendOk("你要拒绝我的邀请吗？那真是……太可惜了。我是真的想化解误会，重新和武陵和睦相处啊……");
        }
        cm.dispose();
        return;
    }

    status++;

    if (status == 0) {
        if (cm.getQuestStatus(3840) == 2 && cm.getQuestStatus(3841) == 0 && cm.getPlayer().getLevel() >= 77 && cm.getPlayer().getMapId() == 250010503) {
            cm.sendYesNo("旅行者~ 看你带着那个丑陋的东西，就知道你是道功派来的。把那东西扔掉，听我说说如何？我很乐意解释围绕我的那些误会。请过来吧……呵呵呵~");
        } else {
            cm.dispose();
        }
    } else if (status == 1) {
        cm.forceStartQuest(3841, 2091004);
        cm.sendOk("这里是妖怪森林最深处……不过只要通过#b妖怪森林2消失雕像的石床#k，就不难来到这里。请尽快过来吧。我会让你看到我，妙仙王真正的一面。");
    } else {
        cm.dispose();
    }
}
