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
            qm.sendYesNo("#b（让我去确认自己是不是使用#p1201001#的英雄？使劲儿抓住#p1201001#试试，肯定会有什么反应的。）");
            break;
        case 1:
            if (type == 1 && mode == 0) { // NO
                qm.sendNext("#b（让我再想想……）");
                qm.dispose();
                break;
            }
            // YES
            if (qm.getPlayer().getJob().getId() == 2000) {
                if (!qm.canHold(1142129)) {
                    qm.sendOk("你的#b装备栏#k已满。你需要腾出至少一个空位来完成这个任务。");
                    qm.dispose();
                    return;
                }
                qm.gainItem(1142129, true);

                qm.changeJobById(2100);
                qm.resetStats();

                const YamlConfig = Java.type('org.gms.config.YamlConfig');
                if (YamlConfig.config.server.USE_FULL_ARAN_SKILLSET) {
                    qm.teachSkill(21000000, 0, 10, -1);   //combo ability
                    qm.teachSkill(21001003, 0, 20, -1);   //polearm booster
                }

                //qm.getPlayer().changeSkillLevel(SkillFactory.getSkill(20009000), 0, -1);
                //qm.getPlayer().changeSkillLevel(SkillFactory.getSkill(20009000), 1, 0);
                //qm.showInfo("You have acquired the Pig's Weakness skill.");
                qm.sendNext("#b（似乎想起来了什么……）", 3);
            }
            qm.completeQuest();
            qm.dispose();
            break;
        default:
            qm.dispose();
            break;
    }
}

function end(mode, type, selection) {
    if (qm.getPlayer().getJob().getId() == 2000) {
        if (!qm.canHold(1142129)) {
            qm.sendOk("你的#b装备栏#k已满。你需要腾出至少一个空位来完成这个任务。");
            qm.dispose();
            return;
        }
        qm.gainItem(1142129, true);

        qm.changeJobById(2100);
        qm.resetStats();

        const YamlConfig = Java.type('org.gms.config.YamlConfig');
        if (YamlConfig.config.server.USE_FULL_ARAN_SKILLSET) {
            qm.teachSkill(21000000, 0, 10, -1);   //combo ability
            qm.teachSkill(21001003, 0, 20, -1);   //polearm booster
        }

        //qm.getPlayer().changeSkillLevel(SkillFactory.getSkill(20009000), 0, -1);
        //qm.getPlayer().changeSkillLevel(SkillFactory.getSkill(20009000), 1, 0);
        //qm.showInfo("You have acquired the Pig's Weakness skill.");
        qm.sendOk("#b(你可能开始记起某些事了...)#k", 3);
    }
    qm.forceCompleteQuest();
    qm.dispose();
}