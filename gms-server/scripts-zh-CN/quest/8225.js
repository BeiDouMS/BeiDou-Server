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
            qm.sendOk("好的，那么。再见了。");
            qm.dispose();
            return;
        }
    }
    if (status == 0) {
        qm.sendAcceptDecline("嘿，伙伴。现在你已经成为渡鸦忍者部族的一员，我有一个任务交给你。你准备好了吗？");
    } else if (status == 1) {
        qm.sendOk("很好。为了证明你在我们队伍中的价值，你必须先通过一个小挑战：你要能在这片森林中行动自如，并了解它隐藏的秘密。去找到一张#b幻影森林地图#k，然后回来找我谈谈。我会评估你是否值得加入我们。");
        qm.forceStartQuest();
    } else if (status == 2) {
        qm.dispose();
    }
}
