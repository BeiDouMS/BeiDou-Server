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
            qm.sendAcceptDecline("我有个请求。你能不能向 #bMaed#k 要一瓶我设计的药剂？显然，不要提到我让你这样做，那会是个问题。由于与 Huroids 的接触，#bKeeny#k 生病了，这让我非常烦恼，以至于我无法在我的研究上取得进展……请 #r把药剂带给她#k，这样我才能感觉好些，并开始取得进展。我指望你了.");
        } else if (status == 1) {
            qm.forceStartQuest();
            qm.dispose();
        }
    }
}
