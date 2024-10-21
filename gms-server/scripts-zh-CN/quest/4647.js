/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

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
*
* @author Blue
*/

var status = -1;

// function start(mode, type, selection) {
//     if (mode == -1) {
//         qm.dispose();
//     } else {
//         status++;
//         if (status == 0) {
//             qm.sendAcceptDecline("若你真的有心想要学习带领多只宠物的技能的话，就去想办法把宠物点心拿给我吧！");
//         } else if (status == 1) {
//             if (mode == 0) {
//                 qm.sendOk("没有付出是不会有收获的！你就再好好考虑看看吧！");
//             } else {
//                 qm.forceStartQuest();
//                 // qm.sendOk("这东西在一般商人那里是买不到，但在商城应该就可以买到宠物点心，你就仔细找找看吧！");
//             }
//             qm.dispose();
//         } else {
//             qm.dispose();
//         }
//     }
// }

function end(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0) {
            if (qm.haveItem(5460000)) {
                qm.completeQuest();
                qm.teachSkill(8, 1, 1, -1);
                qm.gainItem(5460000, -1, false);
                qm.sendOk("你获得了宠物点心！谢谢，现在你可以同时携带多只宠物了！");
            } else {
                qm.sendOk("给我宠物点心！可以在商城中找到....");
            }
        } else if (status == 1) {
            qm.dispose();
        }
    }
}
