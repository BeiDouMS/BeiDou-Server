var status;
function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1 || (mode == 0 && status == 0)) {
        cm.dispose();
        return;
    } else if (mode == 0) {
        status--;
    } else {
        status++;
    }

    if (status == 0) {
        cm.sendGetText("The door reacts to the entry pass inserted. #bPassword#k!");
    } else if (status == 1) {
        var passwordInput = cm.getText();
        var questPassword = cm.getQuestProgress(3360, 0);
        var progress = cm.getQuestProgress(3360, 1);
        if (passwordInput == questPassword) {
            if (progress !== "11") {
                updateQuestProgress(cm, progress);
            }

            const PacketCreator = Java.type('org.gms.util.PacketCreator');
            cm.getPlayer().sendPacket(PacketCreator.playPortalSound());
            cm.warp(261030000, "sp_" + (cm.getMapId() == 261010000 ? "jenu" : "alca"));
        } else {
            cm.sendOk("#rWrong!");
        }
        cm.dispose();
    }
}


function updateQuestProgress(cm, progress) {
    if (cm.getMapId() == 261020200) {
        if (progress == "10") {
            cm.setQuestProgress(3360, 1, "11");
            cm.forceCompleteQuest(3360);
        } else {
            cm.setQuestProgress(3360, 1, "01");
        }
    } else if (cm.getMapId() == 261010000) {
        if (progress == "01") {
            cm.setQuestProgress(3360, 1, "11");
            cm.forceCompleteQuest(3360);
        } else {
            cm.setQuestProgress(3360, 1, "10");
        }
    }

}
