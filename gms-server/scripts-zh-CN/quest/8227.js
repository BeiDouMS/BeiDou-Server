/* ===========================================================
			Ronan Lana
	NPC Name: 		Jack, John
	Description: 	Quest - Lost in Translation
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
            qm.sendOk("来吧，城市真的需要你在这件事上合作！");
            qm.dispose();
            return;
        }
    }
    if (status == 0) {
        qm.sendAcceptDecline("嘿，伙计！好时机。我从要塞官员那里偷到了这份文件，但它的信息被加密了。我无法使用它，所以需要你把它带给约翰，看看他能否解密它？");
    } else if (status == 1) {
        if (qm.canHold(4032032, 1)) {
            qm.gainItem(4032032, 1);
            qm.sendOk("很好，这件事就拜托你了。");
            qm.forceStartQuest();
        } else {
            qm.sendOk("嘿，你的其他栏没有空位了。");
        }
    } else if (status == 2) {
        qm.dispose();
    }
}

function end(mode, type, selection) {
    status++;
    if (mode != 1) {
        if (type == 1 && mode == 0) {
            status -= 2;
        } else {
            qm.dispose();
            return;
        }
    }
    if (status == 0) {
        if (qm.haveItem(4032032, 1)) {
            qm.gainItem(4032032, -1);
            qm.sendOk("哦，你从要塞带来了一封信？很棒！让我看看我能否立刻解密它。");
            qm.forceCompleteQuest();
        } else {
            qm.sendOk("杰克说你没有带来加密的信？来吧，孩子，我们需要它来解读敌人的下一步行动！");
        }
    } else if (status == 1) {
        qm.dispose();
    }
}
