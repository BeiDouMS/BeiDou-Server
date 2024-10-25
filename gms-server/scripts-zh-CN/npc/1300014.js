/*
===========================================================
			GitHub：@Magical-H
	NPC Name: 		SELF
	Map(s): 		Mushroom Castle: Deep inside Mushroom Forest(106020300) 蘑菇森林深处
	Description: 	Upon reaching the magic barrier.    到达魔法孢子结界之后往回走即可触发
	仅北·斗 Bei-dou服务端或其他支持NextLevel函数的服务端可用
=============================================================
Version 1.0 - Script Done.(2024-10-23)
=============================================================
*/

function start() {
    var MapId = cm.getMapId();
    cm.setQuestProgress(2322,0);
    if(MapId == 106020300) {    //蘑菇森林深处
        if (cm.isQuestActive(2314) && cm.getQuestProgressInt(2314) == 0) level2314();
        else if (cm.haveItem(2430014)) level2430014();
        else leveldispose();
    } else if(MapId == 106020500) { //城墙中央
        if(cm.isQuestActive(2322) && cm.getQuestProgressInt(2322) == 0) level2322();
        else leveldispose();
    } else {
        leveldispose();
    }
}

function level2314(){
    cm.showInfo("Effect/OnUserEff/normalEffect/mushroomcastle/chatBalloon3");
    cm.sendNextLevel('2314_1','这里...似乎有点奇怪...？！',3);
}
function level2314_1(){
    cm.sendNextLevel('2314_2','嗯...似乎有一种无形的力量在阻止我通过入口。',3);
}
function level2314_2(){
    cm.sendNextLevel('2314_3','显然这不是普通的障碍，否则我不可能过不去，也许...这应该是 #e#b#p1300003##k#n 提到的结界了。',3);
}
function level2314_3(){
    cm.sendOkLevel('dispose','看来还是得先回去跟 #e#b#p1300003##k#n 报告一下了。',3);
    cm.setQuestProgress(2314,1);
}

function level2322(){
    cm.sendOkLevel('dispose','嗯...城墙爬满了#r长着尖刺得藤蔓#k，确实有点棘手，看来是过不去了。\r\n\r\n还是先回去跟 #e#b#p1300003##k#n 报告一下吧。',3);
    cm.setQuestProgress(2322,1);
}

function level2430014(){
    cm.sendOkLevel('dispose','在这附近使用#e#b#v2430014##t2430014##n#k应该就能消除魔法结界了吧。',3);
}

function leveldispose(){
    cm.dispose();
}