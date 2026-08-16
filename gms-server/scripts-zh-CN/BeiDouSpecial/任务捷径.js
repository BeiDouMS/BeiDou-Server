/**
 * 任务捷径：对进行中的「杀怪 / 非任务物品收集」任务补齐进度。
 * 费用写死 10 万冒险币；进度已齐不收费。
 */
var COST = 100000;
var status = -1;
var eligible = [];
var selectedQuestId = -1;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode === -1 || (mode === 0 && status <= 0)) {
        cm.dispose();
        return;
    }
    if (mode === 1) {
        status++;
    } else {
        status--;
    }

    if (status === 0) {
        eligible = toJsArray(cm.getQuestShortcutEligibleIds());
        if (eligible.length === 0) {
            cm.sendOk("哟，看你一脸轻松……目前没有我能插手的任务啊。\r\n#b我只管杀怪计数、以及收集普通掉落物的任务#k，那些要交任务专属道具、走剧情脚本的，可帮不了你。");
            cm.dispose();
            return;
        }

        var text = "是有任务搞不定了吗？我可以帮你，但你懂的，需要意思意思……\r\n";
        text += "#r每次 #e" + COST + "#n 冒险币#k，帮你把杀怪进度拉满、缺的普通材料补齐。\r\n";
        text += "#b进度本来就够的，我分文不取——你自己去交任务就行。#k\r\n\r\n";
        text += "来，挑一个让你头疼的：\r\n";
        for (var i = 0; i < eligible.length; i++) {
            var qid = eligible[i];
            var name = cm.getQuestName(qid);
            if (!name) {
                name = "未命名任务";
            }
            text += "#L" + i + "##b[" + qid + "] " + name + "#k#l\r\n";
        }
        cm.sendSimple(text);
    } else if (status === 1) {
        if (selection < 0 || selection >= eligible.length) {
            cm.sendOk("嗯？你点到空气了。再来一次吧。");
            cm.dispose();
            return;
        }
        selectedQuestId = eligible[selection];
        var qName = cm.getQuestName(selectedQuestId);
        if (!qName) {
            qName = "任务#" + selectedQuestId;
        }

        if (cm.isQuestShortcutProgressSatisfied(selectedQuestId)) {
            cm.sendOk("《" + qName + "》的进度我瞅了一眼——#b你其实已经够格交任务了#k。\r\n省下那 " + COST + " 冒险币，去找对应 NPC 交差吧，别在我这儿浪费表情。");
            cm.dispose();
            return;
        }

        cm.sendYesNo("《" + qName + "》是吧？\r\n\r\n一手交钱，一手交货：\r\n#r扣除 " + COST + " 冒险币#k\r\n#b杀怪计数拉满 + 缺的普通材料补齐#k\r\n\r\n交任务还得你自己跑一趟，我又不是你腿。同意就点是？");
    } else if (status === 2) {
        var qName = cm.getQuestName(selectedQuestId);
        if (!qName) {
            qName = "任务#" + selectedQuestId;
        }
        var result = cm.applyQuestShortcut(selectedQuestId, COST);
        if (result === 0) {
            cm.sendOk("成交！《" + qName + "》的脏活累活我替你干完了。\r\n#b记得去找任务 NPC 交任务#k——钱我收了，脸我可替你丢不了。");
        } else if (result === 2) {
            cm.sendOk("意思意思也得有本钱啊……你这钱包比任务进度还干瘪，先攒够 #r" + COST + "#k 冒险币再来。");
        } else if (result === 3) {
            cm.sendOk("背包塞得比仓鼠颊帮还满，我往哪儿塞材料？#b腾出点空位#k再来找我。");
        } else if (result === 4) {
            cm.sendOk("等等，你这进度刚才不知怎么已经齐了？那我就不收钱了，直接去交任务吧。");
        } else {
            cm.sendOk("这单我接不了——任务状态不对，或者不在我的业务范围内。换个别的试试？");
        }
        cm.dispose();
    } else {
        cm.dispose();
    }
}

function toJsArray(javaList) {
    var arr = [];
    if (javaList == null) {
        return arr;
    }
    for (var i = 0; i < javaList.size(); i++) {
        arr.push(javaList.get(i));
    }
    return arr;
}
