/*
    This file is part of the HeavenMS (MapleSolaxiaV2) MapleStory Server
    Copyleft (L) 2017 RonanLana

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

let status = -1;

function end(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            qm.dispose();
            return;
        }

        if (mode == 1)
            status++;
        else
            status--;

        if (status == 0) {
            if ((qm.getQuestProgress(3311, 0) == 1 && qm.getQuestProgress(3311, 1) == 1) || qm.getQuestProgress(3311, 0) == 5) {
                // qm.sendNext("Hmm, so the Alcadno doctor wrote something about researching some vanguardist Neo Huroid machine, that could beat by far the existing one, and was about to prepare the last steps of his rehearsal? We don't have a word about him for about three weeks now, something must have gone wrong...");
                qm.sendNext("嗯，那位名叫 卡帕莱特 的医生写了一些内容，提到他在研究一种先进的 氯化洛伊德 机器人技术——这种机器人远比现有的型号更强大。他似乎已经准备好了实验的最后阶段……不过我们已经有三周左右没有收到他的任何消息了，肯定出了什么问题……");
                qm.gainExp(60000 * qm.getPlayer().getExpRate());
                qm.forceCompleteQuest();
            } else {
                // qm.sendNext("Found nothing yet? Please check out Dr. De Lang's house properly, something there may give out a clue about what is going on.");
                qm.sendNext("还没找到任何线索吗？请仔细检查德朗博士的房子，那里肯定有什么东西能帮助我们了解到底发生了什么。");
            }

            qm.dispose();
        }
    }
}