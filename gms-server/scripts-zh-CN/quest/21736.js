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
        qm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            qm.dispose();
            return;
        }

        if (mode == 1) {
            status++;
        } else {
            status--;
        
        if(status == 0) {
            qm.sendNext("好久不见了，英雄。这段时间等级上升很快嘛？看来你确实很拼命地在修炼啊。很勤奋。有点英雄的架势了。利琳也会为你开心的吧？");
        } else if(status == 1) {
            qm.sendNextPrev("对了，现在不是说这些的时候。我觉得光在金银岛搜集情报似乎情报面太窄，为了拓宽情报面，我已经开始在奥西利亚大陆开展搜索。一开始就选择了天空之城，没想到那里果然有问题。");
        } else if(status == 2) {
            qm.sendNextPrev("在神秘岛大陆的天空之城，好像正在发生着什么非比寻常的事。虽然不同于人偶师出现的时候，但总感觉这种奇怪的氛围一定和黑色之翼有关。怎么样，好久没遇到过这么大的事件了。会不会很激动呢？");
        } else if(status == 3) {
            qm.sendAcceptDecline("那么你准备好了吗？如果你接受的话，#b到天空之城，找到莉萨，向他询问发生在天空之城的奇怪事情......#k是怎么回事就行了。");
        } else if(status == 4) {
            qm.forceStartQuest();
            qm.dispose();
        }
    }
}