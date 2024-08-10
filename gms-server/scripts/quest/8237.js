/* ===========================================================
			Ronan Lana
	NPC Name: 		Lita Lawless
	Description: 	Quest - Bounty Hunter - Catch a Bigfoot by the Toe
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
            qm.sendOk("好吧，那么。回头见.");
            qm.dispose();
            return;
        }
    }
    if (status == 0) { //斯库亚斯防御战怪物召唤
        var target = "大脚怪";
        qm.sendAcceptDecline("嘿，旅行者！我需要你的帮助。新叶城的市民们受到了新的威胁。我现在正在招募任何人，这次的目标是 #r" + target + "#k. 你要加入吗？");
    } else if (status == 1) {
        var reqs = "#r1 #t4032013##k";
        qm.sendOk("很好。尽快拿到#r" + reqs + "#k，新叶城市民委员会需要你.");
        qm.forceStartQuest();
    } else if (status == 2) {
        qm.dispose();
    }
}
