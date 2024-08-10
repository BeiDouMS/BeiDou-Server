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
            qm.sendSimple("如果你想说大宇是怪物, 我根本不想听, 你快走吧！……嗯？这不是锂吗？从颜色看, 应该是最高级的锂……状态也很好……嗯？你要把它给我？呵呵……锂的话, 我就不客气了. 对了……这是为什么呢？\r\n\r\n#L0##b我想告诉你大宇是怪物. #l \r\n\r\n#L1##b你听说前往沙漠的商团遭到了怪物的袭击吗？");
        } else if (status == 1) {
            qm.sendSimple("商团？……护卫的人手好像太少了. 火焰之路虽然没有太危险的怪物, 但也不能那样粗心大意……在沙漠里必须时刻保持警惕才行. \r\n\r\n#L0##b消灭了大宇, 就不会发生这种事情了. #l \r\n\r\n#L1##b这都是因为王妃对村子周围的治安疏于管理. #l");
        } else if (status == 2) {
            qm.sendSimple("没错！都是因为王妃！自从那个女人来了之后……原本很聪明的阿得拉８世全变了, 阿里安特也逐渐变得干旱！绿洲变成了荒漠！这都是因为那个女人！\r\n\r\n#L0##b王妃施行暴政, 不知道沙漠的守护神会怎么做. #l \r\n\r\n#L1##b必须尽快组织军队, 使国家摆脱王妃的压迫！#l");
        } else if (status == 3) {
            qm.sendSimple("……你说什么？大宇变成了怪物……他可是阿里安特的守护神啊……不过也是……阿里安特已经和过去不同了……\r\n\r\n#L0##b所以说嘛. 阿烈达王妃在吸收沙漠的精气, 大宇也失去了原来的灵性, 变成了怪物……#l");
        } else if (status == 4) {
            qm.forceCompleteQuest();
            qm.gainItem(4011008, -1);
            qm.gainExp(20000);

            qm.sendNext("对……也许你的话是对的. 阿里安特变成了这样……这也许是因为大宇变了的缘故. 也许大宇真的已经变成了怪物……就像年轻人说的那样, 到了除掉大宇的时候了……");
        } else if (status == 5) {
            qm.dispose();
        }
    }
}
