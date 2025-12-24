/*
 * Quest ID : 2232
 * Quest Name : Registering a Junior!
 * Quest Description : Master Al's task is simple! If there's a character you'd like to mentor, register them as your Junior. Once you've registered at least one Junior, report back to Master Al.
 * End NPC : -1
 *
 * @author ArthurZhu1992
 */

let status = -1;

function end(mode, type, selection) {
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
        if (!qm.isQuestActive(2232)) {
            qm.forceStartQuest();
            qm.dispose();
            return;
        }

        // Check if Junior is registered
        if (familyEntry != null && familyEntry.getJuniorCount() > 0) {
            qm.sendNext("Oh! It looks like you've found a Junior to sponsor. Great job!");
        } else {
            qm.sendOk("Hmm, it seems you haven't found a Junior to sponsor yet.\n(Press the #b[F]#k key to open the Family window and try to register a Junior.)");
            qm.dispose();
        }
    } else if (status == 1) {
        // Check if both Junior slots are filled
        if (familyEntry != null && familyEntry.getJuniorCount() >= 2) {
            qm.sendNext("You've registered both Juniors already? Impressive! I knew I had a good eye for talent. Your leadership is outstanding!");
        } else {
            qm.sendNext("Even if you've only registered one Junior, try to find another one when you can. After all, two Juniors are better than one, don't you think?");
        }
    } else if (status == 2) {
        qm.sendNext("Well then, please continue to be a great Senior to your Juniors.");
    } else if (status == 3) {
        qm.showQuestCompleteEffect();
        qm.gainExp(2000);
        qm.forceCompleteQuest();
        qm.dispose();
    }
}
