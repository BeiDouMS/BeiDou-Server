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
/* Author: Xterminator
	NPC Name: 		Robin
	Map(s): 		Maple Road : Snail Hunting Ground I (40000)
	Description: 		Beginner Helper
*/
var status;
var sel;

function start() {
    status = -1;
    sel = -1;
    cm.sendSimple("现在...问我任何你可能对旅行有的问题！\r\n#L0##b我该怎么移动？#l\r\n#L1#我怎么打倒怪物？#l\r\n#L2#我怎么捡起物品？#l\r\n#L3#我死后会发生什么？#l\r\n#L4#我什么时候可以选择一个职业？#l\r\n#L5#告诉我更多关于这个岛！#l\r\n#L6#我应该怎么做才能成为一个战士？#l\r\n#L7#我应该怎么做才能成为一个弓箭手？#l\r\n#L8#我应该怎么做才能成为一个魔法师？#l\r\n#L9#我应该怎么做才能成为一个盗贼？#l\r\n#L10#我怎么提升角色属性？(S)#l\r\n#L11#我怎么查看刚刚捡起的物品？#l\r\n#L12#我怎么穿上物品？#l\r\n#L13#我怎么查看我穿着的物品？#l\r\n#L14#什么是技能？(K)#l\r\n#L15#我怎么去维多利亚岛？#l\r\n#L16#什么是黄金币？#l#k");
}

function action(mode, type, selection) {
    status++;
    if (mode != 1) {
        if (mode == 0 && type != 4) {
            status -= 2;
        } else {
            cm.dispose();
            return;
        }
    }
    if (status == 0) {
        if (sel == -1) {
            sel = selection;
        }
        if (sel == 0) {
            cm.sendNext("好的，这是你移动的方法。使用#bleft, right arrow#k在平地和斜坡上移动，并按#bAlt#k跳跃。一些特定的鞋子可以提高你的速度和跳跃能力。");
        } else if (sel == 1) {
            cm.sendNext("这里是如何击败怪物的方法。每个怪物都有自己的生命值，你可以用武器或法术来攻击它们。当然，它们越强大，就越难击败。");
        } else if (sel == 2) {
            cm.sendNext("这就是你收集物品的方法。一旦你打倒一个怪物，一个物品就会掉到地上。当这种情况发生时，站在物品前面，按#bZ#k或#b小键盘上的0#k来获取物品。");
        } else if (sel == 3) {
            cm.sendNext("当你死亡时，你会变成一个幽灵。当你的生命值降到0时，会在那个地方出现一块墓碑，你将无法移动，但仍然可以进行聊天。");
        } else if (sel == 4) {
            cm.sendNext("想知道什么时候能转职？哈哈~！你真性急。每个职业都有固有的转职条件。一般8~10级你就可以选择职业。努力啊！");
        } else if (sel == 5) {
            cm.sendNext("想知道这个岛的情况？这里是叫彩虹岛的空中浮动岛。从远古就在天空上飞行了，因此这里很少出现凶猛的怪物。所以是相对安全的岛，是新手练习的好地方。");
        } else if (sel == 6) {
            cm.sendNext("你想当#b战士#k吗？嗯。。。那么你必须要去金银岛。金银岛北部有战士之村，叫#r勇士部落#k。去那里找#b武术教练#k后收下他的培训，你就会当战士。但要当战士你的等级必需达到10级.");
        } else if (sel == 7) {
            cm.sendNext("你想当弓箭手吗？在金银岛你会当弓箭手。在金银岛南部有弓箭手的村落，射手村。在那里赫丽娜会告诉你当弓箭手的方法。但关键是要当弓箭手你的等级应该是10级。");
        } else if (sel == 8) {
            cm.sendNext("你想当魔法师是吗？那你要去金银岛东部的魔法密林。在那里你会见到很多魔法师。而且在那里你要见汉斯。他就会让你当魔法师。");
        } else if (sel == 9) {
            cm.sendNext("你你想当飞侠吗？那你要去金银岛西部的废弃都市。废都的达克鲁就会告诉你当飞侠的办法。关键的是为了当飞侠，你的等级应该是10级。");
        } else if (sel == 10) {
            cm.sendNext("你想知道如何提升你角色的能力属性吗？首先按下#bS#k来查看能力窗口。每次升级时，你会获得5个能力点，也就是AP。将这些AP分配到你选择的能力上。就是这么简单。");
        } else if (sel == 11) {
            cm.sendNext("你想知道如何查看你捡起的物品，是吗？当你打败一个怪物时，它会掉落一个物品在地上，你可以按#bZ#k来捡起这个物品。这个物品将被存储在你的物品库存中，你可以通过简单地按#bI#k来查看它。");
        } else if (sel == 12) {
            cm.sendNext("你想知道如何穿戴物品，对吧？按下#bI#k来查看你的物品库存。将鼠标光标放在物品上，双击它就可以穿在你的角色身上。如果你发现自己无法穿戴该物品，很可能是你的角色不符合等级和属性要求。你也可以通过打开装备库存(#bE#k)并将物品拖入其中来穿戴物品。要脱下物品，双击装备库存中的物品。");
        } else if (sel == 13) {
            cm.sendNext("你想要检查已装备的物品，对吧？按下#bE#k打开装备栏，你会看到你当前穿着的物品。要脱下一个物品，双击该物品。该物品将被发送到物品栏。");
        } else if (sel == 14) {
            cm.sendNext("获得职业后获得的特殊“能力”被称为技能。你将获得专门针对该职业的技能。你现在还没有达到那个阶段，所以还没有任何技能，但记住，要查看你的技能，请按#bK#k打开技能书。这将在日后帮助你。");
        } else if (sel == 15) {
            cm.sendNext("你如何到达金银岛？在这个岛的东部有一个叫做南港的港口。在那里，你会找到一艘在空中飞行的船。船前站着船长。问他关于这件事。");
        } else if (sel == 16) {
            cm.sendNext("这是冒险岛中使用的货币。你可以通过冒险币购买物品。要赚取冒险币，你可以击败怪物，将物品出售给商店，或者完成任务...");
        }
    } else if (status == 1) {
        if (sel == 0) {
            cm.sendNextPrev("为了攻击怪物，你需要装备武器。装备后，按下#bCtrl#k来使用武器。在正确的时机，你就能轻松地击败怪物。");
        } else if (sel == 1) {
            cm.sendNextPrev("一旦你进行职业转职，你将获得不同类型的技能，你可以将它们分配到快捷键上以便更容易地使用。如果是攻击技能，你不需要按下Ctrl键来攻击，只需按下分配为快捷键的按钮。");
        } else if (sel == 2) {
            cm.sendNextPrev("请记住，如果你的物品栏已满，你将无法获得更多物品。因此，如果你有一件不需要的物品，就卖掉它，这样你就可以从中获利。当你进行职业转职后，物品栏可能会扩展。");
        } else if (sel == 3) {
            cm.sendNextPrev("如果你只是一个新手，死亡并不会让你失去太多。但一旦你有了职业，情况就不一样了。当你死亡时，你会失去一部分经验，所以一定要尽量避免危险和死亡。");
        } else if (sel == 4) {
            cm.sendNextPrev("等级并不是唯一决定进步的因素。你还需要根据职业提升特定能力的等级。例如，要成为一名战士，你的力量属性必须超过35，你知道我在说什么吗？确保提升与你的职业直接相关的能力。");
        } else if (sel == 5) {
            cm.sendNextPrev("但是，如果你想成为一个强大的玩家，最好不要考虑在这里呆太久。你也不会找到工作。这个岛屿下面有一个叫做金银岛的巨大岛屿。那个地方比这里大得多，甚至有些荒谬。");
        } else if (sel == 8) {
            cm.sendNextPrev("哦，顺便说一下，与其他职业不同，要成为一个魔法师，你只需要达到8级。提前进行职业转职所带来的好处也伴随着成为一名真正强大的法师所需付出的艰辛。在选择你的道路之前，请仔细考虑。");
        } else if (sel == 10) {
            cm.sendNextPrev("将鼠标光标放在所有技能上方，以获得简要解释。例如，战士的STR，弓箭手的DEX，魔法师的INT，以及盗贼的LUK。但这并不是你需要了解的全部，所以你需要仔细思考如何通过分配点数来强调你角色的优势。");
        } else if (sel == 15) {
            cm.sendNextPrev("哦，是的！在我离开之前，还有一条信息要告诉你。如果你不确定自己在哪里，记得按下#bW#k。世界地图会弹出来，显示你所在的位置。有了这个，你就不用担心迷路了。");
        } else {
            start();
        }
    } else {
        start();
    }
}