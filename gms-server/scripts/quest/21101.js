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
            qm.sendYesNo("#b(Are you certain that you were the hero that wielded the #p1201001#? Yes, you're sure. You better grab the #p1201001# really tightly. Surely it will react to you.)#k");
            break;
        case 1:
            if (type == 1 && mode == 0) { // NO
                qm.sendNext("#b(You need to think about this for a second...)#k");
                qm.dispose();
                break;
            }
            // YES
            if (qm.getPlayer().getJob().getId() == 2000) {
                if (!qm.canHold(1142129)) {
                    qm.sendOk("Wow, your #bequip#k inventory is full. You need to make at least 1 empty slot to complete this quest.");
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
                qm.sendNext("#b(You might be starting to remember something...)#k", 3);
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