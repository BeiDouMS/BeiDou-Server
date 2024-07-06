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
/* Icebyrd Slimm
	Masteria: New Leaf City (600000000)
	Handles the quiz quest. (4900)
 */

var minlevel = 10;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            cm.dispose();
            return;
        }

        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0 && mode == 1) {
            if (cm.isQuestCompleted(4911)) {
                cm.sendNext("干得好！你解决了我关于NLC的所有问题。祝你旅途愉快！");
                cm.dispose();
            } else if (cm.isQuestCompleted(4900) || cm.isQuestStarted(4900)) {  // thanks imbee for pointing out the quiz leak
                cm.sendNext("嘿，注意一下，我要考你另一个问题，伙计！");
                cm.dispose();
            } else {
                var selStr = "What up! Name's Icebyrd Slimm, mayor of New Leaf City! Happy to see you accepted my invite. So, what can I do for you?#b"
                var info = ["What is this place?", "Who is Professor Foxwit?", "What's a Foxwit Door?", "Where are the MesoGears?", "What is the Krakian Jungle?", "What's a Gear Portal?", "What do the street signs mean?", "What's the deal with Jack Masque?", "Lita Lawless looks like a tough cookie, what's her story?", "When will new boroughs open up in the city?", "I want to take the quiz!"];
                for (var i = 0; i < info.length; i++) {
                    selStr += "\r\n#L" + i + "# " + info[i] + "#l";
                }
                cm.sendSimple(selStr);
            }
        } else if (status == 1) {
            switch (selection) {
                case 0:
                    cm.sendNext("我一直梦想建造一座城市。不是普通的城市，而是一个每个人都受欢迎的城市。我曾经住在废弃都市，所以我决定看看我是否能创建一个城市。在寻找实现这一目标的途径时，我遇到了许多人，其中一些我已经视为朋友。比如福克斯维特教授-他是我们的天才；我救他脱离了一群吃人植物。杰克·马斯克是我在阿莫利亚的老猎友-说话太圆滑了，对自己不利。莉塔和我是废弃都市的老朋友-她用她的武器救过我几次；所以我觉得她是镇长的完美选择。需要一些说服，但她最终相信她的命运在这里。至于我们的探险家，巴里卡德来寻找某样东西；他同意把他找到的东西带到博物馆。我在废弃都市的时候就听说过他和他的兄弟的故事。至于埃尔帕姆……嗯，我们就说他不是这里的人。完全不是。我们之前交谈过，他似乎心地善良，所以我允许他留下。我刚意识到我说了很多废话！你还想知道什么？");
                    status -= 2;
                    break;
                case 1:
                    cm.sendNext("一个97岁的老家伙，动作还挺敏捷。有一天我在城外遇到了他，他是个时空旅行者。这个老家伙在丛林里遇到了一些麻烦，好像有些丛林生物想要吃掉他。作为我救了他的回报，他答应建造一个时空博物馆。我感觉他来这里有其他目的，因为他多次提到新叶城在未来将扮演一个有趣的角色。也许你可以多了解一些……");
                    status -= 2;
                    break;
                case 2:
                    cm.sendNext("“嘿，当我看到教授在建造它们时，我也问了同样的问题。它们是传送点。按上键会将你传送到另一个位置。我建议你熟悉它们，它们是我们的交通系统。”");
                    status -= 2;
                    break;
                case 3:
                    cm.sendNext("MesoGears位于比格本塔下方。这是巴里卡德发现的一个充满怪物的区域。看起来它似乎位于塔的一个独立部分，如果你问我，这相当奇怪。我听说他需要一点帮助来探索它，你应该去看看他。不过要小心，那里的狼蜘蛛可不是闹着玩的。");
                    status -= 2;
                    break;
                case 4:
                    cm.sendNext("“啊...嗯。克拉基亚丛林位于新叶城的郊外。许多新的、强大的生物在那些地区漫游，所以如果你要去那里，最好做好战斗的准备。它位于城镇的右端。有传言说丛林通向一个失落的城市，但我们还没有找到任何东西。”");
                    status -= 2;
                    break;
                case 5:
                    cm.sendNext("嗯，当约翰发现自己在大本钟的迷思齿轮部分时，他站在一个上，然后去了另一个地方。然而，他只能来回走动，它们不像狐狡之门那样循环。这就是古老的科技。");
                    status -= 2;
                    break;
                case 6:
                    cm.sendNext("嗯，你几乎可以在任何地方看到它们。它们是正在施工的区域。红灯表示还没完工，但绿灯表示已经开放。经常回来看看，我们一直在建设中！");
                    status -= 2;
                    break;
                case 7:
                    cm.sendNext("啊，杰克。你知道那些自以为很酷、总是能逃脱一切惩罚的家伙吗？还能追到女孩？嗯，那就是杰克，只是没有女孩。他觉得自己错失了机会，于是开始戴上面具隐藏真实身份。我对他的真实身份守口如瓶，但他来自阿莫利亚。如果你问他，他可能会告诉你更多。");
                    status -= 2;
                    break;
                case 8:
                    cm.sendNext("我认识莉塔有一段时间了，虽然我们最近才重新燃起友谊。我有一段时间没见到她，但我理解为什么。她作为一个盗贼进行了非常非常长时间的训练。事实上，那就是我们第一次见面的方式！我被一群迷失的蘑菇围攻，她跳了出来帮忙。当选举警长的时候，毫无疑问是她。她承诺要帮助其他人进行训练并保护城市，所以如果你对公民义务感兴趣，可以和她交谈一下。");
                    status -= 2;
                    break;
                case 9:
                    cm.sendNext("很快，我的朋友。虽然你看不到他们，但城市开发者们正在努力工作。当他们准备好了，我们就会打开它们。我知道你期待着这一刻，我也是！");
                    status -= 2;
                    break;
                case 10:
                    if (cm.getLevel() >= minlevel) {
                        cm.sendNext("没问题。如果你回答正确，我会给你一些好东西！");
                        cm.startQuest(4900);
                    } else {
                        cm.sendNext("急切了吗？在我让你参加测验之前，你想再多探索一下吗？");
                    }

                    cm.dispose();
                    break;
            }
        }
    }
}