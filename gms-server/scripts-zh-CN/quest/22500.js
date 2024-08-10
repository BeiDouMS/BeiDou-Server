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
		qm.sendNext("我终于来了！*吸气啊，这一定是我呼吸的空气。那，那一定是太阳！还有，一棵树！还有，一株植物！还有，一朵花！哇哈哈哈！太不可思议了！这比我被困在蛋壳里时想象的世界要好得多。和你。。。你是我的主人吗？嗯，我对你的印象不太一样.");
	} else if (status == 1) {
		qm.sendNextPrev("#bWhoooooa, it talks!", 2);
	} else if (status == 2) {
		qm.sendNextPrev("我的主人很奇怪。我想我现在无能为力了，因为任务已经完成了。*很高兴见到你。我们会经常见面的.");
	} else if (status == 3) {
		qm.sendNextPrev("#b嗯？你什么意思？我们会经常见面吗？什么任务?", 2);
	} else if (status == 4) {
		qm.sendNextPrev("你什么意思我什么意思？！你把我从蛋壳里吵醒了。你是我的主人！所以你当然有责任照顾我，训练我，帮助我成为一条强壮的龙。显然!");
	} else if (status == 5) {
		qm.sendNextPrev("#b天那！一条龙？你是龙？！我无法理解。。。为什么我是你的主人？你在开玩笑吗?", 2);
	} else if (status == 6) {
		qm.sendNextPrev("你在说什么？你的灵魂和我的灵魂达成了协议！我们现在差不多是同一个人了。我真的要解释吗？结果，你成了我的主人。我们受契约的约束。你不能改变主意。。。契约不能被破坏.");		
	} else if (status == 7) {
		qm.sendNextPrev("#b等等，等等，等等。让我直说吧。你是说除了帮助你我别无选择?", 2);
	} else if (status == 8) {
		qm.sendNextPrev("是啊！嘿。。。！脸怎么了？你…不想当我的主人?");
	} else if (status == 9) {
		qm.sendNextPrev("#b不。。。不是那个。。。我只是不知道我是否准备好养宠物.", 2);
	} else if (status == 10) {
		qm.sendNextPrev("一…一…一只宠物？！你刚才是不是叫我宠物？！怎么敢。。。为什么，我是一条龙！世界上最强壮的!");
	} else if (status == 11) {
		qm.sendNextPrev("#b...#b(你怀疑地盯着他看。他看起来像只蜥蜴。一个瘦小的小家伙.)#k", 2);
	} else if (status == 12) {
		qm.sendAcceptDecline("你为什么那样看着我？！小心点！看看我能用我的力量做些什么。准备好了吗?");
	} else if (status == 13) {
		if (mode == 0 && type == 15) {
			qm.sendNext("你不相信我？grrrr，你让我生气了!");
			qm.dispose();
		} else {
			if (!qm.isQuestStarted(22500)) {
				qm.forceStartQuest();
			}
			qm.sendNext("让我去杀死 #r#o1210100##ks! 现在就去! 我会告诉你龙能多快打败 #o1210100#s! 冲啊!");
		}
	} else if (status == 14) {
		qm.sendNextPrev("等一下！你加了你的能力值吗? 我受主人的影响很大#b智力 和 运气#k! 如果你真的想看看我能做什么, 分配你的能力点 和 #b装备你的魔术师装备#k 在你使用技能之前!");
	} else if (status == 15) {
		qm.sendImage("UI/tutorial/evan/11/0");
		qm.dispose();
	}
}