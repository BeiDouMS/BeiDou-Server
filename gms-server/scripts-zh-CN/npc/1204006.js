/*
 * NPC : Francis
 * Map : 910400000
 */

var dangerInfoShopMapID = 910400000;
var puppeteerNpcID = 1204006;
var puppeteerMobID = 9300345;

var dialogDepth = -1;

function start() {
    dialogDepth = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) { // END CHAT
        cm.dispose();
        return;
    }

    if (type == 0 && mode == 0) { // PREV
        dialogDepth--;
    } else {
        dialogDepth++;
    }

    switch (dialogDepth) {
        case 0:
            cm.sendNext('哼……真的找来了，太容易了？看来你的变身术还是有点用处的嘛。巴洛克，我们走。', 1);
            break;
        case 1:
            tryDestroyFakeTru();
            cm.sendNextPrev('切……这笔帐将来再算。', 1 | 1 << 2, 1204004);
            break;
        case 2:
            cm.sendNextPrev('来得正好。上一次因为刚和冒险骑士团的骑士们战斗完，已经没什么余力了，才会让你得逞。这次我可没那么好惹了！碍眼的家伙，消失掉吧！', 1);
            break;
        case 3:
            replaceNpcWithMonster();
            cm.setQuestProgress(21733, 21762, 1);
            cm.dispose();
            break;
        default:
            cm.dispose();
            break;
    }
}

function tryDestroyFakeTru() {
    const mapObj = cm.getWarpMap(dangerInfoShopMapID);
    if (mapObj.countMonster(9300382) > 0) {
        mapObj.killMonster(9300382);
    }
}

function replaceNpcWithMonster() {
    const LifeFactory = Java.type('org.gms.server.life.LifeFactory');
    const Point = Java.type('java.awt.Point');

    const mapObj = cm.getWarpMap(dangerInfoShopMapID);
    const npcPos = mapObj.getNPCById(puppeteerNpcID).getPosition();
    const monsterObj = LifeFactory.getMonster(puppeteerMobID);
    mapObj.spawnMonsterWithEffect(monsterObj, 12, new Point(npcPos.x + 50, npcPos.y));
    mapObj.destroyNPC(puppeteerNpcID);
}
