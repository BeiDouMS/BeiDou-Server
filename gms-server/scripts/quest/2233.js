/*
 * Quest ID : 2233
 * Quest Name : Let's Raise Reputation!
 * Quest Description : Master Al's task this time is to reach #r1,000 Reputation#k! This is no easy feat. According to Master Al, this cannot be achieved without leadership and caring for your Juniors. His tip? Help your Juniors gain EXP and level up as much as possible.
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
        if (!qm.isQuestActive(2233)) {
            qm.forceStartQuest();
            qm.dispose();
            return;
        }

        // Check if Reputation is 1,000 or more
        if (familyEntry != null && familyEntry.getReputation() >= 1000) {
            qm.sendNext("Oh~! Your Reputation is already over 1,000... You've become quite a famous adventurer. Outstanding job!");
        } else {
            qm.sendOk("Hmm, it seems you haven't gathered 1,000 Reputation points yet. It might be challenging, but if you hunt together with your Juniors, you'll reach it in no time!");
            qm.dispose();
        }
    } else if (status == 1) {
        qm.sendNext("Seeing your activity makes my old blood boil with excitement! I should go out and find some promising young Juniors myself. What do you think? Raising Reputation isn't that hard, right? The key is to provide unwavering support and attention so your Juniors can grow quickly.");
    } else if (status == 2) {
        qm.sendNext("Well then, please continue to be a great Senior.");
    } else if (status == 3) {
        qm.showQuestCompleteEffect();
        qm.gainExp(2400);
        qm.forceCompleteQuest();
        qm.dispose();
    }
}
