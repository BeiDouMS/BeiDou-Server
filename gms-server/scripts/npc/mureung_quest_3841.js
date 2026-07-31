var status = -1;

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
        return;
    }

    if (mode == 0) {
        if (status == 0) {
            cm.sendOk("Are you declining my invitation? That's... too bad. I really wanted to dispell the rumors and be on good terms with Mu Lung once more...");
        }
        cm.dispose();
        return;
    }

    status++;

    if (status == 0) {
        if (cm.getQuestStatus(3840) == 2 && cm.getQuestStatus(3841) == 0 && cm.getPlayer().getLevel() >= 77 && cm.getPlayer().getMapId() == 250010503) {
            cm.sendYesNo("Hey traveler~ I can tell you're from Do Gong, based on that hideous-looking item you are carrying. Throw that away, and just listen to what I have to say, okay? I will be more than happy to dispell the rumors surrounding me here. Please come... hohoho~");
        } else {
            cm.dispose();
        }
    } else if (status == 1) {
        cm.forceStartQuest(3841, 2091004);
        cm.sendOk("You have entered the deepest part of Goblin Forest... But if you go through the #bRock Support of the Disappeared Statue at the Second Goblin Forest#k, then it won't be as hard for you to enter. Please come to this area immediately. I will show you what I am really about, the King Sage Cats.");
    } else {
        cm.dispose();
    }
}
