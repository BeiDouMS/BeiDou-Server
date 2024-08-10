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
		qm.sendNext("哟，主人。现在我已经向你展示了我的能力，轮到你了。向我证明…你能找到食物！我快饿死了.你现在可以用我的力量，所以你必须照顾我.");
	} else if (status == 1) {
		qm.sendNextPrev("呃，我还是不明白怎么回事，但我不能让像你这样可怜的小动物饿死，对吧？食物，你说？你想吃什么?", 2);
	} else if (status == 2) {
		qm.sendNextPrev("嗨，我几分钟前才出生。我怎么知道我吃了什么？我只知道我是一条龙。。。我是你的龙。你是我的主人。你得好好对待我!");
	} else if (status == 3) {
		qm.sendAcceptDecline("我想我们应该一起学习。但我饿了。师父，我要吃的。记住，我是个宝宝！我马上就要哭了!");
	} else if (status == 4) {
		if (mode == 0) {
			qm.sendNext("*gasp* 你怎么能拒绝喂你的龙呢？你这是虐待儿童! ");
		} else {
			qm.forceStartQuest();
			qm.sendOk("#b#b(#p1013000# 小龙看起来非常饿。你必须喂他。也许你父亲可以给你一些关于龙吃什么的建议.)");
		}
	} else if (status == 5) {
                qm.dispose();
        }
}