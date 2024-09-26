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
/* Aura
 * 
 * Adobis's Mission I: Unknown Dead Mine (280010000)
 * 
 * Zakum PQ NPC (the one and only)
*/

var status;
var selectedType;
var gotAllDocs;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        var eim = cm.getPlayer().getEventInstance();

        if (status == 0) {
            if (!eim.isEventCleared()) {
                cm.sendSimple("...#b\r\n#L0#我在这里应该做什么？#l\r\n#L1#我带来了物品！#l\r\n#L2#我想要离开！#l");
            } else {
                cm.sendNext("你完成了这次磨难，现在领取你的奖品。");
            }
        } else if (status == 1) {
            if (!eim.isEventCleared()) {
                selectedType = selection;
                if (selection == 0) {
                    cm.sendNext("为了揭示扎昆的力量，你需要重新制造它的核心。在这个地牢的某个地方隐藏着#b一块 #v4001018##t4001018# #k，这是制作核心所需的材料之一。找到它，然后带给我。\r\n哦，你能帮我一个忙吗？\r\n这附近的石头下面也有一些#b #v4001015##t4001015# #k。如果你能找到#b30份#k，我会奖励你的努力。");
                    cm.dispose();

                } else if (selection == 1) {
                    if (!cm.isEventLeader()) {
                        cm.sendNext("请让你们的队长把材料带给我，以完成这个考验。");
                        cm.dispose();
                        return;
                    }

                    if (!cm.haveItem(4001018)) { //fire ore
                        cm.sendNext("请找到#b #v4001018##t4001018# #k 再带来给我。");
                        cm.dispose();
                    } else {
                        gotAllDocs = cm.haveItem(4001015, 30);
                        if (!gotAllDocs) { //documents
                            cm.sendYesNo("所以，你带了#b #v4001018##t4001018# #k来了？我可以给你和你的每个队员#b一块#v4031061##t4031061##k，这应该足够制作扎昆的核心。确保你的整个队伍在继续之前有足够的背包空间。");
                        } else {
                            cm.sendYesNo("所以，你带来了#b #v4001018##t4001018# #k和#b #v4001015##t4001015# #k吗？我可以给你和你的每个队员#b一块#v4031061##t4031061##k，这应该足够制作扎昆的核心了。\r\n\r\n另外，既然你带来了#b #v4001015##t4001015# * 30#k，我还可以给你#b#v2030007##t2030007# * 5#k，可以随时带你到矿井入口。在继续之前，请确保你的整个队伍的背包有足够的空间。");
                        }
                    }
                } else if (selection == 2) {
                    cm.sendYesNo("你确定要退出吗？如果你是队伍的队长，你的队伍也将离开矿区。");
                }
            } else {
                if (eim.getProperty("gotDocuments") == 1) {
                    if (eim.gridCheck(cm.getPlayer()) == -1) {
                        if (cm.canHoldAll([2030007, 4031061], [5, 1])) {
                            cm.gainItem(2030007, 5);
                            cm.gainItem(4031061, 1);

                            eim.gridInsert(cm.getPlayer(), 1);
                        } else {
                            cm.sendOk("确保在继续之前你的背包有足够的空间。");
                        }
                    } else {
                        cm.sendOk("你已经领取了你的份额。你现s在可以通过那边的传送门离开矿井了。");
                    }
                } else {
                    if (eim.gridCheck(cm.getPlayer()) == -1) {
                        if (cm.canHold(4031061, 1)) {
                            cm.gainItem(4031061, 1);

                            eim.gridInsert(cm.getPlayer(), 1);
                        } else {
                            cm.sendOk("确保在继续之前你的背包有足够的空间。");
                        }
                    } else {
                        cm.sendOk("你已经领取了你的份额。你现在可以通过那边的传送门离开矿井了。");
                    }
                }

                cm.dispose();
            }

        } else if (status == 2) {
            if (selectedType == 1) {
                cm.gainItem(4001018, -1);

                if (gotAllDocs) {
                    cm.gainItem(4001015, -30);

                    eim.setProperty("gotDocuments", 1);
                    eim.giveEventPlayersExp(20000);
                } else {
                    eim.giveEventPlayersExp(12000);
                }

                eim.clearPQ();
                cm.dispose();
            } else if (selectedType == 2) {
                cm.warp(211042300);
                cm.dispose();
            }
        }
    }
}