/* ===========================================================
            Ronan Lana
    NPC Name:       Lita Lawless
    Description:    Quest - Bounty Hunter
============================================================= */

var status = -1;

function start(mode, type, selection) {
    status++;
    if (mode != 1) {
        if (type == 1 && mode == 0) {
            status -= 2;
        } else {
            qm.sendOk("好吧，等你准备好了再来找我。新叶城随时需要可靠的帮手。");
            qm.dispose();
            return;
        }
    }
    if (status == 0) {
        var target = "无头骑士";
        qm.sendAcceptDecline("嘿，旅行者！我需要你的帮助。新叶城附近又出现了新的威胁，这次的目标是#r" + target + "#k。愿意接下这份赏金任务吗？");
    } else if (status == 1) {
        var reqs = "#r1 #t4031903##k";
        qm.sendOk("很好。请尽快把" + reqs + "带回来给我。新叶城就指望你了。");
        qm.forceStartQuest();
    } else if (status == 2) {
        qm.dispose();
    }
}
