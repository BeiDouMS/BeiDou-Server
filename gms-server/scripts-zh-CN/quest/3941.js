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

/* Steal queen's silk
 */

function isTigunMorphed(ch) {
    const BuffStat = Java.type('org.gms.client.BuffStat');
    return ch.getBuffSource(BuffStat.MORPH) == 2210005;
}

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
        }

        if (status == 0) {
            if (!isTigunMorphed(qm.getPlayer())) {
                qm.sendNext("这是什么？我不能简单地把女王的丝绸交给任何人，声称他们会立刻交给女王。离开我的视线。");
                status = 1;
                return;
            }

            qm.sendNext("提古，你在这里做什么？");
        } else if (status == 1) {
            if (!isTigunMorphed(qm.getPlayer())) {
                qm.sendNext("这是什么？我不能简单地把女王的丝绸交给任何人，声称他们会立刻交给女王。离开我的视线。");
                return;
            }

            qm.sendNext("女王现在就想要她的丝绸？好的，我这就拿出来。等一会儿。");
            qm.forceStartQuest();
        } else if (status == 2) {
            qm.dispose();
        }
    }
}

function end(mode, type, selection) {
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
        }

        if (status == 0) {
            if (!isTigunMorphed(qm.getPlayer())) {
                qm.sendNext("这是什么？我不能简单地把女王的丝绸交给任何人，声称他们会立刻交给女王。离开我的视线。");
                qm.dispose();
                return;
            }

            if (qm.canHold(4031571,1)) {
                qm.gainItem(4031571);

                qm.sendNext("拿去吧。请尽快交给女王，提古，如果事情延误她会很生气的。");
                qm.forceCompleteQuest();
            } else {
                qm.sendNext("嘿，你的背包空间不足。我会帮你留着，你整理好背包再来取...");
            }
        } else if (status == 1) {
            qm.dispose();
        }
    }
}

