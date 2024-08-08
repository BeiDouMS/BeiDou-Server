/**
 * @author: lee
 * @npc: npc
 * @map: Multiple towns on MapleStory
 * @func: func
 */

let status, skinId;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode === -1 || (mode === 0 && type > 0)) {
        cm.dispose();
        return;
    }

    updateStatus(mode);

    if (status === 0) {
        let msg = "";

        let skinIds = [];
        for (let i = 0; i <= 29; i++) {
            skinIds.push(i);
        }
        for (let i = 1001; i <= 1007; i++) {
            skinIds.push(i);
        }
        for (let i = 1010; i <= 1012; i++) {
            skinIds.push(i);
        }
        for (let i = 1015; i <= 1021; i++) {
            skinIds.push(i);
        }
        for (let i = 1024; i <= 1043; i++) {
            skinIds.push(i);
        }
        skinIds.push(1045);
        skinIds.push(1048);
        skinIds.push(1049);
        for (let i = 1051; i <= 1054; i++) {
            skinIds.push(i);
        }
        skinIds.push(1056);
        for (let i = 1058; i <= 1060; i++) {
            skinIds.push(i);
        }

        skinIds.forEach(id => {
            msg = msg + "#L" + id + "#" + id + "  #fEffect/BasicEff.img/NoRed0/" + (id !== 0 ? id : "") + "Miss##l\r\n";
        })
        cm.sendSimple(msg);
    } else if (status == 1) {
        skinId = selection;
        let msg = "你确定要使用这款皮肤吗？\r\n\r\n"
        let line1 = "", line2 = "", line3 = "", line4 = "";
        for (let i = 0; i <= 9; i++) {
            line1 = line1 + "#fEffect/BasicEff.img/NoRed0/" + (skinId !== 0 ? skinId * 10 + i : i) + "#";
            //line2 = line2 + "#fEffect/BasicEff.img/NoRed1/" + (skinId !== 0 ? skinId * 10 + i : i) + "#";
            line3 = line3 + "#fEffect/BasicEff.img/NoCri0/" + (skinId !== 0 ? skinId * 10 + i : i) + "#";
            //line4 = line4 + "#fEffect/BasicEff.img/NoCri1/" + (skinId !== 0 ? skinId * 10 + i : i) + "#";
        }
        msg = msg + line1 + "\r\n↑↑↑↑↑↑ 常规效果 ↑↑↑↑↑↑\r\n" + line2 + "\r\n" + line3 + "\r\n↑↑↑↑↑↑ 强力效果 ↑↑↑↑↑↑\r\n" + line4 + "\r\n#fEffect/BasicEff.img/NoRed0/" + (skinId !== 0 ? skinId : "") + "Miss"
        cm.sendNextPrev(msg);
    } else if (status == 2) {
        let player = cm.getPlayer();
        player.setDamageSkin(skinId);
        player.getMap().broadcastDamageSkin(player.getId(), skinId);
        cm.sendOk("设置成功！");
        cm.dispose();
    }
}

function updateStatus(mode) {
    if (mode === 1) {
        status++;
    } else {
        status--;
    }
}

function generateMenu(array) {
    // #fUI/Basic.img/HScr4/enabled/next2#
    let menu = "";
    for (let i = 1; i <= array.length; i++) {
        menu += "#L" + i + "##b" + i + ". " + array[i - 1] + "#k#l\r\n";
    }
    return menu;
}