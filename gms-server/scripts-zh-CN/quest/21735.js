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
        return;
    }

    if (type == 0 && mode == 0) { // PREV
        status--;
    } else {
        status++;
    }

    switch (status) {
        case 0:
            qm.sendNext("#t4032323#我已经找到了。你看，呵呵呵。\r\r\r\r#i4032323#", 1 << 3);
            break;
        case 1:
            qm.sendNextPrev("!!\r\r……你怎么找到的？！", 1 << 1);
            break;
        case 2:
            qm.sendAcceptDecline("上次被人偶师攻击后，我动员了所有的情报网搜遍了整个金银岛。我怎么可能坐以待毙？一定要抢在他们前面找到他们想要的东西……也算是报了上次一箭之仇。");
            break;
        case 3:
            if (type == 12 && mode == 0) { // DECLINE
                qm.dispose();
                break;
            }
            // ACCEPT
            if (!qm.haveItem(4032323, 1)) {
                if (!qm.canHold(4032323, 1)) {
                    qm.sendNext("背包中的其他栏至少需要一个空位来接受任务。");
                    qm.dispose();
                    return;
                }
                qm.forceStartQuest();
                qm.gainItem(4032323, 1);
            }
            qm.sendNext("不过，黑色之翼的家伙们已经认识我了，我再拿着这个恐怕不太安全。英雄大人拿着它走来走去，弄丢了也不好……要不还是交给#b#p1201000##k保管吧。", 1);
            break
        case 4:
            qm.sendNextPrev("里恩岛上一直都只有里恩一族生活。为了不让其他人类接近，他们在岛上设置了各种咒术。黑色之翼要想找到他们恐怕没那么容易。请把这个交给#p1201000#。", 1);
            break;
        case 5:
            qm.sendNextPrev("我打算以后不再让你去做收集情报的工作了，你现在已经对冒险岛有了一定的了解，现在也是时候自己去积累经验了吧？", 1);
            break;
        case 6:
            qm.sendNextPrev("不过我打算集中全力，去收集与黑色之翼相关事件的情报，更何况，关于那个封印石也有必要继续打听，如果有什么消息，我会联系你的。日后再见，英雄。", 1);
            break;
        default:
            qm.dispose();
            break;
    }
}

function end(mode, type, selection) {
    if (mode == -1) {
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
            qm.sendNext("黑色之翼的动向，我已经从真相叔叔那里听说了。听说前不久还被他们袭击了一次……你还好吧？咦？这个……这就是#t4032323#吗？没想到真相叔叔果然比那些家伙们早一步找到#t4032323#。");
            break;
        case 1:
            qm.sendYesNo("不知道这颗宝石到底有什么用……只知道这个东西肯定和黑魔法师有关。既然那些家伙在找这个东西，我们一定要保护好这个东西。看来不论发生什么，你都要不断地变得更强，才行。");
            break;
        case 2:
            if (type == 1 && mode == 0) { // NO
                qm.dispose();
                break;
            }
            // YES
            qm.gainItem(4032323, -1);
            qm.gainExp(6037);
            qm.forceCompleteQuest();
            qm.sendOk("黑色之翼……他们的阴谋还没有结束。特鲁大叔拜托你继续调查黑色之翼。也请你继续你的修炼吧。");
            break;
        default:
            qm.dispose();
            break;
    }
}