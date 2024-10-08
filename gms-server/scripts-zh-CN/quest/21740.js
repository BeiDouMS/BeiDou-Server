/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2019 RonanLana

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

var status = -1;

function start(mode, type, selection) {
    if (mode == -1) {
        return qm.dispose();
    }

    if (type == 0 && mode == 0) { // PREV
        status--;
    } else {
        status++;
    }

    switch (status) {
        case 0:
            qm.sendNext("你回来了，英雄。在#m200000000#的事情办得怎么样？确实是和黑色之翼有关吧？为什么表情这么凝重？说来听听。", 1 << 3);
            break;
        case 1:
            qm.sendNextPrev("#b（讲述有关#m200000000#的封印石的事情。）", 1 << 1);
            break;
        case 2:
            qm.sendNextPrev("嗯。#m200000000#也有封印石啊……这倒是很重要的一个情报。虽说被抢走了很可惜……啊，我倒不是在怪你。没想到黑色之翼这次是有备而来啊。", 1 << 3);
            break;
        case 3:
            qm.sendNextPrev("...", 1 << 1);
            break;
        case 4:
            qm.sendAcceptDecline("振作精神！对，就是这样。#p1201000#这次又解读出来了#b新技能#k。你去#b#m140000000#见见#p1201000##k吧，正好连#m200000000#的消息也一起带过去。");
            break;
        case 5:
            if (type == 12 && mode == 0) { // DECLINE
                return qm.dispose();
            }
            // ACCEPT
            if (!qm.isQuestStarted(21740) && !qm.isQuestCompleted(21740)) {
                qm.forceStartQuest();
            }
            qm.sendNext("#p1201000#也是事件的相关人，而且对于英雄曾经生活过的那个年代，没有人比#p1201000#更了解，所以应该经常和#b#p1201000#共享情报#k，有事情最好也找他商量。");
            break;
        default:
            return qm.dispose();
    }
}

function end(mode, type, selection) {

    if (mode == -1) {
        return qm.dispose();
    }

    if (type == 0 && mode == 0) { // PREV
        status--;
    } else {
        status++;
    }

    switch (status) {
        case 0:
            qm.sendNext("啊，很久不见了。战神。这段时间修炼得还好吧？正好我发现了新的技能想叫你回来呢……你回来的正是时候！", 1 << 3);
            break;
        case 1:
            qm.sendNextPrev("#b（对#p1201000#讲述有关天空之城封印石的事情。）", 1 << 1);
            break;
        case 2:
            qm.sendNextPrev("天空之城封印石啊……原来如此，这下可以肯定黑色之翼的目标果然是封印石，而且封印石不止一个。这是一个很重要的情报。", 1 << 3);
            break;
        case 3:
            qm.sendNextPrev("不过封印石被抢走了……", 1 << 1);
            break;
        case 4:
            qm.sendYesNo("黑色之翼从很早之前就已经开始做准备了。这么看来，我们能得到#t4032323#，已经是万幸了。现在对你而言，学习技能更重要。");
            break;
        case 5:
            if (type == 1 && mode == 0) { // NO
                return qm.dispose();
            }
            // YES
            if (qm.isQuestStarted(21740) && !qm.isQuestCompleted(21740)) {
                qm.forceCompleteQuest();
                qm.teachSkill(21100004, 0, 20, -1); // combo smash
            }
            qm.sendOk("现在最重要的就是让你立刻变得强大起来。关于封印石，有我和特鲁大叔关注着呢，战神你还是专心训练吧。");
        default:
            return qm.dispose();
    }
}
