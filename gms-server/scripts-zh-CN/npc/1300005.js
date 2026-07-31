var status = -1;
var introQuestIds = [2300, 2301, 2302, 2303, 2304, 2305, 2306, 2307, 2308, 2309, 2310];

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
        return;
    }

    if (mode == 0 && type > 0) {
        cm.dispose();
        return;
    }

    if (mode == 1) {
        status++;
    } else {
        status--;
    }

    if (status == 0) {
        if (!hasMushroomIntroCompleted()) {
            cm.sendOk("请先去和管理员对话。");
            cm.dispose();
        } else if (!cm.isQuestStarted(2312) && !cm.isQuestCompleted(2312)) {
            cm.sendAcceptDecline("勇士，我们王国正面临巨大的危机。在把拯救王国的重任交给你之前，我需要先确认你的实力。你愿意接受测试吗？");
        } else if (cm.isQuestStarted(2312)) {
            if (!cm.haveItem(4000499, 50)) {
                cm.sendOk("请去消灭得意的蘑菇仔，并带回50个变种孢子。");
                cm.dispose();
            } else {
                cm.sendNext("你已经教训过那些得意的蘑菇仔了吗？");
            }
        } else if (!cm.isQuestStarted(2313) && !cm.isQuestCompleted(2313)) {
            cm.sendAcceptDecline("我已经把你的表现告诉内务大臣了。请你马上去见他吧。");
        } else if (cm.isQuestStarted(2313)) {
            cm.sendNext("请你一定要拯救我们的王国！");
        } else {
            cm.sendOk("请尽快去见内务大臣。");
            cm.dispose();
        }
    } else if (status == 1) {
        if (!cm.isQuestStarted(2312) && !cm.isQuestCompleted(2312)) {
            cm.startQuest(2312);
            cm.sendOk("从这里继续往前走，你会看到很多得意的蘑菇仔。请教训它们，并带回50个变种孢子作为证明。");
            cm.dispose();
        } else if (cm.isQuestStarted(2312)) {
            cm.completeQuest(2312);
            cm.gainExp(11500);
            cm.gainItem(4000499, -50);
            cm.sendOk("真了不起。请原谅我之前的怀疑。现在，请你拯救我们的蘑菇王国吧！");
            cm.dispose();
        } else if (!cm.isQuestStarted(2313) && !cm.isQuestCompleted(2313)) {
            cm.startQuest(2313);
            cm.sendOk("请你一定要拯救我们的王国！");
            cm.dispose();
        } else if (cm.isQuestStarted(2313)) {
            cm.completeQuest(2313);
            cm.gainExp(4000);
            cm.dispose();
        } else {
            cm.dispose();
        }
    }
}

function hasMushroomIntroCompleted() {
    for (var i = 0; i < introQuestIds.length; i++) {
        if (cm.isQuestCompleted(introQuestIds[i])) {
            return true;
        }
    }
    return false;
}
