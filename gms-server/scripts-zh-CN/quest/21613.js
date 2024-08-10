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
        }

        if (status == 0) {
            qm.sendNext("我们是一群狼在寻找我们丢失的孩子。我听说你在照顾我们的孩子。我们很感激你的好意，但现在是时候把孩子还给我们了.", 9);
        } else if (status == 1) {
            qm.sendNextPrev("狼人也是我的朋友，我不能只认识人类朋友.", 3);
        } else if (status == 2) {
            qm.sendAcceptDecline("我们明白，但我们不会离开我们的小狗。告诉你吧，我们会测试你是否值得养狼。#准备被狼测试的小目标.#k");
        } else if (status == 3) {
            var em = qm.getEventManager("Aran_3rdmount");
            if (em == null) {
                qm.sendOk("抱歉，第三个登山任务（狼群）已经关闭.");

            } else {
                var em = qm.getEventManager("Aran_3rdmount");
                if (!em.startInstance(qm.getPlayer())) {
                    qm.sendOk("地图里已经有人了，请稍后再来.");
                } else {
                    qm.forceStartQuest();
                }
            }
        } else if (status == 4) {
            qm.dispose();
        }
    }
}
