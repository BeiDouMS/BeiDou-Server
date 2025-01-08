/**
 * 东方神州系列地图脚本
 * 北斗项目组	https://github.com/BeiDouMS/BeiDou-Server
 * 作者：@Magical-H
 * 2025-01-02 17:33:23
 */
const isRepeat = true;	//true = 允许完成任务后重复进入。；false = 不允许完成任务后重复进入。
const mapID = 701010324;        //可怕的山丘
const EventName = 'WuGongPQ';   //事件名称
const EventLevel = 1;           //怪物HP倍率，提高此值可以成倍提高怪物血量。
const LevelMin = 25 , LevelMax = 90;        //等级范围限制
var em = null;     //事件实例

function start() {
    if(em == null) {
        em = cm.getEventManager(EventName);
    }
    if(em.getName() != EventName) {
        cm.sendOkLevel('','由于某种神秘力量，暂时无法送你过去，请联系管理员处理。');
    } else {
        cm.sendSelectLevel('',`敬礼！\r\n你好，#e#b#h ##k#n，我是#b#e#p${cm.getNpc()}##k#n\r\n\r\n#L0##b秘密任务#l\r\b\r\n#L1#离开#l#k`);
    }
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
function levelEnter() {
    let msg;
    //4103和8512均为寻找赤珠的任务，似乎是不同版本的任务。
    if(!isRepeat && (cm.isQuestCompleted(4103) || cm.isQuestCompleted(8512))) {//完成了赤珠任务将传送到一个地图
        let level = cm.getLevel();
        if(level >= LevelMin && level <= LevelMax) {
            cm.warp(mapID);
        } else {
            msg = '你目前无法执行这个#b秘密任务#k，因你不符合要求：\r\n\r\n';
            msg += `等级要求：${LevelMin} ~ ${LevelMax}`;
        }
    } else {
        var eli = em.getEligibleParty(cm.getParty());
        if (eli.size() > 0) {
            if (!em.startInstance(cm.getParty(), cm.getPlayer().getMap(), EventLevel)) {
                msg = '已经有其他人员在执行#b秘密任务#k了，请稍后再试。';
            }
        } else {
            msg = '你目前无法执行这个#b秘密任务#k，因你不符合要求：' + em.getProperty('party');
        }
    }
    if(msg) cm.sendOk(msg);
    leveldispose();
}
function level0(){
    cm.sendNextLevel('0_1',`你居然能来到这儿……果然不简单!\r\n不过你听说过这附近有很危险的怪物吗？`);
}
function level0_1() {
    if (cm.getParty() == null) {
        cm.sendOkLevel('','该项#b秘密任务#k十分危险，你需要进行一个组队才能进入执行。');
    } else if (!cm.isLeader()) {
        cm.sendOkLevel('','请让你的队长来开始这个任务。');
    } else {
        cm.sendNextLevel('Enter', `走到下一个地方时，你会遇到很危险的怪物。千万小心。\r\n再见!`);
    }
}
function level1(){
    level();
    cm.openNpc(9310006,'9310007');
}