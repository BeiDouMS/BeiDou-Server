/*
	T\r\nis file is part of the OdinMS Maple Story Server
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
var selected = -1;
var party = 0;
var questComplete = false;

function start() {
    status = -1;
    selected = -1;
    party = 0;
    questComplete = false;

    var text = "你不应该在这张地图和这个 NPC 对话。";
    if (cm.getMapId() == 926020001 || cm.getMapId() == 926010001) {
        text = "站住！你成功通过了奈特的试炼。承蒙奈特的恩典，现在你获得了进入法老雪人之墓的机会。现在要进去吗？\r\n\r\n#b#L0# 是的，现在就去。#l\r\n#L1# 不，我之后再去。#l";
    } else if (cm.getMapId() == 926010000) {
        if (cm.isQuestStarted(3955)) {
            questComplete = true;
            cm.sendNext("站住，愚蠢的人！你不怕死吗？\r\n\r\n#b#L0# 我是替学者巴一岚来的。#l#k");
            return;
        }
        text = "我是杜阿特。\r\n\r\n#b#L0# 询问金字塔的事情。#l\r\n#e#L1# 进入金字塔。#l#n\r\n\r\n#L2# 寻找队伍。#l\r\n\r\n#L3# 进入法老雪人之墓。#l\r\n#L4# 询问法老雪人的宝物。#l\r\n#L5# 领取 <法老守护者> 勋章。#l";
    } else {
        text = "你想放弃挑战并离开吗？\r\n\r\n#b#L0# 离开#l";
    }

    cm.sendSimple(text);
}

function action(mode, type, selection) {
    if (mode == 0 && type == 0) {
        status--;
    } else if (mode < 0 || (type == 4 && mode == 0)) {
        cm.dispose();
        return;
    } else {
        status++;
    }

    if (questComplete) {
        if (status == 0) {
            cm.sendNext("原来如此。那个蠢货。我好心把他从死亡边缘救回来，他却又把自己推向另一扇死亡之门。也罢，就让我看看他能不能逃过阿努比斯的气息。 ");
        } else if (status == 1) {
            cm.forceCompleteQuest(3955);
            cm.sendNext("从今天开始，我允许你出入。但是金字塔是否允许你出入，还得走着瞧。如果金字塔认可了你，在探险结束的时候，你就可以得到法老雪人的宝石。把它交给我，我就会把你送到可以获得珍贵宝物的地方。 ");
        } else {
            cm.dispose();
        }
        return;
    }

    if (cm.getMapId() == 926010000) {
        if (status == 0) {
            if (selection > -1) {
                selected = selection;
            }
            if (selection == 0 || selected == 0) {
                cm.sendNext("这是奈特金字塔，混沌与复仇之神的金字塔。长久以来，它一直埋在沙漠深处，但奈特命令它重现地面。如果你不惧混乱和死亡，就可以挑战沉睡在金字塔里的法老雪人。无论结果如何，选择权都在你手中。");
            } else if (selection == 1) {
                cm.sendSimple("不知畏惧奈特之怒的愚蠢之人，现在是选择命运的时候了！\r\n\r\n#b#L0# 独自进入。#l\r\n#L1# 和 2 人以上的队伍一起进入。#l");
            } else if (selection == 2) {
                cm.openUI(0x16);
                cm.showInfoText("可以使用队伍搜索窗口（快捷键 O）随时随地寻找可加入的队伍！");
                cm.dispose();
            } else if (selection == 3) {
                cm.sendSimple("你带来了哪颗宝石？\r\n\r\n#L0##i4001322# #t4001322##l\r\n#L1##i4001323# #t4001323##l\r\n#L2##i4001324# #t4001324##l\r\n#L3##i4001325# #t4001325##l");
            } else if (selection == 4) {
                cm.sendNext("在法老雪人之墓中，你可以通过击败法老雪人的分身 #b法老小雪人#k 来证明自己的能力，并获得 #e#b#t2022613##k#n。那个盒子里藏着非常特别的宝物，也就是 #e#b#t1132012##k#n。\r\n#i1132012:# #t1132012#\r\n\r\n如果你能在地狱模式中幸存，更强的法老小雪人会掉落 #e#b#t2022618##k#n，里面还有机会获得 #e#b#t1132013##k#n。\r\n\r\n#i1132013:# #t1132013#\r\n\r\n当然，奈特不会轻易允许这种事发生。 ");
            } else if (selection == 5) {
                var progress = cm.getQuestProgressInt(29932, 7760);
                if (cm.isQuestCompleted(29932) || cm.haveItem(1142142)) {
                    cm.sendOk("你已经领取过 <法老守护者> 勋章。");
                } else if (progress >= 50000) {
                    if (!cm.canHold(1142142)) {
                        cm.sendOk("领取勋章前，请确认装备栏有足够空间。");
                    } else {
                        cm.gainItem(1142142, 1);
                        cm.forceCompleteQuest(29932);
                        cm.sendOk("你已经证明了自己拥有成为法老守护者的资格。请收下这枚勋章。");
                    }
                } else {
                    cm.sendNext("你还没有达到条件。请在奈特金字塔击败 50000 只怪物后再来找我。\r\n当前已击败 " + progress + " 只怪物。");
                }
            }
        } else if (status == 1) {
            if (selected == 0) {
                cm.sendNextPrev("一旦进入金字塔，你就会面对奈特的愤怒。看你似乎不太聪明，我就给你一些建议和规则，牢牢记住。#b\r\n\r\n1. 小心不要让你的 #e#r行动槽#b#n 减少。维持行动槽的唯一方法，就是不停地和怪物战斗。\r\n2. 做不到的人会付出沉重代价。小心不要造成任何 #rMISS#b。\r\n3. 小心带有 #v04032424# 标记的法老小雪人。误伤它的话，你一定会后悔。\r\n4. 善用赐予你的技能来完成击杀成就。 ");
            } else if (selected == 1) {
                party = selection;
                cm.sendSimple("不惧死亡残酷的人啊，做出你的决定！\r\n#L0##i3994115##l#L1##i3994116##l#L2##i3994117##l#L3##i3994118##l");
            } else if (selected == 3) {
                var gems = [4001322, 4001323, 4001324, 4001325];
                if (selection >= 0 && selection < gems.length && cm.haveItem(gems[selection])) {
                    cm.gainItem(gems[selection], -1);
                    cm.warp(926010010 + selection);
                } else {
                    cm.sendOk("你需要一颗宝石才能进入法老雪人之墓。确认你真的带着宝石吗？");
                }
                cm.dispose();
            } else {
                cm.dispose();
            }
        } else if (status == 2) {
            if (selected == 0) {
                cm.sendNextPrev("能够承受奈特愤怒的人会获得荣耀，失败者则会面临毁灭。这就是我能给你的全部建议，剩下的就看你自己了。 ");
            } else if (selected == 1) {
                var mode = "EASY";
                if (party == 1) {
                    if (cm.getParty() == null) {
                        cm.sendOk("请先创建队伍。 ");
                        cm.dispose();
                        return;
                    }
                    if (!cm.isLeader()) {
                        cm.sendOk("只有队长可以申请队伍挑战。 ");
                        cm.dispose();
                        return;
                    }
                    if (cm.partyMembersInMap() < 2) {
                        cm.sendOk("请确认当前地图中有 2 名或更多队员。 ");
                        cm.dispose();
                        return;
                    }
                }

                if (cm.getPlayer().getLevel() < 40) {
                    cm.sendOk("你必须达到 40 级以上才能进入这个组队任务。 ");
                    cm.dispose();
                    return;
                }
                if (selection < 3 && cm.getPlayer().getLevel() > 60) {
                    cm.sendOk("等级超过 60 级的玩家只能进入地狱模式。 ");
                    cm.dispose();
                    return;
                }
                if (selection == 1) {
                    mode = "NORMAL";
                } else if (selection == 2) {
                    mode = "HARD";
                } else if (selection == 3) {
                    mode = "HELL";
                }

                if (!cm.createPyramid(mode, party == 1)) {
                    cm.sendOk("该模式的所有房间都已满，请稍后再试或换一个频道。 ");
                }
                cm.dispose();
            }
        } else if (status == 3) {
            cm.dispose();
        }
    } else if (cm.getMapId() == 926020001 || cm.getMapId() == 926010001) {
        if (status == 0) {
            if (selection == 0) {
                cm.warp(926010010);
                cm.dispose();
            } else if (selection == 1) {
                cm.sendNext("我会给你法老雪人的宝石。有了这颗宝石，你就可以随时进入法老雪人之墓。请确认你的其他栏至少有 1 个空位。 ");
            }
        } else if (status == 1) {
            var itemid = 4001325;
            if (cm.canHold(itemid)) {
                cm.gainItem(itemid);
                cm.warp(926010000);
            } else {
                cm.showInfoText("其他栏至少需要 1 个空位才能领取奖励。 ");
            }
            cm.dispose();
        }
    } else {
        var pyramid = cm.getPyramid();
        if (pyramid != null) {
            pyramid.leave(926010000);
        } else {
            cm.warp(926010000);
        }
        cm.dispose();
    }
}
