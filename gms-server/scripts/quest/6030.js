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
/* 
	Quest: Carson's Fundamentals of Alchemy
 */

var status = -1;

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
            qm.sendNext("我将教你有关炼金术的基础知识。");
        } else if (status == 1) {
            qm.sendNextPrev("虽然科学很好地展示了构成物品的元素的思考方面，但单凭科学远远不足以设计出一个物品。");
        } else if (status == 2) {
            qm.sendNextPrev("事实上，要能够让‘碎片’变成一个整体，应该如何做？铁匠的古老方法往往会使物品的潜在潜能减弱。");
        } else if (status == 3) {
            qm.sendNextPrev("炼金术可以胜任这项任务。干净而迅速，#r它将形成物品的部分合并起来，几乎没有任何副作用#k，如果做得正确，几乎不会有任何废料，最大程度地利用了这个过程。要掌握它需要一段时间，但一旦掌握，一切都会井井有条。");
        } else if (status == 4) {
            qm.sendNextPrev("还要记住：#b交换#k的极限，即炼金术基础领域，材料的总量不会改变，没有任何物品可以从虚无中创造出来。明白了吗？");
        } else if (status == 5) {
            qm.gainMeso(-10000);

            qm.forceCompleteQuest();
            qm.dispose();
        }

    }
}
