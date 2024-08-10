/* ===========================================================
			Ronan Lana
	NPC Name: 		John, Elpam
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
        qm.sendAcceptDecline("嗯，这不太好。我似乎无法让这些超级雕纹起作用，该死。...啊，是外来者！他可能知道这张纸上写的是什么语言。让埃尔帕姆试着读一下，也许他知道些什么。");
    } else if (status == 1) {
        if (qm.canHold(4032032, 1)) {
            qm.gainItem(4032032, 1);
            qm.sendOk("好的，我就靠你了。");
            qm.forceStartQuest();
        } else {
            qm.sendOk("嘿。你的其他栏没有空位。");
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
            qm.sendOk("你好，这个世界的本地人。所以你有需要翻译的消息？我们Versal的人民以精通许多外语而闻名，这个也许是我们所熟悉的。请稍等...");
            qm.gainItem(4032032, -1);
            qm.forceCompleteQuest();
        } else {
            qm.sendOk("恐怕你并没有携带你声称带着的信件。");
        }
    } else if (status == 1) {
        qm.dispose();
    }
}
