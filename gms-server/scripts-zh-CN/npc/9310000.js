var status = 0;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (status >= 0 && mode == 0) {
			cm.sendNext("美丽的上海外滩，难道你不想去看看吗！真遗憾。");
			cm.dispose();
			return;
		}
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {
			cm.sendYesNo("嘿！我是#b#e驾驶员 洪#n#k，我负责驾驶飞往上海的飞机。\r\n经过长年的飞行，我的驾驶技术已经很了不得。\r\n有兴趣跟我一起前往美丽的#b#e上海外滩#k#n吗？\r\n只需要#r2000金币#k哦！");
		} else if (status == 1) {
			if (cm.getMeso() < 2000) {
				cm.sendNext("你确定你有 #b2000 金币#k？ 如果没有，我可不能免费送你去。");
				cm.dispose();
			} else {
				cm.gainMeso(-2000);
				cm.warp(701000000);
				cm.dispose();
			}
		}
	}
}