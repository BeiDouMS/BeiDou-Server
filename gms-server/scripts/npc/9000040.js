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
/* Dalair
	Medal NPC.

        NPC Equipment Merger:
        * @author Ronan Lana
 */

var status;
var mergeFee = 50000;
var name;
var medalQuestActions = [];

var medalQuestSeries = [
    { id: 29016, label: "Spirit Diviner" },
    { id: 29017, label: "Soul Conjurer" },
    { id: 29018, label: "Soul Guardian" },
    { id: 29019, label: "Saint Exorcist" },
    { id: 29020, label: "Dynamic Hair" }
];

function start() {
    status = -1;
    action(1, 0, 0);
}

function collectMedalQuestActions() {
    var Quest = Java.type('org.gms.server.quest.Quest');
    var actions = [];
    var player = cm.getPlayer();

    for (var i = 0; i < medalQuestSeries.length; i++) {
        var meta = medalQuestSeries[i];
        var quest = Quest.getInstance(meta.id);
        if (quest == null) {
            continue;
        }

        if (cm.isQuestStarted(meta.id)) {
            var endNpc = quest.getNpcRequirement(true);
            if (quest.canComplete(player, endNpc)) {
                actions.push({ id: meta.id, label: meta.label, action: "complete" });
            }
        } else if (!cm.isQuestCompleted(meta.id)) {
            var startNpc = quest.getNpcRequirement(false);
            if (quest.canStart(player, startNpc)) {
                actions.push({ id: meta.id, label: meta.label, action: "start" });
            }
        }
    }

    return actions;
}

function openMedalQuest(actionInfo) {
    var Quest = Java.type('org.gms.server.quest.Quest');
    var quest = Quest.getInstance(actionInfo.id);
    var player = cm.getPlayer();

    if (actionInfo.action == "complete") {
        quest.complete(player, quest.getNpcRequirement(true));
    } else {
        quest.start(player, quest.getNpcRequirement(false));
    }
    cm.dispose();
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
            const GameConfig = Java.type('org.gms.config.GameConfig');
            medalQuestActions = collectMedalQuestActions();

            var hasMergeService = GameConfig.getServerBoolean("use_enable_custom_npc_script");
            if (medalQuestActions.length == 0 && !hasMergeService) {
                cm.sendOk("There are no medal quests that can be handled right now.");
                cm.dispose();
                return;
            }

            var selStr = "What would you like to do today?";
            for (var i = 0; i < medalQuestActions.length; i++) {
                var actionInfo = medalQuestActions[i];
                var verb = actionInfo.action == "complete" ? "Complete" : "Start";
                selStr += "\r\n#L" + i + "##b" + verb + " <" + actionInfo.label + ">#k#l";
            }
            if (hasMergeService) {
                selStr += "\r\n#L100##dUse the Equipment Merge service#k#l";
            }
            cm.sendSimple(selStr);
        } else if (status == 1) {
            if (selection >= 0 && selection < medalQuestActions.length) {
                openMedalQuest(medalQuestActions[selection]);
                return;
            }
            if (selection != 100) {
                cm.dispose();
                return;
            }

            const GameConfig = Java.type('org.gms.config.GameConfig');
            var levelLimit = !cm.getPlayer().isCygnus() ? 160 : 110;
            var selStr = "The medal ranking system is currently unavailable... Therefore, I am providing the #bEquipment Merge#k service! ";

            const MakerProcessor = Java.type('org.gms.client.processor.action.MakerProcessor');
            if (!GameConfig.getServerBoolean("use_starter_merge") && (cm.getPlayer().getLevel() < levelLimit || MakerProcessor.getMakerSkillLevel(cm.getPlayer()) < 3)) {
                selStr += "However, you must have #rMaker level 3#k and at least #rlevel 110#k (Cygnus Knight), #rlevel 160#k (other classes) and a fund of #r" + cm.numberWithCommas(mergeFee) + " mesos#k to use the service.";
                cm.sendOk(selStr);
                cm.dispose();
                return;
            }
            if (cm.getMeso() < mergeFee) {
                selStr += "I'm sorry, but this service tax is of #r" + cm.numberWithCommas(mergeFee) + " mesos#k, which it seems you unfortunately don't have right now... Please, stop by again later.";
                cm.sendOk(selStr);
                cm.dispose();
                return;
            }

            selStr += "For the fee of #r" + cm.numberWithCommas(mergeFee) + "#k mesos, merge unnecessary equipments in your inventory into your currently equipped gears to get stat boosts into them, statups based on the attributes of the items used on the merge!";
            cm.sendNext(selStr);
        } else if (status == 2) {
            var warning = "#rWARNING#b: Make sure you have your items ready to merge at the slots #rAFTER#b the item you have selected to merge.#k Any items #bunder#k the item selected will be merged thoroughly.\r\n\r\nNote that equipments receiving bonuses from merge are going to become #rUntradeable#k thereon, and equipments that already received the merge bonus #rcannot be used for merge#k.\r\n\r\n";
            cm.sendGetText(warning);
        } else if (status == 3) {
            name = cm.getText();

            if (cm.getPlayer().mergeAllItemsFromName(name)) {
                cm.gainMeso(-mergeFee);
                cm.sendOk("Merging complete! Thanks for using the service and enjoy your new equipment stats.");
            } else {
                cm.sendOk("There is no #b'" + name + "'#k in your #bEQUIP#k inventory!");
            }

            cm.dispose();
        }
    }
}
