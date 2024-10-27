/* ===========================================================
        @author Resonance
	NPC Name: 		Minister of Home Affairs
	Map(s): 		Mushroom Castle: Corner of Mushroom Forest(106020000)
	Description: 	Quest -  Exploring Mushroom Forest(2)
=============================================================
Version 1.0 - Script Done.(18/7/2010)
=============================================================
*/

var status = -1;

function start(mode, type, selection) {
    status++;
    if (mode != 1) {
        if (type == 1 && mode == 0) {
            status -= 2;
        } else {
            qm.sendOk("请你不要忘了勇士的义务。");
            qm.dispose();
            return;
        }
    }
    if (status == 0) {
        qm.sendAcceptDecline("强力的魔法结界……这可怎么办呢……如果不能穿过结界，就无法救出公主。既然你说用物理的力量无法消除结界，我想最好去问问#b魔法大臣#k。");
    } else if (status == 1) {
        qm.forceStartQuest();
        qm.sendOk("请你马上去见见#b魔法大臣#k。他的性格虽然有些古怪，但他是个知识渊博的人，也许会知道些什么。");
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
        qm.sendOk("什么？你对蘑菇森林的结界进行了调查？");
    } else if (status == 1) {
        qm.forceCompleteQuest();
        qm.gainExp(4000);
        qm.sendOk("嗯……有意思。如果是有人用强力的魔法生成的结界，靠人力是无法穿越的。");
    } else if (status == 2) {
        qm.dispose();
    }
}

