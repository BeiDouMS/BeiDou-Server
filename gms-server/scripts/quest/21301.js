var status = -1;

function end(mode, type, selection) {
    status++;
    if (mode == 0 && type == 0) {
        status -= 2;
    } else if (mode != 1) {
        //if (mode == 0)
            qm.sendNext("#b(您需要考虑一下...)#k");
        qm.dispose();
        return;
    }
	
	if (status == 0) {
        qm.sendNext("您已经成功杀死了 #o9001013# ？ 哈哈哈...你确实是我的主人。 好吧，现在给我您在那找到的红玉。 我必须把它放回身上，然后……等等，为什么你不说一句话？ 别告诉我...你没带回来!");
    } else if (status == 1) {
		qm.sendNextPrev("什么？！ 您真的没有带回《红玉》吗？ 为什么？ 您是否完全忘记了它？ 啊...即使受到黑魔法师的诅咒，以及过去的时间和所有的一切，我从来没有想过我的主人会变得愚蠢...");
	} else if (status == 2) {
		qm.sendNextPrev("不，不，我不能让这让我感到绝望。 这是我应该保持镇定和控制的时候，不像我的主人... \r\努萨 ..." );
	} else if (status == 3) {
		qm.sendNextPrev("即使你现在回去，小偷也可能从那里逃走了。这意味着你得重新做红玉。你以前做过，所以你记得做一个的材料，对吧？现在走。。。");
	} else if (status == 4) {
		qm.sendNextPrev("\r\n\r\n\r\n这家伙肯定失去了所有的记忆!");
	} else if (status == 5) {
		qm.sendNextPrev("…没有希望，没有梦想。。。不，不!!");
	} else if (status == 6) {
		qm.completeQuest();
                qm.sendNextPrev("#b(玛哈开始变得歇斯底里了。我最好马上离开。也许莉莉能做点什么.)", 2);
	} else if (status == 7) {
            qm.dispose();
        }
}