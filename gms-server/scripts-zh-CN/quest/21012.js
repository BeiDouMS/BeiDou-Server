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
            qm.sendNext("英雄！你好！啊？你难道不知道自己是英雄吗？前面3个人都喊那么大声了，我还能听不见吗？整个岛都知道英雄苏醒的事情了。");
            break;
        case 1:
            qm.sendNextPrev("咦，你怎么好像不开心的样子？有什么问题吗？啊？不知道自己到底是不是英雄？你失忆了吗？怎么会……看样子是被封冻在冰里数百年来的后遗症。");
            break;
        case 2:
            qm.sendAcceptDecline("嗯，既然你是英雄，挥挥剑也许就会想起什么来呢？试着去#b打猎怪兽#k，怎么样？");
            break;
        case 3:
            if (type == 12 && mode == 0) { // DECLINE
                qm.sendOk("嗯……说不定这方法能够让你恢复记忆～不论怎样，还是值得一试的。");
                qm.dispose();
                break;
            }
            // ACCEPT
            if (!qm.isQuestStarted(21012)) {
                qm.forceStartQuest();
            }
            qm.sendNext("对了，这附近有许多#r#o9300383##k，请击退 #r3只#k试试，说不定你就能想起什么了。");
            break;
        case 4:
            qm.sendNextPrev("哦，你应该还没有忘记使用技能的方法吧？#b将技能拖到快捷栏上，以方便使用#k。除了技能以外，消费道具也可以拖到这里来方便使用。");
            break;
        case 5:
            qm.guideHint(17);
            qm.dispose();
            break;
        default:
            qm.dispose();
            break;
    }
}

function end(mode, type, selection) {
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
            qm.sendYesNo("嗯……看你的表情就知道你啥都没想起来。不过不用担心。说不定这反倒更好。来，这里有一些药水，加油吧！\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0#\r\n#v2000022# #t2000022# 10个\r\n#v2000023# #t2000023# 10个\r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0# 57 exp");
            break;
        case 1:
            if (type == 1 && mode == 0) { // NO
                qm.sendNext("哎哟……？不喜欢药水吗？");
                qm.dispose();
                break;
            }
            // YES
            if (!qm.canHold(2000022) || !qm.canHold(2000023)) {
                qm.dropMessage(1, "背包满了");
                qm.dispose();
                break;
            }
            qm.forceCompleteQuest();
            qm.gainExp(57);
            qm.gainItem(2000022, 10);
            qm.gainItem(2000023, 10);
            qm.sendNext("#b（就算我真的是英雄……一个什么能力都没有的英雄又有什么用呢？）", 3);
        default:
            qm.dispose();
            break;
    }
}