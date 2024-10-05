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
        qm.sendNext("和黑魔法师决斗的英雄...几乎没有任何相关的资讯保存下来。预言书上也只记载有五名英雄，没有任何和外貌有关的资料。你难道什么都想不起来吗？", 8);
    } else if (status == 1) {
        qm.sendNext("什么都想不起来...", 2);
    } else if (status == 2) {
        qm.sendNext("原来如此。黑魔法师的诅咒不会那么容易就被解除。可是就算如此，英雄您和过去之间应该有什么连结。到底有什么武器呢？因为决斗的关系，武器或衣服都不见了...啊！对了！#b武器#k！", 8);
    } else if (status == 3) {
        qm.sendNext("武器？", 2);
    } else if (status == 4) {
        qm.sendNext("之前在冰雪中挖掘英雄时曾经找到一些厉害的武器。当时推测应该是英雄使用过的东西，因此保存在村庄中央。您经过时没看到吗？#b#p1201001##k... \r\r#i4032372#\r\r长这样...", 8);
    } else if (status == 5) {
        qm.sendNext("难怪我觉得很奇怪，有把#p1201001#就在村庄里面...", 2);
    } else if (status == 6) {
        qm.sendAcceptDecline("对，就是那个。根据记载英雄的武器会认主人。假如您是使用#p1201001#的英雄，抓住#p1201001#时应该会有什么反应。快去点击#b#p1201001#吧。#k");
    } else if (status == 7) {
        if (mode == 0) {
            qm.sendOk("是什么让你退却了？我保证，即使#p1201001#对你没有任何反应，我也不会失望。冲过去拿起#p1201001#吧，只需#b点击#k即可。", 8);
            qm.dispose()
        } else {
            qm.forceCompleteQuest();
            qm.sendOk("假如#p1201001#有反应的话，您就是使用#p1201001#的英雄——#b战神#k.", 8);
            qm.showIntro("Effect/Direction1.img/aranTutorial/ClickPoleArm");
        }
    } else if (status == 8) {
        qm.dispose();
    }
}