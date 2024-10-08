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
            qm.sendAcceptDecline("英雄，你好！我是看管雪橇哈士奇犬的#p1202007#。不好意思打扰你了，只是能够帮助我的只有英雄你一人了……如果你不是太忙的话，能否听听我的苦衷？");
            break;
        case 1:
            if (type == 12 && mode == 0) { // DECLINE
                return qm.dispose();
            }
            // ACCEPT
            if (!qm.isQuestStarted(21600) && !qm.isQuestCompleted(21600)) {
                qm.forceStartQuest();
            }
            qm.sendNext("就是不久前的事情。我像平时一样照看可爱的哈士奇犬们，却发现有个奇怪的家伙夹在他们中间。毛色光泽都很不一样，牙齿也锐利很多……怎么看都不像一只哈士奇犬。");
            break;
        case 2:
            qm.sendNextPrev("我开始还以为是只变种的哈士奇犬。后来一查才发现那家伙不是哈士奇，而是只#b狼#k！里恩岛上根本没有狼，也不知道是从哪里混进来的……很奇怪不是吗？");
            break;
        case 3:
            qm.sendNextPrev("我也知道不能把狗和狼一起养，但这小狼崽才刚刚出生，丢掉又太不近人情了。再加上小狼崽的身体还很弱。所以，我打算把这只小狼崽养到他能自食其力的大小。");
            break;
        case 4:
            qm.sendNextPrev("虽然我很精通犬类的饲养，但如何养狼却是一窍不通。所以必须找人帮忙。#b#m230000000#的某个地方#k住着一个叫#b#p2060000##k的人，懂得饲养狼的办法。所以想请英雄去见见她，请求她的帮助。谢谢你了。");
            break;
        case 5:
            qm.sendPrev("得到#p2060000#的同意后，她应该会给你一个东西，你把那个东西带回来给我就行。我的家就在里恩村旁边，#b#m140020100##k附近。");
            break;
        default:
            return qm.dispose();
    }
}