/*
 * 任务 ID : 2233
 * 任务名称 : 去提升名声值吧！
 * 任务描述 : 这次 #b族长艾尔#k 给出的课题是 #r名声值达到 1,000#k！这是一个不小的目标。 根据艾尔的说法，如果没有领导力和对后辈的细心照顾，是绝对无法达成的。他给出的提示是尽量帮助后辈获得经验值和升级。
 * End NPC : -1
 * 
 * @author ArthurZhu1992
 *
 */

let status = -1;

function end(mode, type, selection) {
    // 统一处理状态机逻辑
    if (mode == 1 && type != 1 && type != 11) {
        status++;
    } else {
        if ((type == 1 || type == 11) && mode == 1) {
            status++;
            selection = 1;
        } else if ((type == 1 || type == 11) && mode == 0) {
            status++;
            selection = 0;
        } else {
            qm.dispose();
            return;
        }
    }

    const player = qm.getPlayer();
    const familyEntry = player.getFamilyEntry();

    if (status == 0) {
        if (!qm.isQuestActive(2233)) {
            qm.forceStartQuest();
            qm.dispose();
            return;
        }

        // 检查玩家声望值是否达到要求
        if (familyEntry != null && familyEntry.getReputation() >= 1000) {
            qm.sendNext("噢~！名声值竟然已经超过 1,000 了啊……你现在也算是一位赫赫有名的冒险家了。做得太出色了！");
        } else {
            qm.sendOk("唔，看来你还没攒够 1,000 点名声值啊？虽然可能有点困难，但只要和后辈们一起狩猎，很快就能达成的！");
            qm.dispose();
        }
    } else if (status == 1) {
        qm.sendNext("看到你这么活跃，我这把老骨头也感到热血沸腾了。我也得赶紧再去物色一些年轻有为的后辈才行。怎么样？提升名声值其实并不难吧？核心秘诀就是毫不吝啬地给予后辈支持和关注，让他们快速成长。");
    } else if (status == 2) {
        qm.sendNext("那么，今后也请继续努力，成为一名优秀的导师。");
    } else if (status == 3) {
        qm.gainExp(2400);
        qm.forceCompleteQuest();
        qm.dispose();
    }
}
