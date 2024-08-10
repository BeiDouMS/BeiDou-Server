var status = -1;

function start(mode, type, selection) {
	if (mode == 0 && type == 0) {
		status--;
	} else if (mode == -1) {
		qm.dispose();
		return;
	} else {
		status++;
	}
	if (status == 0) {
		qm.sendNext("我就知道！我知道我们是有联系的，师父！当你变得更强，我也会变得更强。当我变得更强壮时，你可以利用我的力量！这是我们的约定。我知道我选了一个好主人！");
	} else if (status == 1) {
		qm.sendNextPrev("#b我明白了。我们到底是怎么完成这个任务的？", 2);
	} else if (status == 2) {
		qm.sendNextPrev("我不知道。我只是个鸡蛋。我真的不记得了……虽然我隐约记得你，师父，在一片雾蒙蒙的森林里朝我走来。我记得你见到我时的惊讶。我也在呼唤你作为报答。");
	} else if (status == 3) {
		qm.sendNextPrev("#b#b(等等！听起来就像你做的一个梦。。。你们两个是在梦里认识的吗？你在梦中看到的巨龙可能是...#p1013000#?)", 2);
	} else if (status == 4) {
		qm.sendNextPrev("师父，你和我在精神上是一体的。我一见到你就知道了。所以我想和你订个协议。没有其他人。当然，你得给我说好的价格.");
	} else if (status == 5) {
		qm.sendNextPrev("#b我要为此付出代价？?", 2);
	} else if (status == 6) {
		qm.sendNextPrev("你不记得了吗？当你认出我并触摸我的时候？这是我设定的一个条件。你一碰我的蛋，我和你就在精神上合二为一.");	
	} else if (status == 7) {
		qm.sendNextPrev("#b一个在…精神上?", 2);	
	} else if (status == 8) {
		qm.sendNextPrev("对！精神契约！你我有各自的身体，但我们有一个共同的精神。这就是为什么当我变得更强时你会变得更强，反之亦然！太棒了，对吧？至少，我想是的.");	
	} else if (status == 9) {
		qm.sendNextPrev("#b我不知道你在说什么，但听起来很重要.", 2);	
	} else if (status == 10) {
		qm.sendNextPrev("这当然是大事，傻主人！你再也不用担心怪物了。你现在有我来保护你！去测试我吧。事实上，我们现在就得走了！");	
	} else if (status == 11) {
		qm.sendNextPrev("#b但这里很平静。周围没有危险的怪物.", 2);	
	} else if (status == 12) {
		qm.sendNextPrev("天那？！那不好玩！师父，你不喜欢冒险吗？代表你的种族与怪物战斗，战胜邪恶，拯救无辜，诸如此类？你不喜欢这些吗?");
	} else if (status == 13) {
		qm.sendNextPrev("#b这不是我五年计划的一部分。我只是开玩笑，但说真的，我是农民的孩子...", 2);
	} else if (status == 14) {
		qm.sendAcceptDecline("呃，让我告诉你吧。龙师不可能过上平静的生活。我有很多机会证明我的能力。相信我，我们的旅程将是一次大冒险。答应我你会和我在一起，好吗?\r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0# 810 exp");
	} else if (status == 15) {
		if (mode == 0) {
			qm.sendNext("呃，你开玩笑吧？告诉我你的手指滑了！去接受这个任务吧.");
		} else {
			if (!qm.isQuestCompleted(22507)) {
				qm.forceCompleteQuest();
				qm.gainExp(810);
			}
			qm.sendNext("呵呵，那好吧，主人。我们开始吧!");
		}
	} else if (status == 16) {
		qm.sendNextPrev("#b(你有点迷茫，但你现在和大龙米尔一起旅行。也许你会像他说的那样一起去冒险.)", 2);
	} else if (status == 17) {	
		qm.sendPrev("#b#b(你还有任务没完成。你父亲需要和你谈谈，现在就去看看他.)");
	} else if (status == 18) {
                qm.dispose();
        }
}