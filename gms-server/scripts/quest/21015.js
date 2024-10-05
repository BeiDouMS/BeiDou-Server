var status = -1;

function start(mode, type, selection) {
    if (mode == 0 && type == 0) {
        status--;
    } else if (mode == -1) {
        qm.dispose();
        return;
    } else {
        status++;
    }
    if (status == 0) {
        qm.sendNext("Alright, I've done enough explaining for now. Let's move on to the next stage. What's the next stage, you ask? I just told you. Train as hard as you can until you become strong enough to defeat the Black Mage with a single blow.");
    } else if (status == 1) {
        qm.sendNextPrev("You may have been a hero in the past, but that was hundreds of years ago. Even if it weren't for the curse of the Black Mage, all those years you spent frozen in time have stiffened your body. You must loosen up a bit and slowly regain your agility. How do you do that, you ask?");
    } else if (status == 2) {
        qm.sendAcceptDecline("Don't you know that you must first master the fundamentals? So the wise thing to do is to begin with #bBasic Training#k. Oh, of course, I forgot that you lost your memory. Well, that's why I'm here. You'll just have to experience it yourself. Shall we begin?");
    } else if (status == 3) {
        if (mode == 0) {
            qm.sendOk("What are you so hesitant about? You're a hero! You gotta strike while the iron is hot! Come on, let's do this!");
        } else {
            qm.forceStartQuest();
            qm.showInfo("Effect/OnUserEff.img/guideEffect/aranTutorial/tutorialArrow3");
        }
        qm.dispose();
    }
}