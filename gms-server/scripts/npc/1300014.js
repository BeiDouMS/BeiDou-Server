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
    cm.sendNextLevel('2314_1', 'There is something strange here...', 3);
}

function level2314_1() {
    cm.sendNextLevel('2314_2', 'Hmm... some sort of invisible force is blocking the entrance.', 3);
}

function level2314_2() {
    cm.sendNextLevel('2314_3', 'This is clearly no ordinary obstacle. It must be the barrier #b#p1300003##k mentioned.', 3);
}

function level2314_3() {
    cm.sendOkLevel('dispose', 'I should head back and report this to #b#p1300003##k.', 3);
    cm.setQuestProgress(2314, 1);
}

function level2322() {
    cm.sendOkLevel('dispose', 'The castle wall is covered in thorny vines. I should report this to #b#p1300003##k first.', 3);
    cm.setQuestProgress(2322, 1);
}

function level2430014() {
    cm.sendOkLevel('dispose', 'Maybe I can use #b#t2430014##k nearby to clear the magic barrier.', 3);
}

function leveldispose() {
    cm.dispose();
}
