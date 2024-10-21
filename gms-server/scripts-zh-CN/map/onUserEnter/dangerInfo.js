/**
 * Map: 910400000
 */

var questID = 21733;
var questInfoNumber = 21762;

var puppeteerNpcID = 1204006;

var fakeTruMobID = 9300382;
var puppeteerMobID = 9300345;

var dangerInfoShopMapID = 910400000;
var normalInfoShopMapID = 104000004;
var lithHarborMapID = 104000000;

function start(ms) {
    // should not enter if the quest could be completed
    if (ms.getQuestProgressInt(questID, questInfoNumber) == 2) {
        ms.warp(normalInfoShopMapID);
        return;
    }

    const mapObj = ms.getWarpMap(dangerInfoShopMapID);
    // should not enter if there are other players.
    if (mapObj.countPlayers() > 0) {
        ms.warp(lithHarborMapID);
        return;
    }

    // have killed the boss, no need to spawn anything
    if (ms.getQuestProgressInt(questID, puppeteerMobID) > 0) {
        mapObj.resetPQ(1);
        mapObj.destroyNPC(puppeteerNpcID);
        return;
    }

    const LifeFactory = Java.type('org.gms.server.life.LifeFactory');
    const Point = Java.type('java.awt.Point');

    // have talked to puppeteer NPC, just spawn a puppeteer mob
    if (ms.getQuestProgressInt(questID, questInfoNumber) == 1) {
        mapObj.resetPQ(1);
        mapObj.destroyNPC(puppeteerNpcID);

        // resetPQ will set increase progress to 1
        ms.setQuestProgress(questID, puppeteerMobID, 0);

        mapObj.spawnMonsterOnGroundBelow(LifeFactory.getMonster(puppeteerMobID), new Point(125, 0));
        return;
    } 

    // spawn puppeteer NPC
    ms.spawnNpc(puppeteerNpcID, new Point(75, 0), mapObj);
    // spawn a fake Tru
    const monsterObj = LifeFactory.getMonster(fakeTruMobID);
    monsterObj.setStance(4);
    mapObj.spawnFakeMonsterOnGroundBelow(monsterObj, new Point(225, 0));

    ms.getPlayer().message('情报商店被人偶师占领了！确认情况后，击退人偶师！');
}