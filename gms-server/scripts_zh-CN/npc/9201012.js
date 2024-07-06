/*
    This file is part of the HeavenMS MapleStory Server
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
/* Wayne
	Marriage NPC
 */

var status;
var state;
var eim;
var weddingEventName = "WeddingChapel";
var cathedralWedding = false;


function isSuitedForWedding(player, equipped) {
    var baseid = (player.getGender() == 0) ? 1050131 : 1051150;

    if (equipped) {
        for (var i = 0; i < 4; i++) {
            if (player.haveItemEquipped(baseid + i)) {
                return true;
            }
        }
    } else {
        for (var i = 0; i < 4; i++) {
            if (player.haveItemWithId(baseid + i, true)) {
                return true;
            }
        }
    }

    return false;
}

function getMarriageInstance(player) {
    var em = cm.getEventManager(weddingEventName);

    for (var iterator = em.getInstances().iterator(); iterator.hasNext();) {
        var eim = iterator.next();
        if (eim.isEventLeader(player)) {
            return eim;
        }
    }

    return null;
}

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

        if (status == 0) {
            var hasEngagement = false;
            for (var x = 4031357; x <= 4031364; x++) {
                if (cm.haveItem(x, 1)) {
                    hasEngagement = true;
                    break;
                }
            }

            if (hasEngagement) {
                var text = "Hi there. How about skyrocket the day with your fiancee baby~?";
                var choice = new Array("We're ready to get married.");
                for (x = 0; x < choice.length; x++) {
                    text += "\r\n#L" + x + "##b" + choice[x] + "#l";
                }
                cm.sendSimple(text);
            } else {
                cm.sendOk("嗨，朋友们。有没有想过在阿莫利亚举行婚礼？一谈到婚礼，每个人首先想到的就是阿莫利亚，这一点毋庸置疑。我们这里的教堂在枫之世界中以为冒险家提供最好的婚礼服务而闻名！");
                cm.dispose();
            }
        } else if (status == 1) {
            var wid = cm.getClient().getWorldServer().getRelationshipId(cm.getPlayer().getId());
            var cserv = cm.getClient().getChannelServer();

            if (cserv.isWeddingReserved(wid)) {
                if (wid == cserv.getOngoingWedding(cathedralWedding)) {
                    var partner = cserv.getPlayerStorage().getCharacterById(cm.getPlayer().getPartnerId());
                    if (!(partner == null || !cm.getMap().equals(partner.getMap()))) {
                        if (!cm.canHold(4000313)) {
                            cm.sendOk("请确保有一个空余的杂项栏位，以便获取#b#t4000313##k。");
                            cm.dispose();
                            return;
                        } else if (!partner.canHold(4000313)) {
                            cm.sendOk("请让你的伙伴知道，他们必须有一个空闲的ETC槽位才能获得#b#t4000313##k。");
                            cm.dispose();
                            return;
                        } else if (!isSuitedForWedding(cm.getPlayer(), false)) {
                            cm.sendOk("请快速购买时尚的#婚礼服装#k，婚礼马上就要开始了！是时候闪耀了，宝贝~！");
                            cm.dispose();
                            return;
                        } else if (!isSuitedForWedding(partner, false)) {
                            cm.sendOk("你的伴侣必须知道他们必须为婚礼准备时尚的#婚礼服装#。是时候闪耀了，宝贝~！");
                            cm.dispose();
                            return;
                        }

                        cm.sendOk("好的！这对夫妇看起来一如既往地时尚。走吧，伙计们，让我们摇滚起来！！！");
                    } else {
                        cm.sendOk("哎呀，你的伴侣在别的地方... 婚礼上两个人都必须在这里，否则就太无聊了。");
                        cm.dispose();
                    }
                } else {
                    var placeTime = cserv.getWeddingReservationTimeLeft(wid);

                    cm.sendOk("哟。你的婚礼定在#r" + placeTime + "#k举行，穿得体面点，别迟到了，好吗？");
                    cm.dispose();
                }
            } else {
                cm.sendOk("啊呜，很抱歉，目前在这个频道没有为您预订任何位置。");
                cm.dispose();
            }
        } else if (status == 2) {
            var cserv = cm.getClient().getChannelServer();
            var wtype = cserv.getOngoingWeddingType(cathedralWedding);

            var partner = cserv.getPlayerStorage().getCharacterById(cm.getPlayer().getPartnerId());
            if (!(partner == null || !cm.getMap().equals(partner.getMap()))) {
                if (cserv.acceptOngoingWedding(cathedralWedding)) {
                    var wid = cm.getClient().getWorldServer().getRelationshipId(cm.getPlayer().getId());
                    if (wid > 0) {
                        var em = cm.getEventManager(weddingEventName);
                        if (em.startInstance(cm.getPlayer())) {
                            eim = getMarriageInstance(cm.getPlayer());
                            if (eim != null) {
                                eim.setIntProperty("weddingId", wid);
                                eim.setIntProperty("groomId", cm.getPlayer().getId());
                                eim.setIntProperty("brideId", cm.getPlayer().getPartnerId());
                                eim.setIntProperty("isPremium", wtype ? 1 : 0);

                                eim.registerPlayer(partner);
                            } else {
                                cm.sendOk("定位婚礼活动时发生了意外错误。请稍后再试。");
                            }

                            cm.dispose();
                        } else {
                            cm.sendOk("婚礼准备之前发生了意外错误。请稍后再试。");
                            cm.dispose();
                        }
                    } else {
                        cm.sendOk("婚礼准备之前发生了意外错误。请稍后再试。");
                        cm.dispose();
                    }
                } else {    // partner already decided to start
                    cm.dispose();
                }
            } else {
                cm.sendOk("哎呀，看起来你的伴侣在别的地方...两个人都必须在这里参加婚礼，否则就会非常无聊。");
                cm.dispose();
            }
        }
    }
}