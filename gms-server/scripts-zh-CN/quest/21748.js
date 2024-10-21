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
            return qm.sendNext("我听说了，为了调查黑色之翼的事件，你去了#m250000000#，战神。辛苦了。不过……这次是不是又中了黑色之翼的招儿？", 1 << 3);
        case 1:
            return qm.sendNextPrev("#b（讲述#m250000000#封印石的事情。）", 1 << 1);
        case 2:
            return qm.sendNextPrev("……什么？是英雄……过去的你把封印石交给大家的吗？武陵封印石被抢走了也没关系。这个情报可意义重大！", 1 << 3);
        case 3:
            return qm.sendNextPrev("意义重大？", 1 << 1);
        case 4:
            return qm.sendNextPrev("既然过去封印石是英雄的东西，那么#b只要对英雄进行一些调查，哪怕是很琐碎的一些情报，说不定就能发现封印石的下落了呢#k？那样的话，我们就能在黑色之翼之前，找到封印石了！", 1 << 3);
        case 5:
            return qm.sendNextPrev("原来是这样，真是个好办法！", 1 << 1);
        case 6:
            return qm.sendYesNo("呵呵呵……太好了！现在又斗志昂扬了吧？来，战神！这是新的技能！");
        case 7:
            if (type == 1 && mode == 0) { // NO
                return qm.dispose();
            }
            // YES
            qm.gainExp(20000);
            qm.teachSkill(21100002, 0, 30, -1); // final charge
            qm.forceCompleteQuest();
            qm.sendOk("看来应该重新调查英雄的行踪了！#p1002104#大叔会继续打听关于黑色之翼的信息，你还是专心修炼吧！一定要练到把黑色之翼鼻子打扁的程度啊！");
            return qm.dispose();
        default:
            return qm.dispose();
    }
}