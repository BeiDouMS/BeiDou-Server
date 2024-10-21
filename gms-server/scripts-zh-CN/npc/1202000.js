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
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (cm.getPlayer().getMapId() == 140090000) {
            if (!cm.containsAreaInfo(21019, "helper=clear")) {
                if (status == 0) {
                    cm.sendNext("你终于醒来了！", 8);
                } else if (status == 1) {
                    cm.sendNextPrev("……你是？", 2);
                } else if (status == 2) {
                    cm.sendNextPrev("我一直在等你，等着你这个和黑魔法师战斗的英雄醒来！", 8);
                } else if (status == 3) {
                    cm.sendNextPrev("……你在说什么？你到底是谁？", 2);
                } else if (status == 4) {
                    cm.sendNextPrev("等等……我是谁？我怎么什么都想不起来……啊……！头好疼！", 2);
                } else if (status == 5) {
                    cm.showIntro("Effect/Direction1.img/aranTutorial/face");
                    cm.showIntro("Effect/Direction1.img/aranTutorial/ClickLilin");
                    cm.updateAreaInfo(21019, "helper=clear");
                    cm.dispose();
                }
            } else {
                if (status == 0) {
                    cm.sendNextPrev("还好吗？", 8);
                } else if (status == 1) {
                    cm.sendNextPrev("我……我什么都记不起来……这是哪里？你又是谁？", 2);
                } else if (status == 2) {
                    cm.sendNextPrev("镇静一点。黑魔法师的诅咒让你失去了记忆……不过你用不着担心。你想知道的事情，我都会一一告诉你。", 8);
                } else if (status == 3) {
                    cm.sendNextPrev("你是我们的英雄。数百年前，你勇敢地和黑魔法师战斗，并拯救了冒险岛世界。不过，在最后时刻你中了黑魔法师的诅咒，被封冻在冰块里沉睡了好久好久。所以，记忆也渐渐消失了。", 8);
                } else if (status == 4) {
                    cm.sendNextPrev("这个地方叫做里恩岛。黑魔法师把你封冻在了这里。在黑魔法师的诅咒下，不论四季变化，这里永远都是冰封雪飘。我们是在冰窟的最深处发现你的。", 8);
                } else if (status == 5) {
                    cm.sendNextPrev("我叫#p1201000#，属于里恩一族。里恩一族从很久以前就遵照预言在这里等待着英雄的归来。然后……我们终于发现了你。就在这个地方……", 8);
                } else if (status == 6) {
                    cm.sendNextPrev("我是不是一次说了太多？理解起来有些困难？没关系，慢慢你就会明白……#b咱们赶紧回村子里吧#k。回村子的路上，我再慢慢給你解释。", 8);
                } else if (status == 7) {
                    cm.spawnGuide();
                    cm.warp(140090100, 0);
                    cm.dispose();
                }
            }
        } else {
            if (status == 0) {
                cm.sendSimple("你还有什么疑问吗？如果有的话，我会尽量解释得更清楚。#b#l\r\n#L0#我是谁？#l #l\r\n#L1#我在哪里？#l #l\r\n#L2#你是谁？#l #l\r\n#L3#告诉我我该做什么。#l #l\r\n#L4#告诉我关于我的物品栏。#l #l\r\n#L5#我如何提升我的技能？#l #l\r\n#L6#我想知道如何装备物品。#l #l\r\n#L7#我如何使用快捷栏？#l #l\r\n#L8#我如何打开可破坏的容器？#l #l\r\n#L9#我想坐在椅子上，但我忘了怎么做。#k");
            } else if (status == 1) {
                if (selection == 0) {
                    cm.sendNext("你是数百年前拯救冒险岛世界免受黑魔法师侵害的英雄之一。由于黑魔法师的诅咒，你失去了记忆。");
                    cm.dispose();
                } else if (selection == 1) {
                    cm.sendNext("这个岛叫做里恩，这就是黑魔法师的诅咒让你沉睡的地方。这是一个被冰雪覆盖的小岛，大部分居民是企鹅。");
                    cm.dispose();
                } else if (selection == 2) {
                    cm.sendNext("我是莉琳，是利恩的氏族成员，我一直在等待你的归来，正如预言所言。我将会是你的向导。");
                    cm.dispose();
                } else if (selection == 3) {
                    cm.sendNext("我们别浪费时间了，直接去镇上吧。等到了那里我会告诉你详细情况。");
                    cm.dispose();
                } else if (selection == 4) {
                    cm.guideHint(14);
                    cm.dispose();
                } else if (selection == 5) {
                    cm.guideHint(15);
                    cm.dispose();
                } else if (selection == 6) {
                    cm.guideHint(16);
                    cm.dispose();
                } else if (selection == 7) {
                    cm.guideHint(17);
                    cm.dispose();
                } else if (selection == 8) {
                    cm.guideHint(18);
                    cm.dispose();
                } else if (selection == 9) {
                    cm.guideHint(19);
                    cm.dispose();
                }
            }
        }
    }
}