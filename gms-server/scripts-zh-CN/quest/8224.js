/* ===========================================================
			Ronan Lana
	NPC Name: 		Taggrin
	Description: 	Quest - The Fallen Woods
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
            qm.sendOk("好的，那就这样吧。希望能再见到你。");
            qm.dispose();
            return;
        }
    }
    if (status == 0) {
        qm.sendAcceptDecline("嘿，旅行者，过来！我是塔格林，乌鸦忍者团的领袖。我们是目前归属于新叶城郡的雇佣兵。我们的工作是追捕最近在这里潜伏的那些怪物。你有兴趣为我们做个小任务吗？当然，对双方来说，报酬都会很丰厚。");
    } else if (status == 1) {
        qm.sendOk("好的。我需要你去森林里猎杀#b那些假树#k，并收集它们的掉落物50个作为你完成任务的证明。");
        qm.forceStartQuest();
    } else if (status == 2) {
        qm.dispose();
    }
}
