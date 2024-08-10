/*
	NPC Name: 		Nineheart
	Description: 		Quest - Cygnus movie Intro
*/
var status = -1;

function start(mode, type, selection) {
    if (mode == -1) {
        qm.sendOk("在你决定了你真正想做什么之后再跟我说。无论你选择什么，你都不会错过或失去特权，所以不要太认真...");
        qm.dispose();
    } else {
        if(mode == 0 && type > 0 || selection == 1) {
            qm.sendOk("在你决定了你真正想做什么之后再跟我说。无论你选择什么，你都不会错过或失去特权，所以不要太认真...");
            qm.dispose();
            return;
        }

        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            qm.sendNext("我看你已经达到10级了，你已经很努力了。我想现在是时候让你脱颖而出，成为一名贵族，正式成为训练中的骑士了。不过，在这之前，我想问你一件事。你决定要成为哪个骑士了吗?");
        } else if (status == 1) {
            qm.sendNextPrev("没有一条路可以成为骑士。事实上，有五个是为你准备的。这取决于你选择你想走哪条路，但这绝对应该是你不会后悔的事情。所以。。。我愿意向你展示你成为骑士后的样子.");
        } else if (status == 2) {
            qm.sendSimple("你怎么认为？你有兴趣把自己看作骑士团的领袖吗？如果你已经决定了你想成为什么样的骑士，那么你就不必去看它了…\r\n\r\n#b#L0#让我看看我作为骑士领袖的样子.#l ..#b#L1#不，我没事.");
        } else if (status == 3) {
            qm.sendYesNo("你想现在亲自看看吗？很快就会有一个短片出来。为你即将见证的事情做好准备.");
        } else if (status == 4) {
            qm.forceStartQuest();
            qm.forceCompleteQuest();
            qm.warp(913040100, 0);
            qm.dispose();
        }
    }
}
