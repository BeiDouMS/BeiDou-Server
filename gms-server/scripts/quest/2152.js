var status = -1;

function start(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
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
            qm.sendNext("那棵树。。。我以前听说过，我甚至研究过它的行为！如果我没记错，在某些条件下它会复活，开始吸取这些可怕的邪恶力量，这使得它们对附近的人和村子造成非常可怕的影响。");
            qm.forceCompleteQuest();
        } else if (status == 1) {
            qm.dispose();
        }
    }
}

function end(mode, type, selection) {
    qm.dispose();
}
