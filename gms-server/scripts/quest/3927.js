var status = -1;
var firstSelection;
function end(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
        return;
    } else {
        if (mode == 0 && type > 0) {
            qm.dispose();
            return;
        }

        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            if(qm.getQuestProgress(3927) == "1"){
                qm.sendSimple("What did the wall say?\r\n#L0##bIf I had a hammer and a dagger, a bow and an arrow...#l\r\n#L1# Byron loves Eirel #l\r\n#L2# Ah, I forgot.");
            } else {
                qm.sendOk("I guess you haven't found #p2103001# yet. Anyway, finding the sand thief will be difficult for you...");
                qm.dispose();
            }
        } else if (status == 1) {
            if (selection == 0) {
                qm.sendSimple("Did you find the wall?\r\n#L0##bI did, but... I don't know what it's saying.#l");
            } else {
                qm.sendOk("I guess you haven't found #p2103001# yet. Anyway, finding the sand thief will be difficult for you...");
                qm.dispose();
            }
        } else if (status == 2) {
            if (selection == 0) {
                qm.sendOk("If I had a hammer and a dagger... a bow and arrow... What does this mean? Do you want me to tell you? I don't know myself. This is something you should think about. If you need a clue... It would be something like this... A weapon is just an item... until someone uses it?");
            }
            qm.gainExp(28843);
            qm.forceCompleteQuest();
            qm.dispose();
        }
    }
}
