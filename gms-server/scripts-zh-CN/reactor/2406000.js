/*
Dragon nest
*/

function sendToHeaven() {
    rm.spawnNpc(2081008);
    rm.startQuest(100203);
    rm.mapMessage(6, "光芒闪烁间，龙蛋破壳而出，一只璀璨的幼龙降临世间！");
}

function touch() {
    if (rm.haveItem(4001094) && rm.getReactor().getState() == 0) {
        rm.hitReactor();
        rm.gainItem(4001094, -1);
    }
}

function untouch() {}

function act() {
    sendToHeaven();     // thanks Conrad for pointing out the GMS-like way of Nine Spirit's Nest
}