/*
 * Quest ID : 2234
 * Quest Name : Enjoy the Privileges of a Famous Adventurer!
 * Quest Description : This task is to experience #rFamily Privileges#k! #bMaster Al#k wants you to use your privileges until your #rCurrent Reputation is 500 or less#k. Also, to prove you've gathered enough fame, you must reach 2,000 Total Reputation.
 * End NPC : -1
 *
 * @author ArthurZhu1992
 */

let status = -1;

function end(mode, type, selection) {
    // Standard state machine logic
    if (mode == 1 && type != 1 && type != 11) {
        status++;
    } else {
        if ((type == 1 || type == 11) && mode == 1) {
            status++;
            selection = 1;
        } else if ((type == 1 || type == 11) && mode == 0) {
            status++;
            selection = 0;
        } else {
            qm.dispose();
            return;
        }
    }

    const player = qm.getPlayer();
    const familyEntry = player.getFamilyEntry();

    if (status == 0) {
        if (!qm.isQuestActive(2234)) {
            qm.forceStartQuest();
            qm.dispose();
            return;
        }

        // Check conditions: Total Rep >= 2000 AND Current Rep <= 500
        if (familyEntry != null &&
            familyEntry.getTotalReputation() >= 2000 &&
            familyEntry.getReputation() <= 500) {
            qm.sendNext("Oh~! Have you finally tasted the #bPrivileges of a Famous Adventurer#k? Hahaha, looking at your remaining Reputation, it seems you've enjoyed them thoroughly. I didn't expect you to achieve this so quickly... your leadership skills are truly innate.");
        } else {
            qm.sendOk("Hmm, it seems you haven't fully enjoyed your #bFamily Privileges#k yet.\n\nYou need to raise your #rTotal Reputation#k to #b2,000#k or more, and spend your #rCurrent Reputation#k down to #b500#k or less by using privileges.");
            qm.dispose();
        }
    } else if (status == 1) {
        qm.sendNext("Well then, please continue to be an excellent Senior. If you have any more questions about the Family System or are interested in titles, feel free to come see me anytime.");
    } else if (status == 2) {
        qm.showQuestCompleteEffect();
        qm.gainExp(3000);
        qm.forceCompleteQuest();
        qm.dispose();
    }
}
