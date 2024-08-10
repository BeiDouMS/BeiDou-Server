var status = -1;

function start(mode, type, selection) {
    qm.dispose();
}

function end(mode, type, selection) {
    if (mode == -1 || mode == 0 && type > 0) {
        qm.dispose();
        return;
    }

    if (mode == 1) {
        status++;
    } else {
        if (status == 6) {
            qm.dispose();
            return;
        }
        status--;
    }
    if (status == 0) {
    	qm.sendNext("有什么事吗？你不是一直在金银岛上修炼吗？真相叔叔还带口信说你帮了大忙了。......什么？黑色之翼？", 8);
    } else if (status == 1) {
    	qm.sendNextPrev("#b（讲述有关人偶师、黑色之翼，以及黑色之翼的目的事情。）#k", 2);
    } else if (status == 2) {
    	qm.sendNextPrev("原来是这样......叫黑色之翼啊。居然还有这么一帮人......明知很危险也要在冒险岛世界里复活黑魔法师，太不像话了。", 8);
    } else if (status == 3) {
    	qm.sendNextPrev("是，是啊......\r\r#b（他的语气突然变得很决断，好可怕。）#k", 2);
    } else if (status == 4) {
    	qm.sendNextPrev("预言里只说到英雄会苏醒，与黑魔法师战斗。但我还一直半信半疑，这下我才明白黑魔法师是真的存在的。", 8);
    } else if (status == 5) {
    	qm.sendNextPrev("是不是很可怕？", 2);
    } else if (status == 6) {
    	qm.sendYesNo("有什么可怕的？管他是什么黑魔法师还是什么别的，你都会将他们一一打倒的，还有什么好担心的呢？我们只会觉得斗志更加高涨。啊，对了，我发现了这个#b技能#k......看一眼吗？");
    } else if (status == 7) {
	if (qm.getQuestStatus(21720) == 1) {
	    qm.forceCompleteQuest();
	    qm.teachSkill(21001003, qm.getPlayer().getSkillLevel(21001003), 20, -1);
	    qm.gainExp(3900);
	}
        qm.sendNext('#b（你还记得快速矛的技能！）#k', 2);
    } else if (status == 8) {
    	qm.sendNextPrev("这个技能是在一个古老的神秘书籍中发现的。我有预感这可能是你过去用过的一种技能，我想我是对的。", 8);
    } else if (status == 9) {
    	qm.sendNextPrev("你正在渐渐地变得强大起来。我会让你强大起来而倾尽全力帮助你的。有什么好害怕的呢？千万不能退缩。我们为了打败黑魔法师不是已经等待了数百年了吗？这次一定会取得胜利的！", 8);
    } else if (status == 10) {
    	qm.sendPrev("呐，为了这个目标必须继续修炼！修炼明白吗？前往金银岛继续修炼吧。一定要练到能打败黑魔法师的程度才行！", 8);
    } else if (status == 11) {
        qm.dispose();
    }
}