var status = -1;

function end(mode) {
    if (mode == -1) {
        qm.dispose();
        return;
    }

    status++;
    switch (status) {
        case 0:
            qm.sendSimple("Did you manage to reach the Evil Eye Cave? Did you really find the kid living there?\r\n#b#L0#(You tell him about getting to the entrance of the Puppeteer&apos;s Cave.)#l\n#k");
            break;
        case 1:
            qm.sendOk("So I guess he really does live there... But a password... Why would he need a password? Who would even visit that place?");
            break;
        case 2:
            qm.forceCompleteQuest();
            qm.gainExp(200);
            qm.dispose();
            break;
        default:
            qm.dispose();
            break;
    }
}