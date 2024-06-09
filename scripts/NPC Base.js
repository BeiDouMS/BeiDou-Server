/**
 * @author: lee
 * @npc: npc
 * @map: Multiple towns on MapleStory
 * @func: func
 */

let status;

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
        cm.sendOk("Sample text.");
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