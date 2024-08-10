/* ===========================================================
			Ronan Lana
	NPC Name: 		Lukan
	Description: 	Quest - Storming the Castle
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
        qm.sendAcceptDecline("现在是时候了，孩子。我们已经准备好进一步研究最近发生的所有这些奇怪事件的原因。我还必须介绍你认识我的兄弟，杰克。");
    } else if (status == 1) {
        qm.sendOk("他目前正在深红之林山脉漫游，经过邪恶的幻影森林，前往深红之城堡的道路。你的下一个目的地就在那里，祝你旅途平安。");
        qm.forceStartQuest();
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
            qm.sendOk("好的，那么。再见了。");
            qm.dispose();
            return;
        }
    }
    if (status == 0) {
        qm.sendNext("你是谁？哦，你是替我兄弟约翰而来的？太好了。");
    } else if (status == 1) {
        qm.sendOk("看起来你帮助了城市里的一些人办事，对吧？我会好好评价你的。看看这个：这是我在足够的探索后自己制作的幻影森林地图。拿着它，你将获得其他时代未曾发现的路径通行权。记住要#r永远不要丢失它#k，否则你将再也得不到它！\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0# \r\n#i3992040# #t3992040#\r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0# 175000 经验值");
    } else if (status == 2) {
        if (qm.canHold(3992040, 1)) {
            qm.forceCompleteQuest();
            qm.gainItem(3992040, 1);
            qm.gainExp(175000);
            qm.dispose();
        } else {
            qm.sendOk("嘿，你的消耗栏没有足够的空间来存放我要给你的物品。解决这个小问题然后再和我交谈。");
        }
    } else if (status == 3) {
        qm.dispose();
    }
}
