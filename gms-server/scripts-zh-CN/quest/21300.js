var status = -1;

function start(mode, type, selection) {
    if (mode == -1) { // END CHAT
        return qm.dispose();
    }

    if (type == 0 && mode == 0) { // PREV
        status--;
    } else {
        status++;
    }

    switch (status) {
        case 0:
            return qm.sendNext("修炼得如何？嗯……70级了……虽然还不够，不过比起当初把你刚从冰窟里挖出来的那个状态要强百倍了。像这样下去，很快你就能恢复从前的实力了。")
        case 1:
            return qm.sendAcceptDecline("不过，你得先回#b#m140000000##k一趟。你的#b#p1201001##k又出现了奇怪的反应。似乎是有什么话要对你说。说不定能唤醒你的能力也说不定，赶紧回去一趟吧。");
        case 2:
            if (type == 12 && mode == 0) {
                qm.sendOk("你知道吗？不要以为先升级升到70级，以后再转职也可以。到时候辛辛苦苦累计的SP值没法用于3转技能，你就傻眼了。当然，我也不是说#p1201001#非要给你转职不可……只是提前说明一下，供你参考。");
                return qm.dispose();
            }
            if (!qm.isQuestStarted(21300) && !qm.isQuestCompleted(21300)) {
                qm.forceStartQuest();
            }
            return qm.dispose();
        default:
            return qm.dispose();
    }
}