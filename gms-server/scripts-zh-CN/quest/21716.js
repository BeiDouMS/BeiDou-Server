var status = -1;

function start(mode, type, selection) {
    if (mode == -1) { // END CHAT
        qm.dispose();
        return;
    }

    if (type == 0 && mode == 0) { // PREV
        status--;
    } else {
        status++;
    }

    switch (status) {
        case 0:
            qm.sendNext("#p1032112#说了什么？", 8);
            break;
        case 1:
            qm.sendNextPrev("#b（#p1032112#告诉我，不久前，有个奇怪的孩子来到这里，那个孩子手上好像拿着人偶。好像从那之后魔法森林中就出现了奇怪的怪物......）#k", 2);
            break;
        case 2:
            qm.sendAcceptDecline("吼...抱着人偶的小孩...不得不叫人怀疑。是有人故意把怪物变成为凶暴的证据啊...");
            break;
        case 3:
            if (type == 12 && mode == 0) { // DECLINE
                qm.sendNext("什么？我想除了那个孩子没有其他嫌疑人。请再想想。");
                qm.dispose();
                break;
            }
            // ACCEPT
            if (!qm.isQuestStarted(21716)) {
                qm.forceStartQuest();
            }
            qm.sendNext("魔法森林的和平已经被打破......这种恶行绝对不能饶恕......看来我得提醒村民们最近一定要多加小心。", 2);
            break;
        case 4:
            qm.sendPrev("#b（不过话说回来，村庄的人们真的有办法应对残暴的绿蘑菇吗？应该会很辛苦的说...既然找出引起绿蘑菇暴走的原因，现在把搜集到的情报告诉#p1002104#吧。）#k", 2);
            break;
        default:
            qm.dispose();
            break;
    }
}