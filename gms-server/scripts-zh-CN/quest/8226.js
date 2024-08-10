/* ===========================================================
			Ronan Lana
	NPC Name: 		Taggrin
	Description: 	Quest - The Right Path
=============================================================
Version 1.0 - Script Done.(10/7/2017)
=============================================================
*/

var status = -1;

function start(mode, type, selection) {
    status++;
    if (mode != 1) {
        if (type == 1 && mode == 0) {
            status -= 2;
        } else {
            qm.sendOk("好的，那好吧。回头见。");
            qm.dispose();
            return;
        }
    }
    if (status == 0) {
        qm.sendAcceptDecline("既然你已经加入我们的团队，听听我要说的话。我们，射手村的乌鸦忍者团，被雇佣来解决许多问题，为雇主解决各种大陆上的问题。我即将谈论你的任务，你准备好了吗？");
    } else if (status == 1) {
        qm.sendOk("你的下一个任务是：击败在这片森林中徘徊的长者幽灵。他们是一群强大的敌人，所以要保持警惕。我需要你带来100个#t4032010#作为你任务完成的证明。");
        qm.forceStartQuest();
    } else if (status == 2) {
        qm.dispose();
    }
}
