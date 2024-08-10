/* ===========================================================
			Ronan Lana
	NPC Name: 		Jack
	Description: 	Quest - Stemming the Tide
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
            qm.sendOk("好的，那就这样吧。希望再见到你。");
            qm.dispose();
            return;
        }
    }
    if (status == 0) {
        qm.sendAcceptDecline("嘿，旅行者！我需要你的帮助。我看到一个巨大的威胁即将危及射手村的居民。这些在这里四处游荡的生物突然出现…这肯定不是好事。你愿意听听我要说什么吗？");
    } else if (status == 1) {
        qm.sendOk("事情是这样的：扭曲大师，目前控制着红树林要塞的重要人物，计划对射手村发动一次大规模袭击，可能会在接下来的几天内发生。我不能就这样呆在这里观察，而他们准备这次袭击。但是，我不能就这样离开这个位置，我必须不惜一切代价留意他们的动向。这就是你的任务：去找到卢坎，过去红树林要塞的骑士，他目前正在树林中徘徊，从他那里接受进一步的指令，他知道该怎么做。");
        qm.forceStartQuest();
    } else if (status == 2) {
        qm.dispose();
    }
}

function end(mode, type, selection) {
    status++;

    if (status == 0) {
        if (qm.haveItem(3992041)) {
            qm.sendOk("啊，你完成了我交给你的任务。干得好，现在那些家伙正忙着从这次进攻中恢复过来。现在，请记住：#b那把钥匙必须用来进入#k要塞内部圣所。如果你想进入那里，一定要随身携带这把钥匙。");
            qm.forceCompleteQuest();
        } else if (qm.getQuestStatus(8223) == 2) {
            qm.sendOk("你完成了任务但是丢失了钥匙？那可不好，你需要这把钥匙才能进入要塞内部的房间。去找卢坎问问接下来应该做什么，我们需要你进入要塞内部。");
        } else {
            qm.sendOk("城里的人指望着你。请快点。");
        }
    } else if (status == 1) {
        qm.dispose();
    }
}
