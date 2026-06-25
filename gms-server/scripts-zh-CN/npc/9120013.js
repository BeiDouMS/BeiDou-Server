/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

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
//Boss Kitty

var status;
var questions;
var answers;
var correctAnswer;
var questionNum;

function startFinalQuestIfNeeded() {
    if (cm.isQuestStarted(8010)) {
        cm.completeQuest(8010);
        cm.startQuest(8012);
        return true;
    }
    if (cm.isQuestCompleted(8010) && !cm.isQuestStarted(8012) && !cm.isQuestCompleted(8012)) {
        cm.startQuest(8012);
        return true;
    }
    return cm.isQuestStarted(8012);
}

function start() {
    status = -1;
    questions = ["火狸不会掉落以下哪种物品？", "哪位 NPC 负责在废弃都市和古代神社之间接送旅客？", "古代神社出售的哪种食物可以提升攻击力？", "以下哪种物品不是小混混掉落的？", "以下哪种物品并不存在？", "昭和村的蔬菜店老板叫什么名字？", "以下哪种物品确实存在？", "古代神社一带最强的头目叫什么？", "以下哪件装备的职业或等级说明不匹配？", "以下哪种拉面不是古代神社机器人商店出售的？", "以下哪位 NPC 不在昭和电影院前？"]
    answers = [["狸柴火", "坚硬的角", "红砖"], ["佩利", "导游妮妮", "波利"], ["章鱼烧", "炒面", "天妇罗"], ["小混混 A 的徽章", "小混混 B 的束腰", "小混混 C 的项链"], ["冷冻金枪鱼", "扇子", "苍蝇拍"], ["纱美", "嘉美", "由美"], ["云狐的牙齿", "幽灵的花束", "黑云狐的尾巴"], ["乌鸦天狗", "蓝蘑菇王", "姬神"], ["竹枪 - 战士专用武器", "橡皮榔头 - 单手剑", "神秘手杖 - 51级装备"], ["蘑菇拉面（豚骨）", "蘑菇拉面（盐味）", "蘑菇味噌拉面"], ["绘里香", "黑泽", "阿利博士"]];
    correctAnswer = [1, 1, 0, 1, 2, 2, 2, 0, 0, 2, 2];
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0 && mode == 1) {
            if (startFinalQuestIfNeeded() && !cm.haveItem(4031064)) { //quest in progress
                cm.sendYesNo("你都找到了吗？你打算尝试回答我所有的问题吗？");
            } else { //quest not started or already completed
                //cm.sendOk("喵喵喵！");//lol what's this?
                cm.dispose();
            }
        } else if (status == 1 && mode == 1) {
            var hasChicken = true;
            if (!cm.haveItem(2020001, 300)) {
                hasChicken = false;
            }
            if (!hasChicken) {
                cm.sendOk("什么？不行！我要 300 个，一个都不能少。想挑战的话，就先带 300 个炸鸡来。不是谁都像你一样吃得这么饱……");
                cm.dispose();
            } else {
                cm.gainItem(2020001, -300)
                cm.sendNext("很好！等一下……看，我这里有吃的。你们先吃吧。好了，现在轮到我提问了。记住，只要答错一次就结束，要么全对，要么重来！");
            }
        } else if (status == 7 && mode == 1) { //2-6 are the questions
            if (selection != correctAnswer.pop()) {
                cm.sendNext("嗯……人类果然会犯错！想再试一次的话，就带 300 个炸鸡来。")
                cm.dispose();
            } else {
                cm.sendNext("喵……竟然全都答对了。虽然我不太喜欢人类，但我不会食言。这颗 #b#t4031064##k 就给你吧。")
            }
        } else if (status == 8 && mode == 1) { //gain marble
            cm.gainItem(4031064, 1);
            cm.sendOk("我们的交易结束了。谢谢，你可以走了。喵～");
            cm.dispose();
        } else if (status >= 2 && status <= 6 && mode == 1) {//questions
            var cont = true;
            if (status > 2) {
                if (selection != correctAnswer.pop()) {
                    cm.sendNext("嗯……人类果然会犯错！想再试一次的话，就带 300 个炸鸡来。")
                    cm.dispose();
                    cont = false;
                }
            }
            if (cont) {
                questionNum = Math.floor(Math.random() * questions.length);
                if (questionNum != (questions.length - 1)) {
                    var temp;
                    temp = questions[questionNum];
                    questions[questionNum] = questions[questions.length - 1];
                    questions[questions.length - 1] = temp;
                    temp = answers[questionNum];
                    answers[questionNum] = answers[questions.length - 1];
                    answers[questions.length - 1] = temp;
                    temp = correctAnswer[questionNum];
                    correctAnswer[questionNum] = correctAnswer[questions.length - 1];
                    correctAnswer[questions.length - 1] = temp;
                }
                var question = questions.pop();
                var answer = answers.pop();
                var prompt = "第 " + (status - 1) + " 题：" + question;
                for (var i = 0; i < answer.length; i++) {
                    prompt += "\r\n#b#L" + i + "#" + answer[i] + "#l#k";
                }
                cm.sendSimple(prompt);
            }
        }
    }
}