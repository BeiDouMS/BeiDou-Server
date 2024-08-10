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
		qm.sendNext("不，不，不。这不是我需要的。我需要一些更有营养的东西，主人!");
	} else if (status == 1) {
		qm.sendNextPrev("#b嗯。。。所以你不是草食动物。你可能是食肉动物。你毕竟是条龙. 弄一些 #t4032453# 怎么样?", 2);
	} else if (status == 2) {
		qm.sendAcceptDecline("什么是...#t4032453#? 从没听说过，但如果好吃，我接受！给我点好吃的。除了植物！!");
	} else if (status == 3) {
		if (mode == 0) {
			qm.sendNext("你怎么能这样，要饿死我啊。我还只是个宝宝。这是不对的!");
		} else {
			qm.forceStartQuest();
			qm.sendNext("#b#b(尝试是给 #p1013000# 一些 #t4032453#. 你得去农场弄一些 #o1210100#s. 十个就够了...)");
		}
	} else if (status == 4) {
                qm.dispose();
        }
}