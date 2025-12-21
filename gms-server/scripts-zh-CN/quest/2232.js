/*
 * 任务 ID : 2232
 * 任务名称 : 去登记后辈吧！
 * 任务描述 : 族长艾尔给出的课题非常简单！就是如果有想要资助的角色，就将其登记为后辈。登记一名以上的后辈后向族长艾尔报告吧。
 * End NPC : -1
 *
 * @author ArthurZhu1992
 *
 */

let status = -1;

function end(mode, type, selection) {
    // 统一处理状态更新逻辑
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
        if (!qm.isQuestActive(2232)) {
            qm.forceStartQuest();
            qm.dispose();
            return;
        }

        // 检查是否已登记后辈
        if (familyEntry != null && familyEntry.getJuniorCount() > 0) {
            qm.sendNext("噢！看来你已经找到了想要资助的后辈了啊。干得好！");
        } else {
            qm.sendOk("唔，看来你还没有找到可以资助的后辈？\n(请按 #b[F]#k 键打开学院界面，尝试登记后辈吧。)");
            qm.dispose();
        }
    } else if (status == 1) {
        // 检查是否已登记两名后辈
        if (familyEntry != null && familyEntry.getJuniorCount() >= 2) {
            qm.sendNext("两名后辈都登记满了吗？不错~ 看来我果然没有看错人，你的领导力非常优秀！");
        } else {
            qm.sendNext("虽然已经登记了一名后辈，但如果有机会，也试着去寻找第二名后辈吧。两个人总比一个人要更有安全感，不是吗？");
        }
    } else if (status == 2) {
        qm.sendNext("那么，请努力成为一名优秀的导师吧。");
    } else if (status == 3) {
        qm.gainExp(2000);
        qm.forceCompleteQuest();
        qm.dispose();
    }
}
