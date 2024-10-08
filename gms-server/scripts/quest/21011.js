var status = -1;

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
            qm.sendNext("Wait, are you... No way... Are you the hero that Lilin has been talking about all this time?! Lilin! Don't just nod... Tell me! Is this the hero you've been waiting for?!");
            break;
        case 1:
            qm.sendNextPrev("   #i4001171#");
            break;
        case 2:
            qm.sendNextPrev("I'm sorry. I'm just so overcome with emotions... *Sniff sniff* My goodness, I'm starting to tear up. You must be so happy, Lilin.");
            break;
        case 3:
            qm.sendNextPrev("Wait a minute... You're not carrying any weapons. From what I've hear, each of the heroes had a special weapon. Oh, you must have lost it during the battle against the Black Mage.");
            break;
        case 4:
            qm.sendYesNo("This isn't good enough to replace your weapon, but #bcarry this sword with you for now#k. It's my gift to you. A hero can't be walking around empty-handed.\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0#\r\n#v1302000# 1 #t1302000#\r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0# 35 exp");
            break;
        case 5:
            if (type == 1 && mode == 0) { // NO
                qm.sendNext("*sniff sniff* Isn't this sword good enough for you, just for now? I'd be so honored...");
                qm.dispose();
                break;
            }
            // YES
            if (qm.canHold(1302000)) {
                qm.gainItem(1302000, 1);
                qm.gainExp(35);
                qm.forceCompleteQuest();
                qm.sendNext("#b(Your skills are nowhere close to being hero-like... But a sword? Have you ever even held a sword in your lifetime? You can't remember... How do you even equip it?)", 3);
            } else {
                qm.dropMessage(1, "Your inventory is full");
            }
            break;
        case 6:
            qm.guideHint(16);
            qm.dispose();
            break;
        default:
            qm.dispose();
            break;
    }
}