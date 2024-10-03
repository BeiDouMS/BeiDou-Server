/*
    NPC Name: 		Cygnus
    Description: 		Quest - Encounter with the Young Queen
*/

var status = -1;

function start(mode, type, selection) {
    if (mode == -1 || mode == 0 && type > 0) {
        qm.dispose();
        return;
    }

    if (mode == 1) {
        status++;
    } else {
        if (status == 2) {
            qm.dispose();
            return;
        }
        status--;
    }
    if (status == 0) {
        qm.sendNext("What&apos;s that? #p1101002# sent you? Oh! Wow, are you a newbie? Good to meet ya. Really. I&apos;m #p1102000#. I&apos;m the Training Instructor who&apos;s in charge of Noblesse like you. But... why are you looking at me like that...? Ah, this must be your first time seeing a Piyo.");
    } else if (status == 1) {
        qm.sendNextPrev("We are called Piyos. You&apos;ve talked to #p1101001# who&apos;s always next to the Empress, haven&apos;t you? Well, Piyos are from the same family as #p1101001# though we differ in type. Piyos only live in Ereve, so I&apos;m sure it&apos;ll take some time for you to get used to us.");
    } else if (status == 2) {
        qm.sendNextPrev("Oh, and did you know that there are no monsters in Ereve? Not even a smidgeon of evil dare enter Ereve. But don&apos;t you worry. You&apos;ll be able to train with illusory monsters created by #p1101001# called Mimis. Shall we begin the training now?");
    } else if (status == 3) {
        qm.sendAcceptDecline("shall we begin your second training then? Let&apos;s have you hunt #r#o100123##ks this time. It should be a bit more challenging. What are #o100123#s, you wonder? You probably saw them while you were hunting the #o100122#s... They somewhat resemble the #o10012");
    } else if (status == 4) {
        qm.guideHint(12);
        qm.forceStartQuest(20020);
        qm.forceCompleteQuest(20100);
        qm.forceStartQuest();
        qm.dispose();
    }
}