var status = -1;

function end(mode, type, selection) {
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
            return qm.sendYesNo("抓到#o9001013#了吗？呵呵呵……真不愧是我的主人！很好，把你找到的#t4032312#给我吧……啊……？怎么不说话？难道……你没找到？");
        case 1:
            if (type == 1 && mode == 0) { // NO
                return qm.dispose();
            }
            return qm.sendNext("什么？！没找到#t4032312#？怎么会这样？！你忘了吗？啊，怎么……再怎么被黑魔法师诅咒，再怎么被封冻几百年也不能让你变得这么笨啊……", 1 << 3);
        case 2:
            return qm.sendNextPrev("不行。我不能绝望。主人糊里糊涂，我可不能糊涂……深呼吸……深呼吸……", 1 << 3);
        case 3:
            return qm.sendNextPrev("再去找找，反正小偷已经逃走。看来必须重新做#t4032312#了。以前也做过一次，你知道需要哪些材料吧？快去搜集材料……", 1 << 3);
        case 4:
            return qm.sendNextPrev("   #i4001173#");
        case 5:
            return qm.sendNextPrev("……彻底没希望了。啊啊啊！", 1 << 3);
        case 6:
            return qm.sendNextPrev("#b（#p1201002#正在气头上。先撤再说。说不定#p1201000#能给我什么帮助。）", 1 << 1);
        case 7:
            if (!qm.isQuestCompleted(21301)) {
                qm.forceCompleteQuest();
            }
        default:
            return qm.dispose();
    }
}