const questID = 21747;
const npcID = 1204020;
const monsterID = 9300351;
const mapID = 925040100;

function start(ms) {
    const mapObj = ms.getMap(mapID);
    if (ms.getQuestProgressInt(questID, monsterID) > 0 || mapObj.countMonster(monsterID) > 0 || mapObj.containsNPC(npcID)) {
        return;
    }
    
    const Point = Java.type('java.awt.Point');
    ms.spawnNpc(npcID, new Point(850, 0), mapObj);
}