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
/* Papulatus - 7103
 */

var status = -1;

function start(mode, type, selection) {
    if (mode == -1) {
        qm.sendOk("哦，真的吗。你需要更多时间吗？我完全相信你会在时间球形成之前帮助我。");
        qm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            qm.sendOk("哦，真的吗。你需要更多时间吗？我完全相信你会在时间球形成之前帮助我。");
            qm.dispose();
            return;
        }

        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            qm.sendYesNo("我们现在唯一要做的事情...就是让#o8500002#永远消失...你准备好了吗？");
        } else if (status == 1) {
            qm.sendNext("我会向你解释接下来需要做什么。\r\n要进入发电室，你需要通过#b遗忘之路#k或#b扭曲之路#k。一旦打败守卫通道的怪物，你就可以获得#b#t4031172:##k，这是进入发电室所需的物品。");
        } else if (status == 2) {
            qm.sendNextPrev("然后通过中间的门进入房间。这里会比你想象的安静得多。时间球应该隐藏在我们眼中无法察觉的状态...但是如果你封住维度裂缝，#o8500002#会因为其出口被封住而惊慌失措，从那里出现。");
        } else if (status == 3) {
            if (!qm.haveItem(4031179, 1)) {
                if (!qm.canHold(4031179, 1)) {
                    qm.sendOk("请确保有一个#r其他栏位可用#k 来开始这个任务。");
                    qm.dispose();
                    return;
                }

                qm.gainItem(4031179, 1);
            }

            qm.sendAcceptDecline("丢下我给你的#b#t4031179:##k 来封住#o8500002#可能用来进入这个维度的任何裂缝。然后它将从时间球中出来，展示其真正的外貌。请，杀死它然后回来。\r\n\r\n收集#r1 #t4031172:##k\r\n消灭#r#o8500001##k");
        } else if (status == 4) {
            qm.forceStartQuest();
            qm.dispose();
        }
    }
}
