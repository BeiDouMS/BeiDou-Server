/* ===========================================================
        @author Resonance
	NPC Name: 		Minister of Home Affairs
	Map(s): 		Mushroom Castle: Corner of Mushroom Forest(106020000)
	Description: 	Quest -  Over the Castle Wall (2)
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
            qm.sendNext("是吗？你有什么妙招吗？如果不是的话请你回头再来。");
            qm.dispose();
            return;
        }
    }
    if (status == 0) {
        qm.sendYesNo("即使穿越了结界，也不能完全放心。我们蘑菇王国的城墙经过特殊设计，绝对无法从外部侵入，想要进去会很困难。嗯……你能先去城墙外部调查一下吗？");
    } else if (status == 1) {
        // qm.sendNext("经过蘑菇森林往#b#m106020400##k的西面去，就能找到#b#m106020500##k。请你去做调查。");
        qm.forceStartQuest();
        qm.dispose();
    } //else if (status == 2) {
    //     qm.forceStartQuest();
    //     //qm.forceStartQuest(2322, "1");
    //     // qm.gainExp(11000);
    //     qm.sendOk("你做出了正确的选择。"); //你好像还没有完成对城墙的调查。请你快去调查城墙。
    //     qm.forceCompleteQuest();
    // } else if (status == 3) {
    //     qm.dispose();
    // }
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
        qm.sendOk("嗯，果然如此。入口被彻底封了起来。");
    } else if (status == 1) {
        qm.forceCompleteQuest();
        qm.gainExp(11000);
        qm.sendOk("真是辛苦你了。");
    } else if (status == 2) {
        qm.dispose();
    }
}
	
