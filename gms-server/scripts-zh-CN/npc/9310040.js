/*
	NPC Name: 		国清
	Map(s): 		Mount Song: Mountainside (702030000)
	Description: 	Quest NPC - 寻找师兄 chain
	Quest: 			8538 (寻找师兄1), 8539 (寻找师兄2)
*/

var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
        return;
    }
    if (mode == 0) {
        cm.dispose();
        return;
    }
    status++;

    if (status == 0) {
        if (cm.isQuestStarted(8538) && cm.haveItem(4031786, 1)) {
            cm.sendYesNo("阿弥陀佛，贫僧有礼了。不知施主找贫僧，所为何事。");
        } else if (cm.isQuestStarted(8539)) {
            cm.sendNext("请施主早日将书信送与我那小师弟，免得他挂念，于修行不利。");
            cm.dispose();
        } else {
            cm.sendNext("修行啊修行");
            cm.dispose();
        }
    } else if (status == 1) {
        cm.completeQuest(8538);
        cm.sendNext("太谢谢你了，出来这么久让师弟担心了啊~等会再来找我。");
    } else if (status == 2) {
        cm.startQuest(8539);
        cm.sendNextPrev("原来是#b#p9310052##k师弟所托，贫僧入世修行未满，还不能回寺。这里有书信一封，请施主带给我的小师弟。");
    } else if (status == 3) {
        cm.sendNextPrev("如此多谢施主了。");
    } else {
        cm.dispose();
    }
}
