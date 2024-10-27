/* ===========================================================
        @author Resonance
	NPC Name: 		Minister of Home Affairs
	Map(s): 		Mushroom Castle: Corner of Mushroom Forest(106020000)
	Description: 	Quest -  Exploring Mushroom Forest(1)
=============================================================
Version 1.0 - Script Done.(18/7/2010)
=============================================================
*/

var status = -1;

function start(mode, type, selection) {
    status++;
    if (mode != 1) {
        if (type == 1 && mode == 0) {
            status -= 2;
        } else {
            qm.sendOk("请不要抛弃我们蘑菇王国。");
            qm.dispose();
            return;
        }
    }
    if (status == 0) {
        qm.sendAcceptDecline("要想救出公主，必须对蘑菇森林进行调查。不知道企鹅王用了什么手段，在蘑菇森林设下了强力的结界，阻止外人进入蘑菇城。请你马上去进行调查。");
    } else if (status == 1) {
        qm.forceStartQuest();
        // qm.sendOk("从这里往东一直走，就可以碰到蘑菇森林的结界。请你小心。听说整个森林已被凶恶的怪物所占据。");
        qm.dispose();
    } //else if (status == 2) {
    //     // qm.forceStartQuest();
    //     // qm.forceStartQuest(2314,"1");
    //     // qm.gainExp(8300);
    //     // qm.sendOk("我明白了，所以无论如何它都不是一个常规的障碍。干得好。如果没有你的帮助，我们根本不知道这是怎么回事。");
    //     // qm.forceCompleteQuest();
    //     qm.dispose();
    // } else if (status == 3) {
    //     qm.dispose();
    // }
}

function end(mode, type, selection) {
    status++;
    if (mode != 1) {
        if (type == 1 && mode == 0) {
            status -= 2;
        } else {
            qm.dispose();
            return;
        }
    }
    if (status == 0) {
        qm.sendOk("你对蘑菇森林的结界进行调查了吗？结果怎么样？");
    } else if (status == 1) {
        qm.forceCompleteQuest();
        qm.gainExp(8300);
        qm.sendOk("是吗？果然不是普通的结界。辛苦你了。如果没有你的话，我们就不可能知道这一情况。");
    } else if (status == 2) {
        qm.dispose();
    }
}

