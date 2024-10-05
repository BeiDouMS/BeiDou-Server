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
            qm.sendNext("你舍不得离开导师吗？*啜泣*我很感动，但你不能就此止步。你注定会成就更大更好的事情！");
            qm.dispose();
            return;
        }
        status--;
    }
    if (status == 0) {
        qm.sendNext("啊，你打败了所有30个#o9300343#后又回来了。我知道你有这个能力……尽管你没有记忆，能力也很少，但我还是能看出你与众不同！怎么会这样？因为你显然带着长柄武器！");
    } else if (status == 1) {
        qm.sendNextPrev("#b(他在开玩笑吗？)#k", 2);
    } else if (status == 2) {
        qm.sendYesNo("我没什么可教你的了，因为你的技术已经超越了我。现在就去吧！别回头！老头很高兴能成为你的导师。");
    } else if (status == 3) {
        if (qm.isQuestStarted(21703)) {
            qm.forceCompleteQuest();
            qm.teachSkill(21000000, qm.getPlayer().getSkillLevel(21000000), 10, -1);   // Combo Ability Skill
            qm.gainExp(2800);
        }
        qm.sendNext("（你还记得#bCombo技术#k技能！一开始你对这项训练持怀疑态度，因为老人患有老年痴呆症，但是，它非常有效！）", 2);
    } else if (status == 4) {
        qm.sendPrev("现在向#p1201000#汇报。我知道当她看到你所取得的进步时，她一定会欣喜若狂！");
    } else if (status == 5) {
        qm.dispose();
    }
}