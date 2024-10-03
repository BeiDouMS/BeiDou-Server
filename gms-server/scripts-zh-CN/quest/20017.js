/*
    NPC Name: 		Cygnus
    Description: 		Quest - Encounter with the Young Queen
*/

var status = -1;

function start(mode, type, selection) {
    if (mode == -1 || mode == 0 && type > 0) {
        qm.dispose();
        return;
    }

    if (mode == 1) {
        status++;
    } else {
        if (status == 2) {
            qm.dispose();
            return;
        }
        status--;
    }
    if (status == 0) {
        qm.sendNext("嗯？是#p1101002#派你过来的吗？啊哈！你是这次新来的吗？很高兴认识你。很高兴认识你！我的名字是#p1102000#。是专门教你这种贵族的骑士训练师。嗯...你怎么这么看着我...啊，你好像是第一次见到菲约吧。");
    } else if (status == 1) {
        qm.sendNextPrev("我们种族被称为菲约。你和女王旁边的#p1101001#说过话吗？菲约是和#p1101001#一样的种族。虽然派系有些不同...差不多。我们只生活在圣地，虽然会有些陌生，但很快就会熟悉的。");
    } else if (status == 2) {
        qm.sendNextPrev("啊，你或许知道吧？在这圣地根本不存在怪物。拥有邪恶力量的存在是无法涉足圣地的。不过不要担心。训练是以#p1101001#所创造的幻想生物绢毛鸟作为对象来进行的。那么开始训练吗？");
    } else if (status == 3) {
        qm.sendAcceptDecline("运好气了吧！那么...看你的实力，你应该可以捕猎比中等稍强一点的绢毛鸟。捕猎在#m130010100#的15只#o0100122#该足够了吧？怎么样？能搞定#o0100122#吗？");
    } else if (status == 4) {
        qm.guideHint(12);
        qm.forceStartQuest(20020);
        qm.forceCompleteQuest(20100);
        qm.forceStartQuest();
        qm.dispose();
    }
}