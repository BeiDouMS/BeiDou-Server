/* ===========================================================
        @author Resonance
	NPC Name: 		Scarrs
	Map(s): 		Mushroom Castle: Corner of Mushroom Forest(106020000)
	Description: 	Quest -  A Friendship with Bruce
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
            qm.sendOk("我想把这个喜讯尽快告诉#b布鲁斯#k。如果你很忙的话，就算了。");
            qm.dispose();
            return;
        }
    }
    if (status == 0) {
        qm.sendAcceptDecline("我还有一个请求，你愿意帮我吗？");
    } else if (status == 1) {
        qm.forceStartQuest();
        qm.gainItem(4032389, 1);
        qm.sendOk("事实上，#b奇拉蘑菇孢子#k是我的研究成果。你知道#b射手村#k的#b布鲁斯#k吗？他是我小时候的好朋友，他把他的研究成果告诉了我，我才制作出了#b奇拉蘑菇孢子#k。我能取得这样的成果，多亏了他的帮助。请你帮我把它交给布鲁斯。");
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
        qm.sendOk("哦！是#b斯卡斯#k让你来的吗？ \r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0# \r\n#fUI/UIWindow.img/QuestIcon/8/0# 8800 经验");
    } else if (status == 1) {
        qm.forceCompleteQuest();
        qm.gainExp(8800);
        qm.gainItem(4032389, -1);
        qm.sendOk("呵呵，这是我过去研究的#b奇拉蘑菇孢子#k啊。由于材料很难搜集，所以我只停留在理论层面，没想到被那个家伙做出来了。请你帮我谢谢他。");
    } else if (status == 2) {
        qm.dispose();
    }
}

