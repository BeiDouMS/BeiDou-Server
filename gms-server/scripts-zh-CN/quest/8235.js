/* ===========================================================
			Ronan Lana
	NPC Name: 		Lita Lawless
	Description: 	Quest - Bounty Hunter - One Step A-Head
=============================================================
Version 1.0 - Script Done.(11/7/2017)
=============================================================
*/

var status = -1;

function start(mode, type, selection) {
    status++;
    if (mode != 1) {
        if (type == 1 && mode == 0) {
            status -= 2;
        } else {
            qm.sendOk("好的，再见。");
            qm.dispose();
            return;
        }
    }
    if (status == 0) {
        var target = "无头骑士";
        qm.sendAcceptDecline("嘿，游侠！我需要你的帮助。新叶城的居民面临着新的威胁。我正在招募任何人，这次的目标是#r" + target + "#k。你愿意加入吗？");
    } else if (status == 1) {
        var reqs = "#r1 #t4031903##k";
        qm.sendOk("很好。尽快给我#r" + reqs + "#k，新叶城正在依赖你。");
        qm.forceStartQuest();
    } else if (status == 2) {
        qm.dispose();
    }
}
