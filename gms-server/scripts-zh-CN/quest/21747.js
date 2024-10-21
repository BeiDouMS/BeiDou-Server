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
            return qm.sendAcceptDecline("没想到在数百年的岁月之后，英雄的后裔又重新出现了……也不知道对冒险岛世界是福还是祸……怎样都无所谓了。好吧……我告诉你有关#m250000000#封印石的事情。");
        case 1:
            if (type == 12 && mode == 0) { // DECLINE
                return qm.dispose();
            }
            // ACCEPT
            if (!qm.isQuestStarted(21747) && !qm.isQuestCompleted(21747)) {
                qm.forceStartQuest();
            }
            return qm.sendNext("#m250000000#的封印石所在的地方叫做#m925040100#。那里的入口被隐藏在#m250000100#内。你去仔细观察#m250000100#入口处熊猫提着的灯盏 。如果能从中找出#b刻有入口字样的灯盏#k，就可以进入#m925040100#了。暗号是#b道可道非常道#k。", 1);
        case 2:
            return qm.sendNextPrev("说不定那个叫#o9300351#的人已经到了#m925040100#。不过，他应该还没有把东西偷走。不知道是不是在等我……不过，相比我而言，英雄的后裔去会会#o9300351#可能更合适呢。", 1);
        case 3:
            return qm.sendNextPrev("希望你能竭尽全力阻止#o9300351#。英雄的后裔啊……继承英雄过去的光辉事业吧。", 1);
        case 4:
            return qm.sendPrev("#b（他似乎误以为我是英雄的后裔了。他说让我继承英雄过去的 光辉事业……是什么意思呢？先阻止#o9300351#，然后再问他好了。）", 1 | 1 << 1);
        default:
            return qm.dispose();
    }
}

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
            return qm.sendYesNo("成功打败了#o9300351#吗？表情怎么这么凝重……难道说你失败了……");
        case 1:
            if (type == 1 && mode == 0) { // NO
                return qm.dispose();
            }
            // YES
            return qm.sendNext("原来是这样，#m250000000#的封印石最终还是被抢走了……很遗憾，不过也没办法。我现在也不明白英雄们为什么要把封印石交给#m250000000#。", 1 | 1 << 3);
        case 2:
            return qm.sendNextPrev("你说英雄把封印石交给了#m250000000#？", 1 | 1 << 1);
        case 3:
            return qm.sendNextPrev("是的……你还不知道吗？#b很久很久以前，英雄们把封印石交给了#m250000000#。长老制作了#m925040100#并慎重看管起来#k。", 1 | 1 << 3);
        case 4:
            return qm.sendNextPrev("……英雄……", 1 | 1 << 1);
        case 5:
            return qm.sendNextPrev("这些事情，现在很少有人知道了。事实上，#b封印石没有了，对#m250000000#而言到底有没有影响，谁也不知道#k。只不过因为是英雄交给我们保管的东西，所以我们才看得很重。", 1 | 1 << 3);
        case 6:
            return qm.sendNextPrev("#b（英雄把封印石交给了#m250000000#……）", 1 | 1 << 1);
        case 7:
            return qm.sendNextPrev("把英雄交给我们的东西给弄丢了，虽说很可惜，但有英雄的后裔在，我们也觉得心里踏实一些。请继续完成英雄未尽的事业。", 1 | 1 << 3);
        case 8:
            return qm.sendPrev("#b（#m250000000#封印石也被抢走了……得去特鲁问问。）", 1 | 1 << 1);
        case 9:
            qm.gainExp(16000);
            qm.forceCompleteQuest();
        default:
            return qm.dispose();
    }
}