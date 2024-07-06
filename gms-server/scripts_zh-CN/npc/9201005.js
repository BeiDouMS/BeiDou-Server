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
/* Assistant Nicole
	Marriage NPC
 */

var status;
var wid;
var isMarrying;

var cathedralWedding = true;
var weddingEventName = "WeddingCathedral";
var weddingEntryTicketCommon = 5251000;
var weddingEntryTicketPremium = 5251003;
var weddingSendTicket = 4031395;
var weddingGuestTicket = 4031407;
var weddingAltarMapid = 680000210;
var weddingIndoors;

function isWeddingIndoors(mapid) {
    return mapid >= 680000100 && mapid <= 680000500;
}

function hasSuitForWedding(player) {
    var baseid = (player.getGender() == 0) ? 1050131 : 1051150;

    for (var i = 0; i < 4; i++) {
        if (player.haveItemWithId(baseid + i, true)) {
            return true;
        }
    }

    return false;
}

function getMarriageInstance(weddingId) {
    var em = cm.getEventManager(weddingEventName);

    for (var iterator = em.getInstances().iterator(); iterator.hasNext();) {
        var eim = iterator.next();

        if (eim.getIntProperty("weddingId") == weddingId) {
            return eim;
        }
    }

    return null;
}

function hasWeddingRing(player) {
    var rings = [1112806, 1112803, 1112807, 1112809];
    for (var i = 0; i < rings.length; i++) {
        if (player.haveItemWithId(rings[i], true)) {
            return true;
        }
    }

    return false;
}

function start() {
    weddingIndoors = isWeddingIndoors(cm.getMapId());
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

        if (!weddingIndoors) {
            var hasEngagement = false;
            for (var x = 4031357; x <= 4031364; x++) {
                if (cm.haveItem(x, 1)) {
                    hasEngagement = true;
                    break;
                }
            }

            if (status == 0) {
                var text = "Welcome to the #bCathedral#k! How can I help you?";
                var choice = ["How do I prepare a wedding?", "I have an engagement and want to arrange the wedding", "I am the guest and I'd like to go into the wedding"];
                for (x = 0; x < choice.length; x++) {
                    text += "\r\n#L" + x + "##b" + choice[x] + "#l";
                }

                if (cm.haveItem(5251100)) {
                    text += "\r\n#L" + x + "##bMake additional invitation cards#l";
                }

                cm.sendSimple(text);
            } else if (status == 1) {
                switch (selection) {
                    case 0:
                        cm.sendOk("首先，你需要和某人订婚。#p9201000# 制作订婚戒指。一旦获得订婚状态，购买一张#b#t" + weddingEntryTicketCommon + "##k。\r\n给我看你的订婚戒指和婚礼门票，我会为你预订并提供#r15张婚礼门票#k。使用它们邀请你的婚礼客人。他们每人需要一张门票才能进入。");
                        cm.dispose();
                        break;

                    case 1:
                        if (hasEngagement) {
                            var wserv = cm.getClient().getWorldServer();
                            var cserv = cm.getClient().getChannelServer();
                            var weddingId = wserv.getRelationshipId(cm.getPlayer().getId());

                            if (weddingId > 0) {
                                if (cserv.isWeddingReserved(weddingId)) {    // registration check
                                    var placeTime = cserv.getWeddingReservationTimeLeft(weddingId);
                                    cm.sendOk("你的婚礼定于#r" + placeTime + "#k开始。穿正装，不要迟到！");
                                } else {
                                    var partner = wserv.getPlayerStorage().getCharacterById(cm.getPlayer().getPartnerId());
                                    if (partner == null) {
                                        cm.sendOk("你的搭档似乎现在不在线... 确保在时机成熟时把两个人都召集到这里！");
                                        cm.dispose();
                                        return;
                                    }

                                    if (hasWeddingRing(cm.getPlayer()) || hasWeddingRing(partner)) {
                                        cm.sendOk("你或者你的伴侣已经有了结婚戒指。");
                                        cm.dispose();
                                        return;
                                    }

                                    if (!cm.getMap().equals(partner.getMap())) {
                                        cm.sendOk("请让您的伙伴也来这里注册预订。");
                                        cm.dispose();
                                        return;
                                    }

                                    if (!cm.canHold(weddingSendTicket, 15) || !partner.canHold(weddingSendTicket, 15)) {
                                        cm.sendOk("你或者你的伴侣没有空余的ETC槽位来放置婚礼门票！请在尝试注册预订之前腾出一些空间。");
                                        cm.dispose();
                                        return;
                                    }

                                    if (!cm.getUnclaimedMarriageGifts().isEmpty() || !partner.getAbstractPlayerInteraction().getUnclaimedMarriageGifts().isEmpty()) {
                                        cm.sendOk("呃...抱歉，根据阿莫利亚婚礼礼品登记保留，似乎有些不对劲。请与#b#p9201014##k核实情况。");
                                        cm.dispose();
                                        return;
                                    }

                                    var hasCommon = cm.haveItem(weddingEntryTicketCommon);
                                    var hasPremium = cm.haveItem(weddingEntryTicketPremium);

                                    if (hasCommon || hasPremium) {
                                        var weddingType = (hasPremium ? true : false);

                                        var player = cm.getPlayer();
                                        var resStatus = cserv.pushWeddingReservation(weddingId, cathedralWedding, weddingType, player.getId(), player.getPartnerId());
                                        if (resStatus > 0) {
                                            cm.gainItem((weddingType) ? weddingEntryTicketPremium : weddingEntryTicketCommon, -1);

                                            const Channel = Java.type('org.gms.net.server.channel.Channel');
                                            var expirationTime = Channel.getRelativeWeddingTicketExpireTime(resStatus);
                                            cm.gainItem(weddingSendTicket, 15, false, true, expirationTime);
                                            partner.getAbstractPlayerInteraction().gainItem(weddingSendTicket, 15, false, true, expirationTime);

                                            var placeTime = cserv.getWeddingReservationTimeLeft(weddingId);

                                            var wedType = weddingType ? "Premium" : "Regular";
                                            cm.sendOk("你们两个都收到了15张婚礼邀请函，可以发给你们的客人。#b双击邀请函#k 将其发送给某人。邀请只能在婚礼开始时间之前发送。你们的#b" + wedType + " 婚礼#k 定于#r" + placeTime + "#k开始。穿得正式一点，不要迟到！");

                                            player.dropMessage(6, "Wedding Assistant: You both have received 15 Wedding Tickets. Invitations can only be sent before the wedding start time. Your " + wedType + " wedding is set to start at the " + placeTime + ". Get dressed and don't be late!");
                                            partner.dropMessage(6, "Wedding Assistant: You both have received 15 Wedding Tickets. Invitations can only be sent before the wedding start time. Your " + wedType + " wedding is set to start at the " + placeTime + ". Get dressed and don't be late!");

                                            if (!hasSuitForWedding(player)) {
                                                player.dropMessage(5, "Wedding Assistant: Please purchase a wedding garment before showing up for the ceremony. One can be bought at the Wedding Shop left-most Amoria.");
                                            }

                                            if (!hasSuitForWedding(partner)) {
                                                partner.dropMessage(5, "Wedding Assistant: Please purchase a wedding garment before showing up for the ceremony. One can be bought at the Wedding Shop left-most Amoria.");
                                            }
                                        } else {
                                            cm.sendOk("您的婚礼预订可能最近已经处理。请稍后再试。");
                                        }
                                    } else {
                                        cm.sendOk("在尝试注册预约之前，请确保您的现金库存中有一张#b#t" + weddingEntryTicketCommon + "##k。");
                                    }
                                }
                            } else {
                                cm.sendOk("婚礼预订遇到错误，请稍后重试。");
                            }

                            cm.dispose();
                        } else {
                            cm.sendOk("你没有订婚戒指。");
                            cm.dispose();
                        }
                        break;

                    case 2:
                        if (cm.haveItem(weddingGuestTicket)) {
                            var cserv = cm.getClient().getChannelServer();

                            wid = cserv.getOngoingWedding(cathedralWedding);
                            if (wid > 0) {
                                if (cserv.isOngoingWeddingGuest(cathedralWedding, cm.getPlayer().getId())) {
                                    var eim = getMarriageInstance(wid);
                                    if (eim != null) {
                                        cm.sendOk("享受婚礼。不要掉落你的金枫叶，否则你将无法完成整个婚礼。");
                                    } else {
                                        cm.sendOk("请稍等片刻，当这对夫妇准备好进入大教堂时。");
                                        cm.dispose();
                                    }
                                } else {
                                    cm.sendOk("抱歉，但是你并未被邀请参加这场婚礼。");
                                    cm.dispose();
                                }
                            } else {
                                cm.sendOk("现在没有预订婚礼。");
                                cm.dispose();
                            }
                        } else {
                            cm.sendOk("你没有#b#t婚礼宾客券##k。");
                            cm.dispose();
                        }
                        break;

                    default:
                        var wserv = cm.getClient().getWorldServer();
                        var cserv = cm.getClient().getChannelServer();
                        var weddingId = wserv.getRelationshipId(cm.getPlayer().getId());

                        var resStatus = cserv.getWeddingReservationStatus(weddingId, cathedralWedding);
                        if (resStatus > 0) {
                            if (cm.canHold(weddingSendTicket, 3)) {
                                cm.gainItem(5251100, -1);

                                const Channel = Java.type('org.gms.net.server.channel.Channel');
                                var expirationTime = Channel.getRelativeWeddingTicketExpireTime(resStatus);
                                cm.gainItem(weddingSendTicket, 3, false, true, expirationTime);
                            } else {
                                cm.sendOk("请确保有一个空余的ETC槽位以获取更多的邀请。");
                            }
                        } else {
                            cm.sendOk("你目前没有被预订在大教堂上进行额外的邀请。");
                        }

                        cm.dispose();
                }
            } else if (status == 2) {   // registering guest
                var eim = getMarriageInstance(wid);

                if (eim != null) {
                    cm.gainItem(weddingGuestTicket, -1);
                    eim.registerPlayer(cm.getPlayer());     //cm.warp(680000210, 0);
                } else {
                    cm.sendOk("婚礼活动未找到。");
                }

                cm.dispose();
            }
        } else {
            if (status == 0) {
                var eim = cm.getEventInstance();
                if (eim == null) {
                    cm.warp(680000000, 0);
                    cm.dispose();
                    return;
                }

                isMarrying = (cm.getPlayer().getId() == eim.getIntProperty("groomId") || cm.getPlayer().getId() == eim.getIntProperty("brideId"));

                if (eim.getIntProperty("weddingStage") == 0) {
                    if (!isMarrying) {
                        cm.sendOk("欢迎来到#b#m" + cm.getMapId() + "##k。请在其他客人聚集在这里的时候和新郎新娘一起逗留。\r\n\r\n当计时器结束时，夫妇将前往祭坛，在那时你将被允许从#bguests area#k上方观看。");
                    } else {
                        cm.sendOk("欢迎来到 #b#m" + cm.getMapId() + "##k。请在其他人到来之前，向已经到场的客人打招呼。当计时器结束时，新人将前往祭坛。");
                    }

                    cm.dispose();
                } else {
                    cm.sendYesNo("新娘和新郎已经在去教堂的路上了。你想现在就加入他们吗？");
                }
            } else if (status == 1) {
                cm.warp(weddingAltarMapid, "sp");
                cm.dispose();
            }
        }

    }
}