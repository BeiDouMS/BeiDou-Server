var status = 0;
var em;
var eim;

function sendBaseText() {
    cm.sendOk("访问仅限于公众。");
    cm.dispose();
}

function start() {
    em = cm.getEventManager("q3239");
    if (em != null)
        eim = cm.getEventInstance();

    if (em == null) { // No event handler
        sendBaseText();
        return;
    }
    else if (eim == null && !cm.isQuestStarted(3239)) { // Not in instance, quest is not in progress
        sendBaseText();
        return;
    }

    if (eim == null) { // Not in instance
        cm.sendYesNo("你准备好进入 #b#m922000000##k 了吗？");
    }
    else { // Inside the instance
        cm.sendYesNo("你准备好离开这个地方了吗？");
    }
}

function action(mode, type, selection) {
    if (mode < 1) {
        cm.dispose();
        return;
    }

    if (eim == null) { // Not in instance, ready to enter
        cm.removeAll(4031092); // This handling is done in the portal script and in the event end, just for legacy purposes here
        if (!em.startInstance(cm.getPlayer())) {
            cm.sendOk("已经有其他人在为我收集一些零件了。请等待直到该区域被清理。");
        }
    }
    else { // Inside the instance, ready to exit
        eim.removePlayer(cm.getPlayer()); // This will end the event and warp the player out
    }
    cm.dispose();
}