/*
===========================================================
            GitHub: DMagical-H
    NPC Name:       SELF
    Map(s):         Mushroom Castle investigative trigger
    Description:    Upon reaching the barrier or castle wall.
=============================================================
Version 1.0 - Script Done.(2024-10-23)
=============================================================
*/

function start() {
    var mapId = cm.getMapId();
    if (mapId == 106020300) {
        if (cm.isQuestActive(2314) && cm.getQuestProgressInt(2314) == 0) {
            level2314();
        } else if (cm.haveItem(2430014)) {
            level2430014();
        } else {
            leveldispose();
        }
    } else if (mapId == 106020500) {
        if (cm.isQuestActive(2322) && cm.getQuestProgressInt(2322) == 0) {
            level2322();
        } else {
            leveldispose();
        }
    } else {
        leveldispose();
    }
}

function level2314() {
    cm.showInfo("Effect/OnUserEff/normalEffect/mushroomcastle/chatBalloon3");
    cm.sendNextLevel("2314_1", "这里……似乎有些不对劲。", 3);
}

function level2314_1() {
    cm.sendNextLevel("2314_2", "我感觉到一股强大的魔力正挡在入口前。", 3);
}

function level2314_2() {
    cm.sendNextLevel("2314_3", "这显然不是普通的障碍，应该就是 #b#p1300003##k 提到的结界。", 3);
}

function level2314_3() {
    cm.sendOkLevel("dispose", "先回去把这里的情况报告给 #b#p1300003##k 吧。", 3);
    cm.setQuestProgress(2314, 1);
}

function level2322() {
    cm.sendOkLevel("dispose", "城墙上长满了带刺的藤蔓，看来得先回去向 #b#p1300003##k 报告。", 3);
    cm.setQuestProgress(2322, 1);
}

function level2430014() {
    cm.sendOkLevel("dispose", "也许可以在这附近使用 #b#t2430014##k 来解除魔法结界。", 3);
}

function leveldispose() {
    cm.dispose();
}
