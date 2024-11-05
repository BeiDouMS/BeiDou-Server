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
/* Pelvis Bebop
	Marriage NPC
 */

var status;
var state;
var eim;
var weddingEventName = "WeddingChapel";
var cathedralWedding = false;
var weddingIndoors;
const GameConfig = Java.type('org.gms.config.GameConfig');
var weddingBlessingExp = GameConfig.getServerInt("wedding_bless_exp");

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
    eim = cm.getEventInstance();

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
            if (eim == null) {
                cm.warp(680000000, 0);
                cm.dispose();
                return;
            }

            var playerId = cm.getPlayer().getId();
            if (playerId == eim.getIntProperty("groomId") || playerId == eim.getIntProperty("brideId")) {
                var wstg = eim.getIntProperty("weddingStage");

                if (wstg == 2) {
"Awhoooooooooosh~, the guests have proclaimed their love to y'all. The time has come baby~, #rshould I make you Husband and Wife#k?"
                    state = 1;
                } else if (wstg == 1) {
                    cm.sendOk("哇-等一下，好吗？你的客人们正在给你们表达爱意。让我们来点动感，宝贝~~。");
                    cm.dispose();
                } else {
                    cm.sendOk("Wheeeeeeeeeeeeeew! 我们的节日现在已经结束了，和#b#p9201009#交谈一下，她会带领你和你的朋友去参加后派对。感谢你们的热情！");
                    cm.dispose();
                }
            } else {
                var wstg = eim.getIntProperty("weddingStage");
                if (wstg == 1) {
                    if (eim.gridCheck(cm.getPlayer()) != -1) {
                        cm.sendOk("大家让我们把这个地方搞得热闹起来！让我们摇滚起来！！");
                        cm.dispose();
                    } else {
                        if (eim.getIntProperty("guestBlessings") == 1) {
                            cm.sendYesNo("你会向在场的超级明星表达你的爱吗？");
                            state = 0;
                        } else {
                            cm.sendOk("我们的超级明星都聚集在这里。大家，让我们给他们一个美好的派对~！");
                            cm.dispose();
                        }
                    }
                } else if (wstg == 3) {
                    cm.sendOk("哇哦！现在这对夫妇的爱就像一个超级大闪亮的心一样！而且它将在这个节日之后继续下去。请准备好参加派对，宝贝~。跟着新婚夫妇的步伐走吧！");
                    cm.dispose();
                } else {
"It's now guys... Stay with your eyes and ears keened up! They are about to smooch it all over the place!!!"
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

                cm.sendOk("太棒了，我的朋友！你的爱已经融入了他们的爱，现在成为一个更大的心形情感，将永远活跃在我们的心中！哇哦~！");
                cm.dispose();
            } else {            // couple wants to complete the wedding
                var wstg = eim.getIntProperty("weddingStage");

                if (wstg == 2) {
                    var pid = cm.getPlayer().getPartnerId();
                    if (pid <= 0) {
                        cm.sendOk("哎呀~.... 等等，你刚刚是不是把你手上的东西弄坏了？？哎呀，发生了什么事？");
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

                                        cm.getMap().dropMessage(6, "Wayne: I'll call it out right now, and it shall go on: you guys are the key of the other's lock, a lace of a pendant. That's it, snog yourselves!");
                                        eim.schedule("showMarriedMsg", 2 * 1000);
                                    }
                                } else {
                                    eim.setIntProperty("confirmedVows", player.getId());
                                    cm.getMap().dropMessage(6, "Wedding Assistant: " + player.getName() + " has confirmed vows! Alright, one step away to make it official. Tighten your seatbelts!");
                                }

                                break;

                            case -1:
                                cm.sendOk("嗯，看来你们不再拥有订婚时交换的戒指/戒指盒了。哎呀~");
                                break;

                            case -2:
                                cm.sendOk("嗯，看起来你的伴侣不再拥有你们订婚时交换的戒指/戒指盒了。哎呀，太遗憾了~");
                                break;

                            case -3:
                                cm.sendOk("嗯，看起来你没有入口处给的#r#t4000313##k...请找到它，宝贝~");
                                break;

                            case -4:
                                cm.sendOk("啊，我知道这有点糟糕，但时尚的婚礼服装在这里扮演了一个重要的角色。在和我交谈之前，请穿上它。");
                                break;

                            case 1:
                                cm.sendOk("请开放一个装备栏位以获取结婚戒指，可以吗？");
                                break;

                            case 2:
                                cm.sendOk("请让你的伴侣知道要留出一个装备栏位来获取婚戒，好吗？");
                                break;

                            case 3:
                                cm.sendOk("嗯，看起来你的搭档没有入口处给的#r#t4000313##k，请找到它，没有它我无法召唤最终的东西。");
                                break;

                            case 4:
                                cm.sendOk("唉，我知道很遗憾，但看起来你的伴侣没有穿时尚的婚礼服装。请告诉他们在和我交谈之前先穿上。");
                                break;
                        }

                        cm.dispose();
                    } else {
                        cm.sendOk("哦，你的伙伴现在不在吗？...哦不，如果你的伙伴不在，我恐怕不能最终召唤。");
                        cm.dispose();
                    }
                } else {
                    cm.sendOk("哇哦～你们现在#正式成为一对#，而且是一对出色的一对。你们的配合非常出色，祝贺你们！");
                    cm.dispose();
                }
            }
        }
    }
}