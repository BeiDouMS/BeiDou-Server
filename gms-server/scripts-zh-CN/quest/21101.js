var status = -1;

function start(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
        return;
    }

    if (type == 0 && mode == 0) {
        status--;
    } else {
        status++;
    }

    switch (status) {
        case 0:
            qm.sendNext("#b(抚摸着#p1201001#，本来冰凉的战斧上却传来了温暖的感觉，令我似乎想起了些过去的事情。)#k", 3);
            break;
        case 1:
            qm.sendNext("#b(...使用战斧的英雄曾经是一位拥有强劲力量和充沛体力，能够自由使用多种技能进行近身战的战士...)#k", 3);
            break;
        case 2:
            qm.sendNext("#b(...虽然拥有很高的STR，但还有一些DEX，所以并非全靠力气吃饭...)#k", 3);
            break;
        case 3:
            qm.sendYesNo("#b(这是我自己的记忆吗？还是对同伴英雄的记忆呢？...还得再摸一次#p1201001#试试看。)#k", 3);
            break;
        case 4:
            if (mode == 0) {
                qm.sendOk("#b(请慎重考虑清楚。)#k", 3);
            } else {
                qm.forceStartQuest();
            }
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