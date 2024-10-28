/* ===========================================================
        @author Resonance
	NPC Name: 		Minister of Magic
	Map(s): 		Mushroom Castle: Corner of Mushroom Forest(106020000)
	Description: 	Quest -  Exploring Mushroom Forest(3)
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
            qm.sendOk("你既然不愿意，又为什么要来问我呢？奇怪的家伙……");
            qm.dispose();
            return;
        }
    }
    if (status == 0) {
        qm.sendAcceptDecline("我曾经听说有种药品可以解开这种类型的结界。好像叫#b奇拉蘑菇孢子#k？嗯……外面的#b斯卡斯#k是蘑菇专家，你去问问#b斯卡斯#k吧。");
    } else if (status == 1) {
        qm.forceStartQuest();
        qm.sendOk("#b斯卡斯#k应该可以帮助你。");
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
        qm.sendOk("啊！你就是那个勇士吧。我是蘑菇王国的#b王室蘑菇研究家斯卡斯#k。嗯？你需要#b奇拉蘑菇孢子#k？");
    } else if (status == 1) {
        qm.forceCompleteQuest();
        qm.gainExp(4200);
        qm.sendOk("#b奇拉蘑菇孢子#k嘛……好像从哪里听说过。");
    } else if (status == 2) {
        qm.dispose();
    }
}

