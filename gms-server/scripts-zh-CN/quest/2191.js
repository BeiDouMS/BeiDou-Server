
var status = -1;

function start(mode, type, selection) {
    status++;
    if (mode != 1) {
        if (type == 1 && mode == 0) {
            status -= 2;
        } else {
            qm.sendOk("你不想当拳手？");
            qm.dispose();
            return;
        }
    }
    if (status == 0) {
        qm.sendAcceptDecline("已经到了等级30了啊。为了当上拳手而来找我的吗？那么你还真是找对人了。");
    } else if (status == 1) {
        qm.sendNext("但我不能直接就让你转职，你要向我见证你的力量和强壮。拳手是一种利用全身和拳头来击退敌人的海盗。所以想做一个优秀的拳手的话要会好好利用拳头才行。一定要记住这一点。");
    } else if (status == 2) {
        qm.sendNext("见证的方法很简单。我把你送到考试的地方，你在那里好好对抗#r附子章鱼#k并收集15个#b强大力量的结晶#k带回来，你觉得很简单？不要这么轻敌！他们只能用单体攻击，你一定要记住。其他的技巧或者攻击都没有作用。可能有点难了，但为了成为一个优秀的拳手还是要好好拿出精神来大干一场...");
    } else if (status == 3) {
        qm.startQuest();
        qm.warp(108000502, 0);
        qm.dispose();
    }
}

	
