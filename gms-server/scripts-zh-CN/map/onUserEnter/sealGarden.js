/**
 * Map: 920030001
 */

var questID = 21739;
var mapID = 920030001
var giantDagothNpcID = 1204010;
var giantDagothMobID = 9300348;

function start(ms) {
    const mapObj = ms.getMap(mapID);
    if (ms.getQuestProgressInt(questID, giantDagothMobID) > 0 || mapObj.countMonster(giantDagothMobID) > 0 || mapObj.containsNPC(giantDagothNpcID)) {
        return;
    }
    
    const Point = Java.type('java.awt.Point');
    ms.spawnNpc(giantDagothNpcID, new Point(740, 0), mapObj);
}