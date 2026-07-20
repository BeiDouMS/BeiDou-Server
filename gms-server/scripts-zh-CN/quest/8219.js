/* ===========================================================
            Ronan Lana
    NPC Name:       Lukan
    Description:    Quest - Storming the Castle
============================================================= */

var status = -1;

function start(mode, type, selection) {
    status++;
    if (mode != 1) {
        if (type == 1 && mode == 0) {
            status -= 2;
        } else {
            qm.sendOk("好吧，那就下次再说。");
            qm.dispose();
            return;
        }
    }
    if (status == 0) {
        qm.sendAcceptDecline("时机到了，孩子。为了继续调查最近这些怪事的源头，我们已经做好准备了。我还必须介绍你认识我的兄弟，杰克。");
    } else if (status == 1) {
        qm.sendOk("他现在正在绯红山脉一带探索，穿过阴森的幻影森林，就能前往绯红要塞。那里就是你的下一个目的地。愿你一路平安。");
        qm.forceStartQuest();
    } else if (status == 2) {
        qm.dispose();
    }
}

function end(mode, type, selection) {
    status++;
    if (mode != 1) {
        if (type == 1 && mode == 0) {
            status -= 2;
        } else {
            qm.sendOk("好吧，那就下次再说。");
            qm.dispose();
            return;
        }
    }
    if (status == 0) {
        qm.sendNext("你是谁？哦，是约翰拜托你来找我的？太好了。");
    } else if (status == 1) {
        qm.sendOk("看来你已经帮城里的人处理了不少事情，对吧？我会好好评价你的。看看这个：这是我充分探索后亲手绘制的幻影森林地图。带上它以后，你就能通过一些平时无法发现的道路。记住，#r千万不要弄丢它#k，我不会再给第二份！\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0# \r\n#i3992040# #t3992040#\r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0# 175000 经验值");
    } else if (status == 2) {
        if (qm.canHold(3992040, 1)) {
            qm.forceCompleteQuest();
            qm.gainItem(3992040, 1);
            qm.gainExp(175000);
            qm.dispose();
        } else {
            qm.sendOk("你的设置栏没有足够空间放下我要给你的东西。先整理一下背包，再和我谈谈吧。");
        }
    } else if (status == 3) {
        qm.dispose();
    }
}
