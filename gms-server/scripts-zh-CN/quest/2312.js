/* ===========================================================
        @author Resonance
	NPC Name: 		Head Patrol Officer
	Map(s): 		Mushroom Castle: Corner of Mushroom Forest(106020000)
	Description: 	Quest -  The Test
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
            qm.sendOk("嗯……看来你是没有自信啊。如果你有了挑战的勇气，可以随时来找我。");
            qm.dispose();
            return;
        }
    }
    if (status == 0) {
        qm.sendAcceptDecline("勇士，现在我们王国正面临建国以来最大的危机，所以我们需要强壮的勇士。你是得到推荐之后过来的吧？但是我们需要判断一下你是否是我们王国需要的勇士。不好意思，我能请你完成一件事吗？");
    } else if (status == 1) {
        qm.forceStartQuest();
        qm.sendOk("从这里往前走，可以看到很多背叛了蘑菇王国的#b得意的蘑菇仔#k。他们是背叛了我们王国，投降了敌人的人。请你去教训教训他们，并搜集#b50个#k#v4000499##t4000499##b#k作为证据。");//收集变种孢子
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
        qm.sendOk("你帮我教训了得意的蘑菇仔了吗？");
    } else if (status == 1) {
        qm.forceCompleteQuest();
        qm.gainExp(11500);
        qm.gainItem(4000499, -50);
        qm.sendOk("真了不起。不好意思，我不该怀疑你的能力。请你务必把我们蘑菇王国从危机中拯救出来。");
    } else if (status == 2) {
        qm.dispose();
    }
}
	
