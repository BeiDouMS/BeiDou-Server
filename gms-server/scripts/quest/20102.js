/*
 * Cygnus 1st Job advancement - Blaze Wizard
 */
/*
    Author:         Magical-H
    Description:    General script for knight transfer
 */
var job = {
    1 : "DAWNWARRIOR",				// 魂骑士
    2 : "BLAZEWIZARD",				// 炎术士
    3 : "WINDARCHER",				// 风灵使者
    4 : "NIGHTWALKER",				// 夜行者
    5 : "THUNDERBREAKER"			// 奇袭者
};
var chrLevel = {//定义角色等级限制
    1 : 10,
    2 : 30,
    3 : 70,
    4 : 120
};
var InitialEquip = {    //一转才需要配置发放装备物资
    1 : [
        {1302077 : 1},      //新手战士之剑
    ],
    2 : [
        {1372043 : 1},      //初级魔法师的杖
    ],
    3 : [
        {1452051 : 1},      //初级弓手的弓
        {2060000 : 2000}    //弓矢
    ],
    4 : [
        {1472061 : 1},      //初级飞侠拳套
        {2070000 : 2000}    //海星镖
    ],
    5 : [
        {1482014 : 1},      //新手专用指套
    ]
}
var medalid = 1142066;             //定义初级勋章，根据转职次数自动发放对应的勋章
var completeQuestID = 29906;        //定义初级勋章任务，根据转职次数自动完成对应的勋章任务
var jobId = null;
var jobLevel = null;
var QuestID = null;
var checkItem = {itemid:[],quantity:[],InitialEquipMsg:[]};

var status = -1;
var jobType = 3;
var canTryFirstJob = true;

function end(mode, type, selection) {
    if(QuestID == null) {
        QuestID = qm.getQuest();
        jobId = String(QuestID).slice(-1);  //通过任务ID获取职业ID
        jobLevel = String(QuestID).substring(2, 3);  //通过任务ID获取几转
        chrLevel = chrLevel[jobLevel];               //通过转职次数绑定等级限制

        job = Java.type('org.gms.client.Job')[job[jobId] + jobLevel];   //获取转职职业类
        medalid += (jobLevel - 1);    //绑定对应的转职勋章
        completeQuestID += (jobLevel - 1);    //绑定完成对应给予转职勋章的任务

        InitialEquip = InitialEquip[jobId];     //获取绑定的初始物品列表
        checkItem = InitialEquip.reduce((a, o) => (Object.entries(o).forEach(([i, q]) => (a.itemid.push(Number(i)), a.quantity.push(Number(q)), a.InitialEquipMsg.push(`#b#v${i}##t${i}##k * #r${q}#k`))), a), {itemid: [], quantity: [], InitialEquipMsg: []});  //初始化待检测的物品和数量列表

    }
    if (mode == 0) {
        if (status == 0) {
            qm.sendNext("This is an important decision to make.");
            qm.dispose();
            return;
        }
        status--;
    } else {
        status++;
    }
    if (status == 0) {
        qm.sendYesNo(`Have you made your decision? The decision will be final, so think carefully before deciding what to do. Are you sure you want to become a Dawn Warrior?`);
    } else if (status == 1) {
        if (canTryFirstJob) {
            canTryFirstJob = false;
            if (qm.getPlayer().getJob().getId() != (1000+(jobId*100))) {
                if(!qm.canGetFirstJob(jobType)) {
                    qm.sendOk(`Train a bit more until you reach #blevel 10,  ${qm.getFirstJobStatRequirement(jobType)}#k and I can show you the way of the #r${job.getName()}#k`);
                    qm.dispose();
                    return;
                }
                //检查是否可以持有足够数量的道具以及勋章
                if (!qm.canHoldAll(checkItem.itemid,checkItem.quantity) || !qm.canHold(medalid)) {
                    qm.sendOk(`Please first free up a certain amount of space for the backpack to receive initial equipment and supplies.\r\n\r\n\r\n${checkItem.InitialEquipMsg.join("\r\n")}\r\n#b#v${medalid}##t${medalid}##k * #r1#k`);
                    qm.dispose();
                    return;
                } else {
                    qm.changeJob(job);
                    qm.getPlayer().resetStats();
                    qm.forceCompleteQuest();
                    InitialEquip.forEach(o => (Object.entries(o).forEach(([i,q]) => (console.log(i,q),qm.gainItem(Number(i), Number(q))))));
                    qm.gainItem(medalid, 1);
                    qm.sendNext(`From this moment on, the Queen appoints you as #b Junior Knight #k! \Bring the initial supplies I have prepared for you and start your training!\r\n\r\n${checkItem.InitialEquipMsg.join("\r\n")}\r\n#b#v${medalid}##t${medalid}##k * #r1#k`);
                }
            }
        } else {
            qm.sendNext("Unknown error");
            qm.dispose();
            return;
        }
    } else if (status == 2) {
        qm.sendNextPrev("I have also expanded your inventory slot counts for your equipment and etc. inventory. Use those slots wisely and fill them up with items required for Knights to carry..");
    } else if (status == 3) {
        qm.sendNextPrev("I have also given you a hint of #bSP#k, so open the #bSkill Menu#k to acquire new skills. Of course, you can't raise them at all once, and there are some skills out there where you won't be able to acquire them unless you master the basic skills first.");
    } else if (status == 4) {
        qm.sendNextPrev("Unlike your time as a Noblesse, once you become the Dawn Warrior, you will lost a portion of your EXP when you run out of HP, okay?");
    } else if (status == 5) {
        qm.sendNextPrev("Now... I want you to go out there and show the world how the Knights of Cygnus operate.");
    } else if (status == 6) {
        qm.dispose();
    }
}