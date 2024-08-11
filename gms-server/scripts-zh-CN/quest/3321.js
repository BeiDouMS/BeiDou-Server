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

function start(mode, type, selection) {
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
            qm.sendNext("如你所知，我是德朗博士。曾经是艾尔卡德诺社会中的一名有影响力的炼金术士，但因为我实验的失败后果，现在整个马加提亚都可以看到。");
        } else if (status == 1) {
            qm.sendNextPrev("胡罗伊德，我的创造物，最初被设计用于家庭、科学和军事事务，然而主处理单元芯片的关键故障使它们变得不稳定和暴力，迅速引发混乱和灾难。因此，我被剥夺了艾尔卡德诺的炼金术士和研究员的地位，并被发出了逮捕令。");
        } else if (status == 2) {
            qm.sendAcceptDecline("即便如此，现在我不能停下！我的创造物们仍然在城市中四处肆虐，每天造成破坏和伤亡，而我们几乎没有希望驱逐它们！它们还可以自我复制，普通武器无法阻止它们。我一直在不懈地研究一种一举消灭它们的方法，试图找到停止这种疯狂的办法。你一定能理解我的处境吧？");
        } else if (status == 3) {
            qm.sendNext("非常感谢你理解我的立场。你一定见过帕温，因此你知道我在哪里。让他意识到当前的情况。");
        } else if (status == 4) {
            qm.sendNext("哦，如果可以的话，我还有一个私人请求。我担心我的妻子，#b#p2111004##k。自从胡罗伊德事件以来，我没能向她传达消息，这一定对她造成了很大的负担... 如果可以的话，你能去#b家里#k找回#b银色吊坠#k，代我送给她吗？我很后悔没有在她的生日时立刻把这件东西送给她... 也许现在给她，能让她安心一晚上。");
        } else if (status == 5) {
            qm.sendNext("#r记住这个顺序！#k 我把吊坠藏在我家里，水管后面的一个容器里。水管必须按顺序打开：上、下、中。然后，输入秘密密码：'#rmy love Phyllia#k'。");
            qm.forceStartQuest();
        } else if (status == 6) {
            qm.dispose();
        }
    }
}

