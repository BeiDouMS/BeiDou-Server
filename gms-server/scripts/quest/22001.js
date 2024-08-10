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
		qm.sendNext("哈哈。我笑得很开心。哈哈哈。但别胡说了. 喂 #p1013102#, 行吗?");
	} else if (status == 1) {
		qm.sendNextPrev("#b什么? 这是 #p1013101#的工作!", 2);
	} else if (status == 2) {
		qm.sendAcceptDecline("你这个小家伙！我叫你叫我哥哥! 你知道 #p1013102# 有多恨我. 如果我靠近他，他会咬我的。你喂他。他喜欢你.");
	} else if (status == 3) {
		if (mode == 0) {
			qm.sendNext("别再偷懒了。你想看到你弟弟被狗咬吗？快点！再和我谈谈接受这个任务!");
			qm.dispose();		
		} else {//accept
			qm.gainItem(4032447, true);
			qm.forceStartQuest();
			qm.sendNext("去山头 #b左边#k 喂 #b#p1013102##k. 他整个早上都在叫着要喂.");
		}
	} else if (status == 4) {
		qm.sendNextPrev("喂了 #p1013102# 回来找我.");
	} else if (status == 5) {
                qm.dispose();
        }
}