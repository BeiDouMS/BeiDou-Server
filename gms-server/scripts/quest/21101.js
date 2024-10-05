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
            qm.sendNext("#b(You touch the #p1201001#. The polearm is supposed to be ice cold, but it feels so…warm. Makes you feel like your old memories are starting to return.)#k", 3);
            break;
        case 1:
            qm.sendNext("#b(…the hero that wielded the polearm was also the master of melee combat, with amazing strength and stamina...)#k", 3);
            break;
        case 2:
            qm.sendNext("#b(…the hero had high levels of STR but also some DEX, which meant the hero moved with agility...)#k", 3);
            break;
        case 3:
            qm.sendYesNo("#b(Is this from your memories or the memories of a fellow hero…? In order to find out for sure, you have to touch the #p1201001# one more time.)#k", 3);
            break;
        case 4:
            if (mode == 0) {
                qm.sendOk("#b(You need to think about this for a second...)#k", 3);
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
            qm.sendOk("Your #bequip#k inventory is full. You need to make at least 1 empty slot to complete this quest.");
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
        qm.sendOk("#b(You might be starting to remember something...)#k", 3);
    }
    qm.forceCompleteQuest();
    qm.dispose();
}