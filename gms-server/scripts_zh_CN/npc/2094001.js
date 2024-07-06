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

var status = -1;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && status == 0) {
            cm.dispose();
            return;
        }

        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (cm.getMapId() == 925100500) {
            if (status == 0) {
                if (cm.isEventLeader()) {
                    cm.sendOk("多亏了你们的努力，我得救了！谢谢，伙计们！");
                } else {
                    cm.sendOk("多亏了你们的努力，我得救了！谢谢你们！在我给你们奖励之前，让你们的队长先和我说话...");
                    cm.dispose();
                }
            } else {
                cm.getEventInstance().clearPQ();
                cm.dispose();
            }
        } else {
            if (status == 0) {
                cm.sendSimple("谢谢你救了我！我能帮你什么忙吗？\r\n#b#L0#带我离开这里。\r\n#L1#给我海盗帽子。");
            } else if (status == 1) {
                if (selection == 0) {
                    if (!cm.canHold(4001158, 1)) {
                        cm.sendOk("请在杂项栏中腾出空间。");
                        cm.dispose();
                        return;
                    }
                    cm.gainItem(4001158, 1);
                    cm.warp(251010404, 0);
                } else {
                    if (cm.haveItem(1003267, 1)) {
                        cm.sendOk("你有最好的帽子。");
                    } else if (cm.haveItem(1002573, 1)) {
                        if (cm.haveItem(4001158, 20)) {
                            if (cm.canHold(1003267, 1)) {
                                cm.gainItem(1002573, -1);
                                cm.gainItem(4001158, -20);
                                cm.gainItem(1003267, 1);
                                cm.sendOk("我已经给你帽子了。");
                            } else {
                                cm.sendOk("在收到帽子之前，请在您的装备物品栏中腾出空间。");
                            }
                        } else {
                            cm.sendOk("你需要20个#t4001158#来获得下一个帽子。");
                        }
                    } else if (cm.haveItem(1002572, 1)) {
                        if (cm.haveItem(4001158, 20)) {
                            if (cm.canHold(1002573, 1)) {
                                cm.gainItem(1002572, -1);
                                cm.gainItem(4001158, -20);
                                cm.gainItem(1002573, 1);
                                cm.sendOk("我已经给你帽子了。");
                            } else {
                                cm.sendOk("在收到帽子之前，请在您的装备物品栏中腾出空间。");
                            }
                        } else {
                            cm.sendOk("你需要20个#t4001158#来获得下一个帽子。");
                        }
                    } else {
                        if (cm.haveItem(4001158, 20)) {
                            if (cm.canHold(1002572, 1)) {
                                cm.gainItem(4001158, -20);
                                cm.gainItem(1002572, 1);
                                cm.sendOk("我已经给你帽子了。");
                            } else {
                                cm.sendOk("在收到帽子之前，请在您的装备物品栏中腾出空间。");
                            }
                        } else {
                            cm.sendOk("你需要20个#t4001158#来获得下一个帽子。");
                        }
                    }
                }

                cm.dispose();
            }
        }

    }
}