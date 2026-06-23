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
    { id: 29016, label: "安灵师" },
    { id: 29017, label: "阴阳师" },
    { id: 29018, label: "退魔师" },
    { id: 29019, label: "神圣驱魔" },
    { id: 29020, label: "发型变换达人" }
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
                cm.sendOk("当前没有可处理的勋章任务。");
                cm.dispose();
                return;
            }

            var selStr = "你今天想办理什么？";
            for (var i = 0; i < medalQuestActions.length; i++) {
                var actionInfo = medalQuestActions[i];
                var verb = actionInfo.action == "complete" ? "完成" : "开始";
                selStr += "\r\n#L" + i + "##b" + verb + " <" + actionInfo.label + ">#k#l";
            }
            if (hasMergeService) {
                selStr += "\r\n#L100##d使用装备合并服务#k#l";
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
            var selStr = "勋章排行系统目前不可用，因此我额外提供 #b装备合并#k 服务。";

            const MakerProcessor = Java.type('org.gms.client.processor.action.MakerProcessor');
            if (!GameConfig.getServerBoolean("use_starter_merge") && (cm.getPlayer().getLevel() < levelLimit || MakerProcessor.getMakerSkillLevel(cm.getPlayer()) < 3)) {
                selStr += "不过你必须拥有 #r3 级匠人技能#k，并且角色至少达到 #r110 级（骑士团）/#r160 级（其他职业）#k，同时准备 #r" + cm.numberWithCommas(mergeFee) + " 金币#k，才能使用这项服务。";
                cm.sendOk(selStr);
                cm.dispose();
                return;
            }
            if (cm.getMeso() < mergeFee) {
                selStr += "很抱歉，这项服务需要支付 #r" + cm.numberWithCommas(mergeFee) + " 金币#k 的手续费，而你目前的金币不足。";
                cm.sendOk(selStr);
                cm.dispose();
                return;
            }

            selStr += "支付 #r" + cm.numberWithCommas(mergeFee) + "#k 金币后，我可以把背包里多余的装备合并到当前穿戴的装备上，并根据材料装备的属性提供额外能力值。";
            cm.sendNext(selStr);
        } else if (status == 2) {
            var warning = "#r注意#b：请确保你准备拿来合并的装备，位于目标装备 #r后面#b 的栏位。被选中装备 #b下方#k 的装备都会被一并合并。\r\n\r\n另外，接受合并加成的装备会变成 #r无法交易#k，而已经获得过合并加成的装备 #r不能再次作为材料#k。\r\n\r\n";
            cm.sendGetText(warning);
        } else if (status == 3) {
            name = cm.getText();

            if (cm.getPlayer().mergeAllItemsFromName(name)) {
                cm.gainMeso(-mergeFee);
                cm.sendOk("装备合并完成。请查收新的属性加成。");
            } else {
                cm.sendOk("你的#b装备#k栏中没有 #b'" + name + "'#k。\r\n请确认输入的是装备栏里的完整名称。");
            }

            cm.dispose();
        }
    }
}
