let status = -1;
let map = 910220000;
let num = 5;
let maxp = 5;

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode === 1) {
        status++;
    } else {
        if (status <= 1) {
            cm.dispose();
            return;
        }
        status--;
    }
    if (status === 0) {
        if (cm.getLevel() >= 20) {
            cm.sendOk("#r只有低于20级才能进入训练中心。");
            cm.dispose();
            return;
        }

        let selStr = "你要进入训练中心吗？";
        for (let i = 0; i < num; i++) {
            selStr += "\r\n#b#L" + i + "#训练中心 " + i + " (" + cm.getPlayerCount(map + i) + "/" + maxp + ")#l#k";
        }
        cm.sendSimple(selStr);
    } else if (status === 1) {
        if (selection < 0 || selection >= num) {
            cm.dispose();
        } else if (cm.getPlayerCount(map + selection) >= maxp) {
            cm.sendNext("#r里面满人了，换个训练中心或频道试试。");
            status = -1;
        } else {
            cm.warp(map + selection, 0);
            cm.dispose();
        }
    }
}