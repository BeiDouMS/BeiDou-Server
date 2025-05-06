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
        qm.sendNext("“看来你现在已经热身了。这时候严格的训练真的能帮助你打下坚实的基础。我们开始基础训练吧，好吗？”", 8);
    } else if (status == 1) {
        qm.sendNextPrev("这次去#b#m140020200##k打败一些 #r#o0100133##k。我想大约#r20#k就行了。继续前进……嗯？你有什么想说的吗？?", 8);
    } else if (status == 2) {
        qm.sendNextPrev("“数字不是越来越大了吗”?", 2);
    } else if (status == 3) {
        qm.sendNextPrev("“当然是这样。怎么，你对20不满意吗？你想打败100个吗？哦，那999个怎么样？沉睡森林里有人能轻松做到。毕竟，我们正在训练……”", 8);
    } else if (status == 4) {
        qm.sendNextPrev("“哦不，不，不。二十已经足够了”.", 2);
    } else if (status == 5) {
        qm.sendAcceptDecline("“你不必如此谦虚。我理解你想要快速成为曾经的英雄的愿望。这种态度正是你成为英雄的原因。”.");
    } else if (status == 6) {
        if (mode == 0 && type == 15) {
            qm.sendNext("#b(“你因为害怕而拒绝了，但你不能就这样逃避。深呼吸，冷静下来，然后再试一次。”)#k", 2);
            qm.dispose();
        } else {
            if (!qm.isQuestStarted(21017)) {
                qm.forceStartQuest();
            }
            qm.sendNext("#b(“你接受了，想着如果让她继续说下去，你可能最终要处理999个这样的请求。”)#k", 2);
        }
    } else if (status == 7) {
        qm.sendNextPrev("请继续击杀20个 #r#o0100133##.", 8);
    } else if (status == 8) {
        qm.showInfo("Effect/OnUserEff.img/guideEffect/aranTutorial/tutorialArrow3");
        qm.dispose();
    }
}