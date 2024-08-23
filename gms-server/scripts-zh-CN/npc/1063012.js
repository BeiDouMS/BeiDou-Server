var maps = Array(105050200, 105060000, 105070000, 105090000, 105090000, 105090100);

function start() {
    if (cm.getQuestStatus(2236) != 1) {
        cm.dispose();
        return;
    }

    var mapId = cm.getPlayer().getMapId();
    var playerY = cm.getPlayer().getPosition().y;
    var slot;

    for (var i = 0; i < maps.length; i++) {
        if (mapId == maps[i]) {
            if (mapId == 105090000 && playerY < 78) {
                slot = 4;
            } else if (mapId == 105090000 && playerY > 78) {
                slot = 3;
            } else {
                slot = i;
            }
            break;
        }
    }

    if (typeof slot === 'undefined') {
        cm.dispose();
        return;
    }

    var progress = cm.getQuestProgress(2236,1);
    var ch = progress[slot];

    if (ch == '0') {
        var nextProgress = progress.substring(0, slot) + '1' + progress.substring(slot + 1);
        cm.gainItem(4032263, -1);
        cm.setQuestProgress(2236, 1 , nextProgress);
        cm.sendOk("由于灵符的法力，封印了该地区的邪恶势力。");
        cm.dispose();
    }

    cm.dispose();
}
