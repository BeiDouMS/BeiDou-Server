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
/* High Priest John
 Marriage NPC
 */

var status;
var state;
var eim;
var weddingEventName = "WeddingCathedral";
var cathedralWedding = true;
var weddingIndoors;
const GameConfig = Java.type('org.gms.config.GameConfig');
var weddingBlessingExp = GameConfig.getServerInt("wedding_bless_exp");

function isWeddingIndoors(mapid) {
    return mapid >= 680000100 && mapid <= 680000500;
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

function detectPlayerItemid(player) {
    for (var x = 4031357; x <= 4031364; x++) {
        if (player.haveItem(x)) {
            return x;
        }
    }

    return -1;
}

function getRingId(boxItemId) {
    return boxItemId == 4031357 ? 1112803 : (boxItemId == 4031359 ? 1112806 : (boxItemId == 4031361 ? 1112807 : (boxItemId == 4031363 ? 1112809 : -1)));
}

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

function getWeddingPreparationStatus(player, partner) {
    if (!player.haveItem(4000313)) {
        return -3;
    }
    if (!partner.haveItem(4000313)) {
        return 3;
    }

    if (!isSuitedForWedding(player, true)) {
        return -4;
    }
    if (!isSuitedForWedding(partner, true)) {
        return 4;
    }

    var hasEngagement = false;
    for (var x = 4031357; x <= 4031364; x++) {
        if (player.haveItem(x)) {
            hasEngagement = true;
            break;
        }
    }
    if (!hasEngagement) {
        return -1;
    }

    hasEngagement = false;
    for (var x = 4031357; x <= 4031364; x++) {
        if (partner.haveItem(x)) {
            hasEngagement = true;
            break;
        }
    }
    if (!hasEngagement) {
        return -2;
    }

    if (!player.canHold(1112803)) {
        return 1;
    }
    if (!partner.canHold(1112803)) {
        return 2;
    }

    return 0;
}

function giveCoupleBlessings(eim, player, partner) {
    var blessCount = eim.gridSize();

    player.gainExp(blessCount * weddingBlessingExp);
    partner.gainExp(blessCount * weddingBlessingExp);
}

function start() {
    weddingIndoors = isWeddingIndoors(cm.getMapId());
    if (weddingIndoors) {
        eim = cm.getEventInstance();
    }

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
            if (status == 0) {
                var hasEngagement = false;
                for (var x = 4031357; x <= 4031364; x++) {
                    if (cm.haveItem(x, 1)) {
                        hasEngagement = true;
                        break;
                    }
                }

                if (hasEngagement) {
                    var text = "Hi there. How can I help you?";
                    var choice = new Array("We're ready to get married.");
                    for (x = 0; x < choice.length; x++) {
                        text += "\r\n#L" + x + "##b" + choice[x] + "#l";
                    }
                    cm.sendSimple(text);
                } else {
                    cm.sendOk("嗯，今天两颗飘动的心将在爱的祝福下联合在一起！");
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
                                cm.sendOk("请确保有一个空闲的杂项栏位以获取#b#t4000313##k。");
                                cm.dispose();
                                return;
                            } else if (!partner.canHold(4000313)) {
                                cm.sendOk("请告知你的伙伴，他们必须有一个空闲的ETC槽位才能获得#b#t4000313##k。");
                                cm.dispose();
                                return;
                            } else if (!isSuitedForWedding(cm.getPlayer(), false)) {
                                cm.sendOk("请快速购买一件#婚礼服装#，以便参加仪式！没有它，我就不能和你结婚。");
                                cm.dispose();
                                return;
                            } else if (!isSuitedForWedding(partner, false)) {
                                cm.sendOk("请让你的伴侣知道，他们必须准备好一件#婚礼服装#，以便参加仪式。");
                                cm.dispose();
                                return;
                            }

                            cm.sendOk("很好，这里的准备工作也已经完成了。今天确实是个美好的日子，你们两个真的很幸运能在这样的日子结婚。让我们开始婚礼吧！");
                        } else {
                            cm.sendOk("嗯，看来你的伙伴在别处……请让他们在开始仪式之前来这里。");
                            cm.dispose();
                        }
                    } else {
                        var placeTime = cserv.getWeddingReservationTimeLeft(wid);

                        cm.sendOk("请耐心等待。你的婚礼定于在#r" + placeTime + "#k举行。不要忘记婚礼服装。");
                        cm.dispose();
                    }
                } else {
                    cm.sendOk("嗯，很抱歉，目前在这个频道没有为您预订的记录。");
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
                    cm.sendOk("嗯，看来你的伙伴在别处... 请让他们在开始仪式之前来这里。");
                    cm.dispose();
                }
            }
        } else {
            if (status == 0) {
                if (eim == null) {
                    cm.warp(680000000, 0);
                    cm.dispose();
                    return;
                }

                var playerId = cm.getPlayer().getId();
                if (playerId == eim.getIntProperty("groomId") || playerId == eim.getIntProperty("brideId")) {
                    var wstg = eim.getIntProperty("weddingStage");

                    if (wstg == 2) {
                        cm.sendYesNo("非常好，客人们现在已经把所有的祝福都赐予了你。时机已经成熟，#r我应该让你们成为夫妻了吗#k？");
                        state = 1;
                    } else if (wstg == 1) {
                        cm.sendOk("当你们两个向彼此宣誓结婚的时候，你们的客人正在向你们祝福。这是你们两个的幸福时刻，请享受这个仪式。");
                        cm.dispose();
                    } else {
                        cm.sendOk("恭喜你的婚礼！我们的仪式已经结束，你现在可以前往 #b#p9201007##k，她会带领你和你的客人去参加婚礼后的派对。为你们的爱干杯！");
                        cm.dispose();
                    }
                } else {
                    var wstg = eim.getIntProperty("weddingStage");
                    if (wstg == 1) {
                        if (eim.gridCheck(cm.getPlayer()) != -1) {
                            cm.sendOk("大家给这对可爱的夫妇送上祝福吧！");
                            cm.dispose();
                        } else {
                            if (eim.getIntProperty("guestBlessings") == 1) {
                                cm.sendYesNo("你想为这对夫妇祝福吗？");
                                state = 0;
                            } else {
                                cm.sendOk("今天我们在这里聚集，为了让这对活泼的夫妇重新团聚在婚姻的殿堂上！");
                                cm.dispose();
                            }
                        }
                    } else if (wstg == 3) {
                        cm.sendOk("这对恩爱的鸟儿现在已经结婚了。多么热闹的一天！请做好准备参加婚后派对，它应该很快就会开始。跟着新婚夫妇的步伐。");
                        cm.dispose();
                    } else {
                        cm.sendOk("客人的祝福时间已经结束。稍等，这对夫妇很快就会重新宣誓他们的誓言。真是一道美丽的风景！");
                        cm.dispose();
                    }
                }
            } else if (status == 1) {
                if (state == 0) {    // give player blessings
                    eim.gridInsert(cm.getPlayer(), 1);

                    const PacketCreator = Java.type('org.gms.util.PacketCreator');
                    if (GameConfig.getServerBoolean("wedding_blesser_showfx")) {
                        var target = cm.getPlayer();
                        target.sendPacket(PacketCreator.showSpecialEffect(9));
                        target.getMap().broadcastMessage(target, PacketCreator.showForeignEffect(target.getId(), 9), false);
                    } else {
                        var target = eim.getPlayerById(eim.getIntProperty("groomId"));
                        target.sendPacket(PacketCreator.showSpecialEffect(9));
                        target.getMap().broadcastMessage(target, PacketCreator.showForeignEffect(target.getId(), 9), false);

                        target = eim.getPlayerById(eim.getIntProperty("brideId"));
                        target.sendPacket(PacketCreator.showSpecialEffect(9));
                        target.getMap().broadcastMessage(target, PacketCreator.showForeignEffect(target.getId(), 9), false);
                    }

                    cm.sendOk("你的祝福已经融入了他们的爱。对于这对可爱的夫妇来说，这是多么高尚的行为！");
                    cm.dispose();
                } else {            // couple wants to complete the wedding
                    var wstg = eim.getIntProperty("weddingStage");

                    if (wstg == 2) {
                        var pid = cm.getPlayer().getPartnerId();
                        if (pid <= 0) {
                            cm.sendOk("看起来你和你的伴侣已经不再订婚了，就在走向祭坛之前……你们之前展现的所有幸福都去了哪里呢？");
                            cm.dispose();
                            return;
                        }

                        var player = cm.getPlayer();
                        var partner = cm.getMap().getCharacterById(cm.getPlayer().getPartnerId());
                        if (partner != null) {
                            state = getWeddingPreparationStatus(player, partner);

                            switch (state) {
                                case 0:
                                    var pid = eim.getIntProperty("confirmedVows");
                                    if (pid != -1) {
                                        if (pid == player.getId()) {
                                            cm.sendOk("你已经确认了你的誓言。现在只剩下你的伴侣需要确认了。");
                                        } else {
                                            eim.setIntProperty("weddingStage", 3);
                                            var cmPartner = partner.getAbstractPlayerInteraction();

                                            var playerItemId = detectPlayerItemid(player);
                                            var partnerItemId = (playerItemId % 2 == 1) ? playerItemId + 1 : playerItemId - 1;

                                            var marriageRingId = getRingId((playerItemId % 2 == 1) ? playerItemId : partnerItemId);

                                            cm.gainItem(playerItemId, -1);
                                            cmPartner.gainItem(partnerItemId, -1);

                                            const RingActionHandler = Java.type('org.gms.net.server.channel.handlers.RingActionHandler');
                                            RingActionHandler.giveMarriageRings(player, partner, marriageRingId);
                                            player.setMarriageItemId(marriageRingId);
                                            partner.setMarriageItemId(marriageRingId);

                                            //var marriageId = eim.getIntProperty("weddingId");
                                            //player.sendPacket(Wedding.OnMarriageResult(marriageId, player, true));
                                            //partner.sendPacket(Wedding.OnMarriageResult(marriageId, player, true));

                                            giveCoupleBlessings(eim, player, partner);

                                            cm.getMap().dropMessage(6, "High Priest John: By the power vested in me through the mighty Maple tree, I now pronounce you  Husband and Wife. You may kiss the bride!");
                                            eim.schedule("showMarriedMsg", 2 * 1000);
                                        }
                                    } else {
                                        eim.setIntProperty("confirmedVows", player.getId());
                                        cm.getMap().dropMessage(6, "Wedding Assistant: " + player.getName() + " has confirmed vows! Alright, one step away to make it official. Tighten your seatbelts!");
                                    }

                                    break;

                                case -1:
                                    cm.sendOk("看来你不再拥有你和你的伴侣在订婚时共享的戒指/戒指盒了。抱歉，但这是婚礼所需要的...");
                                    break;

                                case -2:
                                    cm.sendOk("似乎你的伴侣不再拥有你们订婚时共同分享的戒指/戒指盒了。抱歉，但这是婚礼所需要的…");
                                    break;

                                case -3:
                                    cm.sendOk("看起来你没有入口处给的 #r#t4000313##k... 请找到它，没有这个物品我不能嫁给你。");
                                    break;

                                case -4:
                                    cm.sendOk("请原谅我的无礼，但服装是仪式的重要组成部分。请为婚礼适当地打扮。");
                                    break;

                                case 1:
                                    cm.sendOk("请开放一个装备栏位以获取结婚戒指，可以吗？");
                                    break;

                                case 2:
                                    cm.sendOk("请让你的伴侣知道要留出一个装备栏位来获取婚戒，好吗？");
                                    break;

                                case 3:
                                    cm.sendOk("看起来你的伴侣没有入口处给的#r#t4000313##k... 请找到它，没有这个物品我不能和你结婚。");
                                    break;

                                case 4:
                                    cm.sendOk("看来你的伴侣没有适当地穿着参加婚礼……请原谅我的无礼，但服装是仪式的重要部分。");
                                    break;
                            }

                            cm.dispose();
                        } else {
                            cm.sendOk("嗯，看来你的伴侣不在这里，在祭坛前……很遗憾，如果你的伴侣不在这里，我无法完成婚礼。");
                            cm.dispose();
                        }
                    } else {
                        cm.sendOk("你们现在是 #b丈夫和妻子#k。恭喜！");
                        cm.dispose();
                    }
                }
            }
        }
    }
}