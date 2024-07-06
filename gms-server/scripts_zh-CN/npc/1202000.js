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
                    cm.sendNext("你终于醒来了...!");
                } else if (status == 1) {
                    cm.sendNextPrev("你是……？");
                } else if (status == 2) {
                    cm.sendNextPrev("“与黑魔法师作战的英雄……我一直在等你醒来！”");
                } else if (status == 3) {
                    cm.sendNextPrev("你是谁？你在说什么？");
                } else if (status == 4) {
                    cm.sendNextPrev("我是谁呢...? 我什么都记不起来... 哎呀，我的头好痛!");
                } else if (status == 5) {
                    cm.showIntro("Effect/Direction1.img/aranTutorial/face");
                    cm.showIntro("Effect/Direction1.img/aranTutorial/ClickLilin");
                    cm.updateAreaInfo(21019, "helper=clear");
                    cm.dispose();
                }
            } else {
                if (status == 0) {
                    cm.sendNextPrev("你没事吧？");
                } else if (status == 1) {
                    cm.sendNextPrev("我什么都记不起来。我在哪里？你是谁……？");
                } else if (status == 2) {
                    cm.sendNextPrev("保持冷静。没有必要惊慌。你什么都记不起来是因为黑魔法师的诅咒抹去了你的记忆。我会一步一步告诉你需要知道的一切。");
                } else if (status == 3) {
                    cm.sendNextPrev("你是数百年前与黑魔法师战斗并拯救了枫之世界的英雄。但就在最后一刻，黑魔法师的诅咒让你长时间沉睡，而在这段时间里你失去了所有的记忆。");
                } else if (status == 4) {
                    cm.sendNextPrev("这个岛叫做里恩，这就是黑魔法师困住你的地方。尽管名字叫里恩，但由于黑魔法师的诅咒，这个岛总是覆盖着冰雪。你被发现在冰洞的深处。");
                } else if (status == 5) {
                    cm.sendNextPrev("我的名字是莉琳，我属于利恩族。利恩族已经等待英雄归来很长时间了，现在我们终于找到了你。你终于回来了！");
                } else if (status == 6) {
                    cm.sendNextPrev("我说得太多了。如果你不是很理解我刚才告诉你的一切，没关系。你最终会明白的。现在，你应该去城镇。我会一直陪在你身边，直到你到达那里。");
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