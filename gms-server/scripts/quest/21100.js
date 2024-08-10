var status = -1;

function start(mode, type, selection) {
    if (mode == 0 && type == 0) {
        status--;
    } else if (mode == -1) {
        qm.dispose();
        return;
    } else {
        status++;
    }
    if (status == 0) {
	qm.sendNext("和黑魔法师决斗的英雄...几乎没有任何相关的资讯保存下来。预言书上也只记录有五名英雄，没有任何和外貌有关的资料。你难道什么都想不起来吗？", 8);
    } else if (status == 1) {
	qm.sendNext("什么都想不起来...", 2);
    } else if (status == 2) {
	qm.sendNext("原来如此。黑魔法师的诅咒不会那么容易就被解除。可是就算如此，英雄您和过去之间应该有什么连结。到底有什么武器呢？因为决斗的关系，武器或衣服都不见了...啊！对了！ #b武器#k！", 8);
    } else if (status == 3) {
	qm.sendNext("武器？", 2);
    } else if (status == 4) {
	qm.sendNext("之前在冰雪中挖掘英雄时曾经找到一些厉害的武器。当时推测应该是英雄使用过的东西，因此保存在村庄中央。您经过时没看到吗？ #b#p1201001##k... \r\r#i4032372#\r\r长成这样...", 8);
    } else if (status == 5) {
	qm.sendNext("难怪我觉得很奇怪，有把 #p1201001# 就在村庄里面...", 2);
    } else if (status == 6) {
	qm.sendYesNo("是，就是那个。根据纪录英雄的武器会认主人。假如您是使用 #p1201001#的英雄，抓住 #p1201001#时应该会有什么反应。快点去按#b#p1201001#吧。#k");
    } else if (status == 7) {
        if (mode == 0 && type == 15) {
            qm.sendNext("What's stopping you? I promise, I won't be disappointed even if the #p1201001# shows no reaction to you. Please, rush over there and grab the #p1201001#. Just #bclick#k on it.", 8);
        } else {
            qm.forceCompleteQuest();
            qm.sendOk("假如 #p1201001#有反应的话，您就使用#p1201001#的英雄 #b狂狼勇士#k.", 8);
            qm.showIntro("Effect/Direction1.img/aranTutorial/ClickPoleArm");
        }
    } else if (status == 8) {
        qm.dispose();
    }
}