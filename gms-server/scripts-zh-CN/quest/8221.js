/* ===========================================================
			Ronan Lana
	NPC Name: 		Jack
	Description: 	Quest - Mark of Heroism
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
            qm.sendOk("好的，那就这样吧。再见。");
            qm.dispose();
            return;
        }
    }
    if (status == 0) {
        qm.sendAcceptDecline("是时候了！我们需要为你制作一种安全前往射手村山谷之巅的方式，否则我们所做的一切都将毫无意义。你必须得到 #b#t3992039##k。你准备好了吗？");
    } else if (status == 1) {
        qm.sendOk("好的，我需要你先准备这些物品：#b10 #t4010006##k，#b4 #t4032005##k 和 #b1 #t4004000##k。去吧！");
        qm.forceStartQuest();
    } else if (status == 2) {
        qm.dispose();
    }
}
