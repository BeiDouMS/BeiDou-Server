/* ===========================================================
        @author Resonance
	NPC Name: 		Scarrs
	Map(s): 		Mushroom Castle: Corner of Mushroom Forest(106020000)
	Description: 	Quest -  Killer Mushroom Spores(2)
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
            qm.sendOk("我知道这很不容易，但没有这种材料的话，就无法制作#b奇拉蘑菇孢子#k。你再好好考虑一下。");
            qm.dispose();
            return;
        }
    }
    if (status == 0) {
        qm.sendAcceptDecline("嗯……在你搜集紫色毒蘑菇盖的时间里，我查了一下，我还需要另外的材料。请你再去帮我搜集一种材料。");
    } else if (status == 1) {
        qm.forceStartQuest();
        qm.sendOk("很好。请你帮我去打猎得意的蘑菇仔，搜集#b50个#t4000499##k。");
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
        qm.sendOk("需要的材料全部搜集到了吗？")
    } else if (status == 1) {
        if (!qm.haveItem(4000499, 50)) {
            qm.sendOk("嗯？你好像还没搜集到#b50个#t4000499##k啊。我需要#b50个#t4000499##k。");
            status = 2;
            return;
        }

        qm.sendNext("好的！有了它的话，我就能制作#b奇拉蘑菇孢子#k了。请稍等一下。");
    } else if (status == 2) {
        qm.sendOk("给，奇拉蘑菇孢子做好了。希望它能对你起到一点帮助，帮助你救出公主，夺回王宫。加油，勇士！");
    } else if (status == 3) {
        qm.forceCompleteQuest();
        qm.gainExp(11500);
        qm.gainItem(4000499, -50);
        qm.gainItem(2430014, 1);
        qm.dispose();
    }
}

