/* ===========================================================
			Ronan Lana
	NPC Name: 		Lita Lawless
	Description: 	Quest - 赏金猎人 - 抓住大脚怪的脚趾 - 后续
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
            qm.sendOk("好吧，那就这样。回头见。");
            qm.dispose();
            return;
        }
    }
    if (status == 0) {
        var target = "大脚怪";
        qm.sendAcceptDecline("嘿，旅行者！我需要你的帮助。新叶城的居民正面临新的威胁。我正在招募愿意出手的人，这次的目标是 #r" + target + "#k。你愿意加入吗？");
    } else if (status == 1) {
        var reqs = "#r1 #t4032013##k";
        qm.sendOk("很好。请尽快把 " + reqs + " 带回来给我。新叶城就指望你了。");
        qm.forceStartQuest();
    } else if (status == 2) {
        qm.dispose();
    }
}
