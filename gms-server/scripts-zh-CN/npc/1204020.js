/**
 * NPC: 1204020 - 影子武士
 */

var dialogDepth = -1;

const mobID = 9300351;
const questID = 21747;
const npcID = 1204020;
const mapID = 925040100;

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
            return cm.sendNext('我一直在等你……英雄的后裔啊……', 1 << 3);
        case 1:
            return cm.sendNextPrev('#b（英雄的后裔……？#o9300351#似乎知道一些关于英雄的事情。不过，他好像也和#p2091007#一样，不认为我是英雄本人啊。）', 1 << 1);
        case 2:
            return cm.sendNextPrev('这个#b武陵封印石#k是英雄们撒下的种子……但收获的却是我们黑色之翼的东西。虽然你很漂亮地打败了#p1104000#和#p1204010#……再也不能让你为所欲为了。', 1 << 3);
        case 3:
            return cm.sendNextPrev('英雄的后裔终于和敌人见面了，真是让人感慨万分……这也是没办法的事情。我要以黑色之翼的名义，干掉你！', 1 << 3);
        case 4:
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