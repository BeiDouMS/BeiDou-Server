var status = -1;

function start(mode, type, selection) {
    if (mode == -1 || mode == 0 && type > 0) {
        qm.dispose();
        return;
    }

    if (mode == 1) {
        status++;
    } else {
	if (status == 6) {
	    qm.sendNext("我知道要超越你的教练需要极大的力量和意志，但你并不是能让自己颓废。你必须朝着更大更好的方向前进！你必须尽你所能去拥抱你的英雄本性!");
	    qm.dispose();
	    return;
	}
	status--;
    }
    if (status == 0) {
    	qm.sendNext("……现在你的能力是什么程度，我大概了解了……呵呵……没想到我这把老骨头还能有今天……真是感动得要流眼泪……不，是鼻涕……");
    } else if (status == 1) {
    	qm.sendNextPrev("#b(……也没怎么修炼嘛……?)#k", 2);
    } else if (status == 2) {
    	qm.sendNextPrev("好，现在让我们开始第3阶段的最后一阶段的锻炼。这次修炼的对象是……#r#o9300343##k！猪猪！你了解他们吗?");
    } else if (status == 3) {
    	qm.sendNextPrev('一点点……', 2);
    } else if (status == 4) {
    	qm.sendNextPrev("他们是天生的战士！从出生的那一刻起，对食物就充满了无穷的愤怒，凡是他们经过的地方都不会留下任何食物。很可怕吧?");
    } else if (status == 5) {
    	qm.sendNextPrev("#b(他不是在开玩笑吧?)#k", 2);
    } else if (status == 6) {
    	qm.sendAcceptDecline("来，快点#b再次进入修炼场#k，去和那些天生的战士们修炼用的猪中战斗吧，打倒#r30只#k后，你的能力将会有一个质的飞跃。全力以赴地去战斗吧！超越我这个教练！");
    } else if (status == 7) {
        qm.forceStartQuest();
        qm.sendOk("快走吧！去打倒那些#o9300343#!");
    } else if (status == 8) {
        qm.dispose();
    }
}

function end(mode, type, selection) {
    if (mode == -1 || mode == 0 && type > 0) {
        qm.dispose();
        return;
    }

    if (mode == 1) {
        status++;
    } else {
        if (status == 2) {
            qm.sendNext("Are you reluctant to leave your instructor? *Sniff sniff* I'm so moved, but you can't stop here. You are destined for bigger and better things!");
            qm.dispose();
            return;
        }
        status--;
    }
    if (status == 0) {
        qm.sendNext("Ah, you've come back after defeating all 30 #o9300343#s. I knew you had it in you... Even though you have no memories and few abilities, I could see that you were different! How? Because you're carrying around a Polearm, obviously!");
    } else if (status == 1) {
        qm.sendNextPrev("#b(Is he pulling your leg?)#k'", 2);
    } else if (status == 2) {
        qm.sendYesNo("I have nothing more to teach you, as you've surpassed my level of skill. Go now! Don't look back! This old man is happy to have served as your instructor.");
    } else if (status == 3) {
        if (qm.isQuestStarted(21703)) {
            qm.forceCompleteQuest();
            qm.teachSkill(21000000, qm.getPlayer().getSkillLevel(21000000), 10, -1);   // Combo Ability Skill
            qm.gainExp(2800);
        }
        qm.sendNext("(You remembered the #bCombo Ability#k skill! You were skeptical of the training at first, since the old man suffers from Alzheimer's and all, but boy, was it effective!)", 2);
    } else if (status == 4) {
        qm.sendPrev("Now report back to #p1201000#. I know she'll be ecstatic when she sees the progress you've made!");
    } else if (status == 5) {
        qm.dispose();
    }
}