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
            qm.sendNext("与黑魔法师战斗的英雄们……有关他们的信息几乎什么都没留下。即使在预言书中也只记载了5位英雄，也没有任何有关他们外貌的描述。你还能记起来些什么吗？", 8);
            break;
        case 1:
            qm.sendNextPrev("一点都想不起来了……", 2);
            break;
        case 2:
            qm.sendNextPrev("果然，黑魔法师的沮咒果然很厉害。不过，作为英雄的你肯定和过去应该还会存在某个联系点的。会是什么呢？武器和衣服是不是在战斗中都遗失了呢……啊，对了，应该是#b武器#k！", 8);
            break;
        case 3:
            qm.sendNextPrev("武器？", 2);
            break;
        case 4:
            qm.sendNextPrev("以前，我们在冰窟中挖掘英雄的时候，发现过一个巨大的武器。我们猜测可能是英雄使用的武器，所以就放在了村子中央。你来来去去的时候没看到吗？#b#p1201001##k……\r\r#i4032372#\r\r大概长这样……", 8);
            break;
        case 5:
            qm.sendNextPrev("确实，那个#p1201001#在村子里，看起来是有些奇怪。", 2);
            break;
        case 6:
            qm.sendAcceptDecline("没错，就是那个东西。据说英雄的武器是会挑选主人。如果你就是使用#p1201001#的英雄，那么在抓住#p1201001#的刹那，武器应该会有反应的。快去点击#b#p1201001#试试#k。");
            break;
        case 7:
            if (type == 12 && mode == 0) { // DECLINE
                qm.sendNext("你还在犹豫什么？就算#p1201001#没有反应，我也没什么好失望的。快去抓住#p1201001#试试吧。需要在武器合适的地方#b点击#k才能。", 8);
                qm.dispose();
                break;
            }
            // ACCEPT
            qm.forceCompleteQuest();
            qm.sendOk("如果#p1201001#有反应，就说明你是使用过#p1201001#的英雄，是#b战神#k。", 8);
            qm.showIntro("Effect/Direction1.img/aranTutorial/ClickPoleArm");
            qm.dispose();
            break;
        default:
            qm.dispose();
            break;
    }
}