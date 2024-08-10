/* ===========================================================
			Ronan Lana
	NPC Name: 		John, Jack
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
        qm.sendAcceptDecline("我知道我们可以依靠外来者处理这个问题！现在我们已经由他翻译的信件，把它交给杰克，他知道该怎么做。");
    } else if (status == 1) {
        if (qm.haveItem(4032018, 1)) {
            qm.forceStartQuest();
        } else if (qm.canHold(4032018, 1)) {
            qm.gainItem(4032018, 1);
            qm.forceStartQuest();
        } else {
            qm.sendOk("喂，你需要在你的其他栏中有一个空位才能得到这份信件。");
        }

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
        if (qm.haveItem(4032018, 1)) {
            qm.sendOk("哦，你带来了。干得好，现在对策过程将会更加容易。\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0# \r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0# 50000 经验值");
        } else {
            qm.sendOk("怎么了？为什么你还没有取回翻译好的消息？请把信的内容带给我，让我尽快制定对策。");
            qm.dispose();
        }
    } else if (status == 1) {
        qm.gainItem(4032018, -1);
        qm.gainExp(50000);
        qm.forceCompleteQuest();

        qm.dispose();
    }
}
