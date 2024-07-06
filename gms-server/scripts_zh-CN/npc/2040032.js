/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/**
 -- Odin JavaScript --------------------------------------------------------------------------------
 Weaver - Ludibrium : Ludibrium Pet Walkway (220000006)
 -- By ---------------------------------------------------------------------------------------------
 Xterminator
 -- Version Info -----------------------------------------------------------------------------------
 1.0 - First Version by Xterminator
 ---------------------------------------------------------------------------------------------------
 **/

function start() {
    cm.sendYesNo("这是你可以带宠物散步的道路。你可以和它一起四处走动，或者在这里训练它通过障碍物。如果你和宠物还不够亲近，可能会出现问题，它就不会像你命令的那样跟着你走了……那么，你觉得呢？想要训练你的宠物吗？");
}

function action(mode, type, selection) {
    if (mode == -1) {
    } else if (mode == 0) {
        cm.sendNext("嗯...现在太忙了？如果你想做的话，回来找我吧。");
    } else if (mode == 1) {
        if (cm.haveItem(4031128)) {
            cm.sendNext("拿到那封信，和你的宠物一起跳过障碍物，把那封信带给我的兄弟弗罗德训练师。把信交给他，你的宠物会有好事发生。");
        } else {
            cm.gainItem(4031128, 1);
            cm.sendOk("好的，这是信。如果你直接去那里，他不会知道我派你去的，所以带着你的宠物通过障碍，到达顶部，然后和弗罗德训练师交谈，把信交给他。如果你在通过障碍时注意你的宠物，这并不难。祝你好运！");
        }
    }
    cm.dispose();
}