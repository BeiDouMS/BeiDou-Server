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
            qm.sendNext("很忙吗，英雄大人？前不久我使尽各种手段在金银岛上四处探查，终于找到了一个有意思的情报。是关于人偶师的……");
            break;
        case 1:
            qm.sendNextPrev("你知道吗？自从你教训了#o9300346#之后，火独眼兽洞穴里的入口不就不能用了吗？#o9300346#那个家伙，好像又在别的地方建立了根据地。");
            break;
        case 2:
            qm.sendAcceptDecline("在#m105040300#的#b#m105040200##k，有人看见#o9300346#走进了一个#b小木屋#k。情报很可靠。快去那边击退#r#o9300346##k吧。");
            break;
        case 3:
            if (type == 12 && mode == 0) { // DECLINE
                qm.sendOk("啊？不会吧？这可是最牛的情报商人找到的最新情报，绝对可靠的。");
                qm.dispose();
                break;
            }
            if (!qm.isQuestStarted(21734)) {
                qm.forceStartQuest();
            }
            qm.dispose();
            break;
        default:
            qm.dispose();
            break;
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
            qm.sendNext("看样子，你应该已经打败人偶师了……怎么不高兴的样子？发生什么事了？", 1 << 3);
            break;
        case 1:
            qm.sendNextPrev("没发现任何有关金银岛封印石的情报。", 1 << 1);
            break;
        case 2:
            qm.sendPrev("啊哈！原来是为这事。呵呵呵……完全不用担心。", 1 << 3);
            break;
        case 3:
            qm.forceCompleteQuest();
            qm.gainExp(12500);
            qm.teachSkill(21100005, 0, 20, -1); // combo drain
        default:
            qm.dispose();
            break;
    }
}