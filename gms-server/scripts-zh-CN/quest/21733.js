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
        qm.dispose();
        return;
    }

    if (type == 0 && mode == 0) { // PREV
        status--;
    } else {
        status++;
    }

    if (status == 0) {
        qm.sendSimple("喂，你在哪？有一件急事！\r\n#b#L0#(喂……？#p1002104#以前不是叫我英雄的吗……)#l\r\n#k");
        return;
    } else if (status == 1) {
        if (mode == 0) { // END CHAT
            qm.dispose();
            return;
        }
        qm.sendAcceptDecline("我有很重要的情报！赶紧到#b#m104000004##k来！");
    } else {
        if (mode == 0) { // DECLINE
            qm.sendOk("你说什么呢？很着急！别废话赶紧来！");
            qm.dispose();
            return;
        }
        // ACCEPT
        qm.forceStartQuest();
        qm.dispose();
    }
}

function end(mode, type, selection) {
    if (mode == -1) { // END CHAT
        qm.dispose();
        return;
    }

    if (type == 0 && mode == 0) { // PREV
        status--;
    } else {
        status++;
    }

    switch (status) {
        case 0:
            qm.sendNext('啊……没想到还会碰上这种事情。怎么都没想到人偶师还会潜伏到这里来。平时大概是疏于修炼了，完全被对方给算计了。', 1 << 3);
            break;
        case 1:
            qm.sendNextPrev('对不起，都是因为我……', 1 << 1);
            break;
        case 2:
            qm.sendNextPrev('啊？英雄大人不必内疚。你也不知道那家伙会来啊。不必道歉。不过，这也暴露出了他们的弱点。', 1 << 3);
            break;
        case 3:
            qm.sendNextPrev('弱点？', 1 << 1);
            break;
        case 4:
            qm.sendNextPrev('人偶师讨厌的那个文件。如果那个文件是假的，人偶师是不会这么兴师动众，带着一群人跑来折腾的。那个文件充分证明了黑色之翼的目标其实是金银岛封印石。', 1 << 3);
            break;
        case 5:
            qm.sendNextPrev('话虽这么说，但我的位置也暴露了。', 1 << 1);
            break;
        case 6:
            qm.sendYesNo('别担心。这次我为了拿利琳寄过来的文件才出去的，没想到中了敌人的招。平时我不会这么不小心的。好歹也是个情报商人，总会为自己准备一条退路的。现在关键的是#b精准矛#k这个技能你知道吗？');
            break;
        case 7:
            if (type == 1 && mode == 0) { // NO
                qm.dispose();
                break;
            }
            // YES
            qm.gainExp(8000);
            qm.teachSkill(21100000, 0, 20, -1); // polearm mastery
            qm.forceCompleteQuest();
            qm.sendOk('黑色之翼再怎么兴风作浪也没法阻止你日益强大起来。继续努力，直到击败黑魔法师为止。我也会尽最大努力为你收集信息的。');
            break;
        default:
            qm.dispose();
            break;
    }
}