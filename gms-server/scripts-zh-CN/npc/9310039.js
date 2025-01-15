/**
 * 通用组队副本脚本
 * 北斗项目组
 * @author: @Magical-H
 */
const EventName = 'YaoSengPQ' , PartyName = '少林密室';
const EventLevel = 1;           //怪物HP倍率，提高此值可以成倍提高怪物血量。
const entryMap = 702060000;     //进入的地图ID
const recruitMap = 702070400;   //必须在此地图才能接任务
const OpenRemotely = false;     //是否允许远程打开，只允许在指定地图打开，其它地图无法打开。
const QuestID = 8534;           //需要完成的前置任务ID
var status = 0;
var state;
var em = null;
var PartyInfo = null;

function start(){
    if(!OpenRemotely && cm.getMapId() != recruitMap) {  //防止远程打开
        level();
        return;
    }
    if(QuestID != null && QuestID > 0 && !cm.isQuestCompleted(QuestID)) {
        cm.sendOkLevel('','...');
        return;
    }
    if(em == null) {
        em = cm.getEventManager(EventName);
        PartyInfo = em.getProperty("party");
    }

    if (em == null || em.getName() != EventName) {
        cm.sendOkLevel('',`#e#b<组队任务> ${PartyName}#k#n 遇到了一个错误。`);
    } else {
        levelStart();
    }
}

function level() {
    cm.dispose();
}
function levelnull() {
    level();
}
function levelStart() {
    let msg = `#e#b<组队任务> ${PartyName}#n\r\n${PartyInfo}#k\r\n\r\n`;
        msg += `你和你的队伍成员一起完成任务怎么样？\r\n在这里，你会遇到障碍和问题，如果没有出色的团队合作，你是无法完成的。\r\n如果你想尝试，请告诉你的#b队长#k来找我谈谈。#b\r\n`;
        msg += `#L0#我想参加组队任务。#l\r\n`;
        msg += `#L1#我想 ${(cm.getPlayer().isRecvPartySearchInviteEnabled() ? "关闭" : "开启")} 组队搜索。#l\r\n`;
        msg += `#L2#我想了解更多细节。#l`;
    cm.sendSelectLevel(msg);
}
function level0() {
    let msg;
    if (cm.getParty() == null) {
        msg = "只有当你加入一个队伍时，你才能参加组队任务。";
    } else if (!cm.isLeader()) {
        msg = "必须由你的队长与我交谈才能开始这个组队任务。";
    } else {
        let eli = em.getEligibleParty(cm.getParty());
        if (eli.size() > 0) {
            if (!em.startInstance(cm.getParty(), cm.getPlayer().getMap(), EventLevel)) {//开始事件
                msg = "另一个队伍已经进入了该频道的#r组队任务#k。请尝试其他频道，或者等待当前队伍完成。";
            }
        } else {
            list = em.getEligibleParty(cm.getParty());
            msg = "你目前无法开始这个组队任务，因为你的队伍可能不符合人数要求，有些队员可能不符合参与条件，或者他们不在这张地图上。如果你找不到队员，可以尝试使用组队搜索功能。\r\n";
        }
    }
    if(msg) {
        cm.sendOkLevel('',msg);
    } else {
        level();
    }
}

function level1() {
    var psState = cm.getPlayer().toggleRecvPartySearchInvite();
    cm.sendOkLevel('',"你的组队搜索状态现在是：#b" + (psState ? "启用" : "禁用") + "#k。想要改变状态时随时找我谈谈。");
}
function level2() {
    cm.sendOkLevel('',`#e#b<组队任务> ${PartyName}#k#n\r\n带领你的队员到#e#b#m${entryMap}##n#k进行调查并解决问题的源头。`);
}