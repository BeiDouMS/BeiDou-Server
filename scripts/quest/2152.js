var status = -1;

function start(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            qm.dispose();
            return;
        }

        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            qm.sendNext("你说那棵树啊，我还研究过它呢，如果我记得没错的话，当土壤因魔法而失去生机时，#b#z3220000##k就会苏醒，其他的木妖也开始陆续出现，它们靠吸收突然里的魔法而不是水和营养来生存。这些木妖对附近的村子造成不小的威胁。");
            qm.forceCompleteQuest();
        } else if (status == 1) {
            qm.dispose();
        }
    }
}

function end(mode, type, selection) {
    qm.dispose();
}