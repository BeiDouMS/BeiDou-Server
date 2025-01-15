/**
 * 东方神州系列地图脚本
 * 北斗项目组	https://github.com/BeiDouMS/BeiDou-Server
 * 作者：@Magical-H
 * 2025-01-02 17:33:23
 */
var mapID_out = 702070400;

function start() {
    cm.sendYesNoLevel('','Out',`你要离开#b#e#m${cm.getMapId()}##k#n 回到 #b#e#m${mapID_out}##k#n 吗？`);
}

function level() {
    leveldispose();
}
function levelnull() {
    leveldispose();
}
function leveldispose() {
    cm.dispose();
}
function levelOut() {
    cm.warp(mapID_out);
    cm.dispose();
}
