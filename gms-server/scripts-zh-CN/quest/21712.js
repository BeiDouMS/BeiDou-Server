var status = -1;

function start(mode, type, selection) {
    if (mode == -1 || mode == 0 && type > 0) {
        qm.dispose();
        return;
    }

    if (mode == 1) {
        status++;
    } else {
	if (status == 2) {
	    qm.sendNext("你还是不明白怎么回事？如果你再跟我说一次，我再跟你解释一遍。");
	    qm.dispose();
	    return;
	}
	status--;
    }
    if (status == 0) {
	qm.sendNext("#t4032315#总是会#b发出奇怪的声音#k。当然，你是听不见的。因为那是只有#o1210102#才能听到的声音。在这种声音的影响下，#o1210102#的性格似乎发生了变化。");
    } else if (status == 1) {
	qm.sendAcceptDecline("性格变得怪异的#o1210102#开始和没有发生变化的#o1210102#战斗，这导致射手村附近的#o1210102#全部变得残暴起来了。#b使得最近怪物们发生了变化的根源就是这个人偶！#k你明白了吧？");
    } else if (status == 2) {
	qm.forceStartQuest();
	qm.sendNext("怎么会发生这种事情呢......这种人偶不可能是自然形成的，一定是有人故意而为......看来要对#o1210102#的状态观察一段时间了。", 9);
    } else if (status == 3) {
	qm.sendPrev("#b（射手村周边的#o1210102#发生变化的原因找到了。现在把搜集到的信息告诉#p1002104#吧。）#k", 2);
    } else if (status == 4) {
        qm.dispose();
    }
}

function end(mode, type, selection) {
    qm.dispose();
}