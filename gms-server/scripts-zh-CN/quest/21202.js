var status = -1;

function start(mode, type, selection) {
    if (mode == -1) { // END CHAT
        qm.dispose();
        return;
    }

    if (type == 0 && mode == 0) { // PREV
        status--;
    } else {
        status++;
    }

    switch (status) {
        case 0:
            qm.sendNext("呵呵……年轻人来这么偏僻的地方干嘛？", 8);
            break;
        case 1:
            qm.sendNextPrev("我想要最厉害的长矛！", 2);
            break
        case 2:
            qm.sendNextPrev("最厉害的长矛？那种东西在小村子里怎么有卖的……", 8);
            break;
        case 3:
            qm.sendNextPrev("我知道你就是冒险岛世界里最厉害的铁匠！我想要你做的武器！", 2);
            break;
        case 4:
            qm.sendAcceptDecline("我这个老头子太老了，哪还能做什么优秀的武器啊。倒是有一支很久以前做的长矛……不过却不能给你。那个家伙太锋利，弄不好连主人都会被伤到。这种武器你还想要吗？");
            break;
        case 5:
            if (type == 12 && mode == 0) { // DECLINE
                qm.sendNext("你不想努力获得终极武器吗？");
                qm.dispose();
                break;
            }
            // ACCEPT
            qm.startQuest();
            qm.sendOk("呵呵……既然你这么说，我这个老头子就试一试你。你去旁边的#b修炼场#k，打败那些#r#o9001012##k，取回#b#t4032311#30个#k给我。", 8);
            break;
        default:
            qm.dispose();
            break;
    }
}

function end(mode, type, selection) {
    if (mode == -1) { // END CHAT
        qm.dispose();
        return;
    }

    if (type == 0 && mode == 0) { // PREV
        status--;
    } else {
        status++;
    }

    switch (status) {
        case 0:
            qm.sendNext("哎呦～#t4032311#都取回来了？你………比我想象的还要厉害一些嘛。不过，对于随时都可能伤到自己的危险武器，你那种毫不畏惧的爽朗豪放的心态实在是……好吧，#p1201001#就给你了。");
            break;
        case 1:
            qm.sendNextPrev("#b（过了好一会儿，#p1203000#才郑重地将裏在布里的#p1201001#交给我。）");
            break;
        case 2:
            qm.sendYesNo("这就是专门为你而做的长矛，名叫#p1201002#……以后就拜托了。");
            break;
        case 3:
            if (type == 1 && mode == 0) { // NO
                qm.sendNext("嗯？经历了这么多之后你现在还犹豫不决吗？好吧，如果你愿意的话再好好考虑考虑。反正最后它会是你的。");
                qm.dispose();
                break;
            }
            // YES
            // qm.showVideo("Polearm");
            qm.completeQuest();
            qm.removeAll(4032311);
            qm.dispose();
        default:
            qm.dispose();
            break;
    }
}