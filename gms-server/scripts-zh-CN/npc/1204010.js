/**
 * NPC: 1204010 - 巨人塔高斯
 */

var dialogDepth = -1;

var npcID = 1204010;
var mobID = 9300348;

function start() {
    dialogDepth = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) { // END CHAT
        return cm.dispose();
    }

    if (type == 0 && mode == 0) { // PREV
        dialogDepth--;
    } else {
        dialogDepth++;
    }

    switch (dialogDepth) {
        case 0:
            cm.sendNext('嗯？怎么回事，你？', 1);
            break;
        case 1:
            cm.sendNextPrev('前不久倒是听说金银岛上的人偶师被人打倒了，难道是你……', 1);
            break;
        case 2:
            cm.sendNextPrev('嘿嘿，那反倒好办了！既拿到了#b天空之城封印石#k，又能顺便打倒你的话，我就能在人偶师之上了！出招吧！', 1);
            break;
        case 3:
            replaceNpcWithMonster();
        default:
            return cm.dispose();
    }
}

function replaceNpcWithMonster() {
    const LifeFactory = Java.type('org.gms.server.life.LifeFactory');
    const Point = Java.type('java.awt.Point');
    
    const mapObj = cm.getWarpMap(cm.getPlayer().getMapId());
    const npcPos = mapObj.getNPCById(npcID).getPosition();
    const monsterObj = LifeFactory.getMonster(mobID);
    mapObj.spawnMonsterWithEffect(monsterObj, 12, new Point(npcPos.x + 50, npcPos.y));
    mapObj.destroyNPC(npcID);
}